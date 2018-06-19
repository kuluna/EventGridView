package jp.kuluna.eventgridview.sample

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import jp.kuluna.eventgridview.Event
import jp.kuluna.eventgridview.EventGridAdapter
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadButton -> {
                showEvents()
            }
        }
        return true
    }

    private fun showEvents() {
        val period = Random().nextInt(24)
        val startedAt = Date()
        val endedAt = Calendar.getInstance().apply {
            time = startedAt
            add(Calendar.HOUR_OF_DAY, period)
        }.time
        val gridColor = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val blackColor = ContextCompat.getColor(this, android.R.color.black)

        val events: List<Event> = ArrayList<Event>().apply {
            add(Event(
                    0,
                    startedAt,
                    endedAt,
                    "ドラッグ不可",
                    gridColor,
                    blackColor,
                    null,
                    null,
                    false))

            add(Event(
                    1,
                    startedAt,
                    endedAt,
                    "ドラッグ可",
                    gridColor,
                    blackColor,
                    null,
                    null,
                    true))
        }

        adapter.replace(events, Date())
    }
}
