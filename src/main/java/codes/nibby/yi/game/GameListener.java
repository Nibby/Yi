package codes.nibby.yi.game;

public interface GameListener {

    void gameInitialized(Game game);

    void gameNodeUpdated(Game game, GameNode currentMove, boolean newMove);

    void gameModified(Game game);

}
