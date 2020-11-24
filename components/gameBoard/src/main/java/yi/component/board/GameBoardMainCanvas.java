package yi.component.board;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import yi.core.go.Annotation;
import yi.core.go.GameModel;
import yi.core.go.StoneColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Draws the primary contents of the game board, such as the board image, grids, star points,
 * stones and annotations that are part of the game state. In principle, any object that isn't
 * expected to be repainted very quickly should be drawn here.
 * <p/>
 * For quick-repaint objects, use {@link GameBoardInputCanvas}.
 */
final class GameBoardMainCanvas extends GameBoardCanvas {

    GameBoardMainCanvas(GameBoardManager manager) {
        super(manager);
    }

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());

        BoardRenderer.render(g, manager);
        BoardStoneRenderer.render(g, manager);
        BoardAnnotationRenderer.render(g, manager);
    }

    private static final class BoardRenderer {

        private static final DropShadow BOARD_BORDER_SHADOW = new DropShadow();

        private static void render(GraphicsContext g, GameBoardManager manager) {
            renderBackground(g, manager);
            renderBoardTexture(g, manager);
            renderCoordinateLabels(g, manager);
            renderGrid(g, manager);

            if (manager.isDebugMode()) {
                renderDebug(g, manager);
            }
        }

        private static void renderDebug(GraphicsContext g, GameBoardManager manager) {
            var size = manager.size;

            var stage = size.getStageBounds();
            g.setStroke(Color.RED);
            g.setLineWidth(1d);
            g.strokeRect(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

            var grid = size.getGridBounds();
            g.setStroke(Color.BLUE);
            g.strokeRect(grid.getX(), grid.getY(), grid.getWidth(), grid.getHeight());

            var board = size.getBoardBounds();
            g.setStroke(Color.MAGENTA);
            g.strokeRect(board.getX(), board.getY(), board.getWidth(), board.getHeight());
        }

        private static void renderBackground(GraphicsContext g, GameBoardManager manager) {
            Image backgroundImage = manager.view.backgroundImage;

            if (backgroundImage != null) {
                Rectangle stage = manager.size.getStageBounds();
                g.drawImage(backgroundImage, stage.getX(), stage.getY(), stage.getWidth(),
                        stage.getHeight());
            }
        }

        private static void renderBoardTexture(GraphicsContext g, GameBoardManager manager) {
            double borderOffset = manager.size.getBoardBorderShadowOffsetInPixels();
            BOARD_BORDER_SHADOW.setRadius(manager.size.getShadowRadius());
            BOARD_BORDER_SHADOW.setOffsetY(borderOffset);
            BOARD_BORDER_SHADOW.setColor(Color.color(0d, 0d, 0d, 0.7d));
            g.setEffect(BOARD_BORDER_SHADOW);

            Rectangle boardBounds = manager.size.getBoardBounds();
            g.drawImage(manager.view.boardImage, boardBounds.getX(), boardBounds.getY(),
                    boardBounds.getWidth(), boardBounds.getHeight());

            g.setEffect(null);

            { // Draw border around board edge
                g.setStroke(Color.BLACK);
                g.setGlobalAlpha(0.1d);
                final double borderThickness = boardBounds.getWidth() / 100d;
                g.setLineWidth(borderThickness);
                g.strokeRect(boardBounds.getX() + borderThickness / 2,
                        boardBounds.getY() + borderThickness / 2,
                        boardBounds.getWidth() - borderThickness,
                        boardBounds.getHeight() - borderThickness);
                g.setGlobalAlpha(1.0d);
            }

        }

        private static void renderCoordinateLabels(GraphicsContext g, GameBoardManager manager) {
            // TODO: Implement later
        }

        private static void renderGrid(GraphicsContext g, GameBoardManager manager) {
            double originalLineWidth = g.getLineWidth();
            double gridLineThickness = manager.size.getGridLineThicknessInPixels();

            // Draw game grid
            g.setLineWidth(gridLineThickness);

            Color lineColor = manager.view.boardGridColor;
            g.setFill(lineColor);
            g.setStroke(lineColor);

            Rectangle gridBounds = manager.size.getGridBounds();

            for (int lineNumber = 0; lineNumber < manager.getGameModel().getBoardWidth(); ++lineNumber) {
                double[] drawXY = manager.size.getGridRenderPosition(lineNumber, 0, 0);
                double x = drawXY[0];

                g.strokeLine(x, gridBounds.getY(), x, gridBounds.getY() + gridBounds.getHeight());
            }

            for (int lineNumber = 0; lineNumber < manager.getGameModel().getBoardHeight(); ++lineNumber) {
                double[] drawXY = manager.size.getGridRenderPosition(0, lineNumber, 0);
                double y = drawXY[1];

                g.strokeLine(gridBounds.getX(), y, gridBounds.getX() + gridBounds.getWidth(), y);
            }

            renderStarPoints(g, manager, gridLineThickness);

            g.setLineWidth(originalLineWidth);
        }

        private static void renderStarPoints(GraphicsContext g, GameBoardManager manager, double lineWidth) {
            // Number of intersections on the game board, not sizing
            int gameBoardWidth = manager.getGameModel().getBoardWidth();
            int gameBoardHeight = manager.getGameModel().getBoardHeight();

            var starPointPositions = StarPointPosition.get(gameBoardWidth, gameBoardHeight);
            double starPointDiameter = lineWidth * 6d;

            for (var starPointPosition : starPointPositions) {
                int x = starPointPosition % gameBoardWidth;
                int y = starPointPosition / gameBoardWidth;

                double[] drawXY = manager.size.getGridRenderPosition(x, y, starPointDiameter);

                g.fillOval(drawXY[0], drawXY[1], starPointDiameter, starPointDiameter);
            }
        }

        enum StarPointPosition {

            _19x19(point(3,  3, 19), point(9,  3, 19), point(15,  3, 19),
                   point(3,  9, 19), point(9,  9, 19), point(15,  9, 19),
                   point(3, 15, 19), point(9, 15, 19), point(15, 15, 19)),


            _13x13(point(3,  3, 13),                   point(9, 3, 13),
                                     point(6,  6, 13),
                   point(3,  9, 13),                   point(9, 9, 13)),


              _9x9(point(2, 2, 9),                     point(6, 2, 9),
                                     point(4,  4, 9),
                   point(2, 6, 9),                     point(6, 6, 9));


            private final Set<Integer> points;

            StarPointPosition(Integer ... points) {
                this.points = Arrays.stream(points).collect(Collectors.toSet());
            }

            static int point(int x, int y, int boardWidth) {
                return x + y * boardWidth;
            }

            static Set<Integer> get(int boardWidth, int boardHeight) {
                StarPointPosition configuration = null;

                if (boardWidth == boardHeight) {
                    if (boardWidth == 19)
                        configuration = _19x19;
                    else if (boardWidth == 13)
                        configuration = _13x13;
                    else if (boardWidth == 9)
                        configuration = _9x9;
                }

                if (configuration == null) {
                    return getDefault(boardWidth, boardHeight);
                } else {
                    return configuration.points;
                }
            }

            private static Set<Integer> getDefault(int boardWidth, int boardHeight) {
                // If the dimension is odd, return the centre intersection as a star point
                if (boardWidth % 2 == 1 && boardHeight % 2 == 1) {
                    int x = (boardWidth - 1) / 2;
                    int y = (boardHeight - 1) / 2;
                    return Collections.singleton(x + y * boardWidth);
                }

                return Collections.emptySet();
            }
        }
    }

    @Override
    public void onGameModelSet(GameModel newModel, GameBoardManager manager) {

    }

    @Override
    public void onGameUpdate(GameModel gameModel, GameBoardManager manager) {
        render(manager);
    }

    private static final class BoardStoneRenderer {

        public static void render(GraphicsContext g, GameBoardManager manager) {
            var boardPosition = manager.getGameModel().getCurrentGameState().getBoardPosition();
            StoneColor[] boardState = boardPosition.getIntersectionState();
            int boardWidth = manager.getGameModel().getBoardWidth();

            for (int i = 0; i < boardState.length; ++i) {
                StoneColor state = boardState[i];

                int x = i % boardWidth;
                int y = i / boardWidth;

                // TODO: Temporary. Allow rendering custom stone images in the future.
                PresetStoneStyle.getDefaultValue().render(g, manager, state, x, y);
            }
        }
    }

    private static final class BoardAnnotationRenderer {

        public static void render(GraphicsContext g, GameBoardManager manager) {
            // Not using a copy because this is performance-sensitive code
            Collection<Annotation> annotations = manager.getGameModel().getCurrentNode().getAnnotationsOriginal();

            for (Annotation annotation : annotations) {
                AnnotationRenderer.render(annotation, g, manager);
            }

            renderCurrentMoveMarker(g, manager);
        }

        private static void renderCurrentMoveMarker(GraphicsContext g, GameBoardManager manager) {
            var primaryMove = manager.getGameModel().getCurrentNode().getPrimaryMove();
            if (primaryMove != null) {
                int x = primaryMove.getX();
                int y = primaryMove.getY();

                if (!manager.getGameModel().getCurrentNode().hasAnnotationAt(x, y)) {
                    AnnotationRenderer.render(new Annotation._Dot(x, y), g, manager);
                }
            }
        }

    }
}
