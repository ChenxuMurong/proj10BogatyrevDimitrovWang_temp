/*
 * File: Controller.java
 * Names: Philipp Bogatyrev, Anton Dimitrov, Baron Wang
 * Class: CS 361
 * Project 6
 * Date: March 18
 */

package proj6BogatyrevDimitrovWang;


import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * Collection of prebuilt Dialog options
 */
public class DialogOptions {

    /***
     * Dialog box for about menuitem in IDE GUI
     * 
     * @return a Dialog window
     */
    public static Dialog getAboutDialog(){
        // create a new dialog
        Dialog dialog = new Dialog();
        dialog.setContentText("This is a code editor! \n\n "
                + "Authors: Anton Dimitrov, Philipp Bogatyrev, Baron Wang");
        // add a close button so that dialog closing rule is fulfilled
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        return dialog;
    }

    /**
     * Unsaved changes dialog used when compiling, exiting, and
     * closing tabs
     * 
     * @param fileName the file name that is unsaved
     * @param purpose a string explaining why the dialog exists
     * @return a Dialog window to handle button events
     */
    public static Dialog getUnsavedChangesDialog(String fileName, String purpose){
        Dialog dialog = new Dialog();
        // set the prompt text depending on exiting or closing
        String promptText;
        if ("exit".equals(purpose)) {
            promptText = String.format(
                    "Do you want to save %s before exiting?",
                    fileName);
        } else if("close".equals(purpose)){
            promptText = String.format(
                    "Do you want to save %s before closing it?",
                    fileName);
        } else{ //if("compile".equals(purpose)){
            promptText = String.format(
                    "Do you want to save %s before compiling it?",
                    fileName);
        }
        dialog.setContentText(promptText);
        // add Yes, No, Cancel button
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        return dialog;
    }
}
