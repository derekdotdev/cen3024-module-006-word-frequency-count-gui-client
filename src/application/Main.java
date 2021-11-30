package application;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Application scrapes text from a website and displays top10 
 *  (and all) word occurrences to a JavaFX GUI.
 *  @author derekdileo */
public class Main extends Application {
	
	// Variables for call to QuestionBox.display()
	protected static boolean defaultSite = false;
	protected static String userWebsite = null;
	protected static String sourceHead = null;
	protected static String sourceEnd = null;
	private String defaultWebsite =  "https://www.gutenberg.org/files/1065/1065-h/1065-h.htm";
	private String defaultSourceHead = "<h1>The Raven</h1>";
	private String defaultSourceEnd = "<!--end chapter-->";
	private String title = "Word Frequency Analyzer";
	private String instruction = "Enter a URL to count frequency of each word.";
	private String siteLabel = "Website to Parse";
	private String sitePlaceholder = "Enter a website to evaluate";
	private String startLabel = "Where to start.";
	private String startPlaceholder= "Text from first line";
	private String endLabel = "Where to finish.";
	private String endPlaceholder = "Text from last line.";
	private String[] defaultEntries = {defaultWebsite, defaultSourceHead, defaultSourceEnd};
	private String[] questionBoxPrompts = {title, instruction, siteLabel, sitePlaceholder, startLabel, startPlaceholder, endLabel, endPlaceholder};
	
	// QuestionBox.display now accepts a third string array to pass to an AlertBox when it launches.
	// This enables us to provide some app instructions to the user. 
	private String appIntroTitle = "Welcome to Word Frequency Counter";
	private String appIntroMessage = "For best results, right-click and inspect the text you'd like to parse. \nThen, copy and paste the elements into the start and finish boxes.";
	private String[] appIntro = {appIntroTitle, appIntroMessage};
	
	// String array to hold QuestionBox.display() responses.
	protected static String[] userResponses;
	
	// Local Lists and Maps to hold return values from Class methods
	private String[] wordsArray;
	
	// Varibles used to show / hide text on GUI
	private StringBuilder sbTen;
	private StringBuilder sbAll;
	
	// These are accessed by the four Controller classes to print to GUI 
	protected static String sbTenString;
	protected static String sbAllString;
	
	// IO Streams for communication to / from Server
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	ByteArrayOutputStream baos = null;
	ByteArrayInputStream bais = null;
	
	// Create socket for Client / Server connection
	public static Socket socket = null;
	
	/** Main method calls launch() to start JavaFX GUI.
	 *  @param args mandatory parameters for command line method call */
	public static void main(String[] args) {
		// Create wordsTable if it doesn't exist?
		// Server side should handle this functionality. 
		launch();
	}
	
	// Declare stage (window) outside of start() method
	// so it is accessible to closeProgram() method
	protected static Stage window;
	
	/** The start method (which is called on the JavaFX Application Thread) 
	 * is the entry point for this application and is called after the init 
	 * method has returned- most of the application logic takes place here. 
	 * @throws UnsupportedEncodingException */
	@Override
	public void start(Stage primaryStage) throws UnsupportedEncodingException {
		
		try(Socket socket = new Socket("localhost", 8000)) {
			
			// Get user input for website, startLine & endLine...
			// Or set to default values if none are entered by user
			userResponses = processUserInput();
			
			// This boolean is used to determine which scene is loaded 
			// (with or without EAP's The Raven graphic elements) 
			if (userResponses[0].equals(defaultWebsite)) {
				defaultSite = true;
			}
			
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

			output.println(userResponses[0]);
//			System.out.println(input.readLine());
			output.println(userResponses[1]);
//			System.out.println(input.readLine());
			output.println(userResponses[2]);
//			System.out.println(input.readLine());
			output.println(userResponses[3]);
//			System.out.println(input.readLine());
			
			// StringBuilder Object to hold Top Ten Results
			sbTen = new StringBuilder();
			
			// Receive Top Ten Results from Server
			while(true) {

				// Create String array to hold each line of results
				String[] lines = null;
				
				// Read input from Server (likely contains more than one "line")
				String str = input.readLine();
				
				// Split up using "," as delimiter
				lines = str.split(",");
				
				// Determine current size of String array
				int size = lines.length;
				
				// Search for and remove "pause" if part of current readLine()
				for (int i = 0; i < size; i ++) {
					if(lines[i].equals("pause")) {
						lines[i] = "";
					}
				} 
				
				// Remove commas, add each line to sbAll
				for (String line : lines) {
					line.replace(",", "\n\n");
					sbTen.append(line + "\n");
				} 
				
				// "pause" sent from Server when data transmission complete
				if(str.equals("pause")) {
					break;
				}
			}
			
			// StringBuilder Object to hold All Results
			sbAll = new StringBuilder();
			
			// Receive All Results from Server
			while(true) {
				
				// Create String array to hold each line of results
				String[] lines = null;
				
				// Read input from Server (likely contains more than one "line")
				String str = input.readLine();
				
				// Split up using "," as delimiter
				lines = str.split(",");
				
				// Determine current size of String array
				int size = lines.length;
				
				// Search for and remove "pause" if part of current readLine()
				for (int i = 0; i < size; i ++) {
					if(lines[i].equals("pause")) {
						lines[i] = "";
					}
				} 
				
				// Remove commas, add each line to sbAll
				for (String line : lines) {
					line.replace(",", "\n\n");
					sbAll.append(line + "\n");
				} 
				
				// "pause" sent from Server when data transmission complete
				if(str.equals("pause")) {
					break;
				}
			}

			// Convert StringBuilder Objects to Strings which are called from either:
			// MainC-, MainDefaultC-, AllResultsC- or AllResultsDefaultC- ontrollers to push to GUI
			sbTenString = sbTen.toString();
			sbAllString = sbAll.toString();
			
			// Rename stage to window for sanity
			window = primaryStage;
			
			// Set stage title
			window.setTitle("Word Frequency Analyzer");
			
			// Handle close button request. 
			// Launch ConfirmBox to confirm if user wishes to quit
			window.setOnCloseRequest(e -> {
				// Consume the event to allow closeProgram() to do its job
				e.consume();
				closeProgram();
			});
			
			// Load GUI
			try {
				if (defaultSite) {
					BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Main.fxml"));				
					Scene scene = new Scene(root,800,600);
					scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
					window.setScene(scene);
					window.show();
				} else {
					BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("MainDefault.fxml"));				
					Scene scene = new Scene(root,800,600);
					scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
					window.setScene(scene);
					window.show();
				}
			} catch(Exception e) {
				System.out.println("Cannot execute Client window.show()");
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			System.out.println("Error in Client Socket");
			e.printStackTrace();
		}
		
	}
	
	/** Method calls QuestionBox to ask user for a website to parse as well as
	 *  where the parsing should start and end.
	 *  @return a String array with responses to pass to WebScrape.parseSite() Method. */
	private String[] processUserInput() {
		// Create string array to hold QuestionBox responses (site, startPoint, endPoint).
		String[] responses = new String[4];
		
		// Gather responses and return to caller
		responses = QuestionBox.display(questionBoxPrompts, defaultEntries, appIntro);
		
		return responses;
		
	}
		
	/** closeProgram() Method uses ConfirmBox class to confirm is user wants to quit */
	protected static void closeProgram() {
       // Ask if user wants to exit (no title necessary, leave blank)
       Boolean answer = ConfirmBox.display("", "Are you sure you want to quit?");
       if (answer) {
           // Run any necessary code before window closes:
		   System.out.println("Window Closed!");
		   window.close();
       }
       
	}
	
}
