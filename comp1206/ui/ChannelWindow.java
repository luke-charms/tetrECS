package uk.ac.soton.comp1206.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * The Channel Window is a custom UI component that displays and holds all the methods associated
 * with the multiplayer channel
 * <p>
 * A new Channel Window is created each time the player enters a new channel in the lobby scene of
 * the multiplayer aspect of the game
 * <p>
 * The Channel Window extends the VBox class so that it can hold the various other components
 * associated with the channel inside it
 */
public class ChannelWindow extends VBox {

  /**
   * Communicator used to communicate with the server
   */
  private final Communicator communicator;

  /**
   * Observable list of Strings used to hold the users currently in the channel
   */
  private final ObservableList<String> users = FXCollections.observableArrayList();

  /**
   * boolean value used to show if the current user is the host of the channel or not
   */
  private boolean host;

  /**
   * TextFlow component used to hold the messages sent and received in the channel
   */
  private TextFlow messages;

  /**
   * HBox used to hold the start game and leave channel buttons
   */
  private HBox buttonBox;

  /**
   * Button used to leave the current channel the user is in
   */
  private Button endGameButton;


  /**
   * Create a new Channel Window Component
   *
   * @param communicator communicator used to communicate with server
   * @param host         boolean value of host
   */
  public ChannelWindow(Communicator communicator, boolean host) {
    this.communicator = communicator;
    this.host = host;

    build();
  }

  /**
   * Build the Channel Window UI component
   */
  private void build() {
    //Create a text box with the username text
    Text usernameText = new Text("");
    usernameText.getStyleClass().add("playerBox");

    //Add a listener to the observable array list with usernames in it
    // to start an event if an update is observed
    users.addListener((ListChangeListener<String>) user -> {
      //Get the username of the newly added user
      String username = user.toString()
          .substring(user.toString().indexOf("[") + 1, user.toString().indexOf("]"));

      while (user.next()) {
        //Checks to see if a user was added
        if (user.wasAdded()) {
          Platform.runLater(() -> {
            //Create a new String variable with the new users currently in the channel
            String userText = usernameText.getText() + " " + username;

            //Set the username text box to this String variable
            usernameText.setText(userText);
          });

          //Checks to see if a user was removed
        } else if (user.wasRemoved()) {
          //If a user was removed from the channel, set the username text box to empty
          Platform.runLater(() -> usernameText.setText(""));
        }
      }
    });

    //Create a text field to hold a message to be sent to the server
    TextField messageToSend = new TextField();
    messageToSend.setPromptText("Send a message");

    //Handle the event of the user pressing the enter key on the text field
    messageToSend.setOnKeyPressed((event -> {
      if (event.getCode() != KeyCode.ENTER) {
        return;
      }

      //Send the current message within the text field box to the server
      communicator.send("MSG " + messageToSend.getText());

      //Clear the text field box
      messageToSend.clear();
    }));

    //Create a new Text Flow component to hold all the server messages
    messages = new TextFlow();
    messages.setMaxHeight(1000);
    messages.getStyleClass().add("TextFlow");

    //Create a label to hold a single message received from the server
    Label message = new Label("");
    messages.getChildren().add(message);

    //Create a new Scroll Pane to hold the Text Flow component
    ScrollPane scroller = new ScrollPane();
    scroller.setFitToWidth(true);
    scroller.setFitToHeight(true);
    scroller.setPrefViewportHeight(300);
    scroller.setPrefViewportWidth(400);
    scroller.setContent(messages);
    scroller.getStyleClass().add("messages");

    //Add the username text box and the Scroll Pane containing the messages to the Channel Window
    this.getChildren().addAll(usernameText, scroller);

    //Create a new VBox to hold the text field containing the message to send
    var messageBox = new VBox(10);
    messageBox.getChildren().add(messageToSend);

    //Create a new HBox to hold the 2 buttons in the channel window
    buttonBox = new HBox(330);

    //Check to see if the current user is the host
    if (host) {
      //If they are, create a Start Game button
      hostButton();
    }

    //Create an End Game button that leaves the current server
    endGameButton = new Button("Leave game");

    //Add it to the HBox containing the buttons
    buttonBox.getChildren().add(endGameButton);
    buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

    //Handle the event of clicking on the End Game Button, which lets the user leave the current channel
    endGameButton.setOnAction(event -> communicator.send("PART"));

    //Add the button HBox to the bottom VBox
    messageBox.getChildren().add(buttonBox);

    //Style this Channel Window
    this.getStyleClass().add("channel");
    this.getChildren().add(messageBox);
    VBox.setMargin(usernameText, new Insets(10));
    VBox.setMargin(messageBox, new Insets(10));
    VBox.setMargin(scroller, new Insets(10));
  }

  /**
   * Creates a Start Game button if the current user is the host of the channel
   */
  private void hostButton() {
    //Clear the current buttons in the button HBox
    buttonBox.getChildren().clear();

    //Create a new Start Game button
    var startGameButton = new Button("Start game");
    buttonBox.getChildren().add(startGameButton);

    //Create a new End Game button
    endGameButton = new Button("Leave game");
    buttonBox.getChildren().add(endGameButton);
    buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

    //Handle the event of clicking on the End Game Button, which lets the user leave the current channel
    endGameButton.setOnAction(event -> communicator.send("PART"));

    //Handle the event of clicking on the Start Game Button, which lets the user start a new multiplayer game
    startGameButton.setOnAction(event -> communicator.send("START"));
  }

  /**
   * Receives messages from the server and formats them correctly to display in the Chat Box
   *
   * @param message message sent from the server
   */
  public void receiveMessage(String message) {
    //Creates a timestamp for the newest received message from the server
    String timeStamp = new SimpleDateFormat("HH:mm").format(new Date());

    //Formats the received message correctly
    Text receivedMessage = new Text("<" + timeStamp + "> " + message + "\n");

    Platform.runLater(() -> {
      //Checks to see if the current message is a user trying to change their nickname and not
      // an actual message
      if (message.split(":")[1].split(" ")[0].equals("/nick")) {

        //Change the user's current nickname
        String nickname = message.split(":")[1].split(" ")[1];

        //Send the new nickname to the server
        communicator.send("NICK " + nickname);
      } else {
        //Otherwise, add this message to the TextFlow
        messages.getChildren().add(receivedMessage);
      }
    });
  }

  /**
   * Updates the users currently in this channel
   *
   * @param userList list of users in current channel
   */
  public void updateUsers(String userList) {
    //Clear the currently stored users
    users.clear();

    //Split the users into their individual usernames
    String[] usersSplit = userList.split("\\R");

    //Add each username to the stored users array list
    for (String user : usersSplit) {
      users.add(user.strip());
    }
  }

  /**
   * Sets the boolean value of host to true to indicate current user is host of channel
   */
  public void setHostTrue() {
    host = true;
    hostButton();
  }

  /**
   * Returns the current users within the channel
   *
   * @return current users in channel
   */
  public ObservableList<String> getUsers() {
    return users;
  }
}
