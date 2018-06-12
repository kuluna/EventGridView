package jp.kuluna.eventgridview.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import jp.kuluna.eventgridview.Event
import jp.kuluna.eventgridview.EventGridAdapter
import jp.kuluna.eventgridview.EventGridView
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var eventGridView: EventGridView
    private lateinit var adapter: EventGridAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        eventGridView = findViewById(R.id.event_grid_view)
        adapter = EventGridAdapter(this)
        eventGridView.adapter = adapter
        showEvents()
    }

    private fun showEvents() {
        val startedAt = Date()
        val endedAt = Calendar.getInstance().apply {
            time = startedAt
            add(Calendar.HOUR_OF_DAY, 12)
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
        //eventGridView.setOverTime()
    }
}
