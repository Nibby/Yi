package yi.editor.framework;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import java.io.PrintWriter;
import java.io.StringWriter;

final class EditorUncaughtExceptionHandler {

    static void initialize() {
        Thread.setDefaultUncaughtExceptionHandler(new Handler());
    }

    private static final class Handler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();

            String message = "It is recommended to restart the program to avoid data loss.\n" +
                    "\n" +
                    "If the problem is reproducible, please consider reporting " +
                    "an issue to the project GitHub repository at: https://github.com/Nibby/Yi and " +
                    "attach the log message from \"Show Details\".";

            String detailsMessage = "Error thread: " + t.getName() + "\n\nStacktrace:\n";

            StringWriter stacktraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stacktraceWriter);
            e.printStackTrace(printWriter);
            detailsMessage += stacktraceWriter.toString();

            BorderPane stackTraceContent = new BorderPane();
            TextArea stackTraceArea = new TextArea();
            stackTraceArea.setText(detailsMessage);
            stackTraceArea.setEditable(false);
            stackTraceContent.setCenter(stackTraceArea);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, message, ButtonType.CLOSE);
            errorAlert.setHeaderText(EditorHelper.getProgramName() + " has encountered an " +
                    "unexpected error.");
            errorAlert.initModality(Modality.APPLICATION_MODAL);
            errorAlert.getDialogPane().setExpandableContent(stackTraceContent);
            errorAlert.show();
        }
    }
}
