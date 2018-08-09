package jp.kuluna.eventgridview.sample

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
        binding.eventGridView.adapter = adapter
        showEvents()
        // 各イベントは下記のように実装してください
        binding.eventGridView.setOnEventClickListener {
            Log.i("MainActivity", it.toString())
        }
        binding.eventGridView.setOnCounterClickListener {
            Log.i("MainActivity", it.toString())
        }
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

        binding.eventGridView.showCounter(events, Date(), limits)
    }
}
