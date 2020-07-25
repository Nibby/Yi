package yi.component.gametree;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jetbrains.annotations.NotNull;
import yi.component.CanvasContainer;
import yi.component.Component;
import yi.core.common.EventListener;
import yi.core.common.GameNode;
import yi.core.common.NodeEvent;
import yi.core.go.GoGameModel;
import yi.core.go.GoGameStateUpdate;

/**
 * A component which displays the game model and its nodes as a tree graph.
 */
public final class GameTreeViewer implements Component {

    private final CanvasContainer component;
    private final GameTreeCanvas canvas;
    private final Camera camera;

    private GoGameModel gameModel;
    private GameTreeStructure treeStructure;
    private final GameTreeElementSize elementSize;

    public GameTreeViewer(GoGameModel gameModel) {
        canvas = new GameTreeCanvas();
        canvas.addInputHandler(new CanvasInputHandler());

        component = new CanvasContainer(canvas);
        elementSize = new GameTreeElementSize();
        camera = new Camera(component.getWidth(), component.getHeight());

        component.addSizeUpdateListener(newSize -> {
            camera.setViewportSize(newSize.getWidth(), newSize.getHeight());
            render();
        });

        camera.addPanAnimationListener(this::render);

        setGameModel(gameModel);
        updateCameraAndRender(gameModel.getCurrentMove());
    }

    private void updateCameraAndRender(GameNode<GoGameStateUpdate> currentNode) {
        treeStructure.getElements().stream()
                .filter(element -> (element instanceof TreeNodeElement) && ((TreeNodeElement) element).getNode().equals(currentNode))
                .findAny()
                .ifPresent(currentNodeElement -> camera.setCenterElementWithAnimation(currentNodeElement, elementSize.getGridSize()));

        render();
    }

    private void render() {
        canvas.render(camera, treeStructure.getElements(), gameModel.getCurrentMove(), elementSize);
    }

    private final EventListener<NodeEvent<GoGameStateUpdate>> treeStructureChangeListener = (event) -> {
        treeStructure.reconstruct();
        render();
    };

    private final EventListener<NodeEvent<GoGameStateUpdate>> currentMoveChangeListener = (event) -> updateCameraAndRender(event.getNode());

    public void setGameModel(@NotNull GoGameModel model) {
        if (this.gameModel != null) {
            this.gameModel.onCurrentNodeUpdate().removeListener(currentMoveChangeListener);
            this.gameModel.onNodeAdd().removeListener(treeStructureChangeListener);
            this.gameModel.onNodeDelete().removeListener(treeStructureChangeListener);
        }

        this.gameModel = model;
        this.treeStructure = new GameTreeStructure(this.gameModel);
        this.camera.reset();

        this.gameModel.onCurrentNodeUpdate().addListener(currentMoveChangeListener);
        this.gameModel.onNodeAdd().addListener(treeStructureChangeListener);
        this.gameModel.onNodeDelete().addListener(treeStructureChangeListener);
    }

    @Override
    public Parent getComponent() {
        return component;
    }


    private final class CanvasInputHandler implements GameTreeCanvas.InputHandler {

        private double dragStartX = 0;
        private double dragStartY = 0;
        private boolean isDragging = false;

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

            render();
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

                treeStructure.getElement(x, y).ifPresent(element -> {
                    if (element instanceof TreeNodeElement) {
                        var selectedNode = ((TreeNodeElement) element).getNode();
                        gameModel.setCurrentMove(selectedNode);
                    }
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
            double deltaX = e.getDeltaX() / 3d;
            double deltaY = e.getDeltaY() / 3d;

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
