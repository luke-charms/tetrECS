package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import javafx.util.Duration;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {


  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
      Color.TRANSPARENT,
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.YELLOW,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE,
      Color.AQUA,
      Color.AQUAMARINE,
      Color.BLUE,
      Color.MEDIUMPURPLE,
      Color.PURPLE
  };

  private final GameBoard gameBoard;

  private final double width;
  private final double height;

  /**
   * The column this block exists as in the grid
   */
  private final int x;

  /**
   * The row this block exists as in the grid
   */
  private final int y;

  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;

    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);
  }

  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
      Number newValue) {
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    //If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
    }

    //If the block is on the current piece PieceBoard, place an indicator on the centre block
    if (gameBoard.getCurrentPieceBoard() && getX() == 1 && getY() == 1) {
      paintIndicator();
    }
  }

  /**
   * Paint this canvas empty
   */
  private void paintEmpty() {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Fill
    gc.setFill(Color.DIMGREY);
    gc.setEffect(new Blend());
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.ANTIQUEWHITE);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Paint this canvas with the given colour
   *
   * @param colour the colour to paint
   */
  private void paintColor(Paint colour) {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Colour fill
    gc.setFill(colour);
    gc.fillRect(0, 0, width, height);

    //Effects to make blocks more unique and futuristic
    gc.setEffect(new BoxBlur(2, 2, 2));

    //Triangle to put on top of block to make it look nice

    gc.setFill(colour);
    gc.setEffect(new DropShadow());
    gc.fillPolygon(new double[]{0, width, height},
        new double[]{width, height, 0}, 3);

    //Border
    gc.setStroke(Color.ANTIQUEWHITE);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Paint the centre block of the current piece PieceBoard with a small circle indicator in the
   * middle
   */
  private void paintIndicator() {
    var gc = getGraphicsContext2D();

    gc.setFill(Color.LIGHTSLATEGREY);
    gc.fillOval(getWidth() / 4, getHeight() / 4, width / 2, height / 2);
  }

  /**
   * Paint the block currently being hovered by the mouse a special hover colour
   */
  public void paintHover() {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Colour fill
    gc.setFill(Color.LIGHTGREY);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.ANTIQUEWHITE);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Fade out effect applied to a row of blocks that are cleared
   */
  public void fadeOutAT() {
    var gc = getGraphicsContext2D();

    //Colour fill
    gc.setFill(Color.LIMEGREEN);
    gc.fillRect(0, 0, width, height);

    //Fade transition to make blocks fade.  Transition is reversed after to allow the blocks
    //to become visible again and not invisible on the board
    FadeTransition ft = new FadeTransition(Duration.millis(250), this);
    ft.setFromValue(1.0);
    ft.setToValue(0);
    ft.setAutoReverse(true);
    ft.setCycleCount(2);
    ft.play();

        /*
        //Fade animation that is applied to a row of blocks that is cleared. Animation slowly
        //decreases opacity until block colour is not visible anymore.
        final double[] opacity = {1};
        var gc = getGraphicsContext2D();
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(0,0, width, height);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (opacity[0] > 0) {
                    opacity[0] = opacity[0] - 0.0003;
                    gc.setGlobalAlpha(opacity[0]);
                } else {
                    stop();
                }
            }
        };
        timer.start();

          */

    //Paint blocks empty
    paintEmpty();
  }


  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing its colour
   *
   * @return value
   */
  public int getValue() {
    return this.value.get();
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

}
