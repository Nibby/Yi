package yi.component.boardviewer;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import yi.core.go.GameModel;
import yi.core.go.GameNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles and manages all keyboard and mouse input to the {@link GameBoardViewer}.
 * Performs rapid repaints of lightweight objects (such as the transparent intersection cursor).
 */
final class GameBoardInputCanvas extends GameBoardCanvas {

    private Map<Integer, GameNode> intersectionsWithPreviewNode = new HashMap<>();

    private int cursorX = 0;
    private int cursorY = 0;
    private boolean renderCursor = false;

    GameBoardInputCanvas(GameBoardManager manager) {
        super(manager);

        setFocusTraversable(true);

        addEventHandler(MouseEvent.ANY, this::onMouseEvent);
        addEventHandler(KeyEvent.ANY, this::onKeyEvent);
        addEventFilter(ScrollEvent.SCROLL, this::onScrollEvent);
    }

    @Override
    protected void renderImpl(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        if (renderCursor) {
            if (manager.edit.isEditable()) {
                var editMode = manager.edit.getEditMode();
                editMode.getMouseCursor().ifPresent(this::setCursor);
                editMode.renderGridCursor(g, manager, cursorX, cursorY);
            } else {
                setCursor(Cursor.DEFAULT);
            }
        }
    }

    @Override
    public void onGameModelSet(GameModel newModel, GameBoardManager manager) {
        newModel.onCurrentNodeChange().addListener(event -> updateIntersectionsWithPreviewNode(event.getNode()));
    }

    private void updateIntersectionsWithPreviewNode(GameNode currentNode) {
        // TODO: Extract this into a setting
        final int MAX_NUMBER_OF_MOVES_IN_PREVIEW = 20;

        intersectionsWithPreviewNode.clear();
        if (currentNode.hasAlternativeNextMoves()) {
            // TODO: Support OGS AI Reviews (1 stone edit rather than primary move)
            var tempIntersectionMap = new HashMap<Integer, GameNode>(currentNode.getNextNodes().size());

            for (GameNode nextNode : currentNode.getNextNodes()) {
                var move = nextNode.getPrimaryMove();
                if (move != null) {
                    var nodeToStore = nextNode;

                    for (int i = 0; i < MAX_NUMBER_OF_MOVES_IN_PREVIEW; ++i) {
                        if (nodeToStore.isLastMoveInThisVariation()) {
                            break;
                        } else {
                            var continuation = nodeToStore.getNextNodeInMainBranch();
                            if (continuation != null) {
                                nodeToStore = continuation;
                            }
                        }
                    }

                    var position = move.getY() * manager.getGameModel().getBoardWidth() + move.getX();
                    tempIntersectionMap.put(position, nodeToStore);
                }
            }

            if (tempIntersectionMap.size() > 1) {
                intersectionsWithPreviewNode = tempIntersectionMap;
            }
        }
    }

    @Override
    public void onGameUpdate(GameModel gameModel, GameBoardManager manager) {

    }

    private void onMouseEvent(MouseEvent e) {
        retrieveCursorPosition(e.getX(), e.getY());

        if (manager.edit.isEditable()) {
            if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
                requestFocus();
                manager.edit.getEditMode().onMousePress(e.getButton(), manager, cursorX, cursorY);
            } else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                requestFocus();
                manager.edit.getEditMode().onMouseDrag(e.getButton(), manager, cursorX, cursorY);
            } else if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
                manager.edit.getEditMode().onMouseRelease(e.getButton(), manager, cursorX, cursorY);
            }
        }

        if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
            maybeShowVariationPreview(e);
        }

        render(manager);
    }

    private void maybeShowVariationPreview(MouseEvent e) {
        var mouseOverPosition = cursorY * manager.getGameModel().getBoardWidth() + cursorX;
        GameNode previewNode = intersectionsWithPreviewNode.get(mouseOverPosition);

        if (manager.isShowingCurrentPosition()
                || (previewNode != null && previewNode != manager.getNodeToShow())) {
            manager.setPreviewNode(previewNode);
        } else if (previewNode == null) {
            manager.setPreviewNode(null);
        }
    }

    private void onKeyEvent(KeyEvent e) {
        if (e.getEventType() == KeyEvent.KEY_PRESSED) {
            manager.edit.getEditMode().onKeyPress(manager, e);
        }
    }

    public void onScrollEvent(ScrollEvent e) {
        requestFocus();
        double deltaY = e.getDeltaY();

        if (deltaY < 0) {
            manager.getGameModel().toNextNode();
        } else if (deltaY > 0) {
            manager.getGameModel().toPreviousNode();
        }
    }

    private void clearCursorPosition() {
        renderCursor = false;
    }

    private void setCursorPosition(int[] gridPosition) {
        renderCursor = true;
        cursorX = gridPosition[0];
        cursorY = gridPosition[1];
    }

    private void retrieveCursorPosition(double mouseX, double mouseY) {
        Optional<int[]> cursorPosition = manager.size.getGridPosition(mouseX, mouseY);
        cursorPosition.ifPresentOrElse(this::setCursorPosition, this::clearCursorPosition);
    }
}
