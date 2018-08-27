package jp.kuluna.eventgridview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.apache.commons.lang.time.DateUtils
import java.util.*

/**
 * CounterGridView用Adapter
 * @param context [Context]
 */
open class CounterGridAdapter(private val context: Context) : RecyclerView.Adapter<CounterGridViewHolder>() {
    /** Counterのクリックイベント */
    var onCounterClickListener: ((Counter) -> Unit)? = null

    private var counters = emptyList<Counter>()
    /** 最初の開始時刻 */
    private val firstStart
        get() = counters.minBy { it.start }?.start
    /** 最後の終了時刻 */
    private val lastEnd
        get() = counters.maxBy { it.end }?.end
    /** 基準日 */
    var day = DateUtils.truncate(Date(), Calendar.DATE)
    /** 最小の時間(単位:時間) */
    val minTime: Int
        get() {
            val selectCal = Calendar.getInstance()
            selectCal.time = day
            val firstStartCal = Calendar.getInstance()
            firstStartCal.time = firstStart ?: day

            return firstStartCal.get(Calendar.HOUR_OF_DAY)
        }
    /** 時間の最大値 */
    val maxTime: Int
        get() {
            val selectCal = Calendar.getInstance()
            selectCal.time = day
            val lastEndCal = Calendar.getInstance()
            lastEndCal.time = lastEnd ?: day

            // 0分なら１時間目盛りを減らして調整する
            var adjustMax = 0
            if (lastEndCal.get(Calendar.MINUTE) == 0) {
                adjustMax = 1
            }

            return if (selectCal.get(Calendar.DATE) != lastEndCal.get(Calendar.DATE)) {
                // 日跨ぎ有りなら+24時間と、端数を考慮して+1時間
                lastEndCal.get(Calendar.HOUR_OF_DAY) + 24 + 1 - adjustMax
            } else {
                // 日跨ぎなしなら端数を考慮して+1時間
                lastEndCal.get(Calendar.HOUR_OF_DAY) + 1 - adjustMax
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterGridViewHolder = CounterGridViewHolder(CounterColumnView(context))

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: CounterGridViewHolder, position: Int) {
        val counter = counters
        holder.view.set(day, counter, holder.layoutPosition)
        holder.view.onCounterClickListener = {
            onCounterClickListener?.invoke(it)
        }
    }

    /**
     * カウンタを全てクリアして引数で渡すカウンタに差し替えます。
     * @param counters カウンタリスト
     */
    fun replace(counters: List<Counter>) {
        this.counters = counters

        notifyDataSetChanged()
    }

    /**
     * カウンタを取得します
     */
    fun getCounters(): List<Counter> {
        return counters
    }
}
