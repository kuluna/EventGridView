package jp.kuluna.eventgridview.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import jp.kuluna.eventgridview.DragableEventGridListView
import jp.kuluna.eventgridview.EventGridAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = EventGridAdapter(this, true)
        findViewById<DragableEventGridListView>(R.id.event_grid_view).eventGridView.adapter = adapter
    }
}
