package uk.ac.soton.comp1206.scene;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Scores scene. Holds the UI for the scores scene.
 */
public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * The game object that has been played, containing the scores
   */
  private final Game game;

  /**
   * Communicator used to send and receive messages from the server
   */
  private final Communicator communicator;

  /**
   * boolean multiPlayerGame to see if game was a multiplayer game or not
   */
  private final boolean multiPlayerGame;

  /**
   * List used to hold the local scores
   */
  private final List<Pair<String, Integer>> localScoresList = new ArrayList<>();

  /**
   * Observable list used to hold the local scores
   */
  private final ObservableList<Pair<String, Integer>> localScores = FXCollections.observableList(
      localScoresList);

  /**
   * List used to hold the online scores
   */
  private final List<Pair<String, Integer>> onlineScoresList = new ArrayList<>();

  /**
   * Observable list used to hold the online scores
   */
  private final ObservableList<Pair<String, Integer>> onlineScores = FXCollections.observableList(
      onlineScoresList);

  /**
   * List used to hold the Leaderboard scores
   */
  private final List<Pair<String, Integer>> leaderboardScoresList = new ArrayList<>();

  /**
   * Observable list used to hold the online scores
   */
  private final ObservableList<Pair<String, Integer>> leaderboardScores = FXCollections.observableList(
      leaderboardScoresList);

  /**
   * Main Border Pane that contains all the other UI components
   */
  protected BorderPane mainPane;

  /**
   * VBox component to hold the text field used to gather the player's username
   */
  protected VBox topBox;

  /**
   * Create a new scores scene, passing in the GameWindow the scene will be displayed in
   *  @param gameWindow   the game window
   * @param game         game object that holds the score
   * @param multiPlayerGame boolean value if multiPlayerGame
   * @param communicator the communicator used to communicate with the server
   */
  public ScoresScene(GameWindow gameWindow, Game game, boolean multiPlayerGame,
      Communicator communicator) {
    super(gameWindow);
    this.game = game;
    this.communicator = communicator;
    this.multiPlayerGame = multiPlayerGame;
    logger.info("Creating Scores Scene!");
  }

  /**
   * Initialise the scene
   */
  @Override
  public void initialise() {
    //Stop playing any background music
    Multimedia.stopBackgroundMusic();

    //keyboard listeners to allow the user to press the escape key to return to the menu scene
    getScene().setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        gameWindow.startMenu();
      }
    });
  }

  /**
   * Build the scores scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Creates a Game Pane to hold the base StackPane of the UI
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane to hold the mainPane of the UI
    var scoresPane = new StackPane();
    scoresPane.setMaxWidth(gameWindow.getWidth());
    scoresPane.setMaxHeight(gameWindow.getHeight());
    scoresPane.getStyleClass().add("menu-background");
    root.getChildren().add(scoresPane);

    //Main Borderpane to hold all the other various UI components
    mainPane = new BorderPane();
    scoresPane.getChildren().add(mainPane);

    //Create a new image of the awful TetrECS title
    var title = new ImageView(
        new Image(Objects.requireNonNull(this.getClass().getResource("/images/TetrECS.png"))
            .toExternalForm()));
    title.setPreserveRatio(true);
    title.setFitWidth(675);
    title.setFitHeight(300);

    //Create a title text that displays "GAME OVER"
    var gameOver = new Text("GAME OVER");
    gameOver.getStyleClass().add("bigtitle");

    //Create a VBox to hold the image title and the game over title text
    topBox = new VBox();
    topBox.setAlignment(Pos.CENTER);
    topBox.getChildren().addAll(title, gameOver);

    //Add it to the main Border Pane
    mainPane.setTop(topBox);
    BorderPane.setAlignment(topBox, Pos.CENTER);

    //Request the server to send the top high scores
    communicator.send("HISCORES");

    //If not a multiplayer game, load the currently save local scores into the local scores list view
    if (!multiPlayerGame) {
      loadScores();
    } else {
      //Request the multiplayer scores from the server
      communicator.send("SCORES");
    }

    //Add a listener to the communicator to send the online scores received from the server
    // to a method that loads them into the online scores list view
    communicator.addListener(this::loadOnlineScores);

    //Make the current thread sleep for 200 milliseconds in order to allow the scene to collect
    // the scores from the server
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Check to see if game was a multiplayer game or not
    if (!multiPlayerGame) {

      //If it's not a multiplayer game, check to see if the score the player got beat any of the local or online scores
      if (checkScore(game) != -1) {
        //Create the components associated with a beaten score
        createBeatScores();
      } else {
        //Otherwise, just build the score lists
        buildScores();
      }
    } else {
      //Otherwise, if it is a multiplayer score, check to see if the user game score beat any online high scores
      if (checkScore(game) == 0) {
        //Create the components associated with a beaten score
        createBeatScores();
      } else {
        //Otherwise, just build the score lists
        buildScores();
      }
    }
  }

  /**
   * Creates the components needed if a score is beaten
   */
  private void createBeatScores() {
    //If it did beat a score, create a new VBox to hold the text field for entering that player's username
    var midBox = new VBox(20);
    midBox.setAlignment(Pos.CENTER);

    //Create a text field for the user to enter their username
    TextField user = new TextField("");
    user.setPromptText("Enter your name");

    //Create a button for the user to press to submit their username to the scores lists
    Button enter = new Button("SUBMIT");

    //Create a smaller header indicating that the player got a new high score
    var newHiScoreText = new Text("You got a new High Score!");
    newHiScoreText.getStyleClass().add("title");

    //Add all these components to the VBox
    midBox.getChildren().addAll(user, enter, newHiScoreText);

    //Add the VBox to the centre of the main Border Pane
    mainPane.setCenter(midBox);
    user.requestFocus();

    //Handle what happens if the user clicks the "submit" button
    enter.setOnAction(event -> {
      //Get the user's entered username and submit their earned score to the scores lists
      String username = user.getText();
      submitScore(username);

      //Clear the components from the VBox
      midBox.getChildren().clear();

      //Build the score lists
      buildScores();
    });

    //Handle what happens if the user presses enter on the text field
    user.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ENTER) {
        //Get the user's entered username and submit their earned score to the scores lists
        String username = user.getText();
        submitScore(username);

        //Clear the components from the VBox
        midBox.getChildren().clear();

        //Build the score lists
        buildScores();
      }
    });

  }

  /**
   * Submits the scores to either the local scores list, online scores list or both depending on the
   * number of scores beaten
   *
   * @param username player's username
   */
  private void submitScore(String username) {
    //Checks to see if the user's score has beaten both the local and online scores
    if (checkScore(game) == 2 && !multiPlayerGame) {
      //If it has, add a new pair to the local scores list with the user's username and their score
      localScoresList.add(new Pair<>(username + " ", game.getScore().get()));

      //Sort the new score list
      localScoresList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

      //and remove the last element so that there are only 10 scores in the list
      localScoresList.remove(localScoresList.size() - 1);

      //write the new scores to the local scores save file
      writeScores(localScoresList);

      //Add a new pair to the online scores list with the user's username and their score
      onlineScoresList.add(new Pair<>(username + " ", game.getScore().get()));

      //Sort the new score list
      onlineScoresList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

      //and remove the last element so that there are only 10 scores in the list
      onlineScoresList.remove(onlineScoresList.size() - 1);

      //write the new score to the online scores save file
      Platform.runLater(() -> writeOnlineScore(username, game.getScore().get()));

      //If the user has only beaten the local scores
    } else if (checkScore(game) == 1 && !multiPlayerGame) {
      //Add a new pair to the local scores list with the user's username and their score
      localScoresList.add(new Pair<>(username + " ", game.getScore().get()));

      //Sort the new score list
      localScoresList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

      //and remove the last element so that there are only 10 scores in the list
      localScoresList.remove(localScoresList.size() - 1);

      //write the new scores to the local scores save file
      writeScores(localScoresList);

      //If the user has only beaten the online scores
    } else if (checkScore(game) == 0) {
      //Add a new pair to the online scores list with the user's username and their score
      onlineScoresList.add(new Pair<>(username + " ", game.getScore().get()));

      //Sort the new score list
      onlineScoresList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

      //and remove the last element so that there are only 10 scores in the list
      onlineScoresList.remove(onlineScoresList.size() - 1);

      //write the new score to the online scores save file
      Platform.runLater(() -> writeOnlineScore(username, game.getScore().get()));
    }
  }

  /**
   * Builds the local and online score list UI components
   */
  private void buildScores() {
    //Create a new title that says "High Score"
    var highScores = new Text("High Scores");
    highScores.getStyleClass().add("title");

    //Add it to the top VBox
    topBox.getChildren().add(highScores);

    //Create 2 new VBoxes to hold the local scores and the online scores
    var localScoresBox = new VBox();
    var onlineScoresBox = new VBox();

    //Initialise a localScoresList component
    ScoresList localScoresListComponent;

    //Check to see if it is a multiplayer game or not
    if (!multiPlayerGame) {
      //If it is not a multiplayer game, create a ScoresList component that holds the local scores list
      localScoresListComponent = new ScoresList(localScores);
      localScoresListComponent.setMaxWidth(localScoresBox.getMaxWidth());
      localScoresListComponent.setMaxHeight(localScoresBox.getMaxHeight());
      localScoresListComponent.getStyleClass().add("scorelist");

      //Create a "local scores" title text
      var localScoresText = new Text("Local Scores");
      localScoresText.getStyleClass().add("heading");

      //Add both components to the local scores VBox
      localScoresBox.getChildren().addAll(localScoresText, localScoresListComponent);
      localScoresBox.setAlignment(Pos.CENTER);
    } else {
      //Otherwise, create a LeaderBoard component
      localScoresListComponent = new Leaderboard(leaderboardScores);
      localScoresListComponent.setMaxWidth(localScoresBox.getMaxWidth());
      localScoresListComponent.setMaxHeight(localScoresBox.getMaxHeight());
      localScoresListComponent.getStyleClass().add("scorelist");

      //Create a "local scores" title text
      var localScoresText = new Text("MultiPlayer Game Scores");
      localScoresText.getStyleClass().add("heading");

      //Add both components to the local scores VBox
      localScoresBox.getChildren().addAll(localScoresText, localScoresListComponent);
      localScoresBox.setAlignment(Pos.CENTER);
    }

    //Create a ScoresList component that holds the online scores list
    var onlineScoresListComponent = new ScoresList(onlineScores);
    onlineScoresListComponent.setMaxWidth(onlineScoresBox.getMaxWidth());
    onlineScoresListComponent.setMaxHeight(onlineScoresBox.getMaxHeight());
    onlineScoresListComponent.getStyleClass().add("scorelist");

    //Create an "online scores" title text
    var onlineScoresText = new Text("Online Scores");
    onlineScoresText.getStyleClass().add("heading");

    //Add both components to the online scores VBox
    onlineScoresBox.getChildren().addAll(onlineScoresText, onlineScoresListComponent);
    onlineScoresBox.setAlignment(Pos.CENTER);

    //Create a new HBox to hold the 2 VBoxes with scores lists in them
    var scoresBox = new HBox(10);
    scoresBox.setAlignment(Pos.CENTER);
    scoresBox.getChildren().addAll(localScoresBox, onlineScoresBox);

    //Add the scores HBox to the main Border Pane
    mainPane.setBottom(scoresBox);
    BorderPane.setAlignment(scoresBox, Pos.CENTER);
    BorderPane.setMargin(scoresBox, new Insets(20));

    //Start the animation of the 2 score lists to reveal the scores
    localScoresListComponent.reveal();
    onlineScoresListComponent.reveal();

    logger.info("Everything completed!");

    //Schedule a timer to take the user back to the start menu after 10 seconds
    // of the scores scene being completed
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(gameWindow::startMenu);
      }
    }, 10000);
  }

  /**
   * Checks to see if the user score has beaten any of the scores in the local or online score
   * lists
   *
   * @param game game object containing score
   * @return int -1: no scores beaten,  0: online scores beaten, 1: local scores beaten and 2: both
   * beaten
   */
  public int checkScore(Game game) {
    boolean localScore = false;
    boolean onlineScore = false;

    //Get the score earned by the user from the game
    int newScore = game.getScore().get();

    //For every string, integer Pair in the local scores list, compare the user score with the
    // stored score and see if it is higher than the stored score
    for (Pair<String, Integer> userAndScore : localScoresList) {
      if (userAndScore.getValue() <= newScore) {
        //If yes, set the local score beaten boolean value to true
        localScore = true;
      }
    }

    //For every string, integer Pair in the online scores list, compare the user score with the
    // stored score and see if it is higher than the stored score
    for (Pair<String, Integer> userAndScore : onlineScoresList) {
      if (userAndScore.getValue() <= newScore) {
        //If yes, set the local score beaten boolean value to true
        onlineScore = true;
      }
    }

    //Return the correct number depending on the scores beaten or not
    if (localScore && onlineScore) {
      return 2;
    } else if (localScore) {
      return 1;
    } else if (onlineScore) {
      return 0;
    }
    return -1;
  }

  /**
   * Loads the stored local scores from the file "scores.txt"
   */
  public void loadScores() {
    try {
      //Get the "scores.txt" input file path
      File input = new File("scores.txt");

      //Initialise a new scanner to read the file
      Scanner reader = new Scanner(input);

      while (reader.hasNextLine()) {
        //Store the current read line as a string variable
        String line = reader.nextLine();

        //Break once the local scores list size equals 10 scores
        if (localScoresList.size() == 10) {
          break;
        }

        //Split the line into the username and the score with the regex "="
        String[] scoreString = line.split(" =");

        //Create a new pair of username and score from the read line
        Pair<String, Integer> score = new Pair<>(scoreString[0] + " ",
            Integer.parseInt(scoreString[1]));

        //Add it to the local scores lists
        localScoresList.add(score);
      }

      reader.close();

    } catch (Exception e) {
      //Catch any exceptions here
      e.printStackTrace();

      //If no file found, write a default set of scores to a new file
      writeDefaultScores();
    }
  }

  /**
   * Writes the new updated local scores to the "scores.txt" file
   *
   * @param list list of usernames and their scores
   */
  public void writeScores(List<Pair<String, Integer>> list) {
    try {
      //Create a new PrintWriter and BufferedWriter objects to write the scores to the file
      PrintWriter printWriter = new PrintWriter("scores.txt");
      BufferedWriter bw = new BufferedWriter(printWriter);

      //For every string, integer pair in the given list of username and scores, write the
      // username and the score to the file
      for (Pair<String, Integer> stringIntegerPair : list) {
        bw.write(stringIntegerPair.toString());
        bw.newLine();
      }

      bw.close();

    } catch (Exception e) {
      //Catch any exceptions here
      e.printStackTrace();
    }
  }

  /**
   * Writes a default set of scores to a "scores.txt" file if no such file was found
   */
  public void writeDefaultScores() {
    try {
      //Create a new PrintWriter and BufferedWriter objects to write the default scores to the file
      PrintWriter printWriter = new PrintWriter("scores.txt");
      BufferedWriter bw = new BufferedWriter(printWriter);

      //For loop that generates 10 default scores and writes them to the "scores.txt" file
      for (int i = 10; i > 0; i--) {
        bw.write("Guest =" + (i * 100));
        bw.newLine();
      }

      bw.close();

    } catch (Exception e) {
      //Catch any exceptions here
      e.printStackTrace();
    }
  }

  /**
   * Writes a new online score to the online scores list stored on the server
   *
   * @param username username of the user's score
   * @param newScore user's earned score
   */
  private void writeOnlineScore(String username, int newScore) {
    communicator.send("HISCORE " + username + ":" + newScore);
  }

  /**
   * Loads the online scores stored on the server into an array list
   *
   * @param communication message containing scores from the server
   */
  public void loadOnlineScores(String communication) {
    //Splits the message at the first space, used to figure out what kind of message has been sent
    String[] messageSplit = communication.split(" ", 2);

    //Checks the kind of message received and acts accordingly
    switch (messageSplit[0]) {

      //If HISCORES message received, add the high scores to the scores list
      case "HISCORES" -> {
        //Clear the online scores list
        onlineScoresList.clear();

        //Split the scores into their individual username and score
        String[] scoreString = messageSplit[1].split("\\R");

        //Iterate over the received usernames and scores and add them to the online scores array list
        for (String part : scoreString) {
          String[] parts = part.split(":");

          //Create a new string, integer pair containing the username and score and add them to the
          // online scores array list
          Pair<String, Integer> score = new Pair<>(parts[0] + " ", Integer.parseInt(parts[1]));
          onlineScoresList.add(score);
        }
      }

      case "SCORES" -> {
        //Clear the scores list
            leaderboardScoresList.clear();

            // Split the message into each separate player's score
            String[] playerInfo = communication.split(" ", 2)[1].split("\\R");

            //Iterate through all the player's and their scores
            for (String player : playerInfo) {
              //Split the player from their score from their lives
              String[] info = player.split(":");

              //Add the player's username and score to the list
              leaderboardScoresList.add(new Pair<>(info[0], Integer.parseInt(info[1])));
            }

            //Tell the server the current player is Dead
            communicator.send("DIE");
      }
    }
      logger.info("Retrieved online scores successfully");
  }
}