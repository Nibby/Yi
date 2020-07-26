package yi.editor.components;

import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import yi.editor.EditorFrame;

/**
 * A series of window component layout strategies depending on the editor use case.
 */
public enum ComponentLayout {

    /**
     * A minimal layout that focuses on the game board.
     */
    COMPACT("Compact") {
        @Override
        public Parent layoutContent(EditorFrame frame) {
            var content = new BorderPane();

            var board = frame.getBoardViewer().getComponent();
            content.setCenter(board);

            // TODO: A footer component showing player name / color and move number

            return content;
        }
    },

    /**
     * An expansive layout that supports power-editing.
     */
    REVIEW("Review") {
        @Override
        public Parent layoutContent(EditorFrame frame) {
            var content = new BorderPane();

            var board = frame.getBoardViewer().getComponent();
            var tree = frame.getTreeViewer().getComponent();

            var splitPane = new SplitPane(board, tree);
            content.setCenter(splitPane);

            return content;
        }
    },
    ;

    private final String friendlyName;

    ComponentLayout(String friendlyName) {
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
    public abstract Parent layoutContent(EditorFrame frame);

    @Override
    public String toString() {
        return getFriendlyName();
    }
}
