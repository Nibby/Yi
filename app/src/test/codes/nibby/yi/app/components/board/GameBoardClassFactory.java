package codes.nibby.yi.app.components.board;

/**
 * Delegates the instantiation of production code objects so that they don't have to be made
 * public in the production code.
 */
public final class GameBoardClassFactory {

    public static GameBoardManager createGameBoardManager() {
        return new GameBoardManager();
    }

}
