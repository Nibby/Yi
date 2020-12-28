package yi.core.go

/**
 * The super class for providing model events to outgoing sources.
 * Each [EventHook] provides a single type of event source to subscribe to.
 */
abstract class EventHook<EventType> {

    private var listeners: HashSet<EventListener<EventType>> = HashSet()

    /**
     * Sends an event message to all existing listeners.
     */
    internal fun fireEvent(event: EventType) {
        listeners.forEach { listener -> listener.onEvent(event) }
    }

    /**
     * Subscribe a new listener to this event.
     */
    fun addListener(listener: EventListener<EventType>) {
        listeners.add(listener)
    }

    /**
     * Removes an existing listener from this event hook.
     */
    fun removeListener(listener: EventListener<EventType>) {
        listeners.remove(listener)
    }

    /**
     * Removes all listeners registered to this event hook.
     */
    fun removeAllListeners() {
        listeners.clear()
    }
}
