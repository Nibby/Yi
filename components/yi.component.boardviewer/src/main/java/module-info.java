module yi.component.boardviewer {
    requires yi.core;
    requires yi.component.shared;

    requires javafx.graphics;
    requires org.jetbrains.annotations;
    requires commons.lang3;

    exports yi.component.boardviewer;
    exports yi.component.boardviewer.editmodes;
}