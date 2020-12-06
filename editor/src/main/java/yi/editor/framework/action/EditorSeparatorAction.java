package yi.editor.framework.action;

import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.editor.EditorTextResources;

public class EditorSeparatorAction extends EditorAbstractAction<SeparatorMenuItem, Separator> {

    public EditorSeparatorAction() {
        super(EditorTextResources.EMPTY, null);
    }

    @Override
    protected @NotNull SeparatorMenuItem getAsMenuItemImpl() {
        return new SeparatorMenuItem();
    }

    @Override
    protected @Nullable Separator getAsComponentImpl() {
        return new Separator();
    }

}
