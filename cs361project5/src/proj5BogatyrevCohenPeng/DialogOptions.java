/*
 * File: DialogOptions.java
 * Names: Philipp Bogatyrev, Erik Cohen, Ricky Peng
 * Class: CS 361
 * Project 5
 * Date: March 7
 */

package src.proj5BogatyrevCohenPeng;


import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;

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
                + "Authors: Erik Cohen, Philipp Bogatyrev, Ricky Peng");
        // add a close button so that dialog closing rule is fulfilled
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        return dialog;
    }

    public static Dialog getFileTypeDialog(){
        // create a new dialog
        Dialog dialog = new Dialog();
        //Button java = new Button("Java");
        //dialog.getDialogPane().setContent(java);
        dialog.setContentText("Which type of file would you like to create?");
        // add a close button so that dialog closing rule is fulfilled
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Java"));
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Python"));


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
