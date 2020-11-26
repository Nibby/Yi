package yi.editor.components;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;
import yi.component.i18n.TextResource;
import yi.editor.AcceleratorId;
import yi.editor.EditorFrame;
import yi.editor.TextKeys;

/**
 * A series of window component layout strategies depending on the editor use case.
 */
public enum ContentLayout {

    // Note: The enum name itself is stored and loaded from general preference files, changing its value
    //       will break backwards compatibility.

    /**
     * A minimal layout that focuses on the game board.
     */
    COMPACT(TextKeys.MENUITEM_PERSPECTIVE_COMPACT, AcceleratorId.TOGGLE_PERSPECTIVE_COMPACT) {
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
    },

    /**
     * An expansive layout that with in-depth editing tools.
     */
    REVIEW(TextKeys.MENUITEM_PERSPECTIVE_REVIEW, AcceleratorId.TOGGLE_PERSPECTIVE_REVIEW) {
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
    };

    private final TextResource friendlyName;
    private final AcceleratorId acceleratorId;

    /**
     * Creates a new type of supported layout for {@link EditorFrame}.
     *
     * @param friendlyName User friendly name of this layout
     */
    ContentLayout(TextResource friendlyName, AcceleratorId acceleratorId) {
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
    public AcceleratorId getAcceleratorId() {
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
    public static ContentLayout getDefaultValue() {
        return COMPACT;
    }

    /**
     *
     * @param serializedValue The content layout setting value serialized as a string.
     * @return The enum value equivalent of the serialized value, or {@link #getDefaultValue()}
     *         if the value is malformed or unsupported.
     */
    public static ContentLayout getValue(@Nullable String serializedValue) {
        try {
            return ContentLayout.valueOf(serializedValue);
        } catch (IllegalArgumentException e) {
            return getDefaultValue(); // Fail gracefully to default options.
        }
    }
}
