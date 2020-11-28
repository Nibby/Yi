package yi.editor.components;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.TextResource;
import yi.editor.EditorFrame;
import yi.editor.EditorMainMenuType;
import yi.editor.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.EditorActionHelper;
import yi.editor.framework.action.EditorActionManager;
import yi.editor.framework.action.EditorRadioAction;
import yi.editor.framework.action.EditorSubMenuAction;

import java.util.function.Consumer;

/**
 * A series of window component layout strategies depending on the editor use case.
 */
public enum EditorPerspective {

    // Note: The enum name itself is stored and loaded from general preference files, changing its value
    //       will break backwards compatibility.

    /**
     * A minimal layout that focuses on the game board.
     */
    COMPACT(EditorTextResources.MENUITEM_PERSPECTIVE_COMPACT, EditorAcceleratorId.TOGGLE_PERSPECTIVE_COMPACT) {
        @Override
        public Parent getContent(EditorFrame frame) {
            var content = new BorderPane();

            var board = frame.getBoardComponent();
            content.setCenter(board);

            // TODO: A footer component showing player name / color and move number

            return content;
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            return new Dimension2D(540, 600);
        }

        @Override
        protected void createAction(EditorActionManager manager, EditorSubMenuAction submenu) {
            EditorPerspective.createPerspectiveAction(manager, this, submenu, 0.001d);
        }
    },

    /**
     * An expansive layout that with in-depth editing tools.
     */
    REVIEW(EditorTextResources.MENUITEM_PERSPECTIVE_REVIEW, EditorAcceleratorId.TOGGLE_PERSPECTIVE_REVIEW) {
        @Override
        public Parent getContent(EditorFrame frame) {
            var content = new BorderPane();

            var board = frame.getBoardComponent();
            var tree = frame.getTreeComponent();

            var splitPane = new SplitPane(board, tree);
            content.setCenter(splitPane);
            SplitPane.setResizableWithParent(tree, false);

            splitPane.getDividers().get(0).setPosition(0.7d);
            splitPane.getDividers().get(0).positionProperty().addListener(positionChanged -> {
                double position = splitPane.getDividerPositions()[0];
                // Prevent sidebar covering too much board content
                if (position < 0.7d) {
                    splitPane.setDividerPosition(0, 0.7d);
                }

                if (position > 0.85d) {
                    splitPane.setDividerPosition(0, 0.85d);
                }
            });

            return content;
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            return new Dimension2D(800, 600);
        }

        @Override
        protected void createAction(EditorActionManager manager, EditorSubMenuAction submenu) {
            EditorPerspective.createPerspectiveAction(manager, this, submenu, 0.002d);
        }
    },

    // Value created solely for the purpose of keeping the layout property non-null in EditorFrame.
    NONE(EditorTextResources.EMPTY, null) {
        @Override
        public Parent getContent(EditorFrame frame) {
            throw new UnsupportedOperationException("Should not be able to set perspective to NONE");
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            throw new UnsupportedOperationException("Should not be able to set perspective to NONE");
        }

        @Override
        protected void createAction(EditorActionManager manager, EditorSubMenuAction submenu) {
        }
    }

    ;

    private static final ToggleGroup MENU_ACTION_TOGGLE_GROUP = new ToggleGroup();

    private final TextResource friendlyName;
    private final EditorAcceleratorId acceleratorId;

    /**
     * Creates a new type of supported layout for {@link EditorFrame}.
     *
     * @param friendlyName User friendly name of this layout
     */
    EditorPerspective(TextResource friendlyName, EditorAcceleratorId acceleratorId) {
        this.friendlyName = friendlyName;
        this.acceleratorId = acceleratorId;
    }

    /**
     *
     * @return User-friendly name for this layout.
     */
    public TextResource getFriendlyName() {
        return friendlyName;
    }

    /**
     *
     * @return The unique identifier for the accelerator for this layout.
     */
    public EditorAcceleratorId getAcceleratorId() {
        return acceleratorId;
    }

    /**
     * Creates a container that has the frame components arranged by this perspective.
     * The client can set the output container as its primary content.
     *
     * @param frame The frame to arrange.
     * @return The container with the layout defined by this perspective.
     */
    public abstract Parent getContent(EditorFrame frame);

    /**
     *
     * @return The default window dimensions when {@link EditorFrame} is first launched
     *         with this layout selected.
     */
    public abstract Dimension2D getMinimumWindowSize();

    protected abstract void createAction(EditorActionManager manager, EditorSubMenuAction submenu);

    /**
     * Each layout may display extra (or fewer) components which require a custom adequate
     * aspect ratio. This value will be used to adjust window size upon switching to the
     * layout so that its contents can fit properly within the window.
     *
     * @return The preferred width/height ratio for this layout.
     */
    public double getPreferredAspectRatio() {
        var startupSize = getMinimumWindowSize();
        return startupSize.getWidth() / startupSize.getHeight();
    }

    @Override
    public String toString() {
        return getFriendlyName().getLocalisedText();
    }

    /**
     *
     * @return The default content layout.
     */
    public static EditorPerspective getDefaultValue() {
        return COMPACT;
    }

    /**
     *
     * @param serializedValue The content layout setting value serialized as a string.
     * @return The enum value equivalent of the serialized value, or {@link #getDefaultValue()}
     *         if the value is malformed or unsupported.
     */
    public static EditorPerspective getValue(@Nullable String serializedValue) {
        try {
            return EditorPerspective.valueOf(serializedValue);
        } catch (IllegalArgumentException e) {
            return getDefaultValue(); // Fail gracefully to default options.
        }
    }

    public static void initializeActions(EditorActionManager manager) {
        var perspectiveSubmenu = new EditorSubMenuAction(manager, EditorTextResources.MENU_PERSPECTIVE);
        perspectiveSubmenu.setInMainMenu(EditorMainMenuType.VIEW, 0d);

        for (EditorPerspective p : values()) {
            p.createAction(manager, perspectiveSubmenu);
        }
    }

    private static EditorPerspective getPerspective(EditorActionManager manager) {
        return getPerspective(manager.getHelper());
    }

    private static EditorPerspective getPerspective(EditorActionHelper helper) {
        return helper.getEditorFrame().getPerspective();
    }

    private static void createPerspectiveAction(EditorActionManager manager, 
                                                EditorPerspective perspective,
                                                EditorSubMenuAction submenu,
                                                double position) {

        Consumer<EditorActionHelper> action = helper -> helper.getEditorFrame().setPerspective(perspective);
        var editorAction = new EditorRadioAction(manager, perspective.getFriendlyName(), action);
        editorAction.setInMainMenu(EditorMainMenuType.VIEW, position);
        editorAction.setSelected(EditorPerspective.getPerspective(manager) == perspective);
        editorAction.setMenuToggleGroup(MENU_ACTION_TOGGLE_GROUP);
        editorAction.setAccelerator(perspective.getAcceleratorId());

        if (submenu != null) {
            submenu.addChildAction(editorAction);
        }
    }
}
