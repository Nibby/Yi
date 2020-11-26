package yi.editor.utilities;

@FunctionalInterface
public interface ValueListener<T> {
    void valueChanged(T newValue);
}
