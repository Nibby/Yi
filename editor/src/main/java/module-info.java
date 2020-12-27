module YiEditor {

    requires YiShared;
    requires org.jetbrains.annotations;
    requires javafx.controls;
    requires YiGameBoardViewer;
    requires YiGameTreeViewer;
    requires YiCommentViewer;
    requires YiCore;

    exports yi.editor;

    opens icons;
    opens fonts;
}