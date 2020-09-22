package zigma;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TextEditor extends Application {

    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("text_editor_sketch.fxml"));
            loader.setController(new TextEditorController());

            Parent parentRoot = loader.load();
            stage.setScene(new Scene(parentRoot));
            stage.setTitle("Text Editor");
            stage.show();
        } catch(Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error message");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText(ex.getMessage());

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            Label lbl = new Label("The exception stack trace was:");
            TextArea txtArea = new TextArea(sw.toString());
            txtArea.setEditable(false);
            txtArea.setWrapText(true);

            txtArea.setMaxWidth(Double.MAX_VALUE);
            txtArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(txtArea, Priority.ALWAYS);
            GridPane.setHgrow(txtArea, Priority.ALWAYS);

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.add(lbl, 0, 0);
            grid.add(txtArea, 0, 1); // columnIndex, rowIndex

            alert.getDialogPane().setExpandableContent(grid);
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
