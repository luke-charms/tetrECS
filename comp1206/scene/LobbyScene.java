package uk.ac.soton.comp1206.scene;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.ChannelWindow;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Multi Player lobby scene. Holds the UI for the lobby scene that is used to enter and create
 * new multiplayer games
 */
public class LobbyScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * Communicator used to send and receive messages from the server
   */
  protected final Communicator communicator;

  /**
   * Timer used to constantly check for any new channels that have been created and update the UI
   * with them
   */
  private final Timer timer = new Timer();

  /**
   * Observable list of Strings used to hold the current channels active
   */
  protected ObservableList<String> channelsList = FXCollections.observableArrayList();

  /**
   * String variable used to hold the username of the current player in the lobby scene
   */
  protected String username;

  /**
   * The mainPane that holds all the UI components
   */
  protected BorderPane mainPane;

  /**
   * A custom UI component used to display all the content and messaging of the currently selected
   * online channel
   */
  protected ChannelWindow channelWindow;

  /**
   * VBox used to hold the currently selected online channel title and ChannelWindow
   */
  protected VBox channelBox;

  /**
   * The timer task assigned to the timer which calls the update channel method
   */
  private TimerTask task;

  /**
   * boolean value that is updated when the player is in a channel or not
   */
  private boolean inChannel = false;


  /**
   * Create a new lobby scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow   the game window
   * @param communicator the communicator used to communicate with the server
   */
  public LobbyScene(GameWindow gameWindow, Communicator communicator) {
    super(gameWindow);
    this.communicator = communicator;
    logger.info("Creating Lobby Scene");
  }

  /**
   * Initialise the scene
   */
  @Override
  public void initialise() {
    //keyboard listeners to allow the user to press the escape key to return to the menu scene
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() != KeyCode.ESCAPE) {
        return;
      }
      //Clear all the communicator listeners
      communicator.clearListeners();

      //Cancel the channel updating timer
      timer.cancel();

      //start the menu scene
      gameWindow.startMenu();
    });

  }

  /**
   * Build the lobby scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Creates a Game Pane to hold the base StackPane of the UI
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane to hold the mainPane of the UI
    var lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    //Main Borderpane to hold all the other various UI components
    mainPane = new BorderPane();
    lobbyPane.getChildren().add(mainPane);

    //Create the scene title text
    var title = new Text("MultiPlayer");
    title.getStyleClass().add("title");

    //Add the scene title text to the top of the Border Pane
    mainPane.setTop(title);
    BorderPane.setAlignment(title, Pos.CENTER);

    //Create a VBox to hold the current channels/games, button to host a new channel
    var leftSideTopBox = new VBox(25);

    //Create the current games heading
    var currentGames = new Text("Current Games");
    currentGames.getStyleClass().add("heading");
    leftSideTopBox.getChildren().add(currentGames);

    //Create a button that can be used to host a new game
    var hostNewGame = new Button("Host New Game");
    hostNewGame.getStyleClass().add("channelItem");
    leftSideTopBox.getChildren().add(hostNewGame);

    //Set what happens when the host new game button is clicked
    hostNewGame.setOnAction(event -> {
      //Checks to see if there already exists a text field within the VBox
      if (leftSideTopBox.getChildren().size() == 3) {
        //If there is one, remove it
        leftSideTopBox.getChildren().remove(2);
      }

      //Create a new text field used to hold the name of the new channel being created
      TextField gameTitle = new TextField("");

      //Add it to the VBox last
      leftSideTopBox.getChildren().add(2, gameTitle);
      gameTitle.requestFocus();

      //Set what happens when the user clicks enter on the text field holding the new channel name
      gameTitle.setOnKeyPressed(keyEvent -> {
        if (keyEvent.getCode() != KeyCode.ENTER) {
          return;
        }

        //Checks to see if the user is not already in a channel
        if (!inChannel) {
          //Gets the text entered into the text field to name the new channel
          String channelText = gameTitle.getText();

          //Iterates through the already active channels to check that the same name as a
          // pre-existing channel has not been used
          for (String channel : channelsList) {
            if (channelText.equals(channel)) {
              //If there is a channel with the same name create an error
              Alert errorAlert = new Alert(AlertType.ERROR);
              errorAlert.setHeaderText("CHANNEL ALREADY EXISTS");
              errorAlert.showAndWait();
            } else {
              //Otherwise, send a message to the server to create a new channel with that name
              communicator.send("CREATE " + channelText);

              //Change the inChannel value to show that the player is now in a channel
              inChannel = true;

              //Create and show the UI element of the new channel, taking into account that the
              // player that has just created the channel is now the host
              showChannel(channelText, true);
            }
          }
        } else {
          //Otherwise, if the player is already in a channel, create an alert
          logger.error("Error! Already in channel!");
          Alert errorAlert = new Alert(AlertType.ERROR);
          errorAlert.setHeaderText("ALREADY IN CHANNEL!");
          errorAlert.showAndWait();
        }
        //Finally, remove the game title text field from the VBox
        leftSideTopBox.getChildren().remove(gameTitle);
      });
    });

    //Create a new VBox to hold all the names of the channels
    var channelsText = new VBox(10);

    //Create a scroll Pane to hold this VBox in case the number of channels created is too many
    // to be contained within the scene and needs to be scrolled
    var scroller = new ScrollPane();
    scroller.setFitToHeight(true);
    scroller.setFitToWidth(true);

    //Add a listener to the observable array list of channels to register any changes made to it
    channelsList.addListener((ListChangeListener<String>) change -> {
      while (change.next()) {
        //Get ONLY the name of the channel that has been added to the array list
        String channel = change.toString()
            .substring(change.toString().indexOf("[") + 1, change.toString().indexOf("]"));

        //Check to see if a change has been added and the channel name is not blank
        if (change.wasAdded() && !channel.equals("")) {
          Platform.runLater(() -> {

            //Create a new button with the same name as the new channel that has been added to the array list
            Button channelButton = new Button();
            channelButton.setText(channel);
            channelButton.getStyleClass().add("channelItem");

            //Set the event that happens if the channel button is clicked
            channelButton.setOnAction((ActionEvent) -> {
              //Check to see if the player is already in channel
              if (!inChannel) {
                //If not, then tell the server to JOIN this channel
                communicator.send("JOIN " + channel);

                //Set the value of inChannel to true to show that the player is in a channel
                inChannel = true;

                //Create and show the UI element of the new channel, taking into account that the
                // player that has just created the channel is not the host
                showChannel(channel, false);
                channelButton.getStyleClass().add("menuItem.selected");

              } else {
                //Otherwise, create an error informing the player they are already in a channel
                logger.error("Already in channel!!");
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setHeaderText("ALREADY IN CHANNEL!");
                errorAlert.showAndWait();
              }
            });
            //Add the newly create channel button to the VBox holding all the channel buttons
            channelsText.getChildren().add(channelButton);
          });
        } else if (channelsList.size() == 0) {
          //Otherwise, if the channel list is empty, remove all the elements in the channel text VBox
          Platform.runLater(() -> channelsText.getChildren().clear());
        }
      }
    });

    //Set the content of the ScrollPane to the VBox with channel buttons
    scroller.setContent(channelsText);
    scroller.getStyleClass().add("scroller");

    //Create a new VBox to hold all scroller containing all the channel buttons
    var leftSideBotBox = new VBox(25);
    leftSideBotBox.getChildren().add(scroller);

    //Create another VBox to hold the heading of current games, the host new game button and the
    // scroll pane containing the channel buttons
    var leftSideBox = new VBox(20);
    leftSideBox.getChildren().addAll(leftSideTopBox, leftSideBotBox);

    //Add it to the left side of the Border Pane
    mainPane.setLeft(leftSideBox);
    BorderPane.setMargin(leftSideBox, new Insets(25));

    //Add a listener to the server communicator to send all incoming messages to the checkMessage method
    communicator.addListener(this::checkMessage);

    //Schedule the timer to request all newly created channels from the server every 2 seconds
    timer.schedule(task = new TimerTask() {
      @Override
      public void run() {
        communicatorSend();
      }
    }, 2000);
  }

  /**
   * Shows all the UI elements of the channel the user is currently in
   *
   * @param channel name of the channel
   * @param host    value used to check is user is the host of the channel
   */
  private void showChannel(String channel, boolean host) {
    //Create a new VBox to hold all the channel elements in
    channelBox = new VBox(10);
    channelBox.setFillWidth(true);

    //Create the title of the channel the user is currently in
    var channelTitle = new Text(channel);
    channelTitle.getStyleClass().add("heading");
    channelBox.getChildren().add(channelTitle);

    //Create a new ChannelWindow UI component to display the current channel the user is in
    channelWindow = new ChannelWindow(communicator, host);
    channelWindow.setMaxWidth(800);
    channelWindow.setMaxHeight(1000);

    //Add the channel window to the VBox
    channelBox.getChildren().add(channelWindow);

    //Set the channel box VBox to the centre of the Border Pane
    mainPane.setCenter(channelBox);
    BorderPane.setMargin(channelBox, new Insets(10));
  }

  /**
   * Clears the current channel UI from the scene and updates the inChannel boolean value to
   * indicate the user is no longer in a channel
   */
  private void removeChannel() {
    channelBox.getChildren().clear();
    inChannel = false;
  }

  /**
   * Requests the list of active channels from server
   */
  private void communicatorSend() {
    communicator.send("LIST");
    timer.schedule(task = new TimerTask() {
      @Override
      public void run() {
        communicatorSend();
      }
    }, 2000);
  }

  /**
   * Checks the received message from the server and acts accordingly
   *
   * @param message message sent from the server
   */
  public void checkMessage(String message) {
    //Splits the message at the first space, used to figure out what kind of message has been sent
    String[] messageSplit = message.split(" ", 2);

    //Checks the kind of message received and acts accordingly
    switch (messageSplit[0]) {

      //If the message contains "CHANNELS", update the channel list Scroll Pane with the new/deleted channels
      case "CHANNELS" -> {
        //Split the message on every channel
        String[] channelsSplit = messageSplit[1].split("\\R");

        //Clear the channels currently being displayed
        channelsList.clear();

        //Add each new channel to the channel list Scroll Pane
        for (String channel : channelsSplit) {
          channelsList.add(channel.strip());
        }
      }

      //If the message contains "MSG", send the received message to the ChannelWindow message chat
      case "MSG" -> Platform.runLater(() -> channelWindow.receiveMessage(messageSplit[1]));

      //If the message contains "USERS", update the channel window with the current users
      case "USERS" -> Platform.runLater(() -> channelWindow.updateUsers(messageSplit[1]));

      //If the message contains "ERROR", update the appropriate variables to deal with the error
      case "ERROR" -> Platform.runLater(() -> {
        if (messageSplit[1].strip().equals("You are already in a channel")) {
          //If the error is that the user is already in a channel, update the inChannel value
          inChannel = true;
        }
      });

      //If the message contains "PARTED", remove the current channel window UI component
      // and update appropriate variables
      case "PARTED" -> Platform.runLater(this::removeChannel);

      //If the message contains "START", start a new multiplayer game
      case "START" -> Platform.runLater(() -> {
        //Stop the background music
        Multimedia.stopBackgroundMusic();

        //Cancel the channel updating timer
        timer.cancel();

        //Clear all listeners from the communicator
        communicator.clearListeners();

        //Start the new multiplayer game with all the users in the channel
        gameWindow.startMultiplayerGame(channelWindow.getUsers(), username);
      });

      //If the message contains "HOST", update the channel privileges and set the current user to host
      case "HOST" -> Platform.runLater(() -> channelWindow.setHostTrue());

      //If the message contains "NICK", update the user's nickname
      case "NICK" -> Platform.runLater(() -> username = messageSplit[1]);
    }
  }
}
