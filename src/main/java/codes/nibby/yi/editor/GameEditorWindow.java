package codes.nibby.yi.editor;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.board.GameBoardController;
import codes.nibby.yi.common.LazyObject;
import codes.nibby.yi.config.Config;
import codes.nibby.yi.config.UiStylesheets;
import codes.nibby.yi.editor.component.GameEditActionsToolbar;
import codes.nibby.yi.editor.component.GameEditorMenuBar;
import codes.nibby.yi.editor.component.GameTreePane;
import codes.nibby.yi.editor.component.MoveCommentPane;
import codes.nibby.yi.editor.layout.AbstractLayout;
import codes.nibby.yi.editor.layout.LayoutType;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.rules.GameRules;
import codes.nibby.yi.io.GameFileParser;
import codes.nibby.yi.io.GameParseException;
import codes.nibby.yi.io.SgfFile;
import codes.nibby.yi.io.UnsupportedFileTypeException;
import codes.nibby.yi.utility.AlertUtility;
import codes.nibby.yi.utility.UiUtility;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The main editor window.
 * Mainly deals with UI layout within the editor.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameEditorWindow extends Stage {

    private Game game;
    private DefaultEditorController controller;

    private LazyObject<GameBoard> gameBoard;
    private LazyObject<GameTreePane> gameTreePane;
    private LazyObject<MoveCommentPane> moveCommentPane;
    private LazyObject<GameEditorMenuBar> toolBar;
    private LazyObject<GameEditActionsToolbar> editActionsToolbar;

    private AbstractLayout layout;
    private Scene scene;
    private ResourceBundle locale;

    private Path lastSavePath;

    public GameEditorWindow() {
        locale = Config.getLanguage().getResourceBundle("GameEditorWindow");
        game = new Game(GameRules.CHINESE, 19, 19);
        controller = new DefaultEditorController();

        editActionsToolbar = new LazyObject<>(() -> new GameEditActionsToolbar(this));
        gameBoard = new LazyObject<>(() -> new GameBoard(game, controller));
        toolBar = new LazyObject<>(() -> new GameEditorMenuBar(this));
        gameTreePane = new LazyObject<>(() -> new GameTreePane(this));
        moveCommentPane = new LazyObject<>(() -> new MoveCommentPane(this));

        game.initialize();

        // Layout
        layout = AbstractLayout.generate(this);
        Pane root = layout.getContentPane();
        scene = new Scene(root, 620, 640);
        scene.setOnDragDone(this::handleDragAndDrop);
        setScene(scene);
        setMinWidth(620);
        setMinHeight(640);
        setTitle(game.getMetadata().gameName + " - " + Yi.TITLE);
        UiStylesheets.applyTo(scene);
    }

    private void handleDragAndDrop(DragEvent e) {
        boolean success = false;
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.ANY);
            success = true;
            // TODO ask the user what to do on multiple file drag?
            List<File> files = e.getDragboard().getFiles();
            // TODO temporary: default to opening one file
            if (files.get(0).isFile()) {
                try {
                    File file = files.get(0);
                    Game game = GameFileParser.parse(file);
                    if (game != null) {
                        setLastSavePath(file.toPath());
                        setGame(game);
                    }
                } catch (IOException | GameParseException | UnsupportedFileTypeException ex) {
                    // TODO: Better exception handling
                    ex.printStackTrace();
                }
            }
        }
        e.setDropCompleted(success);
        e.consume();
    }

    public LayoutType getPerspective() {
        return Config.getEditorLayout();
    }

    public void setPerspective(LayoutType perspective) {
        LayoutType oldPerspective = Config.getEditorLayout();
        if (oldPerspective.equals(perspective))
            return;

        Config.setEditorLayout(perspective);
        layout = AbstractLayout.generate(this);
        Pane root = layout.getContentPane();
        scene.setRoot(root);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        for (GameListener listener : this.game.getActiveGameListeners()) {
            game.addGameListener(listener);
        }
        this.game.removeListeners();

        this.game = game;
        game.initialize();

        if (gameBoard.isInitialized()) {
            this.gameBoard.getOrInitialize().updateBoardObjects(game.getCurrentNode(), true, true);
        }
    }

    public void createDocument() {
        if (game.isModified()) {
            ButtonType responseType =
                    AlertUtility.showAlert(locale.getString("alert.confirm_save.content"),
                            locale.getString("alert.confirm_save.title"),
                            Alert.AlertType.CONFIRMATION, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            if (responseType == null)
                return;
            else if (responseType == ButtonType.CANCEL)
                return;
            else if (responseType.equals(ButtonType.YES))
                saveDocument(false);
        }

        Game game = new Game(GameRules.CHINESE, 19, 19);
        setGame(game);
    }

    public void openDocument() {
        Path defaultPath = getLastSavePath() == null ? Paths.get(System.getProperty("user.dir")) : getLastSavePath();
        FileChooser fc = UiUtility.createGameRecordOpenFileChooser(locale.getString("dialog.open_file.title"),
                defaultPath);
        File file = fc.showOpenDialog(this);
        try {
            Game game = GameFileParser.parse(file);
            if (game != null) {
                setLastSavePath(file.toPath());
                setGame(game);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (GameParseException ex) {
            ex.printStackTrace();
            AlertUtility.showAlert(locale.getString("alert.exception.gameparse.content")
                            + "\n\n" + ex.getMessage(), locale.getString("alert.exception.gameparse.title"),
                    Alert.AlertType.ERROR, ButtonType.OK);
        } catch (UnsupportedFileTypeException ex) {
            ex.printStackTrace();
            AlertUtility.showAlert(locale.getString("alert.exception.unsupported_filetype.content"),
                    locale.getString("alert.exception.unsupported_filetype.title"),
                    Alert.AlertType.ERROR, ButtonType.OK);
        }
    }

    public void saveDocument(boolean forceChoosePath) {
        Path savePath = getLastSavePath();
        if (savePath == null || forceChoosePath) {
            Path defaultPath = savePath == null ? Paths.get(System.getProperty("user.dir")) : savePath;
            FileChooser fc = UiUtility.createGameRecordOpenFileChooser(locale.getString("dialog.save_file.title"), defaultPath);
            File selectedFile = fc.showSaveDialog(this);

            // User cancelled save operation
            if (selectedFile == null)
                return;
            savePath = selectedFile.toPath();
        }

        try {
            SgfFile.write(savePath, game);
            setLastSavePath(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameBoard getGameBoard() {
        return gameBoard.getOrInitialize();
    }

    public GameTreePane getGameTreePane() {
        return gameTreePane.getOrInitialize();
    }

    public MoveCommentPane getMoveCommentPane() {
        return moveCommentPane.getOrInitialize();
    }

    public GameEditActionsToolbar getEditActionsToolbar() {
        return editActionsToolbar.getOrInitialize();
    }

    public DefaultEditorController getController() {
        return controller;
    }

    public AbstractLayout getLayout() {
        return layout;
    }

    public Path getLastSavePath() {
        return lastSavePath;
    }

    public void setLastSavePath(Path lastSavePath) {
        this.lastSavePath = lastSavePath;
    }

    public ResourceBundle getLocaleResourceBundle() {
        return locale;
    }
}
