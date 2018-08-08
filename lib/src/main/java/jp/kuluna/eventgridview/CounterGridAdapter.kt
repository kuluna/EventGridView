package jp.kuluna.eventgridview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * EventGridView用Adapter
 * @param context [Context]
 */
open class CounterGridAdapter(private val context: Context) : RecyclerView.Adapter<CounterGridViewHolder>() {
    private var counters = emptyList<Counter>()
    /** 最後の終了時刻 */
    private val lastEnd
        get() = counters.maxBy { it.end }?.end
    /** 基準日 */
    var day = Date()
    /** 24時間を超えた時間 */
    val overTime: Int
        get() {
            val selectCal = Calendar.getInstance()
            selectCal.time = day
            val lastEndCal = Calendar.getInstance()
            lastEndCal.time = lastEnd ?: day

            return if (selectCal.get(Calendar.DATE) != lastEndCal.get(Calendar.DATE)) {//日跨ぎ有り
                lastEndCal.get(Calendar.HOUR_OF_DAY)
            } else {//日跨ぎ無し
                -1
            }
        }
    /** CounterViewColumnで生成されたのCounterView格納用 */
    private var counterViews = mutableListOf<View>()
    /** ViewHolder全体のEventViewの配列の格納用 */
    private var counterViewGroup = mutableListOf<List<View>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterGridViewHolder = CounterGridViewHolder(CounterColumnView(context))

    override fun getItemCount(): Int = counters.size

    override fun onBindViewHolder(holder: CounterGridViewHolder, position: Int) {
        val counter = counters
        holder.view.set(day, counter, holder.layoutPosition)
        counterViews = holder.view.counterViews
        counterViewGroup.add(counterViews)
    }

    /**
     * カウンタを全てクリアして引数で渡すカウンタに差し替えます。
     * @param counters カウンターリストリスト
     */
    fun replace(counters: List<Counter>, day: Date) {
        this.counters = counters
        this.day = day

        notifyDataSetChanged()
    }

    /**
     * カウンタを全てクリアして引数で渡すカウンタに差し替えます。
     * @param counters カウンターリストリスト
     */
    fun replace(counters: List<Counter>) {
        this.counters = counters

        notifyDataSetChanged()
    }
}
