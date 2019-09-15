package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.Stone;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.utility.CanvasContainer;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * A component for displaying the game tree structure visually.
 * Allows user input to navigate.
 *
 * TODO: Implement this later.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameTreePane extends BorderPane implements GameListener {

    private GameEditorWindow editor;

    public GameTreePane(GameEditorWindow editor) {
        this.editor = editor;
    }

    @Override
    public void gameInitialized(Game game) {

    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {

    }
}
