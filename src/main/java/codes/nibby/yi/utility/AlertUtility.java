package codes.nibby.yi.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * The JavaFx alternative to swing's JOptionPane.showMessageDialog()
 *
 * @author Kevin Yang
 * Created on 24 August 2019
 */
public class AlertUtility {

    public static void showAlert(String content, String title, Alert.AlertType type, ButtonType ... buttons) {
        Alert alert = new Alert(type, content, buttons);
        alert.setTitle(title);
        alert.show();
    }

}
