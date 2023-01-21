package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Following Piece Listener is used to handle the event when the following-piece is updated. It
 * passes the GamePiece that was updated as the following-piece.
 */
public interface FollowingPieceListener {

  /**
   * Handle the following-piece being updated
   *
   * @param gamePiece the new following-piece
   */
  public void followingPiece(GamePiece gamePiece);
}