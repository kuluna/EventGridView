package jp.kuluna.eventgridview

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewEventGridBinding
import java.util.*

class EventGridView : FrameLayout {
    private val binding: ViewEventGridBinding
    private lateinit var counterGridAdapter: CounterGridAdapter
    var adapter: EventGridAdapter?
        get() = binding.eventGridRecyclerView.adapter as? EventGridAdapter
        set(value) {
            binding.eventGridRecyclerView.adapter = value
            value?.onScaleRefreshListener = {
                binding.overTime = it
                if (binding.counterVisibility) {
                    refleshCounter(value?.getEvents() ?: emptyList())
                }
            }
        }

    /** カウンターをセットします */
    fun setCounter(events: List<Event>, date: Date) {
        counterGridAdapter = CounterGridAdapter(context)
        binding.counterVisibility = true
        binding.counterGridRecyclerView.adapter = counterGridAdapter

        var periods = mutableListOf<Date>()
        for (event in events) {
            periods.add(event.start)
            periods.add(event.end)
        }
        periods = periods.distinct().sorted().toMutableList()
        val counters = mutableListOf<Counter>()

        for (i in 0..(periods.size - 2)) {
            val period = periods[i]
            counters.add(Counter(periods[i], periods[i + 1], events.count { it.start <= period && it.end > period }))
        }

        counterGridAdapter.replace(counters, date)
    }

    /** カウンターを更新します */
    private fun refleshCounter(events: List<Event>) {
        var periods = mutableListOf<Date>()
        for (event in events) {
            periods.add(event.start)
            periods.add(event.end)
        }
        periods = periods.distinct().sorted().toMutableList()
        val counters = mutableListOf<Counter>()

        for (i in 0..(periods.size - 2)) {
            val period = periods[i]
            counters.add(Counter(periods[i], periods[i + 1], events.count { it.start <= period && it.end > period }))
        }

        counterGridAdapter.replace(counters)
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
    }
}
