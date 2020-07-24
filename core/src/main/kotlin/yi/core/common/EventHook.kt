package yi.core.common

abstract class EventHook<EventType> {

    private var observers: HashSet<(event: EventType) -> Unit> = HashSet()

    internal fun fireEvent(event: EventType) {
        observers.forEach { observer -> observer.invoke(event) }
    }

    fun execute(observer: (event: EventType) -> Unit) {
        observers.add(observer)
    }

}
