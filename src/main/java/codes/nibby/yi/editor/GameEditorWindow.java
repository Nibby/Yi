package codes.nibby.yi.editor;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.config.Config;
import codes.nibby.yi.config.UiStylesheets;
import codes.nibby.yi.editor.component.GameCommentViewer;
import codes.nibby.yi.editor.component.GameEditorToolBar;
import codes.nibby.yi.editor.component.GameTreeViewer;
import codes.nibby.yi.editor.perspective.Perspective;
import codes.nibby.yi.editor.perspective.PerspectiveLayout;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.rules.GameRules;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The main editor window.
 * Mainly deals with UI layout within the editor.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameEditorWindow extends Stage {

    private static final int START_WIDTH = 600;
    private static final int START_HEIGHT = 600;

    private Game game;
    private GameBoard gameBoard;
    private EditorBoardController controller;
    private GameTreeViewer gameTreeViewer;
    private GameCommentViewer gameCommentViewer;
    private GameEditorToolBar toolBar;

    private PerspectiveLayout layout;
    private Scene scene;

    public GameEditorWindow() {
        controller = new EditorBoardController();
        initializeComponents();
        initializeGame();
        initializeScene();

        UiStylesheets.applyTo(scene);
    }

    /*
        Instantiates all the required objects for the window.
        The components are not added to the scene, that is
        handled by the perspective manager.
     */
    private void initializeComponents() {
        toolBar = new GameEditorToolBar(this);
        gameTreeViewer = new GameTreeViewer(this);
        gameCommentViewer = new GameCommentViewer(this);
    }

    private void initializeGame() {
        game = new Game(GameRules.CHINESE, 19, 19);
        gameBoard  = new GameBoard(game, controller);
    }

    private void initializeScene() {
        setTitle(Yi.TITLE);

        layout = PerspectiveLayout.generate(this);
        Pane root = layout.getContentPane();
        scene = new Scene(root, START_WIDTH, START_HEIGHT);
        setScene(scene);
    }

    public Perspective getPerspective() {
        return Config.getEditorPerspective();
    }

    public void setPerspective(Perspective perspective) {
        Perspective oldPerspective = Config.getEditorPerspective();
        if (oldPerspective.equals(perspective))
            return;

        Config.setEditorPerspective(perspective);
        layout = PerspectiveLayout.generate(this);
        Pane root = layout.getContentPane();
        scene.setRoot(root);
    }

    public Game getGame() {
        return game;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public GameTreeViewer getGameTreeViewer() {
        return gameTreeViewer;
    }

    public GameCommentViewer getGameCommentViewer() {
        return gameCommentViewer;
    }

    public GameEditorToolBar getToolBar() {
        return toolBar;
    }

    public EditorBoardController getController() {
        return controller;
    }
}
