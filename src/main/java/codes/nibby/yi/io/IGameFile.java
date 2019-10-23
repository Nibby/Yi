package codes.nibby.yi.io;

import codes.nibby.yi.game.Game;

public interface IGameFile {

    /**
     * Converts format-specific data into a usable Game object. This process is resource intensive.
     *
     * @return A game object constructed from data in the game file.
     */
    Game createGame();
}
