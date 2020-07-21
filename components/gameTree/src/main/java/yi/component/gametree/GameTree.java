package yi.component.gametree;

import javafx.scene.Parent;
import yi.component.YiComponent;

public final class GameTree implements YiComponent {

    private Parent component;

    public GameTree() {
        
    }

    @Override
    public Parent getComponent() {
        return component;
    }
}
