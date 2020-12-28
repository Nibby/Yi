module yi.editor {

    requires yi.component.shared;
    requires org.jetbrains.annotations;
    requires javafx.controls;
    requires yi.component.boardviewer;
    requires yi.component.treeviewer;
    requires yi.component.commentviewer;
    requires yi.core;
    requires json;

    exports yi.editor;

    opens icons;
    opens fonts;
}