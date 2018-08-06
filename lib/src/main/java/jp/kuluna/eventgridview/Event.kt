package jp.kuluna.eventgridview

import android.graphics.Bitmap
import android.os.Bundle
import android.support.annotation.ColorInt
import org.apache.commons.lang.time.DateUtils
import java.util.*
import kotlin.math.roundToInt

data class Event(
        /** 同じIDをまとめて1列に表示するためのID
         * EventGridAdapterで widthIsMatchParent == true としている場合は任意の値で固定してください
         */
        var groupId: Int,
        /** 開始時間 */
        var start: Date,
        /** 終了時間 */
        var end: Date,
        /** イベント内に表示するテキスト */
        var text: String,
        /** イベントの背景色 */
        @ColorInt var backgroundColor: Int,
        /** テキストの文字色 */
        @ColorInt var textColor: Int,
        /** イベント内に表示するアイコン */
        var icon: Bitmap? = null,
        /** 追加で保持したい値がある場合はここに代入 */
        var extra: String? = null,
        /** イベントのドラッグが可能かどうかのフラグ */
        var draggable: Boolean = false
) {
    companion object {
        fun from(bundle: Bundle) = Event(
                bundle.getInt("groupId"),
                Date(bundle.getLong("start")),
                Date(bundle.getLong("end")),
                bundle.getString("text"),
                bundle.getInt("backgroundColor"),
                bundle.getInt("textColor"),
                bundle.getParcelable("icon"),
                bundle.getString("extra"),
                bundle.getBoolean("draggable")
        )
    }

    fun toBundle() = Bundle().apply {
        putInt("groupId", groupId)
        putLong("start", start.time)
        putLong("end", end.time)
        putString("text", text)
        putInt("backgroundColor", backgroundColor)
        putInt("textColor", textColor)
        putParcelable("icon", icon)
        putString("extra", extra)
        putBoolean("draggable", draggable)

    }

    fun getCrossOverType(base: Date): CrossOver {
        return if (DateUtils.isSameDay(start, end)) {
            CrossOver.None
        } else {
            if (DateUtils.isSameDay(start, base)) {
                CrossOver.ToNextDay
            } else {
                CrossOver.FromPreviousDay
            }
        }
    }

    enum class CrossOver {
        None, ToNextDay, FromPreviousDay
    }
}

data class TimeParams(
        val hour: Int,
        val min: Int
) {
    companion object {
        fun from(y: Float, density: Float): TimeParams {
            val roundY = (y / density / 10).roundToInt() * 10
            return TimeParams(roundY / 40, (roundY % 40) / 10 * 15)
        }
    }

    val fromY = ((hour * 40) + (min * 10 / 15))
}
