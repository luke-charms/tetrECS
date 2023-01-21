package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javafx.animation.FillTransition;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.ui.SettingsBox;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

  protected static final Logger logger = LogManager.getLogger(ChallengeScene.class);

  /**
   * The game object that is currently being played
   */
  protected Game game;

  /**
   * The mainPane that holds all the UI components
   */
  protected BorderPane mainPane;

  /**
   * Text that holds the current game score for the current scene
   */
  protected Text scoreText;

  /**
   * Text that holds the title for the current Scene
   */
  protected Text title;

  /**
   * VBox to hold all the game info (lives, multiplier, etc...) on the right side of the scene
   */
  protected VBox gameInfoBox;

  /**
   * GameBoard that holds the GameBlock blocks
   */
  protected GameBoard board;

  /**
   * GameBlock that is currently selected using the keyboard input
   */
  protected GameBlock selectedBlock;

  /**
   * boolean value that informs scene if game is a multiplayer game
   */
  protected boolean multiPlayerGame = false;

  /**
   * Create a new Single Player challenge scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Sets up a new game with the specified rows and columns
    setupGame();

    //Creates a Game Pane to hold the base StackPane of the UI
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane to hold the mainPane of the UI
    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    //Main Borderpane to hold all the other various UI components
    mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    //Set the GameBoard board component to a new GameBoard with the created Game grid
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    mainPane.setCenter(board);

    //Create the score title and text box and bind the game score integer to it. Add the appropriate style class
    scoreText = new Text("SCORE");
    Text bindScore = new Text();
    bindScore.textProperty().bind(Bindings.convert(game.getScore()));
    scoreText.getStyleClass().add("heading");
    bindScore.getStyleClass().add("score");

    //Vbox to hold the score text box and bound game score text box
    VBox scoresBox = new VBox();
    scoresBox.getChildren().addAll(scoreText, bindScore);
    scoresBox.setAlignment(Pos.CENTER);

    //Create the lives text box and bind the game lives' integer to it. Add the appropriate style class
    Text livesText = new Text("LIVES");
    Text bindLives = new Text();
    bindLives.textProperty().bind(Bindings.convert(game.getLives()));
    livesText.getStyleClass().add("heading");
    bindLives.getStyleClass().add("lives");

    //Vbox to hold the lives text box and bound game lives text box
    VBox livesBox = new VBox();
    livesBox.getChildren().addAll(livesText, bindLives);
    livesBox.setAlignment(Pos.CENTER);

    //Create the current scene title
    title = new Text("Challenge Mode");
    title.getStyleClass().add("title");

    //HBox to hold the score VBox, lives VBox and scene title
    HBox topBox = new HBox(75);
    topBox.setPadding(new Insets(10, 10, 10, 10));
    topBox.setAlignment(Pos.CENTER);
    topBox.getChildren().addAll(scoresBox, title, livesBox);
    topBox.setMaxWidth(gameWindow.getWidth());

    //Create the high-score text box and bind the game high-score to it. Add the appropriate style class
    Text highScoreText = new Text("HIGH SCORE");
    Text bindHighScore = new Text();
    //Get the current stored game high score
    bindHighScore.textProperty().setValue(getHighScore());
    highScoreText.getStyleClass().add("heading");
    bindHighScore.getStyleClass().add("hiscore");

    //Create the level text box and bind the game level integer to it. Add the appropriate style class
    Text levelText = new Text("LEVEL");
    Text bindLevel = new Text();
    bindLevel.textProperty().bind(Bindings.convert(game.getLevel()));
    levelText.getStyleClass().add("heading");
    bindLevel.getStyleClass().add("level");

    //Create the multiplier text box and bind the game multiplier integer to it. Add the appropriate style class
    Text multiText = new Text("MULTIPLIER");
    Text bindMulti = new Text();
    bindMulti.textProperty().bind(Bindings.convert(game.getMultiplier()));
    multiText.getStyleClass().add("heading");
    bindMulti.getStyleClass().add("multi");

    //VBox to hold game high score, level and multiplier text and binding text boxes
    gameInfoBox = new VBox();
    gameInfoBox.setSpacing(5);
    gameInfoBox.getChildren()
        .addAll(highScoreText, bindHighScore, levelText, bindLevel,
            multiText, bindMulti);
    gameInfoBox.setAlignment(Pos.CENTER);

    //Create a PieceBoard component to show the current GamePiece piece
    PieceBoard pieceBoardCurrentPiece = new PieceBoard(new Grid(3, 3), 100, 100);

    //Set the current PieceBoard board to contain the piece passed through the game Next Piece Listener
    game.setNextPieceListener(pieceBoardCurrentPiece::setPiece);

    //Set the current PieceBoard boolean currentPieceBoard value to true
    pieceBoardCurrentPiece.setCurrentPieceBoard(true);

    //Create a smaller PieceBoard component to show the following GamePiece piece
    PieceBoard pieceBoardFollowingPiece = new PieceBoard(new Grid(3, 3), 60, 60);

    //Set the following PieceBoard board to contain the piece passed through the game Following Piece Listener
    game.setFollowingPieceListener(pieceBoardFollowingPiece::setPiece);

    //Set the line cleared listener to call the fadeOut method on the line of blocks cleared
    game.setLineClearedListener(board::fadeOut);

    //Create a 'incoming' title for the current PieceBoard
    Text incomingText = new Text("Incoming");
    incomingText.getStyleClass().add("heading");

    //VBox to hold the gameInfo-Vbox and PieceBoards
    VBox sideBox = new VBox();
    sideBox.setPadding(new Insets(20, 20, 20, 20));
    sideBox.setSpacing(10);
    sideBox.setAlignment(Pos.CENTER);
    sideBox.getChildren()
        .addAll(gameInfoBox, incomingText, pieceBoardCurrentPiece, pieceBoardFollowingPiece);

    //Create a rectangle to be used as the game timer before game loop is called
    Rectangle rectangle = new Rectangle(gameWindow.getWidth(), 5, Color.LIMEGREEN);

    //Create a HBox at the bottom of the scene and add the rectangle timer to it
    HBox botBox = new HBox();
    botBox.setAlignment(Pos.CENTER);
    botBox.getChildren().add(rectangle);

    //Add a settings box to the scene and a VBox to hold it in
    SettingsBox settingsBox = new SettingsBox();
    settingsBox.setMaxWidth(gameWindow.getWidth());
    var topSceneBox = new VBox();
    topSceneBox.getChildren().addAll(settingsBox, topBox);

    //Add the topBox, sideBox and botBox to their various positions on the BorderPane
    mainPane.setTop(topSceneBox);
    mainPane.setRight(sideBox);
    mainPane.setBottom(botBox);

    BorderPane.setMargin(topSceneBox, new Insets(10));

    //Handle block on GameBoard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //Handle a right-click on the GameBoard board
    board.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton() == MouseButton.SECONDARY) {
        //If the registered click was a right click, rotate the current piece
        game.rotateCurrentPieceClock();
      }
    });

    //Handle a click on current PieceBoard to rotate current piece
    pieceBoardCurrentPiece.setOnMouseClicked(mouseEvent -> game.rotateCurrentPieceClock());

    //Handle a click on following PieceBoard to swap current and following piece
    pieceBoardFollowingPiece.setOnMouseClicked(mouseEvent -> game.swapCurrentPiece());

    //Handle an update from the Game Loop
    game.setGameLoopListener(bool -> {
      //If the game loop boolean was updated, and it was true start a new rectangle timer animation
      if (bool) {
        var animation = new Transition() {
          //Set the duration of the animation to the same as the game timer delay
          {
            setCycleDuration(Duration.millis(game.getTimerDelay()));
          }

          /**
           * Method that is called every frame of the animation.
           * The rectangle timer is slowly decreased until it is invisible
           *
           * @param v the current frame the animation is on
           */
          @Override
          protected void interpolate(double v) {
            //Get full width of game window
            double length = gameWindow.getWidth();

            //Take away length of animation left
            double inverse = 1.0 - v;

            //Set the current length of the rectangle to the amount of time left
            double currentLength = length * inverse;
            rectangle.setWidth(currentLength);
          }
        };
        //Create a new fill animation to slowly change the colour of the rectangle as it gets smaller,
        // indicating the timer is about to be finished
        var ft = new FillTransition(Duration.millis(game.getTimerDelay()), rectangle,
            Color.LIMEGREEN, Color.RED);

        //Play both animations
        ft.play();
        animation.play();
      }
    });



    //Handle the game over listener firing, indicating the player has no lives left
    game.setGameOverListener(bool -> {
      if (bool) {
        //If the game is over, start the scores scene
        gameWindow.startScoresScene(game, multiPlayerGame);
      }
    });

    //Handle the x-value of the selected block being updated
    game.getXAim().addListener(observable -> {
      //Clear all previous blocks being painted as 'hovered'
      board.paintAll();

      //Set the new selected block's x and v-values
      selectedBlock = board.getBlock(game.getXAim().get(), game.getYAim().get());
    });

    //Handle the y-value of the selected block being updated
    game.getYAim().addListener(observable -> {
      //Clear all previous blocks being painted as 'hovered'
      board.paintAll();

      //Set the new selected block's x and v-values
      selectedBlock = board.getBlock(game.getXAim().get(), game.getYAim().get());
    });
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clicked
   */
  private void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /**
   * Set up the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");
    //Start new game
    game = new Game(5, 5);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");

    //Start the background music for the scene
    Multimedia.playBackgroundMusic("Non-copyright Tetrics Music.mp3");

    //Start the game
    game.start();

    //keyboard listeners to allow the user to press various keys for various actions
    gameWindow.getScene().setOnKeyPressed(keyEvent -> {

      //If escape key is pressed, cancel the timer, stop the background music and
      // exit out from the challenge scene
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        game.cancelTimer();
        Multimedia.stopBackgroundMusic();
        gameWindow.startMenu();
      }

      //If the right-arrow key or D key is pressed, change the selected block to the one
      // one right of it
      if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
        game.changeXAim(1);
        //Paint the selected GameBlock as hovered
        selectedBlock.paintHover();
      }

      //If the left-arrow key or A key is pressed, change the selected block to the one
      // one left of it
      if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
        game.changeXAim(-1);
        //Paint the selected GameBlock as hovered
        selectedBlock.paintHover();
      }

      //If the up-arrow key or W key is pressed, change the selected block to the one
      // one up of it
      if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
        game.changeYAim(-1);
        //Paint the selected GameBlock as hovered
        selectedBlock.paintHover();
      }

      //If the down-arrow key or S key is pressed, change the selected block to the one
      // one down of it
      if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
        game.changeYAim(1);
        //Paint the selected GameBlock as hovered
        selectedBlock.paintHover();
      }

      //If the enter key or X key is pressed, place the selected block
      if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) {
        game.blockClicked(selectedBlock);
      }

      //If the Q key, Z key or "[" key is pressed, rotate the selected block anti-clockwise
      if (keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z
          || keyEvent.getCode() == KeyCode.OPEN_BRACKET) {
        game.rotateCurrentPieceAnti();
      }

      //If the E key, C key or "]" key is pressed, rotate the selected block clockwise
      if (keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C
          || keyEvent.getCode() == KeyCode.CLOSE_BRACKET) {
        game.rotateCurrentPieceClock();
      }

      //If the SPACE key or R key is pressed, swap the current and following piece
      if (keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
        game.swapCurrentPiece();
      }
    });
  }

  /**
   * Returns the current stored local game high score
   *
   * @return stored high score
   */
  public String getHighScore() {
    try {
      //Try to find the "scores.txt" file
      File input = new File("scores.txt");

      //Create a new scanner object
      Scanner reader = new Scanner(input);

      //Read the second part of the first line of the file, splitting at the "=" regex, as the
      // game high score is stored after it.
      return reader.nextLine().split(" =")[1];
    } catch (IOException e) {
      //Catch any errors here
      e.printStackTrace();

      //Return 0 if no file was found or errors occurred
      return "0";
    }
  }

  /**
   * Sets the boolean value of multiPlayerGame
   *
   * @param bool boolean value to set
   */
  public void setMultiPlayerGame(boolean bool) {
    multiPlayerGame = bool;
  }
}
