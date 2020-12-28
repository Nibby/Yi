package yi.component.shared;

/**
 * Listens to value changes from a {@link DoubleProperty}.
 */
@FunctionalInterface
public interface DoublePropertyListener {

    /**
     * Informs the listener that the {@link DoubleProperty} value has changed.
     *
     * @param newValue New property value.
     */
    void onValueChange(double newValue);

}
