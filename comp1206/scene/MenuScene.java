package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * The TETRECS title image
   */
  private ImageView title;


  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /**
   * Build the menu layout
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Creates a Game Pane to hold the base StackPane of the UI
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane to hold the mainPane of the UI
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    //Main Borderpane to hold all the other various UI components
    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    //Awful TETRECS title
    title = new ImageView(new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/TetrECS.png"))
            .toExternalForm()));
    title.setPreserveRatio(true);
    title.setFitWidth(600);
    title.setFitHeight(400);

    //HBox to hold TETRECS image and centre it on the scene
    var imageBox = new HBox();
    imageBox.getChildren().add(title);
    imageBox.setAlignment(Pos.CENTER);
    imageBox.setPadding(new Insets(120));

    //Add the image HBox to the Border Pane
    mainPane.setTop(imageBox);

    //Create a new button that starts the single player game
    var singlePlayerButton = new Button("Single Player");
    singlePlayerButton.getStyleClass().add("menuItem");
    //Bind the single player button action to the startGame method in the menu scene
    singlePlayerButton.setOnAction(this::startGame);
    //Hover effect that activates when the mouse enters the button
    singlePlayerButton.setOnMouseEntered(
        mouseEvent -> singlePlayerButton.getStyleClass().add("menuItem:hover"));

    //Create a new button that starts the multiplayer lobby scene
    var multiPlayerButton = new Button("Multi Player");
    multiPlayerButton.getStyleClass().add("menuItem");
    //Bind the multiplayer button action to the multiGame method in the menu scene
    multiPlayerButton.setOnAction(this::startMultiGame);
    //Hover effect that activates when the mouse enters the button
    multiPlayerButton.setOnMouseEntered(
        mouseEvent -> multiPlayerButton.getStyleClass().add("menuItem:hover"));

    //Create a new button that starts the instructions scene
    var howPlayButton = new Button("How to Play");
    howPlayButton.getStyleClass().add("menuItem");
    //Bind the howToPlay button action to the howToPlay method in the menu scene
    howPlayButton.setOnAction(this::startHowToPlay);
    //Hover effect that activates when the mouse enters the button
    howPlayButton.setOnMouseEntered(
        mouseEvent -> howPlayButton.getStyleClass().add("menuItem:hover"));

    //Create a new button that exits and stops the game
    var exitButton = new Button("Exit");
    exitButton.getStyleClass().add("menuItem");
    //Bind the exit button action to the endGame method in the menu scene
    exitButton.setOnAction(this::endGame);
    //Hover effect that activates when the mouse enters the button
    exitButton.setOnMouseEntered(mouseEvent -> exitButton.getStyleClass().add("menuItem:hover"));

    //Create a VBox to hold all the buttons
    var buttonSelect = new VBox();
    buttonSelect.setSpacing(10);
    buttonSelect.getChildren()
        .addAll(singlePlayerButton, multiPlayerButton, howPlayButton, exitButton);
    buttonSelect.setAlignment(Pos.CENTER);

    //Add the button VBox to the Border Pane
    mainPane.setCenter(buttonSelect);
    BorderPane.setMargin(buttonSelect, new Insets(10));
  }


  /**
   * Initialise the menu
   */
  @Override
  public void initialise() {
    //Check to see if music is not already playing
    if (!Multimedia.getPlayingMusic()) {
      //If not, start the background game music
      Multimedia.playBackgroundMusic("Menu Music.mp3");
    }

    //Method to animate the shaking of the game title
    animateTitle();

    //keyboard listeners to allow the user to press escape to exit the game
    scene.setOnKeyPressed(keyEvent -> {
        if (keyEvent.getCode() != KeyCode.ESCAPE) {
            return;
        }
      App.shutdown();
    });
  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    Multimedia.stopBackgroundMusic();
    gameWindow.startChallenge();
  }

  /**
   * Handle when the Start MultiPlayer Game button is pressed
   *
   * @param event event
   */
  private void startMultiGame(ActionEvent event) {
    gameWindow.startMultiChallenge();
  }

  /**
   * Handle when the Instructions button is pressed
   *
   * @param event event
   */
  private void startHowToPlay(ActionEvent event) {
    gameWindow.startInstructionsScene();
  }

  /**
   * Handle when the End Game button is pressed
   *
   * @param event event
   */
  private void endGame(ActionEvent event) {
    App.shutdown();
  }

  /**
   * Animates the TETRECS title to slightly shake when entering menu scene
   */
  public void animateTitle() {
    //Create a new Rotation transition animation on the TETRECS title
    var rt = new RotateTransition(Duration.millis(4000), title);
    rt.setFromAngle(-10);
    rt.setByAngle(20);
    rt.setCycleCount(Animation.INDEFINITE);
    rt.setAutoReverse(true);
    rt.play();
  }
}
