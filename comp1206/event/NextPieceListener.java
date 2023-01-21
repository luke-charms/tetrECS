package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece listener is used to handle the event when the next-piece is updated. It passes the
 * GamePiece that was updated as the next-piece.
 */
public interface NextPieceListener {

  /**
   * Handle the next-piece being updated
   *
   * @param gamePiece the new next-piece
   */
  public void nextPiece(GamePiece gamePiece);
}