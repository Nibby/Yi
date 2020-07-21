package yi.component;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * A user interface component in the Yi module.
 */
public interface Component {

    /**
     *
     * @return The top-level container for this component.
     */
    Parent getComponent();

}
