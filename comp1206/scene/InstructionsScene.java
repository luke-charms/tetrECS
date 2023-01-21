package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Instructions Scene. Holds the UI for the instructions on how to play the game and the various
 * pieces that exist within the game
 */
public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new Instructions scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  /**
   * Initialise the scene
   */
  @Override
  public void initialise() {
    //keyboard listeners to allow the user to press escape to exit the instructions
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() != KeyCode.ESCAPE) {
        return;
      }
      gameWindow.startMenu();
    });
  }

  /**
   * Build the Instructions window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Creates a Game Pane to hold the base StackPane of the UI
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane to hold the mainPane of the UI
    var instructionsPane = new StackPane();
    instructionsPane.setMaxWidth(gameWindow.getWidth());
    instructionsPane.setMaxHeight(gameWindow.getHeight());
    instructionsPane.getStyleClass().add("menu-background");
    root.getChildren().add(instructionsPane);

    //Main Borderpane to hold all the other various UI components
    var mainPane = new BorderPane();
    instructionsPane.getChildren().add(mainPane);

    //Creates the scene text title "Instructions"
    var instructions = new Text("Instructions");
    instructions.getStyleClass().add("heading");

    //Creates the information text that describes how to play TetrECS
    var instructionsText = new Label(
        "TetrECS is a fast-paced gravity-free block placement game, "
            + "where you must survive by clearing rows through careful placement "
            + "of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
    instructionsText.getStyleClass().add("instructions");
    //Sets text wrap to true so text box creates new paragraphs on line end
    instructionsText.setWrapText(true);
    instructionsText.setAlignment(Pos.CENTER);

    //Creates a VBox to hold the instructions title and text box
    var instructionsTextBox = new VBox();
    instructionsTextBox.setAlignment(Pos.CENTER);
    instructionsTextBox.getChildren().addAll(instructions, instructionsText);

    //Set the top of the main Pane as the instructions VBox
    mainPane.setTop(instructionsTextBox);
    BorderPane.setMargin(instructionsTextBox, new Insets(5));

    //Create an image of the instructions needed to play TetrECS by getting the image from the '/image' resource folder
    var instructionsImage = new ImageView(new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/Instructions.png"))
            .toExternalForm()));
    instructionsImage.setPreserveRatio(true);
    instructionsImage.setFitWidth(675);
    instructionsImage.setFitHeight(300);
    mainPane.setCenter(instructionsImage);
    BorderPane.setAlignment(instructionsImage, Pos.CENTER);

    //Create a title for the game pieces displayed that can be used within the TetrECS game
    var gamePiecesText = new Text("Game Pieces");
    gamePiecesText.getStyleClass().add("heading");

    //Create a new grid pane to hold all the PieceBoard with the various game pieces in them
    var gridPane = new GridPane();

    //Create an arraylist to hold the 15 different PieceBoards used to display the GamePieces
    var pieces = new ArrayList<PieceBoard>();

    //Create an array of column numbers for the PieceBoards, in order to hold them in a 5x3 formation on the Grid Pane
    int[] columns = new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};

    //Iterate through all 15 GamePieces
    for (int x = 0; x < 15; x++) {
      //Add a new 3x3 PieceBoard to the PieceBoard arraylist
      pieces.add(new PieceBoard(new Grid(3, 3), 50, 50));

      //Get the most recently created PieceBoard and set its GamePiece piece to the current x-value
      pieces.get(x).setPiece(GamePiece.createPiece(x));

      //Set the grid pane constraints to be in a 5x3 formation and add the newly created PieceBoard to the Grid Pane
      GridPane.setConstraints(pieces.get(x), (x % 5), columns[x]);
      gridPane.getChildren().add(pieces.get(x));
    }

    //Adjust some layout settings for the Grid Pane holding the 15 PieceBoards
    gridPane.setAlignment(Pos.CENTER);
    gridPane.setHgap(15);
    gridPane.setVgap(15);

    //Create a VBox to hold the Game Pieces title text and GridPane of PieceBoards
    var botBox = new VBox();
    botBox.setAlignment(Pos.CENTER);
    botBox.getChildren().addAll(gamePiecesText, gridPane);

    //Add the VBox with PieceBoards and title to the main scene pane
    mainPane.setBottom(botBox);
    BorderPane.setMargin(botBox, new Insets(20));
  }
}