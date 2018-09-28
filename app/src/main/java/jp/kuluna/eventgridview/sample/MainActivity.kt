package jp.kuluna.eventgridview.sample

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import jp.kuluna.eventgridview.Event
import jp.kuluna.eventgridview.EventGridAdapter
import jp.kuluna.eventgridview.Limit
import jp.kuluna.eventgridview.sample.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: EventGridAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        adapter = EventGridAdapter(this)
        binding.eventGridView.apply {
            adapter = this@MainActivity.adapter
            // 表示範囲を固定する場合は下記のように指定してください
            setScale(6, 24)
            // 各イベントは下記のように実装してください
            setOnEventClickListener {
                Log.i("onEventClick", it.toString())
                // イベントのポジションを変更する場合は下記のように指定してください
                val newStart = Calendar.getInstance().apply {
                    time = it.start
                    add(Calendar.HOUR_OF_DAY, 1)
                }.time
                val newEnd = Calendar.getInstance().apply {
                    time = it.end
                    add(Calendar.HOUR_OF_DAY, 1)
                }.time
                adapter?.updateEventPosition(newStart, newEnd) { event ->
                    event.text == it.text
                }
                // イベントのExtraに変更を加える場合は下記のように指定してください
                adapter?.updateEventExtra("newCond") { event ->
                    event.text == it.text
                }
            }
            setOnCounterClickListener {
                Log.i("onCounterClick", it.toString())
            }
            setOnEventChangedListener { old, new ->
                Log.i("onEventChanged", "$old -> $new")
            }
            setOnDragStartListener { event ->
                Log.i("onDragStart", event.toString())
            }
            setOnDragEndListener { event ->
                Log.i("onDragEnd", event.toString())
            }
        }
        showEvents()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadButton -> showEvents()
        }
        return true
    }

    private fun showEvents() {
        val textColor = ContextCompat.getColor(this, android.R.color.black)
        val blueGridColor = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val blueBorderColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark)

        val events: List<Event> = (0..20).map {
            val random = Random().nextInt(24)
            val startedAt = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, random)
            }.time
            val endedAt = Calendar.getInstance().apply {
                time = startedAt
                add(Calendar.HOUR_OF_DAY, random)
            }.time

            Event(
                    it,
                    startedAt,
                    endedAt,
                    "Event$it",
                    blueGridColor,
                    textColor,
                    blueBorderColor,
                    null,
                    null,
                    true)
        }

        adapter.replace(events, Date())

        // Eventのカウンタを表示する
        val countStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
        }.time
        val countEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
        }.time
        val limits: List<Limit> = listOf(
                Limit(countStart, countEnd, 3, 6)
        )

        binding.eventGridView.showCounter(events, Date(), limits) {
            // イベントごとにカウントする数量を指定する場合はこのように記述してください
            when {
                it.text == "Event1" -> -1
                else -> 1
            }
        }
    }
}
