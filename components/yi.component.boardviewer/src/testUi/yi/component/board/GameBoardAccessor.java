package yi.component.board;

public final class GameBoardAccessor {

    private GameBoardAccessor() {

    }

    public static GameBoardManager getManager(GameBoardViewer viewer) {
        return viewer.getManager();
    }

}
