package codes.nibby.yi.editor;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.config.Config;
import codes.nibby.yi.config.UiStylesheets;
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
    private GameBoard gameBoard;
    private EditorBoardController controller;
    private GameTreePane gameTreePane;
    private MoveCommentPane moveCommentPane;
    private GameEditorMenuBar toolBar;
    private GameBoard.GameBoardToolBar boardToolBar;

    private AbstractLayout layout;
    private Scene scene;
    private ResourceBundle locale;

    private Path lastSavePath;

    public GameEditorWindow() {
        locale = Config.getLanguage().getResourceBundle("GameEditorWindow");
        controller = new EditorBoardController();
        initializeComponents();
        initializeScene();

        UiStylesheets.applyTo(scene);
    }

    /*
        Instantiates all the required objects for the window.
        The components are not added to the scene, that is
        handled by the perspective manager.
     */
    private void initializeComponents() {
        game = new Game(GameRules.CHINESE, 19, 19);
        boardToolBar = new GameBoard.GameBoardToolBar(this);
        gameBoard = new GameBoard(game, controller, boardToolBar);
        toolBar = new GameEditorMenuBar(this);
        gameTreePane = new GameTreePane(this);
        moveCommentPane = new MoveCommentPane(this);

        game.addGameListener(gameBoard, gameTreePane, moveCommentPane);
        game.initialize();
    }

    private void initializeScene() {
        setTitle(game.getMetadata().gameName + " - " + Yi.TITLE);

        layout = AbstractLayout.generate(this);
        Pane root = layout.getContentPane();
        scene = new Scene(root, 620, 640);
        setScene(scene);
        setMinWidth(620);
        setMinHeight(640);

        scene.setOnDragDone(e -> {
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
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (GameParseException ex) {
                        ex.printStackTrace();
                    } catch (UnsupportedFileTypeException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
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
        for (GameListener listener : this.game.getGameListeners()) {
            game.addGameListener(listener);
        }
        this.game.removeListeners();

        this.game = game;
        this.gameBoard.setGame(game);
        game.initialize();
        this.controller.initialize(game, this.gameBoard);
        gameBoard.updateBoardObjects(game.getCurrentNode(), true, true);
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
        return gameBoard;
    }

    public GameTreePane getGameTreePane() {
        return gameTreePane;
    }

    public MoveCommentPane getMoveCommentPane() {
        return moveCommentPane;
    }

    public GameEditorMenuBar getToolBar() {
        return toolBar;
    }

    public EditorBoardController getController() {
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
