package jp.kuluna.eventgridview

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import jp.kuluna.eventgridview.databinding.ViewEventListBinding

class DragableEventGridListView : ScrollView {
    private lateinit var binding: ViewEventListBinding

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

    private fun load() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_event_list, this, true)
    }
}
