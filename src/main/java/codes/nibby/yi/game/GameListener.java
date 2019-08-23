package codes.nibby.qipan.game;

public interface GameListener {

    void gameInitialized(Game game);
    void gameCurrentMoveUpdate(GameNode currentMove);

}
