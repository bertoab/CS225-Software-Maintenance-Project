//package marchmadness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InvalidClassException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *  MarchMadnessGUI
 * 
 * this class contains the buttons the user interacts
 * with and controls the actions of other objects 
 *
 * @author Grant Osborn
 */
public class MarchMadnessGUI extends Application {
    
    
    //all the gui ellements
    private BorderPane root;
    private ToolBar toolBar;
    private ToolBar btoolBar;
    private Button simulate;
    private Button login;
    private Button scoreBoardButton;
    private Button viewBracketButton;
    // DANIELLE: add help button
    private Button helpButton;
    private Button clearButton;
    private Button resetButton;
    private Button finalizeButton;

    //NEW - Joey
    private Button realResultsButton;
    
    //allows you to navigate back to division selection screen
    private Button back;
  
    
    //LIOR: removed this attribute
    // private Bracket startingBracket;
    //reference to currently logged in bracket
    private Bracket selectedBracket;
    private Bracket simResultBracket;

    
    // Gabe change: this list now tracks only current-session participants
    private ArrayList<Bracket> playerBrackets;

    //LIOR: added attributes for new password system
    private HashMap<String, byte[]> passwordHashes;
    private String currentUserLoggedIn;
    // DANIELLE: for help button
    private Stage helpStage;

    private ScoreBoardTable scoreBoard;
    private TableView table;
    private BracketPane bracketPane;
    private GridPane loginP;

    //LIOR: removed this attribute
    // private TournamentInfo teamInfo;
    
    
    @Override
    public void start(Stage primaryStage) {
        //LIOR: change this to use reworked TournamentInfo properly
        //try to load all the files, if there is an error display it
        try{
            // teamInfo=new TournamentInfo();

            TournamentInfo.loadTeamsFromFile();
            TournamentInfo.loadEmptyBracket();
        } catch (IOException ex) {
            showError(new Exception("Can't find "+ex.getMessage(),ex),true);
        }
        // Gabe change: do not preload saved brackets at startup
        playerBrackets = new ArrayList<Bracket>();
        


        //the main layout container
        root = new BorderPane();
        root.setStyle("-fx-background-color: #231f20");
        scoreBoard= new ScoreBoardTable();
        table=scoreBoard.start();
        loginP=createLogin();
        CreateToolBars();
        
        //display login screen
        login();
        
        setActions();
        root.setTop(toolBar);   
        root.setBottom(btoolBar);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("dark-mode.css").toExternalForm());
        primaryStage.setMaximized(true);

        primaryStage.setTitle("March Madness Bracket Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
    /**
     * simulates the tournament  
     * simulation happens only once and
     * after the simulation no more users can login
     */
    private void simulate(){
        //cant login and restart prog after simulate
        login.setDisable(true);
        simulate.setDisable(true);
        
       scoreBoardButton.setDisable(false);
       viewBracketButton.setDisable(false);

       //NEW - Joey
       realResultsButton.setDisable(false);
       
       simResultBracket = TournamentInfo.getEmptyBracket();
       simResultBracket.simulate();
       
       for(Bracket b:playerBrackets){
           scoreBoard.addPlayer(b,b.scoreBracket(simResultBracket));
       }
        
        displayPane(table);


    }
    
    /**
     * Displays the login screen
     * 
     */
    private void login(){        
        //LIOR: add this statement to work with new password system
        passwordHashes = loadPasswordHashes();

        login.setDisable(true);
        simulate.setDisable(true);
        scoreBoardButton.setDisable(true);
        viewBracketButton.setDisable(true);
        btoolBar.setDisable(true);

        //NEW - Joey
        realResultsButton.setDisable(true);
        
        displayPane(loginP);
    }
    
     /**
     * Displays the score board
     * 
     */
    private void scoreBoard(){
        displayPane(table);
    }
    
     /**
      * Displays Simulated Bracket
      * 
      */
    private void viewBracket(){
       selectedBracket=simResultBracket;
       bracketPane=new BracketPane(selectedBracket);
       GridPane full = bracketPane.getFullPane();
       full.setAlignment(Pos.CENTER);
       full.setDisable(true);
       displayPane(new ScrollPane(full)); 
    }

    // DANIELLE: help button functionality
    /**
     * Displays a popup containing instructions
     * on how to use the program. The popup allows users to continue
     * to interact with the rest of the program while viewing the popup.
     */
    private void help() {
        Stage helpStage = new Stage();

        Text helpText = new Text("To access your bracket, login to your account" +
                " with your username and password. " +
                "If you do not have an account, enter your desired username and password" +
                " (your username must not match any existing account's username)" +
                " and an account will be created for you.\n\n" +
                "After logging in, you can choose a division to view," +
                " or you can view the full bracket containing all divisions." +
                " To change your choice, click on the desired school name." +
                " To clear your choices on a particular bracket, use the 'Clear'" +
                " button on the bottom bar. To clear all of your choices on every bracket," +
                " use the 'Reset' button on the bottom bar. To choose a different division to view," +
                " use the 'Choose Division' button on the bottom bar. All changes to your bracket will be saved" +
                " to your account.\n\n" +
                "When you are ready to finalize your choices, click the 'Finalize' button on the bottom bar." +
                " You will not be able to change your bracket choices after finalizing." +
                " After finalizing your choices, click the 'Simulate' button on the top bar" +
                " to simulate the tournament. You can now use the 'ScoreBoard' and 'View Simulated Bracket'" +
                " buttons on the top bar to view user scores and the bracket" +
                " simulated tournament respectively. In the simulated bracket, correct predictions are marked" +
                " in green while incorrect predictions are marked in red. You will receive points for making" +
                " correct predictions. You can view the scoreboard to see the points you and other accounts" +
                " have obtained. The scoreboard is automatically sorted is descending order by points." +
                " You can also use the '2017 Real Results' button on the top bar to view the results" +
                " of the real-life 2017 March Madness tournament.");

        // Wrap text
        helpText.wrappingWidthProperty().bind(helpStage.widthProperty().subtract(40));

        StackPane helpRoot = new StackPane();

        // Add margins
        helpRoot.setPadding(new Insets(20));
        helpRoot.getChildren().add(helpText);

        Scene helpScene = new Scene(helpRoot, 500, 360);
        helpStage.setTitle("Instructions");
        helpStage.setScene(helpScene);
        helpStage.show();
    }
    
    /**
     * allows user to choose bracket
     * 
     */
   private void chooseBracket(){
        //login.setDisable(true);
        btoolBar.setDisable(false);
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);

    }
    /**
     * resets current selected sub tree
     * for final4 reset Ro2 and winner
     */
    private void clear(){
      
      
      bracketPane.clear();
      bracketPane=new BracketPane(selectedBracket);
      displayPane(bracketPane);
        
    }
    
    /**
     * resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            //LIOR: changed this to work with reworked TournamentInfo
            selectedBracket=new Bracket(TournamentInfo.getEmptyBracket());
            // Gabe change: preserve the current account identity on reset
            selectedBracket.setPlayerName(currentUserLoggedIn);
            // Gabe change: keep the session participant list pointed at the current bracket object
            if (findSessionBracket(currentUserLoggedIn) != null) {
                addSessionParticipant(selectedBracket);
            }
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        }
    }
    
    private void finalizeBracket(){
       if(bracketPane.isComplete()){
           btoolBar.setDisable(true);
           bracketPane.setDisable(true);
           //save the bracket along with account info
           selectedBracket.setPlayerName(currentUserLoggedIn);
           // Gabe change: only add a bracket to the session after a successful save
           if (serializeBracket(selectedBracket)) {
               addSessionParticipant(selectedBracket);
               simulate.setDisable(false);
               login.setDisable(false);
           } else {
               btoolBar.setDisable(false);
               bracketPane.setDisable(false);
           }
            
       }else{
            infoAlert("You can only finalize a bracket once it has been completed.");
            //go back to bracket section selection screen
            // bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        
       }
       //bracketPane=new BracketPane(selectedBracket);
      
      
        
    }
    
    
    /**
     * displays element in the center of the screen
     * 
     * @param p must use a subclass of Pane for layout. 
     * to be properly center aligned in  the parent node
     */
    private void displayPane(Node p){
        root.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }
    
    /**
     * Creates toolBar and buttons.
     * adds buttons to the toolbar and saves global references to them
     */
    private void CreateToolBars(){
        toolBar  = new ToolBar();
        btoolBar  = new ToolBar();
        login=new Button("Login");
        simulate=new Button("Simulate");
        scoreBoardButton=new Button("ScoreBoard");
        viewBracketButton= new Button("View Simulated Bracket");
        // DANIELLE: add help button
        helpButton = new Button("Help");
        clearButton=new Button("Clear");
        resetButton=new Button("Reset");
        finalizeButton=new Button("Finalize");

        //New - Joey
        toolBar.setStyle("-fx-background-color: #2e292a; " +
                        "-fx-border-color: #fe6229; -fx-border-width: 0 0 2 0;");
        
        btoolBar.setStyle("-fx-background-color: #2e292a; " +
                        "-fx-border-color: #3a3435; -fx-border-width: 1 0 0 0;");

        String mutedBtn = "-fx-background-color: transparent; -fx-border-color: #70684e; -fx-border-width: 1;" +
                          "-fx-text-fill: #a9a073; -fx-font-family: Arial; -fx-font-weight: bold;" + 
                          "-fx-font-size: 11px; -fx-padding: 5 12 5 12;";



        scoreBoardButton.setStyle("-fx-background-color: #fe6229; -fx-border-color: #fe6229;" +
                                  "-fx-border-width: 1; -fx-text-fill: #231f20; -fx-font-family: Arial; " +
                                  "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        
        finalizeButton.setStyle("-fx-background-color: #eeab20; -fx-border-color: #eeab20;" +
                                "-fx-border-width: 1; -fx-text-fill: #231f20; -fx-font-family: Arial; " +
                                "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        
        toolBar.getItems().addAll(
                createSpacer(),
                login,
                simulate,
                scoreBoardButton,
                viewBracketButton,
                helpButton,
                createSpacer()
        );
        btoolBar.getItems().addAll(
                createSpacer(),
                clearButton,
                resetButton,
                finalizeButton,
                back=new Button("Choose Division"),
                createSpacer()
        );

        //NEW - Joey
        realResultsButton = new Button("2017 Real Results");
        toolBar.getItems().add(realResultsButton);

        login.setStyle(mutedBtn);
        simulate.setStyle(mutedBtn);
        viewBracketButton.setStyle(mutedBtn);
        clearButton.setStyle(mutedBtn);
        resetButton.setStyle(mutedBtn);
        back.setStyle(mutedBtn);
        realResultsButton.setStyle(mutedBtn);

    }
    
   /**
    * sets the actions for each button
    */
    private void setActions(){
        login.setOnAction(e->login());
        simulate.setOnAction(e->simulate());
        scoreBoardButton.setOnAction(e->scoreBoard());
        viewBracketButton.setOnAction(e->viewBracket());
        // DANIELLE: the help button is available at all times for user to read
        helpButton.setOnAction(e->help());
        clearButton.setOnAction(e->clear());
        resetButton.setOnAction(e->reset());
        finalizeButton.setOnAction(e->finalizeBracket());
        back.setOnAction(e->{
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        });

        //NEW - Joey
        realResultsButton.setOnAction(e -> displayPane(new RealResultsPane()));
    }
    
    /**
     * Creates a spacer for centering buttons in a ToolBar
     */
    private static Pane createSpacer(){
        Pane spacer = new Pane();
        HBox.setHgrow(
                spacer,
                Priority.SOMETIMES
        );
        return spacer;
    }
    
    
    private GridPane createLogin(){
        
        
        /*
        LoginPane
        Sergio and Joao
         */

        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(5, 5, 5, 5));

        //NEW - Joey
        loginPane.setStyle("-fx-background-color: #2e292a; -fx-border-color: #3a3435;" +
                           "-fx-border-width: 4;  -fx-background-radius: 4; -fx-padding: 5 12 5 12;"
        );

        Text welcomeMessage = new Text("March Madness Login Welcome");
        
        //New - Joey
        welcomeMessage.setStyle("-fx-fill: #eeab20; -fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 16px;");

        loginPane.add(welcomeMessage, 0, 0, 2, 1);

        //New - Joey
        String labelStyle = "-fx-text-fill: #a9a073; -fx-font-family: Arial; -fx-font-size: 13px;";
        String fieldStyle = "-fx-background-color: #3a3435; -fx-border-color: #70684e; -fx-text-fill: #a9a073;" +
                           "-fx-border-width: 1;  -fx-font-family: Arial; -fx-font-size: 13px; -fx-padding: 5 12 5 12;";

        Label userName = new Label("User Name: ");
        userName.setStyle(labelStyle); 

        loginPane.add(userName, 0, 1);

        TextField enterUser = new TextField();
        enterUser.setStyle(fieldStyle);

        loginPane.add(enterUser, 1, 1);

        Label password = new Label("Password: ");
        password.setStyle(labelStyle);

        loginPane.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.setStyle(fieldStyle);

        loginPane.add(passwordField, 1, 2);

        Button signButton = new Button("Sign in");

        //New - Joey
        signButton.setStyle("-fx-background-color: #fe6229; -fx-border-color: #fe6229; -fx-text-fill: #231f20;" +
                           "-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 7 20 7 20;");

        loginPane.add(signButton, 1, 4);
        signButton.setDefaultButton(true);//added by matt 5/7, lets you use sign in button by pressing enter

        Label message = new Label();
        //New - Joey
        message.setStyle("-fx-text-fill: #a9a073; -fx-font-family: Arial;");

        loginPane.add(message, 1, 5);

        //LIOR: change this event to work with the new password system
        signButton.setOnAction(event -> {

            // the name user enter
            String name = enterUser.getText();
            // the password user enter
            String playerPass = passwordField.getText();
            byte[] playerPassHash = null;
            try {
                 playerPassHash = stringToHash(playerPass);
            }
            catch(NoSuchAlgorithmException e) {
                showError(e, false);
            }
            // Gabe change: stop login if the entered password could not be hashed
            if (playerPassHash == null) {
                return;
            }

            if (passwordHashes.containsKey(name)) {
                //check password of user
                byte[] passwordHash = passwordHashes.get(name);

                if (Arrays.equals(passwordHash, playerPassHash)) {
                    // Gabe change: reuse the session bracket if this user already joined this run
                    Bracket sessionBracket = findSessionBracket(name);
                    if (sessionBracket != null) {
                        selectedBracket = sessionBracket;
                    } else {
                        // Gabe change: only deserialize the saved bracket for the user who just logged in
                        File savedBracketFile = findSavedBracketFile(name);
                        if (savedBracketFile != null) {
                            selectedBracket = deserializeBracket(savedBracketFile.getName());
                            if (selectedBracket == null) {
                                return;
                            }
                            if (selectedBracket.getPlayerName() != null
                                    && !selectedBracket.getPlayerName().equals("")
                                    && !Objects.equals(selectedBracket.getPlayerName(), name)) {
                                showError(new Exception("Error loading bracket \nSaved bracket does not match requested user \"" + name + "\"."), false);
                                return;
                            }
                        } else {
                            selectedBracket = TournamentInfo.getEmptyBracket();
                        }
                        selectedBracket.setPlayerName(name);
                        addSessionParticipant(selectedBracket);
                    }
                    currentUserLoggedIn = name;
                    chooseBracket();
                }else{
                   infoAlert("The password you have entered is incorrect!");
                }

            } else {
                //check for empty fields
                if(!name.equals("")&&!playerPass.equals("")){
                    //create new bracket
                    //LIOR: changed this to work with reworked TournamentInfo
                    Bracket tmpPlayerBracket = TournamentInfo.getEmptyBracket();
                    tmpPlayerBracket.setPlayerName(name);
                    selectedBracket = tmpPlayerBracket;
                    //alert user that an account has been created
                    infoAlert("No user with the Username \""  + name + "\" exists. A new account has been created.");
                    
                    try {
                        appendLoginInfo(name, playerPassHash);
                    }
                    catch(IOException e) {
                        showError(e, false);
                    }

                    currentUserLoggedIn = name;
                    chooseBracket();
                }
            }
        });
        
        return loginPane;
    }
    
    /**
     * Finds an already loaded participant for the current session.
     */
    // Gabe change: helper for session-only participant tracking
    private Bracket findSessionBracket(String playerName){
        for(Bracket bracket : playerBrackets){
            if(Objects.equals(bracket.getPlayerName(), playerName)){
                return bracket;
            }
        }
        return null;
    }
    
    /**
     * Finds the saved bracket file for a user without loading every .ser file.
     */
    // Gabe change: helper for on-demand saved-bracket lookup
    private File findSavedBracketFile(String playerName){
        File dir = new File(".");
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        String targetFileName = playerName + ".ser";
        for (File fileEntry : files) {
            if (fileEntry.isFile() && fileEntry.getName().equals(targetFileName)) {
                return fileEntry;
            }
        }
        return null;
    }

    /**
     * Adds or updates a participant in the current session list.
     */
    // Gabe change: helper to keep playerBrackets limited to active session participants
    private void addSessionParticipant(Bracket bracket){
        if (bracket == null || bracket.getPlayerName() == null || bracket.getPlayerName().equals("")) {
            return;
        }

        for (int i = 0; i < playerBrackets.size(); i++) {
            if (Objects.equals(playerBrackets.get(i).getPlayerName(), bracket.getPlayerName())) {
                playerBrackets.set(i, bracket);
                return;
            }
        }
        playerBrackets.add(bracket);
    }

    /**
     * The Exception handler
     * Displays a error message to the user
     * and if the error is bad enough closes the program
     * @param fatal true if the program should exit. false otherwise
     */
    private static void showError(Exception e,boolean fatal){
        String msg=e.getMessage();
        if(fatal){
            msg=msg+" \n\nthe program will now close";
            //e.printStackTrace();
        }
        Alert alert = new Alert(AlertType.ERROR,msg);
        alert.setResizable(true);
        alert.getDialogPane().setMinWidth(420);   
        alert.setTitle("Error");
        alert.setHeaderText("something went wrong");
        alert.showAndWait();
        if(fatal){ 
            System.exit(666);
        }   
    }
    
    /**
     * alerts user to the result of their actions in the login pane 
     * @param msg the message to be displayed to the user
     */
    private static void infoAlert(String msg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Prompts the user to confirm that they want
     * to clear all predictions from their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private static boolean confirmReset(){
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                "Are you sure you want to reset the ENTIRE bracket?", 
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }
    
    
    /**
     * Tayon Watson 5/5
     * seralizedBracket
     * @param B The bracket the is going to be seralized
     */
    // Gabe change: return save success so finalize can decide whether to add the bracket to the session
    private static boolean serializeBracket(Bracket B) {
        if (B == null || B.getPlayerName() == null || B.getPlayerName().equals("")) {
            showError(new Exception("Error saving bracket \nMissing player name."), false);
            return false;
        }

        try (FileOutputStream outStream = new FileOutputStream(B.getPlayerName() + ".ser");
             ObjectOutputStream out = new ObjectOutputStream(outStream)) {
            out.writeObject(B);
            return true;
        } catch (IOException e) {
            // Grant osborn 5/6 hopefully this never happens
            showError(new Exception("Error saving bracket \n" + e.getMessage(), e), false);
            return false;
        }
    }

    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @param filename of the seralized bracket file
     * BELOW EDITED BY ROBERTO
     * @return deserialized Bracket object, or `null` if the Bracket cannot be deserialized
     */
    private static Bracket deserializeBracket(String filename){
        Bracket bracket = null;
        FileInputStream inStream = null;
        ObjectInputStream in = null;
    try 
    {
        inStream = new FileInputStream(filename);
        in = new ObjectInputStream(inStream);
        bracket = (Bracket) in.readObject();
        in.close();
    }catch (IOException | ClassNotFoundException e) {
        // ROBERTO
        if (e instanceof InvalidClassException) { // bracket is outdated version (before hashed password)
            // let the user know an outdated file is being skipped
            String header = "Skipping incompatible bracket file";
            String msg = String.format("\"%s\" is incompatible and cannot be read. The program will skip this file.", filename);
            Alert alert = new Alert(AlertType.ERROR,msg);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.showAndWait();

            // `bracket` will be `null` when this method returns
        }

    } 
    return bracket;
    }
    
    //LIOR: utility method for password checking and storing
    private static byte[] stringToHash(String input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(input.getBytes());
    }

    //LIOR: load the hashes in from passwordHashes.txt
    private HashMap<String, byte[]> loadPasswordHashes() {
        Scanner scnr = null;

        try {
            scnr = new Scanner(new File("passwordHashes.txt"));
        }
        catch(FileNotFoundException e) {
            showError(e, false);
        }
        
        HashMap<String, byte[]> passwordHashMap = new HashMap<String, byte[]>();

        while (scnr.hasNext()) {

            String userName = scnr.nextLine().trim();
            byte[] hash = new byte[32]; //SHA-256 algorithm always generates a 32 byte code
            for (int i = 0; i < hash.length; ++i) {
                hash[i] = scnr.nextByte();
            }
            scnr.nextLine();

            passwordHashMap.put(userName, hash);
        }

         return passwordHashMap;
    }

    //LIOR: add set of login info to passwordHashes.txt
    private void appendLoginInfo(String username, byte[] passwordHash) throws IOException {
        FileWriter writer = new FileWriter("passwordHashes.txt", true);
        writer.write(username + "\n");

        for (byte b : passwordHash) {
            writer.write((int)b + " ");
        }
        writer.write("\n");
        writer.close();
    }
}
