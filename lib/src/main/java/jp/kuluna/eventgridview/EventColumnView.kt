package jp.kuluna.eventgridview

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Point
import android.support.v4.view.ViewCompat
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewEventBinding
import java.util.*
import kotlin.math.roundToInt

/**
 * イベントを1列内に表示するためのView
 * @param context Android Context
 */
@SuppressLint("ViewConstructor")
open class EventColumnView(context: Context, private val draggable: Boolean) : FrameLayout(context) {
    /** Eventのクリックイベント */
    var onEventClickListener: ((Event) -> Unit)? = null
    /** Eventをドラッグしたことによる変更イベント */
    var onEventChangedListener: ((Event, Event, hideAll: Boolean) -> Unit)? = null

    /** ディスプレイの密度取得 (この値にdpを掛けるとpxになる) */
    private val density = context.resources.displayMetrics.density
    /** RecyclerViewにおけるこのViewの現在のPosition */
    private var layoutPosition = 0
    private var events: MutableList<Event> = mutableListOf()
    /** Event調整ボタンドラッグ開始Y座標(px) */
    private var adjustStartY = 0f
    /** EventをタップしたときのY座標(px) */
    private var adjustStartTapY = 0f
    /** Eventの横幅(dp) */
    private val widthDp = 48
    /** Eventの最低の高さ */
    private var minEventHeight = convertPxToRoundedDp(90.0F) // 固定値にしてあります
    /** Eventの高さ最大値 */
    private var maxEventHeight = 0
    /** Eventのトップの最大値 */
    private var maxEventTop = (TimeParams(24, 0).fromY - 10) * density// 10dp単位なので-10
    /** EventViewの格納用 */
    var eventViews = mutableListOf<View>()
    /** 調整前のEvent */
    private var oldEvent: Event? = null
    /** Eventの時間 */
    private var elapsedTime: TimeParams? = null

    init {
        layoutParams = FrameLayout.LayoutParams((widthDp * density).toInt(), FrameLayout.LayoutParams.MATCH_PARENT).apply {
            this.setMargins(density.toInt(), 0, density.toInt(), 0)
        }

        // 自身がドロップを受け入れられるようにする
        setOnDragListener { _, dragEvent ->
            return@setOnDragListener when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val intent = dragEvent.clipData.getItemAt(0).intent
                    val position = intent.getIntExtra("itemPosition", -1)
                    val event = Event.from(intent.getBundleExtra("event"))
                    val eventBinding = DataBindingUtil.bind<ViewEventBinding>(dragEvent.localState as View)!!

                    oldEvent = event
                    // 開始地点がマイナスになった時は0時0分開始にする
                    var dropStartY = dragEvent.y - adjustStartTapY
                    if (dropStartY < 0) {
                        dropStartY = 0f
                    } else if (dropStartY >= maxEventTop) {
                        dropStartY = maxEventTop
                    }

                    // ドロップ位置のマージンで再設定
                    val newStart = TimeParams.from(dropStartY, density)
                    eventBinding.root.layoutParams = (eventBinding.root.layoutParams as FrameLayout.LayoutParams).apply {
                        val newMargin = (newStart.fromY * density).toInt()
                        topMargin = newMargin
                    }

                    val startCal = Calendar.getInstance().apply { time = event.start }
                    startCal.set(Calendar.HOUR_OF_DAY, newStart.hour)
                    startCal.set(Calendar.MINUTE, newStart.min)
                    val distance = startCal.time.time - event.start.time

                    // 開始時刻を再設定
                    event.start = startCal.time
                    // 終了時刻を再設定
                    val endCal = Calendar.getInstance().apply { time = event.end }
                    endCal.add(Calendar.MILLISECOND, distance.toInt())
                    event.end = endCal.time

                    events[position] = event

                    // Eventの長さ最大値設定
                    maxEventHeight = eventBinding.root.layoutParams.height

                    // 変更を通知
                    onEventChangedListener?.invoke(Event.from(intent.getBundleExtra("event")), event, true)

                    // 編集表示用のEventを書き換えます
                    eventBinding.root.setOnClickListener {
                        onEventClickListener?.invoke(event)
                    }

                    // Eventの長さを調節するボタンを表示する
                    eventBinding.topAdjust.visibility = View.VISIBLE
                    eventBinding.bottomAdjust.visibility = View.VISIBLE
                    true
                }

                else -> true
            }
        }
    }

    /**
     * イベントを表示します。
     * @param day 表示する日
     * @param events イベントリスト
     * @param layoutPosition RecyclerViewから見たLayoutPosition
     */
    fun set(day: Date, events: List<Event>, layoutPosition: Int) {
        removeAllViews()
        this.layoutPosition = layoutPosition
        this.events = events.toMutableList()

        val inflater = LayoutInflater.from(context)
        events.forEachIndexed { index, event ->
            val binding = DataBindingUtil.inflate<ViewEventBinding>(inflater, R.layout.view_event, null, false)
            binding.event = event

            // クリックイベント
            binding.cardView.setOnClickListener {
                onEventClickListener?.invoke(event)
            }

            // 開始位置と高さを設定
            // 翌日にまたぐものなのかそうでないかで計算が大きく変わる
            val (fromY, y) = when (event.getCrossOverType(day)) {
                Event.CrossOver.None -> {
                    val startParams = getParams(event.start)
                    val endParams = getParams(event.end)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }

                Event.CrossOver.ToNextDay -> {
                    val startParams = getParams(event.start)
                    val endParams = getParams(event.end, 1)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }

                Event.CrossOver.FromPreviousDay -> {
                    val endParams = getParams(event.end)
                    0 to endParams.fromY
                }
            }


            // マージン指定
            val marginParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (y * density).toInt()).apply {
                topMargin = (fromY * density).toInt()
            }

            if (draggable) {
                binding.root.setOnTouchListener { _, touchEvent ->
                    if (touchEvent.action == MotionEvent.ACTION_DOWN) {
                        adjustStartTapY = touchEvent.y
                    }
                    false
                }
                // ロングクリック用のイベント追加します
                binding.root.setOnLongClickListener { view ->
                    val intent = Intent().apply {
                        putExtra("layoutPosition", layoutPosition)
                        putExtra("itemPosition", index)
                        putExtra("event", event.toBundle())
                    }
                    ViewCompat.startDragAndDrop(view, ClipData.newIntent("event", intent), EventDragShadowBuilder(view, adjustStartTapY), view, 0)
                    true
                }
                // ドラッグ用のイベント追加します
                binding.root.setOnDragListener { _, dragEvent ->
                    return@setOnDragListener when (dragEvent.action) {
                        DragEvent.ACTION_DRAG_STARTED -> false // 自身がドロップに反応させないようにする
                        else -> true
                    }
                }

                // イベント上のボタンでイベントの長さ調整をします
                binding.topAdjust.setOnTouchListener { _, touchEvent ->
                    return@setOnTouchListener when (touchEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            parent.requestDisallowInterceptTouchEvent(true)
                            adjustStartY = touchEvent.y
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val distance = convertPxToRoundedDp((touchEvent.y - adjustStartY))
                            binding.cardView.layoutParams = (binding.cardView.layoutParams as FrameLayout.LayoutParams).apply {
                                // Eventの長さはminEventHeightより小さくならず、drop直後のHeightより小さい
                                if ((height - distance) in minEventHeight..maxEventHeight && topMargin + distance < maxEventTop) {
                                    topMargin += distance
                                    height -= distance
                                    elapsedTime = TimeParams.from(height.toFloat(), density)
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (oldEvent != null) {
                                val newEvent = oldEvent!!.copy()
                                val cal = Calendar.getInstance().apply { time = oldEvent!!.end }
                                cal.add(Calendar.HOUR_OF_DAY, -elapsedTime!!.hour)
                                cal.add(Calendar.MINUTE, -elapsedTime!!.min)
                                newEvent.start = cal.time
                                // サーバのEventデータを書き換えます
                                onEventChangedListener?.invoke(oldEvent!!, newEvent, false)

                                // 編集表示用のEventを書き換えます
                                binding.cardView.setOnClickListener(null)
                                binding.cardView.setOnClickListener {
                                    onEventClickListener?.invoke(newEvent)
                                }

                                oldEvent = newEvent
                            }
                            true
                        }
                        else -> true
                    }
                }

                // イベント下のボタンでイベントの長さ調整をします
                binding.bottomAdjust.setOnTouchListener { _, touchEvent ->
                    return@setOnTouchListener when (touchEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            parent.requestDisallowInterceptTouchEvent(true)
                            adjustStartY = touchEvent.y
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val distance = convertPxToRoundedDp((touchEvent.y - adjustStartY))
                            binding.cardView.layoutParams = (binding.cardView.layoutParams as FrameLayout.LayoutParams).apply {
                                // Eventの長さはminEventHeightより小さくならず、drop直後のHeightより小さい
                                if ((height + distance) in minEventHeight..maxEventHeight) {
                                    height += distance
                                    elapsedTime = TimeParams.from(height.toFloat(), density)
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (oldEvent != null) {
                                val newEvent = oldEvent!!.copy()
                                val cal = Calendar.getInstance().apply { time = oldEvent!!.start }
                                cal.add(Calendar.HOUR_OF_DAY, elapsedTime!!.hour)
                                cal.add(Calendar.MINUTE, elapsedTime!!.min)
                                newEvent.end = cal.time
                                // サーバのEventデータを書き換えます
                                onEventChangedListener?.invoke(oldEvent!!, newEvent, false)

                                // 編集表示用のEventを書き換えます
                                binding.cardView.setOnClickListener(null)
                                binding.cardView.setOnClickListener {
                                    onEventClickListener?.invoke(newEvent)
                                }

                                oldEvent = newEvent
                            }
                            true
                        }
                        else -> true
                    }
                }
            }

            // Event長さ調整ボタンの表示制御用に格納
            eventViews.add(binding.root)

            addView(binding.root, FrameLayout.LayoutParams(marginParams))
        }
    }

    private fun getParams(date: Date, addDays: Int = 0): TimeParams {
        val cal = Calendar.getInstance().apply { time = date }
        return TimeParams(cal[Calendar.HOUR_OF_DAY] + (addDays * 24), cal[Calendar.MINUTE])
    }

    private fun convertPxToRoundedDp(px: Float): Int {
        // 10: 最小のドラッグ単位を10dpにする
        val unit = (density * 10).toInt()
        return (px / unit).roundToInt() * unit
    }
}

/**
 * イベント用のDragShadowBuilder
 */
class EventDragShadowBuilder(view: View, private val adjustStartTapY: Float) : DragShadowBuilder(view) {

    override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
        val margin = 20
        //影の分の領域を含めたサイズを設定
        shadowSize.set(view.width + margin, view.height + margin)
        //viewの中央に設定
        shadowTouchPoint.set(view.width / 2, adjustStartTapY.toInt())
    }
}
