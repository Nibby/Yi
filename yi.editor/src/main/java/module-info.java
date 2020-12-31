module yi.editor {

    requires yi.component.shared;
    requires org.jetbrains.annotations;
    requires javafx.controls;
    requires javafx.graphics;
    requires yi.component.boardviewer;
    requires yi.component.treeviewer;
    requires yi.component.commentviewer;
    requires yi.core;
    requires json;

    exports yi.editor;

    opens yi.editor.icons;
    opens yi.editor.fonts;
    opens yi.editor.i18n;

}