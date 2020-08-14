package yi.editor.components;

import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import yi.component.SimpleListenerManager;
import yi.editor.EditorTool;
import yi.editor.utilities.IconUtilities;

import java.util.ArrayList;
import java.util.List;
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

        toolPlayMove = addEditToolButton(EditorTool.PLAY_MOVE, "/icons/playStone32.png", "Play Move");
        toolAddBlackStone = addEditToolButton(EditorTool.ADD_BLACK_STONE, "/icons/addBlackStone32.png", "Add Black Stone");
        toolAddWhiteStone = addEditToolButton(EditorTool.ADD_WHITE_STONE, "/icons/addWhiteStone32.png", "Add White Stone");

        toolAnnotateTriangle = addEditToolButton(EditorTool.ANNOTATE_TRIANGLE, "/icons/annoTriangle32.png", "Add Triangle");
        toolAnnotateCircle = addEditToolButton(EditorTool.ANNOTATE_CIRCLE, "/icons/annoCircle32.png", "Add Circle");
        toolAnnotateSquare = addEditToolButton(EditorTool.ANNOTATE_SQUARE, "/icons/annoSquare32.png", "Add Square");
        toolAnnotateCross = addEditToolButton(EditorTool.ANNOTATE_CROSS, "/icons/annoCross32.png", "Add Cross");
        toolAnnotateText = addEditToolButton(EditorTool.ANNOTATE_LETTER, "/icons/annoLetter32.png", "Add Letter");
        toolAnnotateNumber = addEditToolButton(EditorTool.ANNOTATE_NUMBER, "/icons/annoNumber32.png", "Add Number");
        toolAnnotateLine = addEditToolButton(EditorTool.ANNOTATE_LINE, "/icons/annoLine32.png", "Add Line");
        toolAnnotateArrow = addEditToolButton(EditorTool.ANNOTATE_ARROW, "/icons/annoArrow32.png", "Add Arrow");
        toolAnnotateDim = addEditToolButton(EditorTool.ANNOTATE_DIM, "/icons/annoDim32.png", "Add Shade");
    }

    public void setButtonsForContentLayout(ContentLayout layout) {
        getItems().clear();

        // TODO: This is definitely not a good design. Rethink UI.
        final int gap = 6;

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
        setUp(toggle, iconResource, tooltip);

        toggle.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            if (isSelected) {
                toolSelectionListeners.fireValueChangeEvent(editorTool);
            } else {
                if (toolButtonGroup.getSelectedToggle() == null && wasSelected) {
                    toggle.setSelected(true);
                }
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
        IconUtilities.getIcon(iconResource).ifPresentOrElse(buttonBase::setGraphic, () -> buttonBase.setText("?"));
    }

}
