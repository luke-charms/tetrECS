package uk.ac.soton.comp1206.event;

/**
 * The Game Over Listener is used to handle the event when the game is over. It passes the boolean
 * value of true when the game is over.
 */
public interface GameOverListener {

  /**
   * Handle if game is over event
   *
   * @param bool value to show if game is over
   */
  public void gameOver(boolean bool);

}
