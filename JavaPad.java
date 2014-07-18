/*
Nicholas Fiorentini
CS211 Summer 2012
Assignment 7, Ch14
JavaPadNAF
29 July 2012
JavaPad.java 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

//JavaPadNAF is a small word processing program, akin to a digital Post-It
//note. The program can save the text to a .txt file, be closed, reopened,
//and the original text displayed. This version will start a new line for a
//word that won't fit on its current line and includes a right-click menu
//for editing functions (which was incredibly frustrating to get working
//correctly).
//In doing this assignment, I tried to keep up the scenario of coding something
//with the idea in mind that changes could be made easily down the road.
//I would be understating things to say that I've learned a lot on this 
//assignment.
public class JavaPad{

	//BJP, page 849 recommends putting main into the GUI class, above
	//any fields, constructors, and instance methods.
	public static void main(String args[]){
		JavaPad pad=new JavaPad();
		pad.go();
	}

	//Instead of using frame.pack(), I opted to use static variables 
	//declared at the top to allow the size to be changed easily for 
	//JavaPadNAF, v.2.0. As far as I can tell, it displays exactly the
	//same as the JavaPadWPI example.
	private static final int HEIGHT=385;
	private static final int WIDTH=300;
	private static final int TEXT_AREA_WIDTH=15;
	private static final int TEXT_AREA_HEIGHT=25;
	private static final int FONT_SIZE_FIELD=5;
	private JButton newButton;
	private JButton saveButton;
	private JButton loadButton;
	private JButton quitButton;
	private JButton foregroundButton;
	private JButton backgroundButton;
	private JTextArea bigTextArea;
	private JTextField fontSize;   
	private JFrame frame;

	//These are the fields needed to implement a right-click menu with editing
	//options.
	private enum Actions{UNDO, CUT, COPY, PASTE, SELECT_ALL};
	private JPopupMenu popup;
	private Action cutAction;
	private Action copyAction;
	private Action pasteAction;
	private Action undoAction;
	private Action selectAllAction;
	private JTextComponent textComponent;
	private String savedString="";
	private Actions lastActionSelected;

	//Let's get GUI with it!
	private void go(){
		this.popup=new JPopupMenu();
		this.newButton=new JButton("New");
		this.saveButton=new JButton("Save");
		this.loadButton=new JButton("Load");
		this.quitButton=new JButton("Quit");
		this.foregroundButton=new JButton("Foreground");
		this.backgroundButton=new JButton("Background");
		this.fontSize=new JTextField(FONT_SIZE_FIELD);
		this.bigTextArea=new JTextArea(TEXT_AREA_WIDTH,TEXT_AREA_HEIGHT);

		//Create the frame. Java's default layout manager is BorderLayout(). 
		this.frame=new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(WIDTH,HEIGHT);
		this.frame.setTitle("Macrosoft JavaPad XP");
		//this.frame.setLayout(new BorderLayout());

		//Create the 4 buttons at the top for "New," "Save," "Load," and "Quit."
		JPanel topButtons=new JPanel(new GridLayout(1,4));
		topButtons.add(newButton);
		topButtons.add(saveButton);
		topButtons.add(loadButton);
		topButtons.add(quitButton);

		//Create the "Foreground" and "Background" buttons, the "Font Size: "
		//label, and a text field for entering a font size.
		JPanel bottom=new JPanel(new BorderLayout());
		JPanel bottomButtons=new JPanel(new FlowLayout());
		JPanel fontStuff=new JPanel(new FlowLayout());
		bottomButtons.add(foregroundButton);
		bottomButtons.add(backgroundButton);
		fontStuff.add(new JLabel("Font Size: "));
		fontStuff.add(fontSize);
		bottom.add(bottomButtons,BorderLayout.NORTH);
		bottom.add(fontStuff,BorderLayout.SOUTH);

		//JavaPadWPI will go to a new line before the end of the word. That was ann
		//oying. However, JavaPadNAF will start the word on a new line if it doesn't 
		//fit.
		this.bigTextArea.setLineWrap(true);
		this.bigTextArea.setWrapStyleWord(true);

		//Hook up the buttons and JTextField to their ActionListers.
		this.newButton.addActionListener(new NewListener());
		this.saveButton.addActionListener(new SaveListener());
		this.loadButton.addActionListener(new LoadListener());
		this.quitButton.addActionListener(new QuitListener());
		this.fontSize.addActionListener(new FontSizeListener());
		this.foregroundButton.addActionListener(new FGListener());
		this.backgroundButton.addActionListener(new BGListener());

		//Hook up the JTextArea to the MouseListener
		this.bigTextArea.addMouseListener(new RightClickMenuMouseListener());

		//Put everything together now.
		this.frame.add(bigTextArea);
		this.frame.add(topButtons,BorderLayout.NORTH);
		this.frame.add(bottom,BorderLayout.SOUTH);
		this.frame.add(new JScrollPane(bigTextArea));
		this.frame.setVisible(true);
	}

	//I wanted all graphics things handled in this class, thus error codes are passed
	//into this method and the appropriate error message is displayed.
	private void errorMsgs(int argCode){
		switch(argCode){
		case 0:
			JOptionPane.showMessageDialog(frame,"Could not save to file hardcode.txt", 
					"I/O Error",0);
			break;
		case 1:
			JOptionPane.showMessageDialog(frame,"Could not access file hardcode.txt", 
					"I/O Error",0);
			break;
		case 2:
			JOptionPane.showMessageDialog(frame,"Invalid font size entered.", 
					"I/O Error",2);
			break;

			//There is no "case 42." This is a place holder for if an additional error
			//message is needed in the future.
		case 42:
			JOptionPane.showMessageDialog(frame,"ID 10T Error","System Caffeine Level Is Low",0);
			break;
		}
	}
	

	/* I could've used "public void actionPerformed(ActionEvent e) {...}" and a bunch 
	 * of if/else if statements, however Head First Java page 373 notes that, "It's not very 
	 * OO...One event handler doing many different things means that you have a single 
	 * method doing many different things. If you need to change how one source is handled, 
	 * you have to mess with everybody's event handler...It hurts maintainability and 
	 * extensibility." Their solution: inner classes. Additionally, I think that these inner
	 * classes are far easier to read than the avalanche of if/else ifs this program required.
	 */

	
	//The content of the JTextArea is sent to the save method in JavaPadOps. It then checks
	//the fileSaved field (via the getter method) and, if that field is still false, "0" is
	//passed to the errorMsgs() method in the JavaPadNAF class.
	private class SaveListener extends JavaPadOps implements ActionListener{
		public void actionPerformed(ActionEvent event){
			this.save(bigTextArea);
			if(this.getFileSaved()==false){
				errorMsgs(0);
			}
		}
	}

	//The JTextArea is passed to the load() method in JavaPadOps and the contents of 
	//"hardcode.txt" are placed onto it. Then the actionPerformed method checks the
	//fileLoaded and fileExist fields and, if either return false, "1" is passed to 
	//the errorMsgs() method.
	private class LoadListener extends JavaPadOps implements ActionListener{
		public void actionPerformed(ActionEvent event){
			this.load(bigTextArea);
			if(!(this.getFileLoaded()|| this.getFileExist()) ){
				errorMsgs(1);
			}
		}
	}

	//When the user selects "Quit," a showConfirmDialog box is displayed over the frame.
	//Selecting "Yes" saves the JTextArea's content and exits the program. Selecting "No"
	//only closes the showConfirmDialog box. These behaviors seem identical to 
	//JavaPadWPI.
	private class QuitListener extends JavaPadOps implements ActionListener{
		public void actionPerformed(ActionEvent event){
			int quit=JOptionPane.showConfirmDialog(frame, "Quitting; Save?", 
					"Quit", JOptionPane.YES_NO_OPTION);
			if(quit==JOptionPane.YES_OPTION){
				this.save(bigTextArea);
				System.exit(0);
			}
		}					
	}

	//Selecting "New" seemed to only clear the screen in JavaPadWPI. Easy enough.
	private class NewListener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			bigTextArea.setText("");
		}
	}

	//The current font size and the integer entered into the fontSize field are read
	//and the font is changed to the specified size. If the entry is a non-positive 
	//integer, a code is sent to errorMsgs() letting the user know that they goofed.
	//After the user clicks "Ok," the fontSize field is cleared and is ready for another
	//entry. JavaPadWPI would accept negative integers (which made the text disappear)
	//and did nothing when a non-integer character was entered.
	//My gut tells me that this method is more complicated than it needs to be, but I 
	//couldn't figure out a simpler way to catch all of the what-ifs.
	private class FontSizeListener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			String sizeWanted=new String(event.getActionCommand());
			boolean allDigits=false;			
			for(char c : sizeWanted.toCharArray()){
				if(!Character.isDigit(c)){
					errorMsgs(2);
					fontSize.setText("");
				}
			}
			if(!allDigits && Integer.parseInt(sizeWanted)>0){
				Integer sizeWanted1=new Integer(event.getActionCommand());
				Font oldFontSize=fontSize.getFont();
				Font newFontSize=new Font(oldFontSize.getFontName(),oldFontSize.getStyle(),
						sizeWanted1.intValue());
				bigTextArea.setFont(newFontSize);
			}
		}
	}

	//The font color is always initially BLACK and pressing the Foreground button alternates
	//the font color between BLACK and MAGENTA. The ternary expressions in the next two classes
	//might push the limits of good taste, but I think that the white space helps readability.
	private class FGListener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			bigTextArea.setForeground((bigTextArea.getForeground().equals(Color.MAGENTA)) ? 
					Color.BLACK : Color.MAGENTA);
		}
	}

	//The JTextArea is always initially WHITE and pressing the Background button alternates
	//the JTextArea color between WHITE and LIGHT_GRAY.
	private class BGListener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			bigTextArea.setBackground((bigTextArea.getBackground().equals(Color.LIGHT_GRAY)) ? 
					Color.WHITE : Color.LIGHT_GRAY);
		}
	}
	
	/*
	 * I used the following resources for the RightClickMenuMouseListener class:
	 * http://stackoverflow.com/questions/766956/how-do-i-create-a-right-click-context-menu-in-java-swing
	 * http://www.roseindia.net/java/example/java/swing/PopUpMenu.shtml
	 * http://stackoverflow.com/questions/2793940/why-right-click-is-not-working-on-java-application
	 * http://stackoverflow.com/questions/4525733/java-mouse-event-right-click
	 * Java, The Complete Reference, Eighth Edition, Herbert Schildt
	 * Silver City WhoopPass Double IPA
	 * 
	 */
	
	//This where the right click menu happens! This class builds the popup menu, adds the editing
	//functions, and monitors where the cursor is on the screen. When the right click button
	//is pressed, the menu is displayed and this class handles the commands appropriately.
	//Interestingly, the actionPerformed() and mouseClicked() methods require public access.	
	private class RightClickMenuMouseListener extends MouseAdapter{
		
		//This is needed to make Eclipse happy.
		@SuppressWarnings("serial")
		
		private RightClickMenuMouseListener(){
			undoAction=new AbstractAction("Undo"){
				public void actionPerformed(ActionEvent ae){
					textComponent.setText("");
					textComponent.replaceSelection(savedString);
					lastActionSelected=Actions.UNDO;
				}
				
			//I had never seen this "};" before. In researching, "}" don't necessarily end statements.
			//The actionPerformed method is actually part of the definition of AbstractAction and so
			//the statement isn't done until we reach the semicolon.
			};
			popup.add(undoAction);
			popup.addSeparator();

			cutAction=new AbstractAction("Cut") {
				public void actionPerformed(ActionEvent ae){
					lastActionSelected=Actions.CUT;
					savedString=textComponent.getText();
					textComponent.cut();
				}
			};
			popup.add(cutAction);
			
			copyAction=new AbstractAction("Copy"){
				public void actionPerformed(ActionEvent ae){
					lastActionSelected=Actions.COPY;
					textComponent.copy();
				}
			};
			popup.add(copyAction);
			
			pasteAction=new AbstractAction("Paste"){
				public void actionPerformed(ActionEvent ae){
					lastActionSelected=Actions.PASTE;
					savedString=textComponent.getText();
					textComponent.paste();
				}
			};
			popup.add(pasteAction);
			popup.addSeparator();

			selectAllAction=new AbstractAction("Select All"){
				public void actionPerformed(ActionEvent ae){
					lastActionSelected=Actions.SELECT_ALL;
					textComponent.selectAll();
				}
			};
			popup.add(selectAllAction);
		}
		
		//mouseClicked patiently waits until BUTTON3 (in Java is the right button) is clicked and then uses
		//javax.swing library to make everything work. I don't know how this works on a Mac.
		public void mouseClicked(MouseEvent e){
			if (e.getModifiers()==InputEvent.BUTTON3_MASK){
				if (!(e.getSource() instanceof JTextComponent)){
					return;
				}
				textComponent=(JTextComponent) e.getSource();
				textComponent.requestFocus();

				boolean enabled=textComponent.isEnabled();
				boolean editable=textComponent.isEditable();
				boolean nonempty=!(textComponent.getText()==null || textComponent.getText().equals(""));
				boolean marked=textComponent.getSelectedText()!=null;

				boolean pasteAvailable=Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null)
							.isDataFlavorSupported(DataFlavor.stringFlavor);

				undoAction.setEnabled(enabled && editable && (lastActionSelected==Actions.CUT || 
							lastActionSelected==Actions.PASTE));
				cutAction.setEnabled(enabled && editable && marked);
				copyAction.setEnabled(enabled && marked);
				pasteAction.setEnabled(enabled && editable && pasteAvailable);
				selectAllAction.setEnabled(enabled && nonempty);

				int nx=e.getX();
				if(nx>500){
					nx=nx-popup.getSize().width;
				}
				popup.show(e.getComponent(),nx,e.getY()-popup.getSize().height);
			}
		}
	}
}	






