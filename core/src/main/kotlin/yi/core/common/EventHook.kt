package yi.core.common

/**
 * The super class for providing model events to outgoing sources.
 * Each [EventHook] provides a single type of event source to subscribe to.
 */
abstract class EventHook<EventType> {

    private var listeners: HashSet<(event: EventType) -> Unit> = HashSet()

    /**
     * Sends an event message to all existing listeners.
     */
    internal fun fireEvent(event: EventType) {
        listeners.forEach { observer -> observer.invoke(event) }
    }

    /**
     * Subscribe a new listener to this event.
     */
    fun addListener(listener: (event: EventType) -> Unit) {
        listeners.add(listener)
    }

    /**
     * Removes an existing listener from this event hook.
     */
    fun removeListener(listener: (event: EventType) -> Unit) {
        listeners.remove(listener)
    }
}
