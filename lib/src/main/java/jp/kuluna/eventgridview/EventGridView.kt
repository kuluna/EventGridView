package jp.kuluna.eventgridview

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import jp.kuluna.eventgridview.databinding.ViewEventGridBinding
import jp.kuluna.eventgridview.databinding.ViewScaleBinding

class EventGridView : FrameLayout {
    private val binding: ViewEventGridBinding

    var adapter: EventGridAdapter?
        get() = binding.eventGridRecyclerView.adapter as? EventGridAdapter
        set(value) {
            binding.eventGridRecyclerView.adapter = value
            value.onRplaceListener = {
                binding.scaleLinearLayout.run {
                    // 一番下の目盛り(これの下のビューを追加)
                    val indexScale = indexOfChild(binding.bottomScaleView)
                    // 一番下のスペース(これの上に追加したビューがある)
                    val indexSpace = indexOfChild(binding.bottomSpaceView)
                    // 追加すべき要素数
                    val addCount = it + 1
                    // 追加済みの要素数
                    val addedCount = indexSpace - indexScale - 1

                    if (addCount > addedCount) {
                        //足りないぶんを追加
                        for (i in addedCount..(addCount - 1)) {
                            addView(getScaleLayout(this, 24 + i), indexScale + 1 + i)
                        }
                    } else if (addCount < addedCount) {
                        //不要なぶんを削除
                        for (i in addCount..(addedCount - 1)) {
                            removeViewAt(indexScale + 1)
                        }
                    }
                }
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_event_grid, this, true)
        binding.eventGridRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.eventGridRecyclerView.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    adapter?.hideAllAdjustButton()
                    false
                }
                else -> false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getScaleLayout(parent: ViewGroup, hour: Int): View {
        val scaleViewBinding: ViewScaleBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_scale, parent, false)
        scaleViewBinding.text.text = "$hour:00"
        return scaleViewBinding.root
    }
}
