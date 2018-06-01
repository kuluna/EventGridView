package jp.kuluna.eventgridview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class TouchableRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * Android Studio > 3.0時
     * onTouchEvent実装時はperformClickをoverrideしないとLint警告が出てしまう為用意しました
     */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
