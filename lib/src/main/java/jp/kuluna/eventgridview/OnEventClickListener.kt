package jp.kuluna.eventgridview

interface OnEventClickListener {
    fun onEventClick(event: Event)
}

interface OnEventChangedListener {
    fun onChange(oldEvent: Event, newEvent: Event)
}

interface OnCounterClickListener {
    fun onCounterClick(counter: Counter)
}