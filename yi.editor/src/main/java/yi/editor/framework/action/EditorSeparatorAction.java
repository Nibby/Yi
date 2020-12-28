package yi.editor.framework.action;

import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import yi.editor.components.EditorTextResources;

public final class EditorSeparatorAction extends EditorAbstractAction<SeparatorMenuItem, Pane> {

    public EditorSeparatorAction() {
        super(EditorTextResources.EMPTY);
    }

    @Override
    protected @NotNull SeparatorMenuItem getAsMenuItemImpl() {
        return new SeparatorMenuItem();
    }

    @Override
    protected @NotNull Pane getAsComponentImpl() {
        var spacer = new Pane();
        spacer.setPrefWidth(8);
        spacer.setPrefHeight(8);
        return spacer;
    }
}
