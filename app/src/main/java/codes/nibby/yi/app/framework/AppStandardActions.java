package codes.nibby.yi.app.framework;

import codes.nibby.yi.app.components.AppComponent;
import codes.nibby.yi.app.components.AppMainMenuType;
import codes.nibby.yi.app.framework.action.*;
import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.app.framework.modal.ModalActionButton;
import codes.nibby.yi.app.framework.modal.YiModalAlertPane;
import codes.nibby.yi.models.GameModel;
import codes.nibby.yi.models.GameNode;
import codes.nibby.yi.models.editor.GameModelEditor;
import codes.nibby.yi.models.editor.edit.MoveEdit;
import codes.nibby.yi.models.editor.edit.RemoveNodeEdit;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class AppStandardActions implements AppComponent<Object> {

    private final ActionHandler handler;
    private final Set<AppAction> standardActions = new HashSet<>();

    AppStandardActions(@NotNull ActionHandler handler) {
        this.handler = Objects.requireNonNull(handler);

        createNewGameAction();
        createNewGameInNewWindowAction();
        createDivider(AppMainMenuType.FILE, 0.00055);
        createOpenGameAction();
        createSaveAction();
        createSaveAsAction();
        createDivider(AppMainMenuType.FILE, 0.8d);

        createDivider(AppMainMenuType.EDIT, 0.0999d);
        createPassAction();
        createDivider(AppMainMenuType.EDIT, 0.1001d);
        createRemoveNodeAction();
    }

    private void createRemoveNodeAction() {
        var actionItem = new AppBasicAction(AppText.REMOVE_NODE, context -> {
            var window = context.getInvokerWindow();
            GameModel gameModel = window.getGameModel();
            GameModelEditor editor = gameModel.getEditor();
            GameNode node = gameModel.getCurrentNode();
            if (!node.isRoot()) {
                if (node.isLastMoveInThisVariation()) {
                    editor.recordAndApplyUndoable(new RemoveNodeEdit(node));
                } else {
                    var modal = new YiModalAlertPane("Confirm Deletion",
                            "Deleting this move will remove all subsequent variations.\n\nWould you like to continue?");
                    var buttons = new ModalActionButton[] {
                            ModalActionButton.createOkayButton(),
                            ModalActionButton.createCancelButton()
                    };
                    modal.setActionButtons(buttons);
                    modal.setDefaultControlButton(buttons[1]); // The cancel button
                    modal.setCloseCallback(button -> {
                        if (button == buttons[0]) {
                            editor.recordAndApplyUndoable(new RemoveNodeEdit(node));
                        }
                        return true;
                    });
                    modal.setPrefSize(420, 180);
                    window.pushModalContent(modal);
                }
            }
        });
        actionItem.setInMenuBar(AppMainMenuType.EDIT, 0.11d);
        actionItem.setAccelerator(AppAccelerator.REMOVE_NODE);
        standardActions.add(actionItem);
    }

    private void createPassAction() {
        var actionItem = new AppBasicAction(AppText.PASS, context -> {
            var window = context.getInvokerWindow();
            GameModel gameModel = window.getGameModel();
            GameModelEditor editor = gameModel.getEditor();
            editor.recordAndApplyUndoable(MoveEdit.Companion.pass());
        });
        actionItem.setInMenuBar(AppMainMenuType.EDIT, 0.1d);
        actionItem.setAccelerator(AppAccelerator.PASS);
        standardActions.add(actionItem);
    }

    private void createDivider(AppMainMenuType menuType, double position) {
        var sep = new AppSeparatorAction();
        sep.setInMenuBar(menuType, position);
        standardActions.add(sep);
    }

    private void createNewGameAction() {
        var actionItem = new AppBasicAction(AppText.MENUITEM_NEW_GAME, handler::handleCreateNewGame);
        actionItem.setInMenuBar(AppMainMenuType.FILE, 0d);
        actionItem.setAccelerator(AppAccelerator.NEW_GAME);
        standardActions.add(actionItem);
    }

    private void createNewGameInNewWindowAction() {
        var actionItem = new AppBasicAction(AppText.MENUITEM_NEW_WINDOW, handler::handleCreateGameInNewWindow);
        actionItem.setInMenuBar(AppMainMenuType.FILE, 0.0005d);
        actionItem.setAccelerator(AppAccelerator.NEW_WINDOW);

        standardActions.add(actionItem);
    }

    private void createOpenGameAction() {
        var actionItem = new AppBasicAction(AppText.MENUITEM_OPEN_GAME, handler::handleOpenGame);
        actionItem.setInMenuBar(AppMainMenuType.FILE, 0.001d);
        actionItem.setAccelerator(AppAccelerator.OPEN_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAction() {
        var actionItem = new AppBasicAction(AppText.MENUITEM_SAVE_GAME, handler::handleSaveGame);
        actionItem.setInMenuBar(AppMainMenuType.FILE, 0.002d);
        actionItem.setAccelerator(AppAccelerator.SAVE_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAsAction() {
        var actionItem = new AppBasicAction(AppText.MENUITEM_SAVE_AS_GAME, handler::handleSaveAsGame);
        actionItem.setInMenuBar(AppMainMenuType.FILE, 0.003d);
        actionItem.setAccelerator(AppAccelerator.SAVE_AS_GAME);

        standardActions.add(actionItem);
    }

    @Override
    public AppAction[] getActions(AppActionManager actionManager) {
        return standardActions.toArray(new AppAction[0]);
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }

    interface ActionHandler {

        void handleCreateNewGame(AppActionContext context);
        void handleCreateGameInNewWindow(AppActionContext context);
        void handleOpenGame(AppActionContext context);
        void handleSaveGame(AppActionContext context);
        void handleSaveAsGame(AppActionContext context);

    }
}
