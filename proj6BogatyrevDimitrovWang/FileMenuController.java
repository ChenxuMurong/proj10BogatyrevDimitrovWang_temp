package proj6BogatyrevDimitrovWang;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileMenuController {

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
