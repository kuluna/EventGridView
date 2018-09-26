package jp.kuluna.eventgridview

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

internal class TouchableRecyclerView : RecyclerView {
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

internal class TouchableImageView : AppCompatImageView {
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
