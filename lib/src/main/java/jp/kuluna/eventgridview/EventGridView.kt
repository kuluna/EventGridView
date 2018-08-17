package jp.kuluna.eventgridview

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewEventGridBinding
import jp.kuluna.eventgridview.databinding.ViewScaleListBinding
import java.util.*

class EventGridView : FrameLayout {
    private val binding: ViewEventGridBinding
    private var counterGridAdapter: CounterGridAdapter? = null
    private var limits: List<Limit> = emptyList()
    private var scaleListAdapter: ScaleListAdapter
    var adapter: EventGridAdapter?
        get() = binding.eventGridRecyclerView.adapter as? EventGridAdapter
        set(value) {
            binding.eventGridRecyclerView.adapter = value
            value?.onScaleRefreshListener = {
                scaleListAdapter.setItemsIn(1, 24 + (counterGridAdapter?.overTime ?: -1))
                if (binding.counterVisibility) {
                    refreshCounter(value?.getEvents() ?: emptyList())
                }
            }
        }

    /**
     * Eventのカウンタを表示します
     * @param events 集計するEventのリスト
     * @param date 基準となる日付
     * @param limits 各時間帯の上限・下限値(任意)
     */
    fun showCounter(events: List<Event>, date: Date, limits: List<Limit> = emptyList()) {
        val counterGridAdapter = CounterGridAdapter(context)
        this.counterGridAdapter = counterGridAdapter
        binding.counterVisibility = true
        binding.counterGridRecyclerView.adapter = counterGridAdapter
        this.limits = limits

        // カウンタの状態が変わる区切りを保存する
        var periods = mutableListOf<Date>()
        for (event in events) {
            periods.add(event.start)
            periods.add(event.end)
        }
        for (limit in limits) {
            periods.add(limit.start)
            periods.add(limit.end)
        }
        periods = periods.distinct().sorted().toMutableList()

        // カウンタのリストを作成する
        val counters = mutableListOf<Counter>()
        for (i in 0..(periods.size - 2)) {
            val period = periods[i]
            val limit = limits.firstOrNull { it.start <= period && it.end > period }
            counters.add(Counter(periods[i], periods[i + 1], events.count { it.start <= period && it.end > period }, limit?.minimum, limit?.maximum))
        }
        counterGridAdapter.replace(counters, date)
        // 目盛りを再設定します
        scaleListAdapter.setItemsIn(1, 24 + counterGridAdapter.overTime)
    }

    /** カウンタを更新します */
    private fun refreshCounter(events: List<Event>) {
        val counterGridAdapter = this.counterGridAdapter ?: return
        // カウンタの状態が変わる区切りを保存する
        var periods = mutableListOf<Date>()
        for (event in events) {
            periods.add(event.start)
            periods.add(event.end)
        }
        for (limit in limits) {
            periods.add(limit.start)
            periods.add(limit.end)
        }
        periods = periods.distinct().sorted().toMutableList()

        // カウンタのリストを作成する
        val counters = mutableListOf<Counter>()
        for (i in 0..(periods.size - 2)) {
            val period = periods[i]
            val limit = limits.firstOrNull { it.start <= period && it.end > period }
            counters.add(Counter(periods[i], periods[i + 1], events.count { it.start <= period && it.end > period }, limit?.minimum, limit?.maximum))
        }
        counterGridAdapter.replace(counters, counterGridAdapter.day)
        // 目盛りを再設定します
        scaleListAdapter.setItemsIn(1, 24 + (counterGridAdapter.overTime))
    }

    /** イベントにクリックリスナを実装します */
    fun setOnEventClickListener(onEventClickListener: ((Event) -> Unit)?) {
        adapter?.onEventClickListener = onEventClickListener
    }

    /** カウンタにクリックリスナを実装します */
    fun setOnCounterClickListener(onCounterClickListener: ((Counter) -> Unit)?) {
        counterGridAdapter?.onCounterClickListener = onCounterClickListener
    }

    class ScaleListAdapter(context: Context) : ArrayAdapter<Int>(context, R.layout.view_scale_list) {
        private var items: List<Int> = emptyList()

        override fun getCount(): Int = items.size

        override fun getItemId(position: Int): Long = 0

        override fun getItem(position: Int): Int = items[position]

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = if (convertView == null) {
                createView(parent).apply { tag = this }
            } else {
                convertView.tag as View
            }
            return bindView(view, position)
        }

        private fun createView(parent: ViewGroup): View {
            return ViewScaleListBinding.inflate(LayoutInflater.from(context), parent, false).root
        }

        private fun bindView(view: View, position: Int): View {
            return DataBindingUtil.bind<ViewScaleListBinding>(view)!!.apply { hour = getItem(position) }.root
        }

        /** from-toの間の時間(単位:時間)をItemsに格納します */
        fun setItemsIn(from: Int, to: Int) {
            val newItems = mutableListOf<Int>()
            for (i in from..to) {
                newItems.add(i)
            }
            items = newItems.toList()
            notifyDataSetChanged()
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_event_grid, this, true)
        binding.eventGridRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.counterGridRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.eventGridRecyclerView.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    adapter?.hideAllAdjustButton()
                    false
                }
                else -> false
            }
        }

        binding.counterVisibility = false
        scaleListAdapter = ScaleListAdapter(context)
        binding.scaleListView.adapter = scaleListAdapter
    }
}
