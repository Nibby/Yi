module yi.component.boardviewer {
    requires yi.core;
    requires yi.component.shared;

    requires javafx.graphics;
    requires org.jetbrains.annotations;

    exports yi.component.board;
    exports yi.component.board.editmodes;
    exports yi.component.board.edits;
}