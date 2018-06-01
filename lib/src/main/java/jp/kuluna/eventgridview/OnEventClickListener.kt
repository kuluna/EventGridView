package jp.kuluna.eventgridview

import jp.kuluna.eventgridview.Event

interface OnEventClickListener {
    fun onEventClick(event: Event)
}

interface OnEventChangedListener {
    fun onChange(oldEvent: Event, newEvent: Event)
}
