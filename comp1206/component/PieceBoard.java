package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A PieceBoard is a visual component to represent the smaller visual GameBoard. It extends the
 * GameBoard class to hold a grid of GameBlocks.
 * <p>
 * A PieceBoard is used to hold the smaller current-piece GamePiece as well as the following-piece
 * GamePiece as well.
 */
public class PieceBoard extends GameBoard {

  /**
   * Create a new PieceBoard, which gives the parameters to the GameBoard class to create a new
   * GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid   linked grid
   * @param width  the visual width
   * @param height the visual height
   */
  public PieceBoard(Grid grid, double width, double height) {
    super(grid, width, height);
  }

  /**
   * Sets a specified GamePiece within the piece-board to be displayed on the UI
   *
   * @param gamePiece GamePiece to be set as piece to be displayed
   */
  public void setPiece(GamePiece gamePiece) {
    //the 3x3 array of blocks of the GamePiece
    int[][] blocks = gamePiece.getBlocks();

    //Method to clear the current piece displayed
    clearPiece();

    //Loop through every part of the game-piece
    for (var x = 0; x < blocks.length; x++) {
      for (var y = 0; y < blocks[x].length; y++) {
        int val = blocks[x][y];
        if (val != 0) {
          //if the value is not equal to zero set the piece-board value to the block value
          grid.set(x, y, gamePiece.getValue());
        }
      }
    }
  }

  /**
   * Clears the current piece being displayed in the PieceBoard
   */
  public void clearPiece() {
    for (var x = 0; x < blocks.length; x++) {
      for (var y = 0; y < blocks[x].length; y++) {
        grid.set(x, y, 0);
      }
    }
  }

  /**
   * Returns the boolean value to indicate if PieceBoard is the current-piece GameBoard
   *
   * @return boolean value
   */
  public boolean getCurrentPieceBoard() {
    return this.currentPieceBoard;
  }

  /**
   * Sets the boolean value to indicate if PieceBoard is the current-piece GameBoard
   *
   * @param bool value
   */
  public void setCurrentPieceBoard(boolean bool) {
    this.currentPieceBoard = bool;
  }
}