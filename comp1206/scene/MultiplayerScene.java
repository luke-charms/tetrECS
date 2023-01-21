package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Multi Player challenge scene. Holds the UI for the multiplayer challenge mode in the game.
 */
public class MultiplayerScene extends ChallengeScene {

  /**
   * Communicator used to send and receive messages from the server
   */
  private final Communicator communicator;

  /**
   * Observable list of Strings used to hold the current channel's users
   */
  private final ObservableList<Text> users = FXCollections.observableArrayList();

  /**
   * String that holds the current player's username
   */
  private final String username;

  /**
   * The game object that is currently being played
   */
  protected MultiplayerGame game;

  /**
   * Text box to hold any messages sent during the game
   */
  protected Text chatTextInfo;

  /**
   * VBox to hold the GameBoard board and the chat text messages sent during the game
   */
  protected VBox middleBox;


  /**
   * Create a new Multi Player challenge scene
   *
   * @param gameWindow   the Game Window
   * @param communicator communicator to send and receive messages from the server
   * @param users        list of users currently in the game's channel
   * @param username     the current player's username
   */
  public MultiplayerScene(GameWindow gameWindow, Communicator communicator,
      ObservableList<String> users, String username) {
    super(gameWindow);
    this.communicator = communicator;
    this.username = username;

    //For every user currently in the channel, create a new text box and set the text to the user's name and their score of 0
    for (String user : users) {
      Text text = new Text();
      text.setStyle("-fx-fill: white");
      text.setText(user + " : 0");
      this.users.add(text);
    }

    build();
  }

  /**
   * Build the Multiplayer Scene window
   */
  @Override
  public void build() {
    //Set the boolean value of multiPlayerGame to true
    super.setMultiPlayerGame(true);

    //Requests a buffer queue of pieces from the server
    communicator.send("PIECE");
    communicator.send("PIECE");

    //Add a listener to the communicator to send all incoming messages to the checkMessage method
    communicator.addListener(this::checkMessage);

    //Make the current thread sleep for 300 milliseconds in order to allow the buffer queue of
    // pieces to be updated with the pieces
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //Execute the superclass' build method
    super.build();

    //Set the text of the score text to the player's username
    super.scoreText.setText(username);

    //Set the text of the title text to "MultiPlayer Match"
    super.title.setText("Multiplayer Match");

    //Clear all the unnecessary game components not needed
    super.gameInfoBox.getChildren().clear();

    //Create some new text to add to the right side VBox
    Text versusText = new Text("Versus");
    versusText.getStyleClass().add("heading");

    //Create a list view to hold the scores of the users currently playing in the game
    ListView<Text> userScores = new ListView<>();
    userScores.getStyleClass().add("scorelist");

    //Set the list view to contain the users and their current game scores
    userScores.setItems(users);
    userScores.setMaxHeight(100);

    //Add these new components to the VBox on the right side
    super.gameInfoBox.getChildren().addAll(versusText, userScores);

    //Create a text box to display online chat messages during the game
    chatTextInfo = new Text("In-Game Chat: Press T to send a chat message");
    chatTextInfo.getStyleClass().add("playerBox");

    //Create a middle VBox to hold GameBoard board and the online chat message text box
    middleBox = new VBox(2);
    middleBox.getChildren().addAll(super.board, chatTextInfo);
    middleBox.setAlignment(Pos.CENTER);

    //Add this new VBox to the main Border Pane
    super.mainPane.setCenter(middleBox);

    //Add a listener to the game score to update the online score as well every time it updates
    super.game.getScore().addListener(observable -> {
      //Send a message to the server with the new score of the current player
      communicator.send("SCORE " + super.game.getScore().get());

      //Request all the scores from all the players in the current game
      communicator.send("SCORES");

      //Send the current player's board to ensure no cheating is happening
      communicator.send("BOARD " + super.game.getGrid());
    });

    //Add a listener to the game lives to update the online lives as well every time it updates
    super.game.getLives().addListener(observable -> {
      //Check to see if current player no longer has any lives
      if (super.game.getLives().get() == -1) {
        //If so, inform the server this player is dead
        //communicator.send("DIE");
      }
      //Otherwise, send the number of lives this player has left to the server
      communicator.send("LIVES " + super.game.getLives().get());
    });
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    super.initialise();

    //Add a listener to the current scene to check for any keyboard inputs
    gameWindow.getScene().setOnKeyPressed(keyEvent -> {

      //If the escape button is pressed, inform the server the player has left and start the menu scene
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        communicator.send("DIE");
        Multimedia.stopBackgroundMusic();
        super.gameWindow.startMenu();
      }

      //If the T button is pressed, open the message text field to type a message
      if (keyEvent.getCode() == KeyCode.T) {
        TextField messageField = new TextField();
        middleBox.getChildren().add(messageField);
        messageField.requestFocus();

        //If the enter key is pressed on the message text field send the message to the server
        messageField.setOnKeyPressed(event -> {
          if (event.getCode() == KeyCode.ENTER) {
            communicator.send("MSG " + messageField.getText());
            messageField.clear();
            middleBox.getChildren().remove(messageField);
          }
        });
      }
      //If the right-arrow key or D key is pressed, change the selected block to the one
      // one right of it
      if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
        super.game.changeXAim(1);
        //Paint the selected GameBlock as hovered
        super.selectedBlock.paintHover();
      }

      //If the left-arrow key or A key is pressed, change the selected block to the one
      // one left of it
      if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
        super.game.changeXAim(-1);
        //Paint the selected GameBlock as hovered
        super.selectedBlock.paintHover();
      }

      //If the up-arrow key or W key is pressed, change the selected block to the one
      // one up of it
      if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
        super.game.changeYAim(-1);
        //Paint the selected GameBlock as hovered
        super.selectedBlock.paintHover();
      }

      //If the down-arrow key or S key is pressed, change the selected block to the one
      // one down of it
      if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
        super.game.changeYAim(1);
        //Paint the selected GameBlock as hovered
        super.selectedBlock.paintHover();
      }

      //If the enter key or X key is pressed, place the selected block
      if ((keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) && middleBox.getChildren().size() != 3) {
        super.game.blockClicked(selectedBlock);
      }

      //If the Q key, Z key or "[" key is pressed, rotate the selected block anti-clockwise
      if (keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z
          || keyEvent.getCode() == KeyCode.OPEN_BRACKET) {
        super.game.rotateCurrentPieceAnti();
      }

      //If the E key, C key or "]" key is pressed, rotate the selected block clockwise
      if (keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C
          || keyEvent.getCode() == KeyCode.CLOSE_BRACKET) {
        super.game.rotateCurrentPieceClock();
      }

      //If the SPACE key or R key is pressed, swap the current and following piece
      if (keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
        super.game.swapCurrentPiece();
      }
    });
  }

  /**
   * Checks the received message from the server and acts accordingly
   *
   * @param message message sent from the server
   */
  private void checkMessage(String message) {
    //Splits the message at the first space, used to figure out what kind of message has been sent
    String[] messageSplit = message.split(" ", 2);

    //Checks the kind of message received and acts accordingly
    switch (messageSplit[0]) {

      //If the message contains "MSG", send the received message to the ChannelWindow message chat
      case "MSG" -> Platform.runLater(() -> receiveMessage(messageSplit[1]));

      //If the message contains "SCORES", update the scores with the received new scores
      case "SCORES" -> Platform.runLater(() -> updateScores(messageSplit[1]));

      //If the message contains "PIECE", add a new GamePiece piece to the buffer queue of pieces
      case "PIECE" -> Platform.runLater(
          () -> super.game.addPiece(Integer.parseInt(messageSplit[1])));

      //If the message contains "DIE", request the scores from the server for the current game
      case "DIE" -> Platform.runLater(() -> communicator.send("SCORES"));
    }
  }

  /**
   * Updates the current scores for the game being played
   *
   * @param message new scores to be used to update
   */
  private void updateScores(String message) {
    //Clear the previous scores
    users.clear();

    //Split the message into each separate player's score
    String[] playerInfo = message.split("\\R");

    //Iterate through all the player's and their scores
    for (String player : playerInfo) {
      //Split the player from their score from their lives
      String[] info = player.split(":");

      //Check to see if the player is dead and is not the current player
      if (info[2].equals("DEAD") && !info[0].equals(username)) {

        //If the player is dead, create a new text with the player's username and score strike through
        Text usernameStrike = new Text();
        usernameStrike.setStyle("-fx-fill: white");
        usernameStrike.setText(info[0] + " : " + info[1]);
        usernameStrike.setStrikethrough(true);

        //Add it to the list view showing the player scores
        users.add(usernameStrike);
      } else {
        //Otherwise, just update the list view normally with a new text object containing the
        // player's username and their score
        Text text = new Text();
        text.setStyle("-fx-fill: white");
        text.setText(info[0] + " : " + info[1]);

        //Add it to the list view showing the player scores
        users.add(text);
      }
    }
  }

  /**
   * Sets the chat text box to the most recent message received from the server
   *
   * @param message message received from server
   */
  private void receiveMessage(String message) {
    String[] messageSplit = message.split(":", 2);
    chatTextInfo.setText("<" + messageSplit[0] + "> " + messageSplit[1]);
  }

  /**
   * Set up the Multi Player Game object and model
   */
  @Override
  public void setupGame() {
    super.game = new MultiplayerGame(5, 5, communicator);
  }

}
