package yi.core.common

@FunctionalInterface
interface EventListener<EventType> {

    /**
     * Invoked when an event of this type is triggered.
     */
    fun onEvent(event: EventType)

}