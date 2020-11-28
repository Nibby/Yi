package yi.component.gametree;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.NullableProperty;
import yi.common.NullablePropertyListener;
import yi.component.CanvasContainer;
import yi.component.YiComponent;
import yi.core.go.EventListener;
import yi.core.go.GameModel;
import yi.core.go.GameNode;
import yi.core.go.NodeEvent;

import java.util.List;
import java.util.Objects;

/**
 * A component which displays the game model and its nodes as a tree graph.
 */
public final class GameTreeViewer implements YiComponent {

    private final CanvasContainer component;
    private GameTreeViewerSettings settings = GameTreeViewerSettings.getDefault();
    private final GameTreeCanvas canvas;
    private final Camera camera;

    private GameModel gameModel;
    private GameTreeStructure treeStructure;
    private final GameTreeElementSize elementSize;
    private final NullableProperty<GameNode> previewNode = new NullableProperty<>(null);

    public GameTreeViewer() {
        canvas = new GameTreeCanvas();
        canvas.addInputHandler(new CanvasInputHandler());

        component = new CanvasContainer(canvas);
        elementSize = new GameTreeElementSize();
        camera = new Camera(component.getWidth(), component.getHeight());

        component.addSizeUpdateListener(newSize -> {
            camera.setViewportSize(newSize.getWidth(), newSize.getHeight());
            render();
        });

        camera.addOffsetChangeListener(this::render);
    }

    public void setSettings(GameTreeViewerSettings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    private void updateCameraAndRender(GameNode nodeToCenter) {
        treeStructure.getTreeNodeElementForNode(nodeToCenter)
                .ifPresent(treeElement -> camera.setCenterElementWithAnimation(treeElement, elementSize.getGridSize()));

        render();
    }

    private void render() {
        var elements = getVisibleElementsInViewport();
        var currentNode = gameModel.getCurrentNode();
        var previewNode = treeStructure.getPreviewNode();

        canvas.render(settings, camera, elements, currentNode, previewNode, elementSize);
    }

    private List<TreeNodeElement> getVisibleElementsInViewport() {
        var gridWidth = elementSize.getGridSize().getWidth();
        var gridHeight = elementSize.getGridSize().getHeight();
        var offsetX = -camera.getOffsetX();
        var offsetY = -camera.getOffsetY();

        var startX = (int) Math.floor(offsetX / gridWidth) - 1;
        var startY = (int) Math.floor(offsetY / gridHeight) - 1;
        var endX = startX + (int) Math.ceil(canvas.getWidth() / gridWidth) + 2;
        var endY = startY + (int) Math.ceil(canvas.getHeight() / gridHeight) + 2;

        return treeStructure.getNodeElementsWithinVerticalRegion(startX, startY, endX, endY);
    }

    private final EventListener<NodeEvent> treeStructureChangeListener = (event) -> {
        treeStructure.reconstruct();
        render();
    };

    private final EventListener<NodeEvent> currentMoveChangeListener = (event) -> updateCameraAndRender(event.getNode());

    public void setGameModel(@NotNull GameModel model) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeChange().removeListener(currentMoveChangeListener);
            this.gameModel.onNodeAdd().removeListener(treeStructureChangeListener);
            this.gameModel.onNodeRemove().removeListener(treeStructureChangeListener);
        }

        this.gameModel = model;
        this.treeStructure = new GameTreeStructure(this.gameModel);
        this.camera.reset();

        this.gameModel.onCurrentNodeChange().addListener(currentMoveChangeListener);
        this.gameModel.onNodeAdd().addListener(treeStructureChangeListener);
        this.gameModel.onNodeRemove().addListener(treeStructureChangeListener);

        updateCameraAndRender(model.getCurrentNode());
    }

    /**
     * Subscribes to current highlighted node update events. Highlighted node may be
     * {@code null} if none is highlighted.
     *
     * @param listener New listener to add.
     */
    public void addHighlightedNodeChangeListener(NullablePropertyListener<GameNode> listener) {
        previewNode.addListener(listener);
    }

    public void removeHighlightedNodeChangeListener(NullablePropertyListener<GameNode> listener) {
        previewNode.removeListener(listener);
    }

    @Override
    public Parent getComponent() {
        return component;
    }

    public @Nullable GameNode getHighlightedNode() {
        return treeStructure.getPreviewNode();
    }

    public void setHighlightedNode(@Nullable GameNode node) {
        treeStructure.setHighlightedNode(node);
        updateCameraAndRender(node != null ? node : this.gameModel.getCurrentNode());
    }

    /*
        Put this here to access all the required fields without introducing excessive coupling to the
        canvas component itself.
     */
    final class CanvasInputHandler implements GameTreeCanvas.InputHandler {

        private double dragStartX = 0;
        private double dragStartY = 0;
        private boolean isDragging = false;

        private GameNode lastFiredHighlightedNodeValue = null;

        @Override
        public void mouseMoved(MouseEvent e) {
            int[] gridPosition = getGridPosition(e);
            int x = gridPosition[0];
            int y = gridPosition[1];

            boolean hasItem = treeStructure.setHighlightedGrid(x, y);
            if (!hasItem) {
                canvas.setCursor(Cursor.OPEN_HAND);
            } else {
                canvas.setCursor(Cursor.HAND);
            }

            maybeFireHighlightedNodeChangeEvent(hasItem);
            render();
        }

        private void maybeFireHighlightedNodeChangeEvent(boolean hasItem) {
            GameNode newValue = treeStructure.getPreviewNode();
            if (hasItem) {
                if (lastFiredHighlightedNodeValue != newValue) {
                    previewNode.set(newValue);
                }
                lastFiredHighlightedNodeValue = newValue;
            } else if (lastFiredHighlightedNodeValue != null) {
                lastFiredHighlightedNodeValue = null;
                previewNode.set(null);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragStartX = e.getX();
            dragStartY = e.getY();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!isDragging) {
                int[] gridPosition = getGridPosition(e);
                int x = gridPosition[0];
                int y = gridPosition[1];

                treeStructure.getNodeElement(x, y).ifPresent(element -> {
                    var selectedNode = element.getNode();
                    gameModel.setCurrentNode(selectedNode);
                });
            }

            isDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            isDragging = true;
            canvas.setCursor(Cursor.CLOSED_HAND);

            double xDiff = e.getX() - dragStartX;
            double yDiff = e.getY() - dragStartY;
            dragStartX = e.getX();
            dragStartY = e.getY();

            double offsetX = camera.getOffsetX();
            double offsetY = camera.getOffsetY();

            camera.setOffset(offsetX + xDiff, offsetY + yDiff);
        }

        @Override
        public void mouseScrolled(ScrollEvent e) {
            // TODO: Scrolling behaviour adjusts view offset rather than game
            //       state. Potentially make this adjustable in preferences so
            //       that those who are migrating from another app can feel at home.
            double deltaX = e.getDeltaX();
            double deltaY = e.getDeltaY();

            double offsetX = camera.getOffsetX();
            double offsetY = camera.getOffsetY();

            camera.setOffset(offsetX + deltaX, offsetY + deltaY);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            render();
        }

        private int[] getGridPosition(MouseEvent e) {
            double gridWidth = elementSize.getGridSize().getWidth();
            double gridHeight = elementSize.getGridSize().getHeight();

            int gridX = (int) Math.round(((e.getX() - gridWidth / 2) - camera.getOffsetX()) / gridWidth);
            int gridY = (int) Math.round(((e.getY() - gridHeight / 2) - camera.getOffsetY()) / gridHeight);

            return new int[] { gridX, gridY };
        }
    }
}
