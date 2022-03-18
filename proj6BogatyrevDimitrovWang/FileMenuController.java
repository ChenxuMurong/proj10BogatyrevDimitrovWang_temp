package proj6BogatyrevDimitrovWang;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

public class FileMenuController {

    private TabPane tabPane;
    // List of saved tabs and their content
    private HashMap<Tab, String> savedContents = new HashMap<>();
    // List of saved tabs and their saving path
    private HashMap<Tab, String> savedPaths = new HashMap<>();
    // Keep track of the id for new tabs created
    private int newTabID = 1;

    @FXML
    private void handleOpen(ActionEvent event) {
        // create a new file chooser
        FileChooser fileChooser = new FileChooser();
        File initialDir = new File("./saved");
        // handles the case if ./saved directory does not exist
        if (!initialDir.exists()) {
            initialDir = new File("./");
        }
        fileChooser.setInitialDirectory(initialDir);
        List<String> extensionList = Arrays.asList(new String[]{"*.txt", "*.fxml", "*.css", "*.java"});
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", extensionList),
                new FileChooser.ExtensionFilter("FXML Files", extensionList),
                new FileChooser.ExtensionFilter("CSS Files", extensionList),
                new FileChooser.ExtensionFilter("Java Files", extensionList));
        File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        // if user selects a file (instead of pressing cancel button
        if (selectedFile != null) {
            // open a new tab
            this.handleNew(event);
            // set text/name of the tab to the filename
            this.getSelectedTab().setText(selectedFile.getName());
            Tooltip t = new Tooltip(selectedFile.getPath());
            this.getSelectedTab().setTooltip(t);
            this.newTabID--; // no need to increment
            try {
                // reads the file content to a String
                String content = new String(Files.readAllBytes(
                        Paths.get(selectedFile.getPath())));
                this.getSelectedTextBox().replaceText(content);
                // update savedContents field
                this.savedContents.put(getSelectedTab(), content);
                this.savedPaths.put(getSelectedTab(), selectedFile.getPath());

                //getSelectedTextBox().setFile(File(selectedFile.getPath()));

            } catch (Exception e) {
                exceptionAlert(e);
            }
        }
    }

    /**
     * helper function to get the currently selected tab in tabPane
     *
     * @return Tab the selected tab
     */
    private Tab getSelectedTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }



    /**
     * helper function to get the text box in the selected tab
     *
     * @return TextArea the text box in the selected tab
     */
    private CodeArea getSelectedTextBox() {
        Tab currentTab = getSelectedTab();
        VirtualizedScrollPane scrollPane;
        scrollPane = (VirtualizedScrollPane) currentTab.getContent();
        return (CodeArea) scrollPane.getContent();
    }

    /**
     * Helper method that checks if the text in the selected tab is saved.
     *
     * @return boolean whether the text in the selected tab is saved.
     */
    private boolean selectedTabIsSaved() {
        // if tab name has been changed and current text matches with
        // the saved text in record
        if (this.savedContents.containsKey(getSelectedTab()) &&
                getSelectedTextBox().getText().equals(
                        this.savedContents.get(getSelectedTab()))) {
            return true;
        }
        return false;
    }


    /**
     * Helper method that handles unsaved text and closes the tab. Shows a dialog
     * if the text gets modified since its last save or has never been saved.
     * Closes the tab if the text has been saved or user confirms.
     *
     * @param event   An ActionEvent object that gives information about the event
     *                and its source.
     * @param exiting a string used in Dialog modification. represents if the
     *                program
     *                is exiting or simply closing a single tab
     *
     * @return Optional the Optional object returned by dialog.showAndWait().
     *         returns null if tab text is already saved.
     */
    private Optional<ButtonType> closeSelectedTab(ActionEvent event, String exiting) {
        // if content is not saved
        if (!selectedTabIsSaved()) {
            Dialog dialog = DialogOptions.getUnsavedChangesDialog(
                    getSelectedTab().getText(), exiting);

            Optional<ButtonType> result = dialog.showAndWait();
            // call handleSave() if user chooses YES
            if (result.get() == ButtonType.YES) {
                this.handleSave(event);
                // keep the tab if the save is unsuccessful (eg. canceled)
                if (!this.selectedTabIsSaved()) {
                    return result;
                }
            }
            // quit the dialog and keep selected tab if user chooses CANCEL
            else if (result.get() == ButtonType.CANCEL) {
                return result;
            }
        }
        // remove tab from tabPane if text is saved or user chooses NO
        this.savedContents.remove(getSelectedTab());
        this.savedPaths.remove(getSelectedTab());
        tabPane.getTabs().remove(getSelectedTab());
        return Optional.empty();
    }


    @FXML
    private void handleNew(ActionEvent event) {
        // create a new tab
        this.newTabID++;
        Tab newTab = new Tab("Untitled-" + this.newTabID);
        newTab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                handleClose(new ActionEvent());
                event.consume();
            }
        });

        // create a code area
        JavaCodeArea javaCodeArea = new JavaCodeArea();
        CodeArea codeArea = javaCodeArea.getCodeArea();
        newTab.setContent(new VirtualizedScrollPane<>(codeArea));
        // add new tab to the tabPane
        tabPane.getTabs().add(newTab);
        // make the newly created tab the topmost
        tabPane.getSelectionModel().selectLast();
        newTab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tabPane.getSelectionModel().select(newTab);
                handleClose(new ActionEvent());
                event.consume();
            }
        });
    }


    /**
     * Handler method for menu bar item Save. Behaves like Save as... if the text
     * has never been saved before. Otherwise, save the text to its corresponding
     * text file.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        // if the text has been saved before
        if (this.savedContents.containsKey(getSelectedTab())) {
            // create a File object for the corresponding text file
            File savedFile = new File(this.savedPaths.get(getSelectedTab()));
            try {
                // write the new content to the text file
                FileWriter fw = new FileWriter(savedFile);
                fw.write(getSelectedTextBox().getText());
                fw.close();
                // update savedContents field
                this.savedContents.put(getSelectedTab(), getSelectedTextBox().getText());
            } catch (Exception e) {
                exceptionAlert(e);
            }
        }
        // if text in selected tab was not loaded from a file nor ever saved to a file
        else {
            this.handleSaveAs(event);
        }
    }


    /**
     * Helper method to display error message to user when an exception is thrown
     *
     * @type this type is default since main need to use it to print possible exception message
     */
    void exceptionAlert(Exception ex){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.close();
        String exceptionText = sw.toString();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Alert");
        alert.setHeaderText(exceptionText.split("\\s+")[0]);
        alert.setContentText("An exception has been thrown.");
        Label label = new Label("The exception stacktrace is:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Handler method for menu bar item Close. It creates a dialog if
     * the selected tab is unsaved and closes the tab.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    public void handleClose(ActionEvent event) {
        // call helper method
        this.closeSelectedTab(event, "close");
    }

    /**
     * Handler method for menu bar item Save as.... a dialog appears
     * in which the user is asked for the name of the file into which the
     * contents of the current text area are to be saved.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSaveAs(ActionEvent event) {
        // create a new fileChooser
        FileChooser fileChooser = new FileChooser();
        List<String> extensionList = Arrays.asList(new String[]{"*.txt", "*.fxml", "*.css", "*.java"});
        fileChooser.getExtensionFilters().addAll(
                //new FileChooser.ExtensionFilter("Text Files", extensionList));
                new FileChooser.ExtensionFilter("Text Files", extensionList),
                new FileChooser.ExtensionFilter("FXML Files", extensionList),
                new FileChooser.ExtensionFilter("CSS Files", extensionList),
                new FileChooser.ExtensionFilter("Java Files", extensionList));




        File fileToSave = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        // if user did not choose CANCEL
        if (fileToSave != null) {
            try {
                // save file
                FileWriter fw = new FileWriter(fileToSave);
                fw.write(this.getSelectedTextBox().getText());
                fw.close();
                // update savedContents field and tab text
                this.savedContents.put(getSelectedTab(), getSelectedTextBox().getText());
                this.savedPaths.put(getSelectedTab(), fileToSave.getPath());
                this.getSelectedTab().setText(fileToSave.getName());
                Tooltip t = new Tooltip(fileToSave.getPath());
                this.getSelectedTab().setTooltip(t);
            } catch (Exception e) {
                exceptionAlert(e);
            }
        }
    }
}
