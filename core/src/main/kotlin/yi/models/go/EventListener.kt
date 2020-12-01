package yi.models.go

@FunctionalInterface
interface EventListener<EventType> {

    /**
     * Invoked when an event of this type is triggered.
     */
    fun onEvent(event: EventType)

}