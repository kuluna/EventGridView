package jp.kuluna.eventgridview

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.apache.commons.lang3.time.DateUtils
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

            // 0分でなければ１時間目盛りを増やして調整する
            val adjustMax = if (lastEndCal.get(Calendar.MINUTE) == 0) {
                0
            } else {
                1
            }

            return if (selectCal.get(Calendar.DATE) != lastEndCal.get(Calendar.DATE)) {
                // 日跨ぎ有りなら+24時間
                lastEndCal.get(Calendar.HOUR_OF_DAY) + 24 + adjustMax
            } else {
                lastEndCal.get(Calendar.HOUR_OF_DAY) + adjustMax
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
