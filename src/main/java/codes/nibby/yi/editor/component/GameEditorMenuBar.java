package codes.nibby.yi.editor.component;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.editor.layout.LayoutType;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The main toolbar accompanying the editor window.
 * <p>
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
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    itemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP));
                else
                    itemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP));
                itemNew.setOnAction(event -> editor.createDocument());

                MenuItem itemOpen = new MenuItem(lang.getString("menubar.file.open"));
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    itemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP));
                else
                    itemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP));
                itemOpen.setOnAction(event -> editor.openDocument());

                MenuItem itemSave = new MenuItem(lang.getString("menubar.file.save"));
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    itemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP));
                else
                    itemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP));
                itemSave.setOnAction(event -> editor.saveDocument(false));

                MenuItem itemSaveAs = new MenuItem(lang.getString("menubar.file.save_as"));
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    itemSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP));
                else
                    itemSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.DOWN, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP, KeyCombination.ModifierValue.UP));
                itemSaveAs.setOnAction(event -> editor.saveDocument(true));

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
