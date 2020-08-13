package yi.component.gametree;

import javafx.scene.paint.Color;

public final class GameTreeViewerSettings {

    public GameTreeViewerSettings() { }

    private Color background = Color.WHITE;
    private Color node = Color.BLACK;
    private Color nodeHover = Color.GRAY;
    private Color nodeInCurrentVariation = Color.BLACK;
    private Color currentNode = Color.BLUE;

    public static GameTreeViewerSettings getDefault() {
        return new GameTreeViewerSettings();
    }

    public Color getBackgroundColor() {
        return background;
    }

    public void setBackgroundColor(Color background) {
        this.background = background;
    }

    public Color getNodeColor() {
        return node;
    }

    public void setNodeColor(Color node) {
        this.node = node;
    }

    public Color getNodeHoverColor() {
        return nodeHover;
    }

    public void setNodeHoverColor(Color nodeHover) {
        this.nodeHover = nodeHover;
    }

    public Color getNodeInCurrentVariationColor() {
        return nodeInCurrentVariation;
    }

    public void setNodeInCurrentVariationColor(Color nodeInCurrentVariation) {
        this.nodeInCurrentVariation = nodeInCurrentVariation;
    }

    public Color getCurrentNodeColor() {
        return currentNode;
    }

    public void setCurrentNodeColor(Color currentNode) {
        this.currentNode = currentNode;
    }
}
