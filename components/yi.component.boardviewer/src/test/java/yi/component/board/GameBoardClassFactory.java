package yi.component.board;

import yi.component.board.GameBoardManager;
import yi.component.board.GameModelEditor;

/**
 * Delegates the instantiation of production code objects so that they don't have to be made
 * public in the production code.
 */
public final class GameBoardClassFactory {

    public static GameBoardManager createGameBoardManager() {
        return new GameBoardManager();
    }

    public static GameModelEditor createGameModelEditor() {
        return new GameModelEditor();
    }
}
