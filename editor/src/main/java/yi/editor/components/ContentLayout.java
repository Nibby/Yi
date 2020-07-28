package yi.editor.components;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;
import yi.editor.EditorFrame;

/**
 * A series of window component layout strategies depending on the editor use case.
 */
public enum ContentLayout {

    // Note: The enum name itself is stored and loaded from general preference files, changing its value
    //       will break backwards compatibility.

    /**
     * A minimal layout that focuses on the game board.
     */
    COMPACT("Compact") {
        @Override
        public Parent getContent(EditorFrame frame) {
            var content = new BorderPane();

            var board = frame.getBoardViewer().getComponent();
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
    REVIEW("Review") {
        @Override
        public Parent getContent(EditorFrame frame) {
            var content = new BorderPane();

            var board = frame.getBoardViewer().getComponent();
            var tree = frame.getTreeViewer().getComponent();

            var splitPane = new SplitPane(board, tree);
            content.setCenter(splitPane);
            SplitPane.setResizableWithParent(splitPane, false);

            splitPane.getDividers().get(0).setPosition(0.7d);
            splitPane.getDividers().get(0).positionProperty().addListener(positionChanged -> {
                double position = splitPane.getDividerPositions()[0];
                // Prevent sidebar covering too much board content
                if (position < 0.7d) {
                    splitPane.setDividerPosition(0, 0.7d);
                }
            });

            return content;
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            return new Dimension2D(800, 600);
        }
    };

    private final String friendlyName;

    /**
     * Creates a new type of supported layout for {@link EditorFrame}.
     *
     * @param friendlyName User friendly name of this layout
     */
    ContentLayout(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     *
     * @return User-friendly name for this layout.
     */
    public String getFriendlyName() {
        return friendlyName;
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
     * @return The default window dimensions when {@link EditorFrame} is first launched with this
     *         layout selected.
     */
    public abstract Dimension2D getMinimumWindowSize();


    /**
     * Each layout may display extra (or fewer) components which require a custom adequate
     * aspect ratio. This value will be used to adjust window size upon switching to the layout
     * so that its contents can fit properly within the window.
     *
     * @return The preferred width/height ratio for this layout.
     */
    public double getPreferredAspectRatio() {
        var startupSize = getMinimumWindowSize();
        return startupSize.getWidth() / startupSize.getHeight();
    }


    @Override
    public String toString() {
        return getFriendlyName();
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
     * @return The enum value equivalent of the serialized value, or {@link #getDefaultValue()} if the value is
     *         malformed or unsupported.
     */
    public static ContentLayout getValue(@Nullable String serializedValue) {
        try {
            return ContentLayout.valueOf(serializedValue);
        } catch (IllegalArgumentException e) {
            return getDefaultValue(); // Fail gracefully to default options.
        }
    }
}
