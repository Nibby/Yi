package yi.editor;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import yi.component.board.GameBoardViewer;
import yi.component.gametree.GameTreeViewer;
import yi.core.go.GameModel;
import yi.editor.components.ComponentLayout;
import yi.editor.components.ControlToolBar;
import yi.editor.components.EditorMenuBar;
import yi.editor.components.EditorToolBar;
import yi.editor.settings.Settings;

/**
 * The main frame for an editor session.
 */
public class EditorFrame extends Stage {

    private final EditorMenuBar menuBar;
    private final EditorToolBar editorToolBar;
    private final ControlToolBar controlToolBar;

    private final GameBoardViewer boardViewer;
    private final GameTreeViewer treeViewer;
    private GameModel gameModel;

    public EditorFrame(GameModel gameModel) {
        this(gameModel, ComponentLayout.COMPACT);
    }

    public EditorFrame(GameModel gameModel, ComponentLayout layout) {
        this.gameModel = gameModel;

        boardViewer = new GameBoardViewer(Settings.getBoardSettings());
        boardViewer.setGameModel(gameModel);

        treeViewer = new GameTreeViewer(gameModel);

        menuBar = new EditorMenuBar();

        editorToolBar = new EditorToolBar();
        controlToolBar = new ControlToolBar();

        setLayout(layout);

        setMinWidth(640);
        setMinHeight(735);
    }

    private void setLayout(ComponentLayout layout) {
        var content = layout.layoutContent(this);

        var controlPane = new BorderPane();
        controlPane.setTop(menuBar);

        var toolBarPane = new BorderPane();
        {
            toolBarPane.setCenter(editorToolBar);
            toolBarPane.setRight(controlToolBar);

            toolBarPane.heightProperty().addListener(newHeight -> {
                editorToolBar.setPrefHeight(toolBarPane.getHeight());
                controlToolBar.setMinHeight(toolBarPane.getHeight());
            });
        }
        controlPane.setCenter(toolBarPane);

        var container = new BorderPane();
        container.setTop(controlPane);
        container.setCenter(content);

        setScene(new Scene(container));
    }

    public EditorMenuBar getMenuBar() {
        return menuBar;
    }

    public EditorToolBar getEditorToolBar() {
        return editorToolBar;
    }

    public GameBoardViewer getBoardViewer() {
        return boardViewer;
    }

    public GameTreeViewer getTreeViewer() {
        return treeViewer;
    }

    public GameModel getGameModel() {
        return gameModel;
    }
}
