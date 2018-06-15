package jp.kuluna.eventgridview

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ScrollView
import jp.kuluna.eventgridview.databinding.ViewEventListBinding

class DraggableEventGridListView : FrameLayout {
    private lateinit var binding: ViewEventListBinding
    /** 現在タッチしているy座標 */
    private var touchingAbsoluteY: Int = 0

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

    var adapter: EventGridAdapter?
        get() = eventGridView.adapter
        set(value) {
            eventGridView.adapter = value
            value?.onEventDragListener = {
                if (it.action == DragEvent.ACTION_DRAG_ENDED) {
                    handler.removeMessageAll()
                } else if (it.action != DragEvent.ACTION_DRAG_EXITED) {
                    val frameY = it.y - binding.scrollView.scrollY
                    when {
                        frameY < height * 0.1 -> handler.moveToTop()
                        frameY > height * 0.9 -> handler.moveToBottom()
                        else -> handler.removeMessageAll()
                    }
                }
            }

            value?.onEventStretchListener = {
                if (it.action == MotionEvent.ACTION_UP) {
                    handler.removeMessageAll()
                } else if (it.action == MotionEvent.ACTION_MOVE) {
                    val frameY = touchingAbsoluteY
                    Log.d("testtest", it.y.toString())
                    when {
                        frameY < height * 0.1 -> handler.moveToTop()
                        frameY > height * 0.9 -> handler.moveToBottom()
                        else -> handler.removeMessageAll()
                    }
                }
            }
        }
    private lateinit var handler: ScrollHandler

    private fun load() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_event_list, this, true)
        handler = ScrollHandler(binding.scrollView)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (ev == null) {
            return super.dispatchTouchEvent(ev)
        }
        touchingAbsoluteY = ev.y.toInt()
        return super.dispatchTouchEvent(ev)
    }
}

class ScrollHandler(val scrollView: ScrollView) : Handler() {
    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        msg?.arg1 ?: return
        scrollView.scrollBy(0, msg.arg1)
        sendMessageDelayed(Message.obtain(this, msg.what, msg.arg1, msg.arg2), 15)
    }

    fun removeMessageAll() {
        removeMessages(1)
        removeMessages(2)
    }

    fun moveToTop() {
        removeMessages(2)
        if (!hasMessages(1)) sendMessage(Message.obtain(this, 1, -20, 0))
    }

    fun moveToBottom() {
        removeMessages(1)
        if (!hasMessages(2)) sendMessage(Message.obtain(this, 2, 20, 0))
    }
}