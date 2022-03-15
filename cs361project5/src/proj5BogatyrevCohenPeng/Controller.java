/*
 * File: Controller.java
 * Names: Philipp Bogatyrev, Erik Cohen, Ricky Peng
 * Class: CS 361
 * Project 5
 * Date: March 7
 */

package src.proj5BogatyrevCohenPeng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import java.io.Reader;

/**
 * Controller class contains handler methods for buttons and menu items
 *
 */
public class Controller {

    @FXML
    private Button compileButton; // disable when no files

    @FXML
    private Button runButton; // disable when no files

    @FXML
    private Button stopButton; // disable when not running a file

    @FXML
    private TabPane tabPane; // tabPane in the window

    @FXML
    private Tab initialTab; // the initial tab

    @FXML
    private VirtualizedScrollPane<StyleClassedTextArea> consoleScrollPane;

    @FXML
    private StyleClassedTextArea console; // console textarea

    @FXML
    private MenuItem undoMI, redoMI; // menu items

    @FXML
    private MenuItem selectAllMI, cutMI, copyMI, pasteMI; // menu items

    @FXML
    private MenuItem saveMI, saveAsMI, closeMI; // menu items

    @FXML
    private ComboBox comboBox;

    // List of saved tabs and their content
    private HashMap<Tab, String> savedContents = new HashMap<>();
    // List of saved tabs and their saving path
    private HashMap<Tab, String> savedPaths = new HashMap<>();
    // Keep track of the id for new tabs created
    private int newTabID = 1;
    // Fields for managing compiler and console processes
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ProcessBuilder processBuilder = new ProcessBuilder();
    private Boolean cancel_compiler = false;
    private String outStreamCommand = "";

    /**
     * Initialize the first tab so that VirtualizedScrollPanes hold CodeArea in the tab
     */
    public void initializeFirstTab() {
        runButton.setDisable(true);
        compileButton.setDisable(true);
        stopButton.setDisable(true);

        JavaCodeArea javaCodeArea = new JavaCodeArea();
        CodeArea codeArea = javaCodeArea.getCodeArea();
        //CodeArea codeArea = new CodeArea();
        initialTab.setContent(new VirtualizedScrollPane<>(codeArea));

        compileButton.disableProperty().bind(noTabs());
        runButton.disableProperty().bind(noTabs());

        comboBox.getItems().setAll("Java", "Python");
    }

    /**
     * 1. Save dialog for unsaved files
     * 2. compiles the file currently open (unless cancelled)
     *
     * @param event ActionEvent related to javafx
    */
    @FXML
    void handleCompileButton(ActionEvent event) {
        if (getSelectedTab() == null) return; // Does nothing when no tab is open
        cancel_compiler = false;
        if (!selectedTabIsSaved()) {
            Dialog dialog = DialogOptions.getUnsavedChangesDialog(
                    getSelectedTab().getText(), "compile");

            Optional<ButtonType> result = dialog.showAndWait();
            // Call handleSave() if user chooses YES
            if (result.get() == ButtonType.YES) {
                this.handleSave(event);
            }
            // Quit the process if user chooses CANCEL
            else if (result.get() == ButtonType.CANCEL) {
                cancel_compiler = true;
                return;
            }
        }
        File savedFile = new File(this.savedPaths.get(getSelectedTab()));
        try {
            // t.join() blocks thread untill process ends. 
            // Hangs GUI until current command /compile finishes 
            Thread t;
            t = runProcess("pwd");
            t.join(); 

            t = runProcess("cd " + savedFile.getPath().replace(savedFile.getName(),""));
            t.join();

            console.replaceText(
                "cd " + savedFile.getPath().replace(savedFile.getName(),"") 
                + System.lineSeparator());
            System.out.println("**********");
            t = runProcess("javac " + savedFile.getPath());
            t.join();

        } catch (Exception e) {
            Alert alertBox = new Alert(Alert.AlertType.ERROR);
            alertBox.setHeaderText("Process Interrupted");
            alertBox.setContentText(e.toString());
            alertBox.show();
        }
    }

    /**
     * Handler method for stop button
     *
     * Stop the compilation when clicked
     *
     * @param event ActionEvent related to JavaFX
     */
    @FXML
    void handleStopButton(ActionEvent event){
        executor.shutdownNow();
        console.clear();
        console.appendText("Process Interrupted");
        stopButton.setDisable(true);
    }

    /**
     * Handler method for Run button
     *
     * First compiles code then runs code
     * Terminal Output redirected to console
     *
     * @param event ActionEvent related to JavaFX
     */
    @FXML
    void handleRunButton(ActionEvent event) {
        if (getSelectedTab() == null) return;
        handleCompileButton(event);

        if(! cancel_compiler){
            File savedFile = new File(this.savedPaths.get(getSelectedTab()));
            try {
                // prepare classpath location
                int endOfPath = 
                    savedFile.getPath().length() - savedFile.getName().length();

                String pathWithoutFile = savedFile.getPath().substring(0, endOfPath);

                String fileWithoutExtension = 
                    savedFile.getName().substring(0,savedFile.getName().length()-5);

                console.appendText(
                    "java " + "-cp " + pathWithoutFile + " " 
                    + fileWithoutExtension + "\n");
                runProcess("java " + "-cp " + pathWithoutFile + " " + fileWithoutExtension);

            } catch (Exception e) {
                Alert alertBox = new Alert(Alert.AlertType.ERROR);
                alertBox.setHeaderText("Process Interrupted");
                alertBox.setContentText(e.toString());
                alertBox.show();
            }
        }
    }

    /**
     * public method that calls handleExit() and can be accessed
     * by the Main class
     */
    public void handleWindowExit() {
        handleExit(new ActionEvent());
    }

    /**
     * Sets up listeners to disable/enable menu items +
     * connects existing exit buttons to the created close / exit MenuItems
     */
    @FXML
    private void initialize() {
        // handles clicking "x" for initial tab and primary window

        tabPane.getTabs().get(0).setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                tabPane.getSelectionModel().select(0);
                handleClose(new ActionEvent());
                event.consume();
            }
        });

        // set disable property when no tabs are open
        closeMI.disableProperty().bind(noTabs());
        saveMI.disableProperty().bind(noTabs());
        saveAsMI.disableProperty().bind(noTabs());
        undoMI.disableProperty().bind(noTabs());
        redoMI.disableProperty().bind(noTabs());
        selectAllMI.disableProperty().bind(noTabs());
        cutMI.disableProperty().bind(noTabs());
        copyMI.disableProperty().bind(noTabs());
        pasteMI.disableProperty().bind(noTabs());
    }

    /**
     * returns a new BooleanBinding that holds true if
     * there is no tab in TabPane
     */
    private BooleanBinding noTabs() {
        return Bindings.isEmpty(tabPane.getTabs());
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
     * Handler method for menu bar item About. It shows a dialog that contains
     * program information.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        Dialog dialog = DialogOptions.getAboutDialog();
        dialog.showAndWait();
    }

    /**
     * Handler method for menu bar item New. It creates a new tab and adds it
     * to the tabPane.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
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
     * Handler method for menu bar item Open. It shows a dialog and let the user
     * select a text file to be loaded to the text box.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
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
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("FXML Files", "*.fxml"),
                new FileChooser.ExtensionFilter("CSS Files", "*.css"),
                new FileChooser.ExtensionFilter("Java Files", "*.java"));
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

            } catch (IOException e) {
                Alert alertBox = new Alert(Alert.AlertType.ERROR);
                alertBox.setHeaderText("File Opening Error");
                alertBox.setContentText(e.toString());
                alertBox.show();
            }
        }
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

    /**
     * Handler method for menu bar item Exit. When exit item of the menu
     * bar is clicked, the application quits if all tabs in the tabPane are
     * closed properly.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        // start while loop iteration from the last tab
        tabPane.getSelectionModel().selectLast();
        // loop through the tabs in tabPane
        while (tabPane.getTabs().size() > 0) {
            // try close the currently selected tab
            Optional<ButtonType> result = closeSelectedTab(event, "exit");
            // if the user chooses Cancel at any time, then the exiting is canceled,
            // and the application stays running.
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        // exit if all tabs are closed
        System.exit(0);
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
            } catch (IOException e) {
                Alert alertBox = new Alert(Alert.AlertType.ERROR);
                alertBox.setHeaderText("File Saving Error");
                alertBox.setContentText("File was not saved successfully.");
                alertBox.show();
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
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("FXML Files", "*.fxml"),
                new FileChooser.ExtensionFilter("CSS Files", "*.css"),
                new FileChooser.ExtensionFilter("Java Files", "*.java"));
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
            } catch (IOException e) {
                Alert alertBox = new Alert(Alert.AlertType.ERROR);
                alertBox.setHeaderText("File Saving Error");
                alertBox.setContentText("File was not saved successfully.");
                alertBox.show();
            }
        }
    }

    /**
     * Handler method for menu bar item Undo.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleUndo(ActionEvent event) {
        getSelectedTextBox().undo();
    }

    /**
     * Handler method for menu bar item Redo.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleRedo(ActionEvent event) {
        getSelectedTextBox().redo();
    }

    /**
     * Handler method for menu bar item Cut.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCut(ActionEvent event) {
        getSelectedTextBox().cut();
    }

    /**
     * Handler method for menu bar item Copy.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleCopy(ActionEvent event) {
        getSelectedTextBox().copy();
    }

    /**
     * Handler method for menu bar item Paste.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handlePaste(ActionEvent event) {
        getSelectedTextBox().paste();
    }

    /**
     * Handler method for menu bar item Select all.
     *
     * @param event An ActionEvent object that gives information about the event
     *              and its source.
     */
    @FXML
    private void handleSelectAll(ActionEvent event) {
        getSelectedTextBox().selectAll();
    }

    /**
     * helper method that starts processes in new thread and
     * reads terminal output
     *
     * @param command The command of the process being run
     * @throws Exception
     */
    private Thread runProcess(String command) throws Exception {
        processBuilder.command("bash", "-c", command).redirectErrorStream(true);
        Boolean compilingCommand = command.startsWith("javac");
        // new thread to read process results
        Thread t = new Thread(new Runnable() {
            // start process and read from input stream
            public void run(){
                stopButton.setDisable(false);
                Boolean compilerError = false;
                Process process;
                try {
                    process = processBuilder.start();
                    OutputStream outStream = process.getOutputStream();
                    // output stream
                    console.setOnKeyTyped(new EventHandler<>(){  
                        String result = "";  
                        @Override
                        public void handle(KeyEvent event) {
                            result += event.getCharacter();
                            if (event.getCharacter().equals("\r")) {
                                try {
                                    for (char c : result.toCharArray()) {
                                        outStream.write(c);
                                    }
                                    outStream.flush();
                                    result = "";
                                } catch (IOException e) {
                                    Alert alertBox = new Alert(Alert.AlertType.ERROR);
                                    alertBox.setHeaderText("Command Line Error");
                                    alertBox.setContentText(e.toString());
                                    alertBox.show();
                                }
                            }
                        }
                    });
                    while(process.isAlive()){
                        // input stream
                        Reader reader = new InputStreamReader(process.getInputStream());
                        // buffer for efficiency
                        Reader buffer = new BufferedReader(reader);
                        int r;
                        while ((r = buffer.read()) != -1) {
                            compilerError = true;
                            char ch = (char) r;
                            String s = Character.toString(ch);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        console.appendText(s);
                                    }
                                });
                        }
                    }
                    if(compilingCommand){
                        if(!compilerError){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                console.appendText("Compilation Was Succesful!\n");
                                }
                            });
                        }//compilerError
                    } //compilingCommand
                } catch (IOException e) {
 
                    Alert alertBox = new Alert(Alert.AlertType.ERROR);
                    alertBox.setHeaderText("Command Line Error");
                    alertBox.setContentText( e.toString() );
                    alertBox.show();
                }
            }// run
        });// runnable

        t.start();
        stopButton.setDisable(true);
        return t;
    }
}
