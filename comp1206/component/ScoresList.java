package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * A ScoresList is a visual component to represent the stored scores of all the local games as well
 * as all the multiplayer games.
 * <p>
 * It extends a ListView to hold a list of Pair values of Strings and Integers, to represent the
 * username and the score associated with that username.
 */
public class ScoresList extends ListView<Pair<String, Integer>> {

  /**
   * Create a new ScoresList, based off a set of given scores.
   *
   * @param scores the scores to be displayed by the ScoresList
   */
  public ScoresList(ObservableList<Pair<String, Integer>> scores) {
    super(scores);
    this.getStyleClass().add("scorelist");
  }

  /**
   * Animate the ScoresList to reveal the scores
   */
  public void reveal() {
    //Fade Transition to slowly fade in the scores
    FadeTransition ft = new FadeTransition(Duration.millis(2500), this);
    ft.setFromValue(0);
    ft.setToValue(1.0);
    ft.play();
  }
}
