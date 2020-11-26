package yi.editor.utilities;

import java.util.ArrayList;
import java.util.List;

public final class ValueListenerManager<T> {

    private final List<ValueListener<T>> listeners = new ArrayList<>(2);

    public void addListener(ValueListener<T> l) {
        listeners.add(l);
    }

    public void removeListener(ValueListener<T> l) {
        listeners.remove(l);
    }

    public void fireValueChanged(T newValue) {
        listeners.forEach(l -> l.valueChanged(newValue));
    }

}
