package codes.nibby.yi.models

@FunctionalInterface
interface EventListener<EventType> {

    /**
     * Invoked when an event of this type is triggered.
     */
    fun onEvent(event: EventType)

}