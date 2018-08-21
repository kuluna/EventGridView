package jp.kuluna.eventgridview

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import jp.kuluna.eventgridview.databinding.ViewEventBinding
import java.util.*

/**
 * EventGridView用Adapter
 * @param context [Context]
 * @param widthIsMatchParent true に設定すると EventGridColumnView の width が match_parent に
 */
open class EventGridAdapter(private val context: Context, private val widthIsMatchParent: Boolean = false) : RecyclerView.Adapter<EventGridViewHolder>() {
    /** Eventのクリックイベント */
    var onEventClickListener: ((Event) -> Unit)? = null
    /** Eventの変更イベント */
    var onEventChangedListener: ((Event, Event) -> Unit)? = null
    /** 目盛が変わったことによる変更イベント */
    var onScaleRefreshListener: ((Int, Int) -> Unit)? = null
    /** ドラッグのイベント */
    var onEventDragListener: ((DragEvent) -> Unit)? = null
    /** Event伸縮のイベント */
    var onEventStretchListener: ((MotionEvent) -> Unit)? = null

    private var scaleFrom: Int = 0
    private var scaleTo: Int = 23

    private var events = emptyList<Event>()
    private var group = emptyList<Pair<Int, List<Event>>>()
    private var day = Date()
    /** 最初の開始時刻 */
    private val firstStart
        get() = events.minBy { it.start }?.start
    /** 最後の終了時刻 */
    private val lastEnd
        get() = events.maxBy { it.end }?.end
    /** 最小の時間(単位:時間) */
    val minTime: Int
        get() {
            val selectCal = Calendar.getInstance()
            selectCal.time = day
            val firstStartCal = Calendar.getInstance()
            firstStartCal.time = firstStart ?: day

            return firstStartCal.get(Calendar.HOUR_OF_DAY)
        }
    /** 最大の時間(単位:時間) */
    val maxTime: Int
        get() {
            val selectCal = Calendar.getInstance()
            selectCal.time = day
            val lastEndCal = Calendar.getInstance()
            lastEndCal.time = lastEnd ?: day

            return if (selectCal.get(Calendar.DATE) != lastEndCal.get(Calendar.DATE)) {
                // 日跨ぎ有りなら+24時間と、端数を考慮して+1時間
                lastEndCal.get(Calendar.HOUR_OF_DAY) + 24 + 1
            } else {
                // 日跨ぎなしなら端数を考慮して+1時間
                lastEndCal.get(Calendar.HOUR_OF_DAY) + 1
            }
        }
    /** EventViewColumnで生成されたのEventView格納用 */
    private var eventViews = mutableListOf<View>()
    /** ViewHolder全体のEventViewの配列の格納用 */
    private var eventViewGroup = mutableListOf<List<View>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventGridViewHolder = EventGridViewHolder(EventColumnView(context, widthIsMatchParent, scaleFrom, scaleTo))

    override fun getItemCount(): Int = group.size

    override fun onBindViewHolder(holder: EventGridViewHolder, position: Int) {
        val event = group[holder.layoutPosition].second
        holder.view.set(day, event, holder.layoutPosition)
        holder.view.onEventClickListener = {
            onEventClickListener?.invoke(it)
        }
        holder.view.onEventDragListener = {
            onEventDragListener?.invoke(it)
        }
        holder.view.onEventStretchListener = {
            onEventStretchListener?.invoke(it)
        }
        holder.view.onEventChangedListener = { old, new, hideAll ->
            onEventChangedListener?.invoke(old, new)

            // イベント長さ調整ボタンを全て非表示
            if (hideAll) {
                hideAllAdjustButton()
            }
            val index = events.indexOf(old)
            events[index].start = new.start
            events[index].end = new.end
            scaleRefresh()
        }

        eventViews = holder.view.eventViews
        eventViewGroup.add(eventViews)
    }

    /**
     * 今のイベントを全てクリアして引数で渡すイベントに差し替えます。
     * @param events イベントリスト
     */
    fun replace(events: List<Event>, day: Date) {
        this.events = events
        this.group = this.events.groupBy { it.groupId }.toList()
        this.day = day

        scaleRefresh()
        notifyDataSetChanged()
    }

    /**
     * 全てのEventViewの長さ調整ボタンを非表示にします
     */
    fun hideAllAdjustButton() {
        eventViewGroup.forEach { views ->
            views.forEach { view ->
                DataBindingUtil.bind<ViewEventBinding>(view)?.run {
                    topAdjust.visibility = View.GONE
                    bottomAdjust.visibility = View.GONE
                }
            }
        }
    }

    /** 目盛りの範囲を設定します */
    internal fun setScale(from: Int, to: Int) {
        scaleFrom = from
        scaleTo = to
        replace(events, day)
    }

    /**
     * イベントを取得します
     */
    fun getEvents(): List<Event> {
        return events
    }

    /**
     * 目盛を終了が最も遅いイベントに合わせて更新します
     */
    private fun scaleRefresh() {
        onScaleRefreshListener?.let {
            it(minTime, maxTime)
        }
    }
}
