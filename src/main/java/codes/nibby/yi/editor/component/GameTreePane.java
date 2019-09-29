package codes.nibby.yi.editor.component;

import codes.nibby.yi.board.StoneRenderer;
import codes.nibby.yi.board.StoneStyle;
import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * A component for displaying the game tree structure visually.
 * Allows user input to navigate.
 *
 * TODO: Implement this later.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameTreePane extends GridPane implements GameListener {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private static final int COMPONENT_WIDTH = 24;
    private static final int COMPONENT_HEIGHT = 24;

    private int orientation = VERTICAL;
    private GameEditorWindow editor;
    private Game game;

    public GameTreePane(GameEditorWindow editor) {
        this.editor = editor;
    }

    private void rebuildTree() {
        getChildren().clear();
        GameNode current = game.getGameTree();
        int row = 0, col = 0;
        buildBranch(current, row, col);

//
//        do {
//            // First add the node component
//            NodeComponent _node = new NodeComponent(current);
//            add(_node, row, col);
//
//            // Then add child branches
//            List<GameNode> children = current.getChildren();
//            for (int i = 0; i < children.size(); i++) {
//                if (i == 0) {
//                    // Create a connector
//                    int cRow = (orientation == HORIZONTAL) ? row + 1 : row;
//                    int cCol = (orientation == HORIZONTAL) ? col : col + 1;
//                    TrackComponent _line = new TrackComponent();
//                    add(_line, cRow, cCol);
//                } else {
//                    // TODO: create child branch
//                    int cRow = (orientation == HORIZONTAL) ? row : row + 1;
//                    int cCol = (orientation == HORIZONTAL) ? col + 1 : col;
//                    buildBranch(children.get(i), cRow, cCol);
//                }
//            }
//
//            // Next...
//            if (current.hasChildren()) {
//                current = children.get(0);
//                if (orientation == HORIZONTAL)
//                    row += 2;
//                else
//                    col += 2;
//
//            }
//        } while (current.hasChildren());
    }

    private void buildBranch(GameNode branchRoot, int startRow, int startCol) {
        // TODO add connectors connecting back to main (in case this isn't the main variation)

        int row = startRow;
        int col = startCol;
        GameNode current = branchRoot;
        do {
            // Build current variation
            NodeComponent _node = new NodeComponent(current);
            add(_node, row, col);
            // Build child variation branches
            List<GameNode> children = current.getChildren();
            for (int i = 1; i < children.size(); i++) {
                int cRow = (orientation == HORIZONTAL) ? row : row + 1;
                int cCol = (orientation == HORIZONTAL) ? col + 1 : col;
                buildBranch(children.get(i), cRow, cCol);
            }

            // If there is a continuation on this variation,
            // continue laying tracks...

            if (current.hasChildren()) {
                if (orientation == HORIZONTAL)
                    row++;
                else
                    col++;
                TrackComponent _track = new TrackComponent(current, current.getChildren().get(0));
                add(_track, row, col);

                current = current.getChildren().get(0);
                if (orientation == HORIZONTAL)
                    row++;
                else
                    col++;

            } else
                current = null;
        } while (current != null);
    }

    @Override
    public void gameInitialized(Game game) {
        this.game = game;
        rebuildTree();
    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {
        rebuildTree();
    }

    private static class NodeComponent extends Canvas {

        private GraphicsContext g;
        private GameNode node;

        private NodeComponent(GameNode node) {
            g = getGraphicsContext2D();
            this.node = node;
            setWidth(COMPONENT_WIDTH);
            setHeight(COMPONENT_HEIGHT);

            int iconSize = (int) Math.min(getWidth(), getHeight());
            StoneRenderer.renderTexture(g, node.getColor(), iconSize, 0, 0);


            // TODO temporary
            g.setStroke(Color.BLACK);
            g.strokeRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class TrackComponent extends Canvas {
        private static final int TRACK_HORIZONTAL_THROUGH = 0;
        private static final int TRACK_HORIZONTAL_T = 1;
        private static final int TRACK_HORIZONTAL_CORNER = 2;

        private static final int TRACK_VERTICAL = 3;
        private static final int TRACK_VERTICAL_T = 4;
        private static final int TRACK_VERTICAL_CORNER = 5;

        private int trackType;
        private GraphicsContext g;

        private TrackComponent(GameNode parent, GameNode child) {
            g = getGraphicsContext2D();
            int height = COMPONENT_HEIGHT / 2;
            setWidth(COMPONENT_WIDTH);
            setHeight(height);

            g.setStroke(Color.BLACK);
            g.strokeLine(getWidth() / 2, 0, getWidth()/ 2, getHeight());

            // TODO temporary
            g.strokeRect(0, 0, getWidth(), getHeight());
        }
    }
}
