package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import java.util.Set;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Line Cleared Listener is used to handle the event when a line is cleared in the Game. It
 * passes the coordinates of the line of blocks that were cleared
 */

public interface LineClearedListener {

  /**
   * Handle the event of a line of blocks cleared
   *
   * @param line coordinates of line of blocks cleared
   */
  public void lineClear(Set<GameBlockCoordinate> line);
}
