package codes.nibby.yi.game;

public interface GameListener {

    void gameInitialized(Game game);
    void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove);

}
