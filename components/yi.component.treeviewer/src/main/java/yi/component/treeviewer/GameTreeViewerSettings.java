package yi.component.treeviewer;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.i18n.TextResource;

import java.util.Objects;

/**
 * Defines the color palette used to paint the game tree.
 */
public final class GameTreeViewerSettings {

    private Color background = Color.WHITE;
    private Color node = Color.BLACK;
    private Color nodeHover = Color.GRAY;
    private Color nodeInCurrentVariation = Color.BLACK;
    private Color currentNode = Color.BLUE;
    private Color nodeWithCommentary = Color.ORANGE;
    private Color nodePass = null;
    private Color nodeWithStoneEdit = null;
    private Color previewPromptBackground = Color.BLACK;
    private Color previewPromptForeground = Color.WHITE;
    private TextResource previewTextResource = null;
    private Font previewTextFont = new Font(12);
    private boolean previewPromptEnabled = false;

    public static GameTreeViewerSettings getDefault() {
        return new GameTreeViewerSettings();
    }

    public Color getNodeWithCommentaryColor() {
        return nodeWithCommentary != null ? nodeWithCommentary : getNodeColor();
    }

    /**
     * Set the color of nodes that have move commentary.
     * Can be {@code null} to use the same color as {@link #getNodeColor()}.
     *
     * @param color Color of node, nullable
     */
    public void setNodeWithCommentaryColor(@Nullable Color color) {
        this.nodeWithCommentary = color;
    }

    public Color getNodePassColor() {
        return nodePass != null ? nodePass : getNodeColor();
    }

    /**
     * Set the color of nodes that represent a pass.
     * Can be {@code null} to use the same color as {@link #getNodeColor()}.
     *
     * @param color Color of node, nullable
     */
    public void setNodePassColor(@Nullable Color color) {
        this.nodePass = color;
    }

    public Color getNodeWithStoneEditColor() {
        return nodeWithStoneEdit != null ? nodeWithStoneEdit : getNodeColor();
    }

    /**
     * Set the color of nodes that represent stone edits.
     * Can be {@code null} to use the same color as {@link #getNodeColor()}.
     *
     * @param color Color of node, nullable
     */
    public void setNodeWithStoneEditColor(@Nullable Color color) {
        this.nodeWithStoneEdit = color;
    }

    public Color getBackgroundColor() {
        return background;
    }

    /**
     * Set the background color of the tree viewer component.
     * This is the color on which nodes are drawn on.
     *
     * @param color Color of component background
     */
    public void setBackgroundColor(@NotNull Color color) {
        Objects.requireNonNull(color, "Color must not be null");
        this.background = color;
    }

    public Color getNodeColor() {
        return node;
    }

    /**
     * Set the color of ordinary nodes. Must not be null.
     *
     * @param color Color of node
     */
    public void setNodeColor(@NotNull Color color) {
        Objects.requireNonNull(color, "Color must not be null");
        this.node = color;
    }

    public Color getNodeHoverColor() {
        return nodeHover != null ? nodeHover : getNodeColor();
    }

    /**
     * Set the color of the currently hovered node.
     * Can be {@code null} to use the same color as {@link #getNodeColor()}.
     *
     * @param color Color of hovered node, nullable.
     */
    public void setNodeHoverColor(@Nullable Color color) {
        this.nodeHover = color;
    }

    public @NotNull Color getNodeInCurrentVariationColor() {
        return nodeInCurrentVariation != null ? nodeInCurrentVariation : getNodeColor();
    }

    /**
     * Set the color of nodes that are part of the move history of
     * the current game move. This does not set the background color
     * of the current node, use {@link #setCurrentNodeColor(Color)}
     * for that.
     * <p/>
     * Can be {@code null} to use the same color as {@link #getNodeColor()}
     *
     * @param color Color of nodes in the current variation.
     */
    public void setNodeInCurrentVariationColor(@NotNull Color color) {
        this.nodeInCurrentVariation = color;
    }

    public Color getCurrentNodeColor() {
        return currentNode;
    }

    /**
     * Set the color of the current node.
     *
     * @param color Color of node
     */
    public void setCurrentNodeColor(@NotNull Color color) {
        Objects.requireNonNull(color, "Color must not be null");
        this.currentNode = color;
    }

    public Color getPreviewPromptBackground() {
        return previewPromptBackground;
    }

    public void setPreviewPromptBackground(Color previewPromptBackground) {
        this.previewPromptBackground = previewPromptBackground;
    }

    public Color getPreviewPromptForeground() {
        return previewPromptForeground;
    }

    public void setPreviewPromptForeground(Color previewPromptForeground) {
        this.previewPromptForeground = previewPromptForeground;
    }

    public TextResource getPreviewTextResource() {
        return previewTextResource;
    }

    public void setPreviewTextResource(TextResource previewTextResource) {
        this.previewTextResource = previewTextResource;
    }

    public Font getPreviewTextFont() {
        return previewTextFont;
    }

    public void setPreviewTextFont(Font previewTextFont) {
        this.previewTextFont = previewTextFont;
    }

    public boolean isPreviewPromptEnabled() {
        return previewPromptEnabled;
    }

    public void setPreviewPromptEnabled(boolean previewPromptEnabled) {
        this.previewPromptEnabled = previewPromptEnabled;
    }
}
