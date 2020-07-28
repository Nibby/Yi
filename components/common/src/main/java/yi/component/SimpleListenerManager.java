package yi.component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SimpleListenerManager<ValueType> {

    private final Set<Consumer<ValueType>> listeners = new HashSet<>();

    public void addListener(Consumer<ValueType> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<ValueType> listener) {
        listeners.remove(listener);
    }

    public void fireValueChangeEvent(ValueType newValue) {
        listeners.forEach(listener -> listener.accept(newValue));
    }

    public void removeAllListeners() {
        listeners.clear();
    }
}
