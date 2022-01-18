package codes.nibby.yi.app.framework.action;

import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.app.framework.AppText;

public final class AppSeparatorAction extends AppAbstractAction<SeparatorMenuItem, Pane> {

    public AppSeparatorAction() {
        super(AppText.EMPTY);
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
