module YiGameBoardViewer {
    requires YiShared;
    requires javafx.graphics;
    requires org.jetbrains.annotations;
    requires YiCore;

    exports yi.component.board;
    exports yi.component.board.editmodes;
    exports yi.component.board.edits;
}