package yi.core.common

@FunctionalInterface
interface EventObserver<EventType> {

    fun onEvent(event: EventType)

}