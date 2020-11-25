package yi.editor.components;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import yi.component.SimpleListenerManager;
import yi.editor.EditorTool;
import yi.editor.utilities.IconUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Primary toolbar for {@link yi.editor.EditorFrame} that displays a set of supported editing tools
 * and other options.
 */
public class EditorToolBar extends ToolBar {

    private final SimpleListenerManager<EditorTool> toolSelectionListeners = new SimpleListenerManager<>();

    private final ToggleGroup toolButtonGroup;

    private final ToggleButton toolPlayMove;
    private final ToggleButton toolAddBlackStone;
    private final ToggleButton toolAddWhiteStone;
    private final ToggleButton toolAnnotateTriangle;
    private final ToggleButton toolAnnotateCircle;
    private final ToggleButton toolAnnotateSquare;
    private final ToggleButton toolAnnotateCross;
    private final ToggleButton toolAnnotateText;
    private final ToggleButton toolAnnotateNumber;
    private final ToggleButton toolAnnotateLine;
    private final ToggleButton toolAnnotateArrow;
    private final ToggleButton toolAnnotateDim;

    private final List<ToggleButton> editToolToggleButtons = new ArrayList<>();

    public EditorToolBar() {

        toolButtonGroup = new ToggleGroup();

        toolPlayMove = addEditToolButton(EditorTool.PLAY_MOVE, "/icons/playStone32_white.png", TOOLBAR_TOOL_PLAY_MOVE);
        toolAddBlackStone = addEditToolButton(EditorTool.ADD_BLACK_STONE, "/icons/addBlackStone32_white.png", TOOLBAR_TOOL_ADD_BLACK);
        toolAddWhiteStone = addEditToolButton(EditorTool.ADD_WHITE_STONE, "/icons/addWhiteStone32_white.png", TOOLBAR_TOOL_ADD_WHITE);

        toolAnnotateTriangle = addEditToolButton(EditorTool.ANNOTATE_TRIANGLE, "/icons/annoTriangle32_white.png", TOOLBAR_TOOL_TRIANGLE);
        toolAnnotateCircle = addEditToolButton(EditorTool.ANNOTATE_CIRCLE, "/icons/annoCircle32_white.png", TOOLBAR_TOOL_CIRCLE);
        toolAnnotateSquare = addEditToolButton(EditorTool.ANNOTATE_SQUARE, "/icons/annoSquare32_white.png", TOOLBAR_TOOL_SQUARE);
        toolAnnotateCross = addEditToolButton(EditorTool.ANNOTATE_CROSS, "/icons/annoCross32_white.png", TOOLBAR_TOOL_CROSS);
        toolAnnotateText = addEditToolButton(EditorTool.ANNOTATE_LETTER, "/icons/annoLetter32_white.png", TOOLBAR_TOOL_LABEL_LETTER);
        toolAnnotateNumber = addEditToolButton(EditorTool.ANNOTATE_NUMBER, "/icons/annoNumber32_white.png", TOOLBAR_TOOL_LABEL_NUMBER);
        toolAnnotateLine = addEditToolButton(EditorTool.ANNOTATE_LINE, "/icons/annoLine32_white.png", TOOLBAR_TOOL_LINE);
        toolAnnotateArrow = addEditToolButton(EditorTool.ANNOTATE_ARROW, "/icons/annoArrow32_white.png", TOOLBAR_TOOL_ARROW);
        toolAnnotateDim = addEditToolButton(EditorTool.ANNOTATE_DIM, "/icons/annoDim32_white.png", TOOLBAR_TOOL_DIM);

        toolPlayMove.setSelected(true);

        getStyleClass().add("bg-black-60");
    }

    public void setButtonsForContentLayout(ContentLayout layout) {
        getItems().clear();

        // TODO: This is definitely not a good design. Rethink UI.
        final int gap = 6;

        getItems().add(dynamicSpacer());
        getItems().add(toolPlayMove);
        getItems().add(staticSpacer(gap));
        getItems().add(toolAddBlackStone);
        getItems().add(toolAddWhiteStone);
        getItems().add(staticSpacer(gap));
        getItems().add(toolAnnotateTriangle);
        getItems().add(toolAnnotateCircle);
        getItems().add(toolAnnotateSquare);
        getItems().add(toolAnnotateCross);
        getItems().add(toolAnnotateText);
        getItems().add(toolAnnotateNumber);
        getItems().add(toolAnnotateDim);
        getItems().add(staticSpacer(gap));
        getItems().add(toolAnnotateLine);
        getItems().add(toolAnnotateArrow);
        getItems().add(dynamicSpacer());
    }

    private Pane dynamicSpacer() {
        var dynamicSpacer = new Pane();
        HBox.setHgrow(dynamicSpacer, Priority.SOMETIMES);

        return dynamicSpacer;
    }

    private Pane staticSpacer(int width) {
        var spacer = new Pane();
        spacer.setPrefWidth(width);

        return spacer;
    }

    public void addToolSelectionListener(Consumer<EditorTool> listener) {
        toolSelectionListeners.addListener(listener);
    }

    public void removeToolSelectionListener(Consumer<EditorTool> listener) {
        toolSelectionListeners.removeListener(listener);
    }

    private ToggleButton addEditToolButton(EditorTool editorTool, String iconResource, String tooltip) {
        var toggle = new ToggleButton();
        toggle.setFocusTraversable(false);
        toggle.getStyleClass().add("button-style2");
        setUp(toggle, iconResource, tooltip);

        // TODO: I am way too tired to work out why clicking on the already-selected
        //       toggle button causes the icon to go back to the unselected version,
        //       so I put this flag here to ignore every second update to the icon.
        //       I suspect toggle.setSelected(true) has something to do with it...
        //
        //       Nonetheless right now the code around this area is a very dirty trick.
        AtomicBoolean ignoreIconUpdate = new AtomicBoolean(false);
        toggle.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            if (isSelected) {
                toolSelectionListeners.fireValueChangeEvent(editorTool);
            } else {
                if (toolButtonGroup.getSelectedToggle() == null && wasSelected) {
                    toggle.setSelected(true);
                    ignoreIconUpdate.set(true);
                }
            }

            if (!ignoreIconUpdate.get()) {
                if (isSelected) {
                    setIcon(iconResource.replace("_white", ""), toggle);
                } else {
                    setIcon(iconResource, toggle);
                }
            } else {
                ignoreIconUpdate.set(false);
            }
        });

        toolButtonGroup.getToggles().add(toggle);
        editToolToggleButtons.add(toggle);

        return toggle;
    }

    private Button createButton(String iconResource, String toolTip) {
        var button = new Button();
        setUp(button, iconResource, toolTip);

        return button;
    }

    private void setUp(ButtonBase buttonBase, String iconResource, String tooltip) {
        buttonBase.setTooltip(new Tooltip(tooltip));
        setIcon(iconResource, buttonBase);
    }

    private void setIcon(String iconResource, ButtonBase buttonBase) {
        IconUtilities.getIcon(iconResource).ifPresentOrElse(buttonBase::setGraphic, () -> buttonBase.setText("?"));
    }

}
