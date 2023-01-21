package uk.ac.soton.comp1206.component;

import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 * A component that holds all the scores of the people playing a multiplayer match.
 * <p>
 * Each person's score in the lobby is updated via the leaderboard and then presented as a
 * ScoresList component at the end of the multiplayer match.
 * <p>
 * LeaderBoard extends the component ScoresList to present the player scores.
 */
public class Leaderboard extends ScoresList {

  public Leaderboard(ObservableList<Pair<String, Integer>> localScores) {
    super(localScores);
  }
}