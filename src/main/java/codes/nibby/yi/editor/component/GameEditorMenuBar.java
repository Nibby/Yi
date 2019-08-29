package codes.nibby.yi.editor.component;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.editor.layout.LayoutType;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The main toolbar accompanying the editor window.
 *
 * TODO: Implement later
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameEditorMenuBar extends BorderPane {

    private GameEditorWindow editor;
    private MenuBar menuBar;

    public GameEditorMenuBar(GameEditorWindow editor) {
        this.editor = editor;

        menuBar = new MenuBar();
        menuBar.getStyleClass().add("editor_tb_menubar");
        // Sets up the menu bar
        {
            ResourceBundle lang = Config.getLanguage().getResourceBundle("GameEditorWindow");
            Menu menuFile = new Menu(lang.getString("menubar.file"));
            {
                // TODO: Implement functionality later
                MenuItem itemNew = new MenuItem(lang.getString("menubar.file.new"));
                MenuItem itemOpen = new MenuItem(lang.getString("menubar.file.open"));
                MenuItem itemSave = new MenuItem(lang.getString("menubar.file.save"));
                MenuItem itemSaveAs = new MenuItem(lang.getString("menubar.file.save_as"));

                menuFile.getItems().addAll(itemNew, itemOpen, itemSave, itemSaveAs);
            }

            Menu menuEdit = new Menu(lang.getString("menubar.edit"));

            Menu menuView = new Menu(lang.getString("menubar.view"));
            {
                Menu menuLayouts = new Menu(lang.getString("menubar.view.layouts"));
                menuView.getItems().add(menuLayouts);
                List<CheckMenuItem> items = new ArrayList<>();

                for (LayoutType p : LayoutType.values()) {
                    String text = lang.getString(p.getTextKey());
                    CheckMenuItem menuItem = new CheckMenuItem(text);
                    menuItem.setSelected(Config.getEditorLayout().equals(p));
                    menuLayouts.getItems().add(menuItem);
                    items.add(menuItem);

                    menuItem.setOnAction(e -> {
                        editor.setPerspective(p);

                        items.forEach(item -> {
                            item.setSelected(false);
                            menuItem.setSelected(true);
                        });
                    });
                }

                menuView.getItems().add(new SeparatorMenuItem());

                CheckMenuItem chkGameTree = new CheckMenuItem(lang.getString("menubar.view.game_tree"));
                chkGameTree.setOnAction(value -> editor.getLayout().setGameTreePaneVisible(chkGameTree.isSelected()));

                CheckMenuItem chkMoveComments = new CheckMenuItem(lang.getString("menubar.view.move_comments"));
                chkMoveComments.setOnAction(value -> editor.getLayout().setGameCommentPaneVisible(chkMoveComments.isSelected()));
                menuView.getItems().addAll(chkGameTree, chkMoveComments);
            }

            Menu menuHelp = new Menu(lang.getString("menubar.help"));
            {
                MenuItem itemAbout = new MenuItem(lang.getString("menubar.help.about"));
                MenuItem itemSettings = new MenuItem(lang.getString("menubar.help.settings"));

                menuHelp.getItems().addAll(itemAbout, itemSettings);
            }

            menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuHelp);
        }

//        perspectivePane = new PerspectivePane(editor);
        setCenter(menuBar);
//        setRight(perspectivePane);

        getStyleClass().add("editor_tb_base");
    }
}
