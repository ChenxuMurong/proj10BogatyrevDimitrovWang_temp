# cs361project5

CS 361, Spring 2022
Project 5
Enhance the IDE so that it will compile and run Java programs
assigned: Tuesday, March 1
due: Monday, March 7, at 11:59pm (7 days)
Assignment

There are two main parts to this project: 
Replace the two buttons in the Toolbar with three buttons so that when the first is clicked a Java program is compiled, when the second is clicked a Java program is compiled and run, and when the third is clicked, a running Java program is halted.
Create a console pane where you can type in input and where you can see output when the Java program is running.
Details for the Toolbar

Replace the two buttons in the toolbar with 3 new buttons: "Compile", "Compile & Run", and "Stop". The first should have a pale blue background, the second should have a pale green background, and the third should have a pale red background.
When the user clicks the Compile button, the Java code in the selected tab should be compiled by the javac compiler.  If errors occur during compilation,  the error messages should be displayed in the console pane.  If no errors occur, the console pane should display a message like "Compilation was successful."
When the user clicks the Compile & run button, your IDE should behave the same as if the Compile button were clicked, except that, if the compilation is successful, the newly compiled program is then run using the same java interpreter that runs your Java programs.
If the Compile button or Compile & Run button is clicked when the selected tab has unsaved changes, a dialog should appear asking the user whether they want to save the changes first.  There should be Yes, No, and Cancel buttons in the dialog.  If Yes is clicked, the tab is saved and then compilation is done.  If No is clicked, the currently saved version of the selected tab is compiled without the unsaved changes.  If Cancel is clicked, no compilation or execution is performed.
Note that if the Java program you are editing in your IDE is divided among several files and you want to compile and test your latest changes to those files, you'll need to compile all of them--one at a time--and then compile and run the main class.
When the Stop button is clicked, any current Java compilation or execution is halted. This button is mainly needed when the Java compiler or the program you are running is taking too long or is stuck in an infinite loop.
To run the javac compiler and java interpreter, you will want to create new Processes using the ProcessBuilder class.  Start such processes executing in a new thread to ensure that your GUI doesn't freeze while the compilation and execution are being performed.
Just like you already did with menu items, make sure to disable all buttons in the toolbar whenever it is not appropriate to click them. For example, if there are no open tabs, then the Compile and the Compile & Run buttons should be disabled. Similarly, if the javac and the java processes are not running, the Stop button should be disabled.
You are welcome to implement your project so that at most one Process is running at any given time. In that case, be sure to disable the Compile and Compile & Run buttons until the Process is finished.
Details for the Console

Add to the bottom of your IDE window a "console" pane.  Standard input and standard output is performed in that pane during the execution of your Java program.  For example, if the Java program has the statement System.out.print("Hello") then "Hello" should be printed to the console pane.
The console pane should be a StyleClassedTextArea, a class that is declared in the same RichTextFX jar file that declares CodeArea.  This way, we can later add styling to the contents of the console pane to make it more readable.
To redirect standard input and output to/from the console, use either the ProcessBuilder's redirectInput and redirectOutput methods or get the InputStream and OutputStreams of the Process and use them to allow the user to interact with the console pane.
Odds and Ends

Make sure all keyboard shortcuts for the menu items are displayed in the menu items' text.
Make sure there is an easy way for the user to tell exactly which file is being edited in each tab. For example, you could use tooltips.