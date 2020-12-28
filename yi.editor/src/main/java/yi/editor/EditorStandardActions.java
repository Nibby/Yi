package yi.editor;

import org.jetbrains.annotations.NotNull;
import yi.editor.components.EditorMainMenuType;
import yi.editor.components.EditorTextResources;
import yi.editor.framework.EditorComponent;
import yi.editor.framework.accelerator.EditorAcceleratorId;
import yi.editor.framework.action.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class EditorStandardActions implements EditorComponent<Object> {

    private final ActionHandler handler;
    private final Set<EditorAction> standardActions = new HashSet<>();

    protected EditorStandardActions(@NotNull ActionHandler handler) {
        this.handler = Objects.requireNonNull(handler);

        createNewGameAction();
        createNewGameInNewWindowAction();
        createDivider(EditorMainMenuType.FILE, 0.00055);
        createOpenGameAction();
        createSaveAction();
        createSaveAsAction();
    }

    private void createDivider(EditorMainMenuType menuType, double position) {
        var sep = new EditorSeparatorAction();
        sep.setInMainMenu(menuType, position);
        standardActions.add(sep);
    }

    private void createNewGameAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_NEW_GAME, handler::handleCreateNewGame);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0d);
        actionItem.setAccelerator(EditorAcceleratorId.NEW_GAME);
        standardActions.add(actionItem);
    }

    private void createNewGameInNewWindowAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_NEW_WINDOW, handler::handleCreateGameInNewWindow);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.0005d);
        actionItem.setAccelerator(EditorAcceleratorId.NEW_WINDOW);

        standardActions.add(actionItem);
    }

    private void createOpenGameAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_OPEN_GAME, handler::handleOpenGame);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.001d);
        actionItem.setAccelerator(EditorAcceleratorId.OPEN_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_GAME, handler::handleSaveGame);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.002d);
        actionItem.setAccelerator(EditorAcceleratorId.SAVE_GAME);

        standardActions.add(actionItem);
    }

    private void createSaveAsAction() {
        var actionItem = new EditorBasicAction(EditorTextResources.MENUITEM_SAVE_AS_GAME, handler::handleSaveAsGame);
        actionItem.setInMainMenu(EditorMainMenuType.FILE, 0.003d);
        actionItem.setAccelerator(EditorAcceleratorId.SAVE_AS_GAME);

        standardActions.add(actionItem);
    }

    @Override
    public EditorAction[] getActions(EditorActionManager actionManager) {
        return standardActions.toArray(new EditorAction[0]);
    }

    @Override
    public Optional<Object> getComponent() {
        return Optional.empty();
    }

    interface ActionHandler {

        void handleCreateNewGame(EditorActionContext context);
        void handleCreateGameInNewWindow(EditorActionContext context);
        void handleOpenGame(EditorActionContext context);
        void handleSaveGame(EditorActionContext context);
        void handleSaveAsGame(EditorActionContext context);

    }
}
