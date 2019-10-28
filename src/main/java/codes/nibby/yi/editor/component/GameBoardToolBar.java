package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.BoardInputHintType;
import codes.nibby.yi.board.BoardMetrics;
import codes.nibby.yi.editor.EditorToolType;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * A HUD toolbar above the game board, offers input controls.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameBoardToolBar extends ToolBar implements GameListener {

    public static final int BUTTON_SIZE = BoardMetrics.RESERVED_TOOLBAR_SIZE - 15;

    private GameEditorWindow editor;

    private Label moveLabel;
    private ToggleGroup annotationGroup;
    private ToggleButton btnPlay;
    private ToggleButton btnTriangle;
    private ToggleButton btnCircle;
    private ToggleButton btnSquare;
    private ToggleButton btnCross;
    private ToggleButton btnNumber;
    private ToggleButton btnLetter;
    private ToggleButton btnMarkBlack;
    private ToggleButton btnMarkWhite;
    private ToggleButton btnShowLabel;

    public GameBoardToolBar(GameEditorWindow editor) {
        this.editor = editor;
        this.editor.getGame().addGameListener(this);
        annotationGroup = new ToggleGroup();

        Label lbTools = new Label("Tools: ");
        lbTools.getStyleClass().add("label");
        getItems().add(lbTools);

        addToolButton(btnPlay = new ToggleButton(), "play", "Place stones", true);
        btnPlay.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.PLAY_MOVE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.DYNAMIC);
        });

        Label lbSep1 = new Label("-");
        lbSep1.getStyleClass().add("label");
        getItems().add(lbSep1);

        addToolButton(btnMarkBlack = new ToggleButton(), "circle-thick", "Add black stones", true);
        btnMarkBlack.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.ADD_HELPER_BLACK);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.STONE_BLACK);
        });

        addToolButton(btnMarkWhite = new ToggleButton(), "circle-thick-fill", "Add white stones", true);
        btnMarkWhite.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.ADD_HELPER_WHITE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.STONE_WHITE);
        });

        Label lbSep2 = new Label("-");
        lbSep2.getStyleClass().add("label");
        getItems().add(lbSep2);

        addToolButton(btnTriangle = new ToggleButton(), "triangle", "Triangle marker", true);
        btnTriangle.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_TRIANGLE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_TRIANGLE);
        });

        addToolButton(btnSquare = new ToggleButton(), "square", "Square marker", true);
        btnSquare.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_SQUARE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_SQUARE);
        });

        addToolButton(btnCircle = new ToggleButton(), "circle", "Circle marker", true);
        btnCircle.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_CIRCLE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_CIRCLE);
        });

        addToolButton(btnCross = new ToggleButton(), "cross", "Cross marker", true);
        btnCross.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_CROSS);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_CROSS);
        });

        addToolButton(btnNumber = new ToggleButton("1"), "", "Number marker", true);
        btnNumber.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_LABEL_NUMBER);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_LABEL);
        });

        addToolButton(btnLetter = new ToggleButton("A"), "", "Letter marker", true);
        btnLetter.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_LABEL_LETTER);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_LABEL);
        });

        HBox divider = new HBox();
        HBox.setHgrow(divider, Priority.ALWAYS);
        getItems().add(divider);

        addToolButton(btnShowLabel = new ToggleButton(), "grid", "Show grid labels", false);
        btnShowLabel.selectedProperty().addListener(e -> {
//            editor.getGobanEditor().setDrawLabels(btnShowLabel.isSelected());
        });
        moveLabel = new Label(" Move: 0");
        getItems().add(moveLabel);

        annotationGroup.selectToggle(btnPlay);

        getStyleClass().add("editor_board_tb");
        setMinHeight(BoardMetrics.RESERVED_TOOLBAR_SIZE);
        setMaxHeight(BoardMetrics.RESERVED_TOOLBAR_SIZE);
        setPrefHeight(BoardMetrics.RESERVED_TOOLBAR_SIZE);
        setOrientation(Orientation.HORIZONTAL);
    }

    private void addToolButton(ToggleButton b, String icon, String tooltip, boolean group) {
        b.applyCss();
        Tooltip tt = new Tooltip(tooltip);
        tt.setShowDelay(Duration.millis(500));
        tt.setHideDelay(Duration.millis(500));
        tt.setShowDuration(Duration.seconds(2));
        b.setTooltip(tt);
        b.setPrefWidth(BUTTON_SIZE);
        b.setPrefHeight(b.getPrefWidth());
        b.getStyleClass().add("editor_board_tb_toggle");

        if (!icon.isEmpty()) {
            InputStream inputStream = GameBoardToolBar.class.getResourceAsStream("/icons/" + icon + "_invert.png");
            ImageView icn = new ImageView(new Image(inputStream));
            icn.setFitWidth(b.getPrefWidth() - 10);
            icn.setFitHeight(b.getPrefHeight() - 10);
            b.setGraphic(icn);
        }

        if (group) {
            b.selectedProperty().addListener(e -> {
                boolean selected = b.isSelected();
                if (!selected) {
                    boolean hasSelection = false;
                    for (Toggle toggle : annotationGroup.getToggles()) {
                        if (toggle.isSelected()) {
                            hasSelection = true;
                            break;
                        }
                    }

                    if (!hasSelection) {
                        b.setSelected(true);
                    }
                }
            });
            annotationGroup.getToggles().add(b);
        }

        b.selectedProperty().addListener(e -> {
            boolean selected = b.isSelected();
            if (!icon.isEmpty()) {
                InputStream inputStream = GameBoardToolBar.class
                        .getResourceAsStream("/icons/" + icon + ((selected) ? "" : "_invert") + ".png");
                ImageView iconImage = new ImageView(new Image(inputStream));
                iconImage.setFitHeight(b.getPrefHeight() - 10);
                iconImage.setFitWidth(b.getPrefWidth() - 10);

                b.setGraphic(iconImage);
            }
        });
        getItems().add(b);
    }

    @Override
    public void gameInitialized(Game game) {
        moveLabel.setText(" Move: " + game.getCurrentNode().getMoveNumber());
    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {
        moveLabel.setText(" Move: " + currentMove.getMoveNumber());
    }

    @Override
    public void gameModified(Game game) {
    }
}
