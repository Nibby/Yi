package yi.component;

import javafx.scene.Node;

/**
 * A user interface component in the Yi module.
 */
public interface YiComponent {

    /**
     * @return The Fx component represented by this implementation.
     */
    default Node getComponent() {
        if (this instanceof Node) {
            return (Node) this;
        } else {
            throw new UnsupportedOperationException(this.getClass() + " is not a subclass of Node");
        }
    }

}
