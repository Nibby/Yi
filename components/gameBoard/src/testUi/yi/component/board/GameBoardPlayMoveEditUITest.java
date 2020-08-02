package yi.component.board;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import yi.core.go.GameRules;

@ExtendWith(ApplicationExtension.class)
public final class GameBoardPlayMoveEditUITest extends GameBoardUITestBase {



    @Override
    protected int getBoardWidth() {
        return 19;
    }

    @Override
    protected int getBoardHeight() {
        return 19;
    }

    @Override
    protected GameRules getGameRules() {
        return GameRules.CHINESE;
    }
}
