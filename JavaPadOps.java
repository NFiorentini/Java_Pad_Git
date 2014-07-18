/*
Nicholas Fiorentini
CS211 Summer 2012
Assignment 7, Ch14
JavaPadNAF
29 July 2012
JavaPadOps.java 
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JTextArea;

//My intent was to put all file mechanics in this class, separate from the
//GUI things. This allows different GUIs to be created, perhaps in different
//languages, like for when Macrosoft decides to enter the competitive Luxembourgian
//office software market. Make a GUI in Luxenbourgish and...voila!
public class JavaPadOps{
	
	//Using a static variable for "hardcode.txt" makes it easier to change the
	//destination file later.
	private static final String DEST_FILE="hardcode.txt";
	private boolean fileExist;		
	private boolean fileSaved;
	private boolean fileLoaded;
	private String textStuff="";

	//save() accepts the JTextArea and will save its contents to DEST_File.
	//The success or failure is sent to the setter method for fileSaved. The
	//GUI uses the getter method to determine whether or not to fire off a 
	//showMessageDialog.
	protected void save(JTextArea bigTextArea){
		File f1=new File(DEST_FILE);
		long oldLastModified=f1.lastModified();
		
		try{
			FileWriter writer=new FileWriter(new File(DEST_FILE));
			bigTextArea.write(writer);
			File f2=new File(DEST_FILE);
			
			//Determining if whether a file has been successfully saved is somewhat
			//tricky. I could have a method compare 'before' and 'after' ArrayList<String>s, 
			//char by char. To keep the program lightweight, I opted to simply compare 
			//the time (in milliseconds) from when the file was last changed to the time 
			//after attempting to save it.
			long newLastModified=f2.lastModified();
			if(oldLastModified!=newLastModified){
				setFileSaved(true);
			}
		} 
		catch (IOException saveFailure){
			setFileSaved(false);
		}
	}

	//load() tries to read the destination file and, if it can, uses a setter to make the
	//fileExist field "true." It then loads the text into a temp file, sets fileLoaded to 
	//"true" (via another setter), and then hands the text to the JTextArea in the GUI.
	protected void load(JTextArea bigTextArea){
		try {
			File f=new File(DEST_FILE);
			if(f.canRead()){
				setFileExist(true);
			}
			String tempStuff="";
			Scanner scanInto=new Scanner(new File(DEST_FILE));
			while(scanInto.hasNextLine()){
				tempStuff+=scanInto.nextLine()+"\n";
			}
			textStuff=tempStuff;
			setFileLoaded(true);
		} 
		catch (FileNotFoundException loadFailure) {
			setFileLoaded(false);
		}
		bigTextArea.setText(textStuff);
	}
	
	//Encapsulation follows! Getters are public and setters are private.
	private void setFileExist(boolean fileExist) {
		this.fileExist=fileExist;
	}
	
	protected boolean getFileExist() {
		return fileExist;
	}

	private void setFileSaved(boolean fileSaved) {
		this.fileSaved=fileSaved;
	}

	protected boolean getFileSaved() {
		return fileSaved;
	}

	private void setFileLoaded(boolean fileLoaded) {
		this.fileLoaded=fileLoaded;
	}

	protected boolean getFileLoaded() {
		return fileLoaded;
	}
}

// Witty comment here for GitHub practice!
