package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.FollowingPieceListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.multimedia.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  protected static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * The values of the score, level, lives and multiplier
   */
  protected final SimpleIntegerProperty score = new SimpleIntegerProperty();
  protected final SimpleIntegerProperty level = new SimpleIntegerProperty();
  protected final SimpleIntegerProperty lives = new SimpleIntegerProperty();
  protected final SimpleIntegerProperty multiplier = new SimpleIntegerProperty();

  /**
   * The random number generator used for GamePiece generation
   */
  protected final Random random = new Random();

  /**
   * The timer used to schedule game loop method
   */
  protected final Timer timer = new Timer();

  /**
   * Number of rows
   */
  protected final int rows;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;

  /**
   * The listener to call when the next-piece is updated
   */
  protected NextPieceListener nextPieceListener;

  /**
   * The listener to call when the following-piece is updated
   */
  protected FollowingPieceListener followingPieceListener;

  /**
   * The listener to call when a line is cleared
   */
  protected LineClearedListener lineClearedListener;

  /**
   * The listener to call when the game loop method is restarted
   */
  protected GameLoopListener gameLoopListener;

  /**
   * The listener to call when the game is over
   */
  protected GameOverListener gameOverListener;

  /**
   * The current GamePiece
   */
  protected GamePiece currentPiece;

  /**
   * The following GamePiece
   */
  protected GamePiece followingPiece;

  /**
   * The timer task assigned to the timer which calls the game loop method
   */
  protected TimerTask task;

  /**
   * boolean value used to stop the timer
   */
  protected boolean bStop = false;

  /**
   * The value used to hold the current selected GameBlock x-value using keyboard inputs
   */
  protected SimpleIntegerProperty xAim = new SimpleIntegerProperty();

  /**
   * The value used to hold the current selected GameBlock y-value using keyboard inputs
   */
  protected SimpleIntegerProperty yAim = new SimpleIntegerProperty();


  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Returns the value of the current game score
   *
   * @return game score
   */
  public IntegerProperty getScore() {
    return this.score;
  }

  /**
   * Sets the value of the game score
   *
   * @param score new game score
   */
  public void setScore(int score) {
    this.score.set(score);
  }

  /**
   * Returns the value of the current game level
   *
   * @return game level
   */
  public IntegerProperty getLevel() {
    return this.level;
  }

  /**
   * Sets the value of the game level
   *
   * @param level new game score
   */
  public void setLevel(int level) {
    this.level.set(level);
  }

  /**
   * Returns the value of the current game lives
   *
   * @return game lives
   */
  public IntegerProperty getLives() {
    return this.lives;
  }

  /**
   * Sets the value of the game lives
   *
   * @param lives new game score
   */
  public void setLives(int lives) {
    this.lives.set(lives);
  }

  /**
   * Returns the value of the current game multiplier
   *
   * @return game multiplier
   */
  public IntegerProperty getMultiplier() {
    return this.multiplier;
  }

  /**
   * Sets the value of the game multiplier
   *
   * @param multiplier new game score
   */
  public void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");
    logger.info("""
        Setting score to: 0
        level: 0
        lives: 3
        multiplier: 1""");

    //Setting the appropriate game values
    setScore(0);
    setLevel(0);
    setLives(3);
    setMultiplier(1);
    setXAim(0);
    setYAim(0);

    //Schedule the game loop timer to start with a duration of the calculated timer delay
    timer.schedule(task = new TimerTask() {
      @Override
      public void run() {
        gameLoop();
      }
    }, getTimerDelay());

    //Set game loop listener to true to indicate the game loop timer has started
    gameLoopListener.gameLoop(true);

    //Set the current GamePiece to a randomly generated GamePiece
    currentPiece = spawnPiece();

    //Set the following GamePiece to a randomly generated GamePiece
    followingPiece = spawnPiece();

    //Update the Next Piece Listener and the Following Piece Listener
    nextPieceListener.nextPiece(currentPiece);
    followingPieceListener.followingPiece(followingPiece);

    logger.info("Everything setup!");
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    //Try and place the current piece at the position of the clicked GameBlock
    if (grid.playPiece(currentPiece, x, y)) {
      logger.info("Piece played!");

      //If piece was played, play the appropriate "YES" sound
      Multimedia.playSound("place.wav");

      //Reset the game loop timer
      task.cancel();
      timer.schedule(task = new TimerTask() {
        @Override
        public void run() {
          gameLoop();
        }
      }, getTimerDelay());

      //Update the game loop listener
      gameLoopListener.gameLoop(true);

      //Fetch the next piece
      nextPiece();

      //Update the Next Piece Listener and the Following Piece Listener
      nextPieceListener.nextPiece(currentPiece);
      followingPieceListener.followingPiece(followingPiece);

      //Check for any lines that need to be cleared
      afterPiece();
    } else {
      //Play the appropriate "piece cannot be played" sound
      Multimedia.playSound("fail.wav");

      logger.info("Invalid Location!");
    }
  }

  /**
   * Check to see if any lines need to be cleared
   */
  protected void afterPiece() {
    //Initialise two boolean variables to update if lines need to be cleared
    boolean clearRow = false;
    boolean clearCol = false;

    //Initialise two array lists to hold the rows and columns that need to be cleared
    ArrayList<Integer> row = new ArrayList<>();
    ArrayList<Integer> col = new ArrayList<>();

    //Iterate through the rows to check if horizontal lines need to be cleared
    for (int x = 0; x < cols; x++) {
      int counter = 0;
      for (int y = 0; y < rows; y++) {
        //If the value of the grid is 0, skip over the row
        if (grid.get(x, y) == 0) {
          break;
        }
        //If not, add up the number of blocks with values in them
        counter++;
      }
      //If the counter with the number of blocks with values is equal
      // to the number of rows on the grid, set boolean clearRow to true and save the row number
      if (counter == rows) {
        clearRow = true;
        row.add(x);
      }
    }

    //Repeat for columns and iterate through the columns to check if
    // vertical lines need to be cleared
    for (int y = 0; y < rows; y++) {
      int counter = 0;
      for (int x = 0; x < cols; x++) {
        if (grid.get(x, y) == 0) {
          break;
        }
        counter++;
      }
      if (counter == cols) {
        //If any columns were found with block values equal to the number columns, set
        // the boolean clearCol to true and save the column number
        clearCol = true;
        col.add(y);
      }
    }

    //Check to see if there are both columns and rows that need clearing
    if (clearRow && clearCol) {
      for (Integer rows : row) {
        clearRow(rows);
      }
      for (Integer cols : col) {
        clearCol(cols);
      }

      //Play the clear line sound
      Multimedia.playSound("clear.wav");

      //Set the game score to the calculated value of lines and blocks  cleared, taking
      // into account any extra blocks that may have been counted twice
      setScore(getScore().get() + score(row.size() + col.size(),
          ((row.size() + col.size()) * 5) - 1));

      //Set the game multiplier to one value higher than the previous
      setMultiplier(getMultiplier().get() + 1);

      //Otherwise, check to see if only any rows need to be cleared
    } else if (clearRow) {
      for (Integer rows : row) {
        clearRow(rows);
      }

      //Play the clear line sound
      Multimedia.playSound("clear.wav");

      //Set the game score to the calculated value of lines and number of blocks cleared
      setScore(getScore().get() + score(row.size(), (row.size()) * 5));

      //Set the game multiplier to one value higher than the previous
      setMultiplier(getMultiplier().get() + 1);

      //Otherwise, check to see if only any columns need to be cleared
    } else if (clearCol) {
      for (Integer cols : col) {
        clearCol(cols);
      }

      //Play the clear line sound
      Multimedia.playSound("clear.wav");

      //Set the game score to the calculated value of lines and number of blocks cleared
      setScore(getScore().get() + score(col.size(), (col.size()) * 5));

      //Set the game multiplier to one value higher than the previous
      setMultiplier(getMultiplier().get() + 1);
    } else {
      //Otherwise, set the multiplier back to 1
      setMultiplier(1);
    }

    //Check to see if enough points were earned to increase the game level
    levelCheck();
  }

  /**
   * Checks to see if enough points were scored to increase the game level
   */
  protected void levelCheck() {
    if (getScore().get() >= ((getLevel().get() + 1) * 1000)) {

      //Play level up sound
      Multimedia.playSound("level.wav");

      //Set the level to one plus the previous level
      setLevel(getLevel().get() + 1);
    }
  }

  /**
   * Takes a row and clears all the blocks in that row
   *
   * @param row row to be cleared
   */
  protected void clearRow(int row) {
    //Creates a set to hold the game block coordinates
    Set<GameBlockCoordinate> set = new HashSet<>();
    for (int x = 0; x < cols; x++) {
      if (x == row) {
        for (int y = 0; y < rows; y++) {
          //Add the valid block coordinate to the hashset
          set.add(new GameBlockCoordinate(x,y));

          //Set the grid value to 0 (empty)
          grid.set(x, y, 0);
        }
      }
    }
    //Pass and update the Line Cleared Listener with the Hashset
    lineClearedListener.lineClear(set);
  }

  /**
   * Takes a column and clears all the blocks in that column
   *
   * @param col column to be cleared
   */
  protected void clearCol(int col) {
    //Creates a set to hold the game block coordinates
    Set<GameBlockCoordinate> set = new HashSet<>();
    for (int y = 0; y < rows; y++) {
      if (y == col) {
        for (int x = 0; x < cols; x++) {
          //Add the valid block coordinate to the hashset
          set.add(new GameBlockCoordinate(x,y));

          //Set the grid value to 0 (empty)
          grid.set(x, y, 0);
        }
      }
    }
    //Pass and update the Line Cleared Listener with the Hashset
    lineClearedListener.lineClear(set);
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
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
   * Creates and returns a random GamePiece generated using the random variable
   *
   * @return a randomly generated GamePiece
   */
  public GamePiece spawnPiece() {
    return GamePiece.createPiece(random.nextInt(0, 15));
  }

  /**
   * Used to generate the next piece after a GamePiece is played.
   */
  public void nextPiece() {
    //Set current piece as the following piece and create a new following piece
    currentPiece = followingPiece;
    followingPiece = spawnPiece();

    //Update both piece listeners
    nextPieceListener.nextPiece(currentPiece);
    followingPieceListener.followingPiece(followingPiece);
  }

  /**
   * Swaps the current piece with the following piece
   */
  public void swapCurrentPiece() {
    //Play the appropriate piece swap sound
    Multimedia.playSound("rotate.wav");

    //Store the current piece in a preliminary GamePiece variable
    GamePiece prelimPiece = currentPiece;

    //Swap the current piece with the following piece
    currentPiece = followingPiece;

    //Set the following piece as the preliminary GamePiece (current piece)
    followingPiece = prelimPiece;

    //Update both piece listeners
    nextPieceListener.nextPiece(currentPiece);
    followingPieceListener.followingPiece(followingPiece);
  }

  /**
   * Rotates the current piece once anti-clockwise
   */
  public void rotateCurrentPieceAnti() {
    //Play the appropriate piece rotation sound
    Multimedia.playSound("rotate.wav");

    //Rotate the piece once anti-clockwise
    currentPiece.rotate(3);

    //Update the Next Piece Listener
    nextPieceListener.nextPiece(currentPiece);
  }

  /**
   * Rotates the current piece once clockwise
   */
  public void rotateCurrentPieceClock() {
    //Play the appropriate piece rotation sound
    Multimedia.playSound("rotate.wav");

    //Rotate the piece once clockwise
    currentPiece.rotate();

    //Update the Next Piece Listener
    nextPieceListener.nextPiece(currentPiece);
  }

  /**
   * Method used to calculate the correct score in terms of lines and blocks cleared
   *
   * @param numOfLines  number of lines cleared
   * @param numOfBlocks number of blocks cleared
   * @return the calculated game score
   */
  public int score(int numOfLines, int numOfBlocks) {
    return (numOfLines * numOfBlocks * 10 * getMultiplier().get());
  }

  /**
   * Method to calculate the correct length of time before the game loop method should fire
   *
   * @return the calculated time
   */
  public int getTimerDelay() {
    return Math.max((12000 - 500 * getLevel().get()), 2500);
  }

  /**
   * Method that fires once the game timer gets to 0 after scheduling.
   * <p>
   * The player loses a life, the multiplier is set to 1, and a new piece is generated.
   */
  public void gameLoop() {
    //Play appropriate life lost sound
    Multimedia.playSound("explode.wav");

    //Set the lives to one less than before
    setLives(getLives().get() - 1);

    //Set multiplier back to 1
    setMultiplier(1);

    //Generate a new GamePiece
    nextPiece();

    //Check to see if player lives is less than 0
    if (getLives().get() < 0) {
      //If true, timer should stop executing...
      bStop = true;

      //...and a message should be sent to the Challenge Scene
      Platform.runLater(this::gameOver);
    }

    //Check to see if timer should continue scheduling a new Timer Task
    if (!bStop) {
      //If not, cancel the previous task...
      task.cancel();

      //...and schedule a new task/game loop.
      timer.schedule(task = new TimerTask() {
        @Override
        public void run() {
          gameLoop();
        }
      }, getTimerDelay());

      //Update the Game Loop Listener
      gameLoopListener.gameLoop(true);
    }
  }

  /**
   * Cancels the timer once the player lives reaches below 0
   */
  public synchronized void cancelTimer() {
    timer.cancel();
  }

  /**
   * Updates the Game Over Listener to indicate the game is over
   */
  public synchronized void gameOver() {
    gameOverListener.gameOver(true);
  }

  /**
   * Updates the x-value of the selected GameBlock with the input value
   *
   * @param change number to update the x-value with
   */
  public void changeXAim(int change) {
    int newXAim = xAim.get() + change;
    if (newXAim >= 0 && newXAim < 5) {
      xAim.set(newXAim);
    }
  }

  /**
   * Updates the y-value of the selected GameBlock with the input value
   *
   * @param change number to update the y-value with
   */
  public void changeYAim(int change) {
    int newYAim = yAim.get() + change;
    if (newYAim >= 0 && newYAim < 5) {
      yAim.set(newYAim);
    }
  }

  /**
   * Returns the x-value of the selected GameBlock block
   *
   * @return x-value of selected GameBlock
   */
  public IntegerProperty getXAim() {
    return xAim;
  }

  /**
   * Sets the x-value of the selected GameBlock
   *
   * @param xAim number to set the x-value to
   */
  public void setXAim(int xAim) {
    this.xAim.set(xAim);
  }

  /**
   * Returns the y-value of the selected GameBlock block
   *
   * @return y-value of selected GameBlock
   */
  public IntegerProperty getYAim() {
    return yAim;
  }

  /**
   * Sets the y-value of the selected GameBlock
   *
   * @param yAim number to set the y-value to
   */
  public void setYAim(int yAim) {
    this.yAim.set(yAim);
  }

  /**
   * Set the listener to handle an event when the next-piece is updated
   *
   * @param listener listener to add
   */
  public void setNextPieceListener(NextPieceListener listener) {
    nextPieceListener = listener;
  }

  /**
   * Set the listener to handle an event when the following-piece is updated
   *
   * @param listener listener to add
   */
  public void setFollowingPieceListener(FollowingPieceListener listener) {
    followingPieceListener = listener;
  }

  /**
   * Set the listener to handle an event when a line is cleared
   *
   * @param listener listener to add
   */
  public void setLineClearedListener(LineClearedListener listener) {
    lineClearedListener = listener;
  }

  /**
   * Set the listener to handle an event when a game loop is restarted
   *
   * @param listener listener to add
   */
  public void setGameLoopListener(GameLoopListener listener) {
    gameLoopListener = listener;
  }

  /**
   * Set the listener to handle an event when the game is over
   *
   * @param listener listener to add
   */
  public void setGameOverListener(GameOverListener listener) {
    gameOverListener = listener;
  }

  /**
   * Method used by subclass MultiPlayerGame to add a GamePiece to the queue of pieces
   *
   * @param i the number value of the GamePiece to be added
   */
  public void addPiece(int i) {
  }

}