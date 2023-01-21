package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.multimedia.Multimedia;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display
 * of the contents of the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside
 * the grid.
 * <p>
 * The Grid should be linked to a GameBoard for its display.
 */
public class Grid {

  /**
   * The number of columns in this grid
   */
  private final int cols;

  /**
   * The number of rows in this grid
   */
  private final int rows;

  /**
   * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
   */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    //Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x     column
   * @param y     row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      //Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      //No such index
      return -1;
    }
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Checks to see if a GamePiece can be placed at the specified coordinate. Returns true if it can
   * and false if it can not.
   *
   * @param gamePiece GamePiece trying to be placed
   * @param valX      x-value of the coordinate
   * @param valY      y-value of the coordinate
   * @return boolean, if piece can be played or not
   */
  public boolean canPlayPiece(GamePiece gamePiece, int valX, int valY) {
    valX = valX - 1;  //offset coordinate of block x-value
    valY = valY - 1;  //offset coordinate of block y-value

    int[][] blocks = gamePiece.getBlocks();  //the 3x3 array of int blocks of GamePiece

    //Loop through every part of the game piece
    for (int x = 0; x < blocks.length; x++) {
      for (int y = 0; y < blocks[x].length; y++) {
        int val = blocks[x][y];
        if (val == 0) {
          //if there isn't a block, ignore
          continue;
        }
        //Get the grid value of the block where the GamePiece is trying to be placed
        int gridVal = get(x + valX, y + valY);

        //If the grid value is not equal to 0, return false, the piece cannot be played
        if (gridVal != 0) {
          return false;
        }
      }
    }
    //No blocks in the way, piece can be played
    return true;
  }

  /**
   * Called after canPlayPiece to actually play the piece into the specified coordinate clicked on
   * by the player
   *
   * @param gamePiece GamePiece trying to be placed
   * @param valX      x-value of the coordinate
   * @param valY      y-value of the coordinate
   * @return boolean, if piece was played
   */
  public boolean playPiece(GamePiece gamePiece, int valX, int valY) {
    //Check to see if piece can actually be played
    if (!canPlayPiece(gamePiece, valX, valY)) {
      //Tell game piece was not played!
      return false;
    }

    valX = valX - 1;  //offset coordinate of block x-value
    valY = valY - 1;  //offset coordinate of block y-value
    int[][] blocks = gamePiece.getBlocks();  //the 3x3 array of int blocks

    //Loop through every part of the game piece
    for (int x = 0; x < blocks.length; x++) {
      for (int y = 0; y < blocks[x].length; y++) {
        int val = blocks[x][y];
        if (val == 0) {
          //if there isn't a block, ignore
          continue;
        }
        //Update the grid of the game board with the piece
        set(x + valX, y + valY, val);

      }
    }
    //Return true to indicate piece was played
    return true;
  }
}