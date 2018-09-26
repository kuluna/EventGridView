package jp.kuluna.eventgridview

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewCounterBinding
import java.util.*

/**
 * カウンタを1列内に表示するためのView
 * @param context Android Context
 */
@SuppressLint("ViewConstructor")
open class CounterColumnView(context: Context) : FrameLayout(context) {
    /** Counterのクリックイベント */
    var onCounterClickListener: ((Counter) -> Unit)? = null
    /** ディスプレイの密度取得 (この値にdpを掛けるとpxになる) */
    private val density = context.resources.displayMetrics.density
    /** Eventの横幅(dp) */
    private val widthDp = 40
    /** 目盛り一つの幅 */
    private val aScale: Int = context.resources.getDimensionPixelSize(R.dimen.a_scale)
    /** RecyclerViewにおけるこのViewの現在のPosition */
    private var layoutPosition = 0
    private var counters: MutableList<Counter> = mutableListOf()
    /** 日付 */
    lateinit var day: Date
    /** EventViewの格納用 */
    var counterViews = mutableListOf<View>()

    init {
        layoutParams = FrameLayout.LayoutParams((widthDp * density).toInt(), FrameLayout.LayoutParams.MATCH_PARENT).apply {
            // カウンタの左右に1dpのマージンを開ける
            setMargins(density.toInt(), 0, density.toInt(), 0)
        }
    }

    /**
     * カウンタを表示します。
     * @param day 表示する日
     * @param counters カウンタリスト
     * @param layoutPosition RecyclerViewから見たLayoutPosition
     */
    fun set(day: Date, counters: List<Counter>, layoutPosition: Int) {
        this.day = day
        removeAllViews()
        this.layoutPosition = layoutPosition
        this.counters = counters.toMutableList()

        val inflater = LayoutInflater.from(context)

        counters.forEach { counter ->
            val binding = DataBindingUtil.inflate<ViewCounterBinding>(inflater, R.layout.view_counter, null, false)
            binding.counter = counter

            // 開始位置と高さを設定
            // 翌日にまたぐものなのかそうでないかで計算が大きく変わる
            val (fromY, y) = when (counter.getCrossOverType(day)) {
                Counter.CrossOver.None -> {
                    val startParams = getParams(counter.start)
                    val endParams = getParams(counter.end)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }

                Counter.CrossOver.ToNextDay -> {
                    val startParams = getParams(counter.start)
                    val endParams = getParams(counter.end, 1)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }

                Counter.CrossOver.FromPreviousDay -> {
                    val endParams = getParams(counter.end)
                    0 to endParams.fromY
                }

                Counter.CrossOver.FromNextDay -> {
                    val startParams = getParams(counter.start, 1)
                    val endParams = getParams(counter.end, 1)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }
            }

            // マージン指定
            val marginParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (y * density).toInt()).apply {
                topMargin = (fromY * density).toInt() + aScale / 2
            }

            binding.counterFrame.setOnClickListener {
                onCounterClickListener?.invoke(counter)
            }
            addView(binding.root, FrameLayout.LayoutParams(marginParams))
        }
    }

    private fun getParams(date: Date, addDays: Int = 0): TimeParams {
        val cal = Calendar.getInstance().apply { time = date }
        return TimeParams(cal[Calendar.HOUR_OF_DAY] + (addDays * 24), cal[Calendar.MINUTE])
    }
}
