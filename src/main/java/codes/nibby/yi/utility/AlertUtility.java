package codes.nibby.yi.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * The JavaFx alternative to swing's JOptionPane.showMessageDialog()
 *
 * @author Kevin Yang
 * Created on 24 August 2019
 */
public class AlertUtility {

    public static Optional<ButtonType> showAlert(String content, String title, Alert.AlertType type, ButtonType... buttons) {
        Alert alert = new Alert(type, content, buttons);
        alert.setTitle(title);
        return alert.showAndWait();
    }

}
