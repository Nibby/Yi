package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.editor.settings.Settings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
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

    @Override
    protected void _render(GraphicsContext g, GameBoardManager manager) {
        g.clearRect(0, 0, getWidth(), getHeight());

        BoardRenderer.render(g, manager);
        StoneRenderer.render(g, manager);
    }

    private void renderStones(GraphicsContext g, GameBoardManager manager) {

    }

    private static final class BoardRenderer {

        private static final DropShadow BOARD_BORDER_SHADOW = new DropShadow();
        private static final Color BOARD_BORDER_COLOR = Color.color(0.25d, 0.25d, 0.25d, 0.25d);

        private static void render(GraphicsContext g, GameBoardManager manager) {
            renderBorderAndShadow(g, manager);
            renderBoardTexture(g, manager);
            renderCoordinateLabels(g, manager);
            renderGrid(g, manager);
        }

        private static void renderBorderAndShadow(GraphicsContext g, GameBoardManager manager) {
            BOARD_BORDER_SHADOW.setRadius(manager.size.getShadowRadius());
            BOARD_BORDER_SHADOW.setOffsetX(manager.size.getShadowOffsetXInPixels());
            BOARD_BORDER_SHADOW.setOffsetY(manager.size.getShadowOffsetYInPixels());
            BOARD_BORDER_SHADOW.setColor(Color.color(0d, 0d, 0d, 0.5d));

            g.setEffect(BOARD_BORDER_SHADOW);
            g.setFill(BOARD_BORDER_COLOR);

            Rectangle borderBounds = manager.size.getBoardBorderBounds();
            g.fillRect(borderBounds.getX(), borderBounds.getY(), borderBounds.getWidth(), borderBounds.getHeight());
            g.setEffect(null);
        }

        private static void renderBoardTexture(GraphicsContext g, GameBoardManager manager) {
            Rectangle boardBounds = manager.size.getBoardBounds();
            g.drawImage(Settings.boardTheme.getBoardImage(), boardBounds.getX(), boardBounds.getY(), boardBounds.getWidth(), boardBounds.getHeight());
        }

        private static void renderCoordinateLabels(GraphicsContext g, GameBoardManager manager) {
            // TODO: Implement later
        }

        private static void renderGrid(GraphicsContext g, GameBoardManager manager) {
            Color lineColor = Settings.boardTheme.getBoardGridColor();
            g.setFill(lineColor);
            g.setStroke(lineColor);

            double originalLineWidth = g.getLineWidth();
            double gridLineThickness = manager.size.getGridLineThicknessInPixels();
            g.setLineWidth(gridLineThickness);

            double stoneSize = manager.size.getStoneSizeInPixels();
            double offset = stoneSize / 2; // Okay to calculate this here since it's more related to rendering than sizing
            Rectangle gridBounds = manager.size.getGridBounds();

            for (int lineNumber = 0; lineNumber < manager.model.getBoardWidth(); ++lineNumber) {
                g.strokeLine(getRenderedGridX(lineNumber, manager, offset, 0),
                             gridBounds.getY() + offset,
                             getRenderedGridX(lineNumber, manager, offset, 0),
                             gridBounds.getY() + gridBounds.getHeight() - stoneSize + offset);
            }

            for (int lineNumber = 0; lineNumber < manager.model.getBoardHeight(); ++lineNumber) {
                g.strokeLine(gridBounds.getX() + offset,
                             getRenderedGridY(lineNumber, manager, offset, 0),
                             gridBounds.getX() + gridBounds.getWidth() - stoneSize + offset,
                             getRenderedGridY(lineNumber, manager, offset, 0));
            }

            renderStarPoints(g, manager, offset, gridLineThickness);

            g.setLineWidth(originalLineWidth);
        }

        private static void renderStarPoints(GraphicsContext g, GameBoardManager manager, double gridOffset, double lineWidth) {
            // Number of intersections on the game board, not sizing
            int gameBoardWidth = manager.model.getBoardWidth();
            int gameBoardHeight = manager.model.getBoardHeight();

            var starPointPositions = StarPointPosition.get(gameBoardWidth, gameBoardHeight);
            double starPointDiameter = lineWidth * 6d;

            for (var starPointPosition : starPointPositions) {
                int x = starPointPosition % gameBoardWidth;
                int y = starPointPosition / gameBoardWidth;

                g.fillOval(getRenderedGridX(x, manager, gridOffset, starPointDiameter),
                           getRenderedGridY(y, manager, gridOffset, starPointDiameter),
                           starPointDiameter,
                           starPointDiameter);
            }
        }

        private static double getRenderedGridX(int gridX, GameBoardManager manager, double gridOffset, double objectSize) {
            return manager.size.getGridBounds().getX() + gridOffset
                    + manager.size.getStoneSizeInPixels() * gridX - objectSize / 2;
        }

        private static double getRenderedGridY(int gridY, GameBoardManager manager, double gridOffset, double objectSize) {
            return manager.size.getGridBounds().getY() + gridOffset
                    + manager.size.getStoneSizeInPixels() * gridY - objectSize / 2;
        }

//        private static double getStoneX(int gridX, GameBoardManager manager, double gridOffset) {
//            return getGridX()
//        }

        enum StarPointPosition {

            _19x19(point(3,  3, 19), point(9,  3, 19), point(15,  3, 19),
                   point(3,  9, 19), point(9,  9, 19), point(15,  9, 19),
                   point(3, 15, 19), point(9, 15, 19), point(15, 15, 19)),


            _13x13(point(3,  3, 13),                   point(9, 3, 13),
                                     point(6,  6, 13),
                   point(3,  9, 13),                   point(9, 9, 13)),


              _9x9(point(2, 2, 9),                     point(6, 2, 9),
                                     point(4,  4, 9),
                   point(2, 8, 9),                     point(8, 8, 9));


            private Set<Integer> points;

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
                    int x = (boardWidth - 1) / 2 + 1;
                    int y = (boardHeight - 1) / 2 + 1;
                    return Collections.singleton(x + y * boardWidth);
                }

                return Collections.emptySet();
            }
        }
    }

    private static final class StoneRenderer {

        public static void render(GraphicsContext g, GameBoardManager manager) {
            //TODO: Implement later
        }
    }
}
