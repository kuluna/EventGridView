package jp.kuluna.eventgridview

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewEventListBinding

class DraggableEventGridListView : FrameLayout {
    private lateinit var binding: ViewEventListBinding
    private var dragging = false

    constructor(context: Context) : super(context) {
        load()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        load()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        load()
    }

    val eventGridView: EventGridView
        get() = binding.eventGridView

    @SuppressLint("ClickableViewAccessibility")
    private fun load() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_event_list, this, true)

        binding.scrollView.setOnTouchListener { _, _ ->
            if (dragging) {
                binding.scrollView.scrollTo(binding.scrollView.x.toInt(),binding.scrollView.y.toInt() + 1)
            }
            false
        }
    }
}
