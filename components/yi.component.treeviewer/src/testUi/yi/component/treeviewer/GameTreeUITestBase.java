package yi.component.treeviewer;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public abstract class GameTreeUITestBase {

    protected GameTreeViewer treeViewer;
    protected Stage stage;

    @Start
    public void startFx(Stage stage) {
        var container = new BorderPane();
        treeViewer = new GameTreeViewer();
        container.setCenter(treeViewer.getComponent());
        var scene = new Scene(container, 300, 600);
        stage.setScene(scene);
        stage.show();

        this.stage = stage;
    }

    @AfterEach
    public void dispose() {
        System.gc();
    }

}
