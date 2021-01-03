package yi.component.commentviewer;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.StandardGameRules;

@ExtendWith(ApplicationExtension.class)
public class GameCommentViewerUITest {

    private GameModel gameModel;
    private GameCommentViewer commentViewer;

    @Start
    public void start(Stage stage) {
        gameModel = new GameModel(3, 3, StandardGameRules.CHINESE);
        commentViewer = new GameCommentViewer();
        commentViewer.setGameModel(gameModel);

        var container = new BorderPane();
        container.setCenter(commentViewer.getComponent());
        var scene = new Scene(container, 400, 300);
        stage.setScene(scene);
        stage.show();
        stage.requestFocus();
    }

    @AfterEach
    public void dispose() {
        gameModel.dispose();
        commentViewer = null;
    }

    @Test
    public void testEnteringText_CommentAreaIsEditable_SavesCommentToNode(FxRobot robot) throws InterruptedException {
        final KeyCode[] keys = new KeyCode[] {
                KeyCode.S, KeyCode.O, KeyCode.M, KeyCode.E,
                KeyCode.SPACE, KeyCode.T, KeyCode.E, KeyCode.X, KeyCode.T
        };

        commentViewer.setEditable(true);
        GameNode currentNode = gameModel.getCurrentNode();
        Assertions.assertTrue(currentNode.getComments().isEmpty());

        robot.clickOn(commentViewer.getComponent(), MouseButton.PRIMARY);
        Thread.sleep(100);
        robot.type(keys);
        Thread.sleep(100);

        Assertions.assertEquals("some text", currentNode.getComments());
    }

    @Test
    public void testSetText_CommentAreaNotEditable_DoesNotSaveCommentToNode() {
        final String commentThatShouldNotBeSaved = "Some comment that should not be saved";

        commentViewer.setEditable(false);
        GameNode currentNode = gameModel.getCurrentNode();
        Assertions.assertTrue(currentNode.getComments().isEmpty());

        commentViewer.setText(commentThatShouldNotBeSaved);

        Assertions.assertTrue(currentNode.getComments().isEmpty());
    }
}
