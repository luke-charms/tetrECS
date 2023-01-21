package uk.ac.soton.comp1206.event;

/**
 * The Game Loop Listener is used to handle the event when the Game Loop is restarted. It * passes
 * the boolean value of true when the Game Loop is restarted
 */
public interface GameLoopListener {

  /**
   * Handle game loop method being restarted
   *
   * @param bool value to show if gameLoop was restarted
   */
  public void gameLoop(boolean bool);
}
