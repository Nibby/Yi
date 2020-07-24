package yi.core.common

abstract class EventHook<EventType> {

    private var observers: HashSet<(event: EventType) -> Unit> = HashSet()

    internal fun fireEvent(event: EventType) {
        observers.forEach { observer -> observer.invoke(event) }
    }

    fun addObserver(observer: (event: EventType) -> Unit) {
        observers.add(observer)
    }

    fun removeObserver(observer: (event: EventType) -> Unit) {
        observers.remove(observer)
    }
}
