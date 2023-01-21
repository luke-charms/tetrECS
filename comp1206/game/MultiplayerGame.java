package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * The MultiPlayerGame is a subclass of the Game class, used when playing a multiplayer game.
 * <p>
 * Certain methods are overridden in order to allow the online game to run appropriately.
 */
public class MultiplayerGame extends Game {

  /**
   * Communicator to communicative with web socket and send and receive game messages
   */
  private final Communicator communicator;
  /**
   * ArrayList to hold the upcoming GamePieces sent by the communicator online
   */
  public ArrayList<GamePiece> pieceQueue = new ArrayList<>();



  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols         number of columns
   * @param rows         number of rows
   * @param communicator web socket communicator
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
  }

  /**
   * Overrides the Game superclass start method
   */
  @Override
  public void start() {
    super.start();
  }

  /**
   * Overrides the Game superclass initialiseGame method
   */
  @Override
  public void initialiseGame() {
    super.initialiseGame();
  }

  /**
   * Overrides the Game superclass spawnPiece method in order to update the GamePiece queue with a
   * new GamePiece requested from the server
   *
   * @return a randomly generated GamePiece received from the server
   */
  @Override
  public GamePiece spawnPiece() {
    //Request a new GamePiece piece to add to the queue
    communicator.send("PIECE");

    //Set the next GamePiece in the queue as a preliminary GamePiece variable
    GamePiece piece = pieceQueue.get(0);

    //Remove the next GamePiece in the queue
    pieceQueue.remove(0);

    //Return the next GamePiece in the queue
    return piece;
  }

  /**
   * Adds a new GamePiece piece to the game-piece queue
   *
   * @param i the number value of the GamePiece to be added
   */
  @Override
  public void addPiece(int i) {
    this.pieceQueue.add(GamePiece.createPiece(i));
  }
}
