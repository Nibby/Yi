package yi.component;

@FunctionalInterface
public interface ValueListener<T> {
    void valueChanged(T newValue);
}
