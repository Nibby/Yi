package codes.nibby.yi.editor.component;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.editor.perspective.Perspective;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ResourceBundle;

/**
 * The main toolbar accompanying the editor window.
 *
 * TODO: Implement later
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameEditorToolBar extends BorderPane {

    private GameEditorWindow editor;
    private HBox toolbox;
    private PerspectivePane perspectivePane;


    public GameEditorToolBar(GameEditorWindow editor) {
        this.editor = editor;

        toolbox = new HBox();
        perspectivePane = new PerspectivePane(editor);
        setCenter(toolbox);
        setRight(perspectivePane);

        getStyleClass().add("editor_tb_base");
    }

    private static class PerspectivePane extends BorderPane {

        private static final int MENU_WIDTH = 120;
        private MenuButton btnPerspective;
        private Label description;
        private GameEditorWindow editor;

        private PerspectivePane(GameEditorWindow editor) {
            this.editor = editor;
            ResourceBundle lang = Config.getLanguage().getResourceBundle("GameEditorWindow");
//            description = new Label(lang.getString("toolbar.perspective.desc") + ": ");
//            VBox box = new VBox(description);
//            box.setAlignment(Pos.CENTER);
//            setLeft(box);

            Perspective p = Config.getEditorPerspective();
            String currentName = lang.getString(p.getTextKey());
            btnPerspective = new MenuButton(currentName);
            btnPerspective.getItems().addAll(getMenuItems());
            btnPerspective.getStyleClass().add("editor_tb_perspective_btn");
            setCenter(btnPerspective);
        }

        private MenuItem[] getMenuItems() {
            Perspective p = Config.getEditorPerspective();
            ResourceBundle lang = Config.getLanguage().getResourceBundle("GameEditorWindow");
            MenuItem[] items = new MenuItem[Perspective.values().length - 1];
            int added = 0;
            for (int i = 0; i < Perspective.values().length; i++) {
                Perspective pp = Perspective.values()[i];
                if (!pp.equals(p)) {
                    String text = lang.getString(pp.getTextKey());
                    MenuItem item = new MenuItem(text);

                    item.setOnAction(e -> {
                        editor.setPerspective(pp);
                        btnPerspective.setText(text);
                        btnPerspective.getItems().clear();
                        btnPerspective.getItems().addAll(getMenuItems());

                    });
                    items[added] = item;
                    added++;
                }
            }
            return items;
        }
    }
}
