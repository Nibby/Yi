package yi.editor;


import javafx.application.Application;
import yi.editor.framework.EditorOpenFileHandler;

public class EditorMain {

    public static void main(String[] args) {
        EditorOpenFileHandler.initialize();
        Application.launch(EditorFxMain.class, args);
    }

}
