package zigma;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.Event;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class TextEditorController {

    private String ls = System.getProperty("line.separator");
    private File file = null;
    private int searchIndex = 0;
    private String src = null;
    private FontWeight fw;
    private FontPosture fp;
    private MenuItem undoItem, cutItem, copyItem, pasteItem, deleteItem, selectAllItem;
    private List<String> fontList = new ArrayList<>();
    private String[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    private String javaVersion = System.getProperty("java.version");
    private String javafxVersion = System.getProperty("javafx.runtime.version");

    @FXML private TextArea txtArea;
    @FXML private TextField searchBar;

    {
        fontList = Arrays.asList(fonts);
    }

    private void readFromFile(File file) {
        if(file != null) {
            String text = "", buff = "";
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                text = br.readLine();
                while((buff = br.readLine()) != null) {
                    text += ls + buff;
                }
            } catch(IOException ex) {
                new Alert(Alert.AlertType.ERROR, ex.toString(), ButtonType.OK).show();
            }
            txtArea.setText(text);
        }
    }

    private void writeToFile(File file) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            bw.write(txtArea.getText());
        } catch(IOException ex) {
            new Alert(Alert.AlertType.ERROR, ex.toString(), ButtonType.OK).show();
        }
    }

    @FXML
    public void actionPerformed(ActionEvent evt) {
        String btnName = ((Button)evt.getSource()).getText();
        // Common vars
        FileChooser chooser = null;

        if(btnName.equals("Open")) {
            chooser = new FileChooser();
            chooser.setTitle("Open a text file");
            chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            file = chooser.showOpenDialog(null);
            if(file != null) readFromFile(file);
        } else if(btnName.equals("Save")) {
            if(file != null) {
                if(file.exists()) writeToFile(file);
            } else {
                chooser = new FileChooser();
                chooser.setTitle("Save file");
                chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text File", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                file = chooser.showSaveDialog(null);
                if(file != null) if(file.exists()) {
                    writeToFile(file);
                } else {
                    try { file.createNewFile(); }
                    catch(IOException ex) { return; }
                    writeToFile(file);
                }
            }
        } else if(btnName.equals("New")) {
            file = null;
            txtArea.clear();
        } else if(btnName.equals("Font")) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Segoe UI", fontList);
            dialog.setTitle("Font chooser");
            dialog.setHeaderText("Choose font and styles");
            dialog.setContentText("Choose a font: ");

            Label lbl = new Label("Choose a font style: ");
            ComboBox<String> cmb = new ComboBox<>();
            String[] fStyles = { "Bold", "Italic", "Bold Italic", "Normal" };
            cmb.getItems().addAll(fStyles);
            cmb.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent evt) {
                    if(cmb.getValue().equals(fStyles[0])) changeFont(1);
                    else if(cmb.getValue().equals(fStyles[1])) changeFont(2);
                    else if(cmb.getValue().equals(fStyles[2])) changeFont(4);
                    else changeFont(4);
                }
            });

            Label lbl2 = new Label("Choose a font color: ");
            final ColorPicker colorPicker = new ColorPicker();
            colorPicker.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent evt) {
                    txtArea.setStyle("-fx-text-fill: " + String.valueOf(colorPicker.getValue()).replace("0x", "#"));
                }
            });

            Label lbl3 = new Label("Enter font size: ");
            TextField sizer = new TextField("16");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.add(lbl, 0, 0); // col, row
            grid.add(cmb, 1, 0);
            grid.add(lbl3, 0, 1);
            grid.add(sizer, 1, 1);
            grid.add(lbl2, 0, 2);
            grid.add(colorPicker, 1, 2);

            dialog.getDialogPane().setExpandableContent(grid);

            java.util.Optional<String> result = dialog.showAndWait();
            result.ifPresent(font -> { txtArea.setFont(new Font(16).font(font, fw, fp, Double.parseDouble(sizer.getText()))); });
        } else if(btnName.equals("Find")) {
            if(searchBar.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Missing search text");
                alert.setHeaderText("You have missed the finding text");
                alert.setContentText("First you should enter the finding text into the search bar and press the find button!");
                alert.show();
            } else findText();
        } else if(btnName.equals("Replace")) {
            searchIndex = 0;
            if(searchBar.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Missing search text");
                alert.setHeaderText("You have missed the finding text");
                alert.setContentText("First you should enter the finding text into the search bar and press the replace button!");
                alert.show();
            } else {
                findText();
                int car = txtArea.getCaretPosition();
                int anc = txtArea.getAnchor();
                String replaceTxt = null;
                TextInputDialog dialog = new TextInputDialog("Replace text here");
                dialog.setTitle("Replace text dialog");
                dialog.setHeaderText("Find replace box");
                dialog.setContentText("Replace with: ");

                java.util.Optional<String> result = dialog.showAndWait();
                if(result.isPresent()) {
                    replaceTxt = result.get();
                    if(car > anc) txtArea.replaceText(anc, car, replaceTxt);
                    else txtArea.replaceText(car, anc, replaceTxt);
                }
                /** With lambda
                 * result.ifPresent(name -> System.out.println(name)).orElse(System.err.println("name is empty"));
                 */
            }
        } else if(btnName.equals("?")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About Dialog");
            alert.setHeaderText("Text Editor v1.0");
            alert.setContentText("Created by: Damian Rodrigo" + ls + "Written in: Java-" + javaVersion + ls + "JavaFX Version: " + javafxVersion);
            alert.show();
        }
    }

    private void changeFont(int n) {
        switch(n) {
            case 1: fw = FontWeight.BOLD; break;
            case 2: fp = FontPosture.ITALIC; break;
            case 3: fp = FontPosture.ITALIC; fw = FontWeight.BOLD; break;
            case 4: fp = FontPosture.REGULAR; fw = FontWeight.NORMAL; break;
        }
    }

    private void findText() {
        String txt = txtArea.getText().toUpperCase();
        String temp = null;
        temp = searchBar.getText().toUpperCase();
        if(!temp.equals(src)) {
            searchIndex = 0;
            src = temp;
        }
        if(txt.substring(searchIndex + 1).contains(src)) {
            searchIndex = txt.indexOf(src, ++searchIndex);
            System.out.println(searchIndex + " to " + (searchIndex + src.length()));
            txtArea.selectRange(searchIndex + src.length(), searchIndex);
        }
    }

    private void createContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        undoItem = new MenuItem("Undo");
        cutItem = new MenuItem("Cut");
        copyItem = new MenuItem("Copy");
        pasteItem = new MenuItem("Paste");
        deleteItem = new MenuItem("Delete");
        selectAllItem = new MenuItem("Select All");

        undoItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                txtArea.undo();
            }
        });

        cutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                txtArea.cut();
            }
        });

        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                txtArea.copy();
            }
        });

        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                txtArea.paste();
            }
        });

        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                int anc = txtArea.getAnchor();
                int car = txtArea.getCaretPosition();
                if(anc > car) txtArea.deleteText(car, anc);
                else txtArea.deleteText(anc, car);
            }
        });

        selectAllItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                txtArea.selectAll();
            }
        });

        contextMenu.getItems().addAll(undoItem, cutItem, copyItem, pasteItem, deleteItem, selectAllItem);
        txtArea.setContextMenu(contextMenu);
    }

    private void windowActions() {
        javafx.stage.Stage win = (javafx.stage.Stage)txtArea.getScene().getWindow();
        win.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent evt) {
                System.out.println("Hello");
            }
        });
    }

    @FXML
    public void initialize() {
        System.out.println("Java Runtime Version: " + javaVersion);
        System.out.println("JavaFX Runtime Version: " + javafxVersion);

        searchBar.setTooltip(new Tooltip("Case-insensitive search"));
        createContextMenu();
        txtArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if(evt.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    if(!txtArea.isUndoable()) undoItem.setDisable(true);
                    else undoItem.setDisable(false);
                    if(txtArea.getSelectedText().equals("")) {
                        cutItem.setDisable(true);
                        copyItem.setDisable(true);
                        deleteItem.setDisable(true);
                    } else {
                        cutItem.setDisable(false);
                        copyItem.setDisable(false);
                        deleteItem.setDisable(false);
                    }
                }
            }
        });
    }
}
