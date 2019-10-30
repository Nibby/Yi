package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.BoardInputHintType;
import codes.nibby.yi.board.BoardMetrics;
import codes.nibby.yi.editor.EditorToolType;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
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
public class GameBoardToolBar extends ToolBar {

    public static final int BUTTON_SIZE = BoardMetrics.RESERVED_TOOLBAR_SIZE - 15;

    private GameEditorWindow editor;

    private Label moveLabel;
    private ToggleGroup annotationGroup;
    private ToggleButton buttonPlay;
    private ToggleButton buttonTriangle;
    private ToggleButton buttonCircle;
    private ToggleButton buttonSquare;
    private ToggleButton buttonCross;
    private ToggleButton buttonNumber;
    private ToggleButton buttonLetter;
    private ToggleButton buttonMarkBlack;
    private ToggleButton buttonMarkWhite;
    private ToggleButton buttonCoordinates;
    private Button buttonShowTray;

    public GameBoardToolBar(GameEditorWindow editor) {
        this.editor = editor;
        annotationGroup = new ToggleGroup();

        Label lbTools = new Label("Tools: ");
        lbTools.getStyleClass().add("label");
        getItems().add(lbTools);

        addToolButton(buttonPlay = new ToggleButton(), "play", "Place stones", true);
        buttonPlay.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.PLAY_MOVE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.DYNAMIC);
        });

        Label lbSep1 = new Label("-");
        lbSep1.getStyleClass().add("label");
        getItems().add(lbSep1);

        addToolButton(buttonMarkBlack = new ToggleButton(), "circle-thick", "Add black stones", true);
        buttonMarkBlack.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.ADD_HELPER_BLACK);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.STONE_BLACK);
        });

        addToolButton(buttonMarkWhite = new ToggleButton(), "circle-thick-fill", "Add white stones", true);
        buttonMarkWhite.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.ADD_HELPER_WHITE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.STONE_WHITE);
        });

        Label lbSep2 = new Label("-");
        lbSep2.getStyleClass().add("label");
        getItems().add(lbSep2);

        addToolButton(buttonTriangle = new ToggleButton(), "triangle", "Triangle marker", true);
        buttonTriangle.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_TRIANGLE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_TRIANGLE);
        });

        addToolButton(buttonSquare = new ToggleButton(), "square", "Square marker", true);
        buttonSquare.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_SQUARE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_SQUARE);
        });

        addToolButton(buttonCircle = new ToggleButton(), "circle", "Circle marker", true);
        buttonCircle.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_CIRCLE);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_CIRCLE);
        });

        addToolButton(buttonCross = new ToggleButton(), "cross", "Cross marker", true);
        buttonCross.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_CROSS);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_CROSS);
        });

        addToolButton(buttonNumber = new ToggleButton("1"), "", "Number marker", true);
        buttonNumber.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_LABEL_NUMBER);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_LABEL);
        });

        addToolButton(buttonLetter = new ToggleButton("A"), "", "Letter marker", true);
        buttonLetter.selectedProperty().addListener(e -> {
            editor.getController().setToolType(EditorToolType.MARKUP_LABEL_LETTER);
            if (editor.getGameBoard() != null)
                editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_LABEL);
        });

        HBox divider = new HBox();
        HBox.setHgrow(divider, Priority.ALWAYS);
        getItems().add(divider);

        addToolButton(buttonCoordinates = new ToggleButton(), "grid", "Show grid labels", false);
        buttonCoordinates.selectedProperty().addListener(e -> {
//            editor.getGobanEditor().setDrawLabels(buttonShowLabel.isSelected());
        });

        // TODO somehow verify the intial stat

        buttonShowTray = new Button("", null);
        buttonShowTray.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        ImageView buttonShowTrayIcon = new ImageView(new Image(GameBoardToolBar.class.getResourceAsStream("/icons/arrow_down_invert.png")));
        buttonShowTrayIcon.setFitWidth(BUTTON_SIZE);
        buttonShowTrayIcon.setFitHeight(BUTTON_SIZE);
        buttonShowTray.setGraphic(buttonShowTrayIcon);
        buttonShowTray.setOnAction(evt -> {
            boolean newState = !editor.getLayout().isShowingRightSidebar();
            editor.getLayout().setShowRightSidebar(newState);
            String iconPath;
            if (newState == true)
                iconPath = "/icons/arrow_down_invert.png";
            else
                iconPath = "/icons/arrow_right_invert.png";
            ImageView icon = new ImageView(new Image(GameBoardToolBar.class.getResourceAsStream(iconPath)));
            icon.setFitWidth(BUTTON_SIZE);
            icon.setFitHeight(BUTTON_SIZE);
            buttonShowTray.setGraphic(icon);
        });
        getItems().add(buttonShowTray);

        annotationGroup.selectToggle(buttonPlay);

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
}
