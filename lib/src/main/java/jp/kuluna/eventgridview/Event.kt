package jp.kuluna.eventgridview

import android.graphics.Bitmap
import android.os.Bundle
import androidx.annotation.ColorInt
import org.apache.commons.lang3.time.DateUtils
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
        /** テキストの文字色 */
        @ColorInt var borderColor: Int,
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
                bundle.getString("text") ?: "",
                bundle.getInt("backgroundColor"),
                bundle.getInt("textColor"),
                bundle.getInt("borderColor"),
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
        putInt("borderColor", borderColor)
        putParcelable("icon", icon)
        putString("extra", extra)
        putBoolean("draggable", draggable)

    }

    fun getCrossOverType(base: Date): CrossOver {
        val baseDate = DateUtils.truncate(base, Calendar.DATE)
        val startDate = DateUtils.truncate(start, Calendar.DATE)
        return if (startDate > baseDate) {
            CrossOver.FromNextDay
        } else if (DateUtils.isSameDay(start, end)) {
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
        None, ToNextDay, FromPreviousDay, FromNextDay
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


data class Counter(
        /** 開始時間 */
        var start: Date,
        /** 終了時間 */
        var end: Date,
        /** カウントした値 */
        var count: Int,
        /** 最小値 */
        var minimum: Int? = null,
        /** 最大値 */
        var maximum: Int? = null
) {
    val text: String
        get() = count.toString()

    fun getCrossOverType(base: Date): CrossOver {
        return if (!DateUtils.isSameDay(start, base)) {
            CrossOver.FromNextDay
        } else if (DateUtils.isSameDay(start, end)) {
            CrossOver.None
        } else {
            if (DateUtils.isSameDay(start, base)) {
                CrossOver.ToNextDay
            } else {
                CrossOver.FromPreviousDay
            }
        }
    }

    fun validate(): Boolean {
        if (minimum != null && minimum!! > count) {
            return false
        }
        if (maximum != null && maximum!! < count) {
            return false
        }
        return true
    }

    enum class CrossOver {
        None, ToNextDay, FromPreviousDay, FromNextDay
    }
}

data class Limit(
        /** 開始時間 */
        var start: Date,
        /** 終了時間 */
        var end: Date,
        /** 最小値 */
        var minimum: Int = 0,
        /** 最大値 */
        var maximum: Int = 0
)
