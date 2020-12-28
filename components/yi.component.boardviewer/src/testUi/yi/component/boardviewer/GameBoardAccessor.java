package yi.component.boardviewer;

public final class GameBoardAccessor {

    private GameBoardAccessor() {

    }

    public static GameBoardManager getManager(GameBoardViewer viewer) {
        return viewer.getManager();
    }

}
