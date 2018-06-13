package jp.kuluna.eventgridview.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import jp.kuluna.eventgridview.DragableEventGridListView
import jp.kuluna.eventgridview.Event
import jp.kuluna.eventgridview.EventGridAdapter
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: EventGridAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = EventGridAdapter(this, true)
        findViewById<DragableEventGridListView>(R.id.event_grid_view).eventGridView.adapter = adapter
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
                    "名前",
                    gridColor,
                    blackColor,
                    null,
                    null))
        }

        adapter.replace(events, Date())
    }
}
