package uk.ac.soton.comp1206.ui;

import java.util.Objects;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import uk.ac.soton.comp1206.multimedia.Multimedia;

/**
 * The Settings Box is a custom UI component that displays and holds the settings used to
 * set music volume/mute, ...
 *
 * The settings box extends the HBox class in order to hold various other components associated
 * with the settings box class
 */
public class SettingsBox extends HBox {

  /**
   * CheckBox to register if sound should be playing or not
   */
  private CheckBox sound;

  /**
   * boolean value that shows if settings box is visible or not
   */
  private boolean visible = true;


  /**
   * Create a new SettingsBox component
   */
  public SettingsBox() {
    setPrefHeight(10);
    setSpacing(10);
    getStyleClass().add("settingsBox");
    build();
  }

  /**
   * Build the settings box UI component
   */
  public void build() {

    //Add the settings image opener
    var image = new ImageView(new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/settingscog.png")).toExternalForm()));
    image.setPreserveRatio(true);
    image.setFitWidth(20);

    //Add a button to mute the sound
    sound = new CheckBox("Mute");
    sound.selectedProperty().bindBidirectional(Multimedia.audioEnabled);

    getChildren().addAll(sound, image);
    setAlignment(Pos.CENTER_RIGHT);

    toggleSettingsBox();

    image.setOnMouseClicked((mouseEvent -> toggleSettingsBox()));
  }

  /**
   * Animate the settings box opening and closing
   */
  private void toggleSettingsBox() {
    if(visible) {
      visible = false;
      for(var child : getChildren()) {
        if(child instanceof ImageView) continue; //Don't hide the settings box image
        child.setVisible(false);
      }
      Duration duration = Duration.millis(500);
      Timeline timeline = new Timeline(
          new KeyFrame(duration,new KeyValue(this.prefHeightProperty(), 10, Interpolator.EASE_BOTH))
      );
      timeline.play();
    } else {
      visible = true;
      Duration duration = Duration.millis(500);
      Timeline timeline = new Timeline(
          new KeyFrame(duration,new KeyValue(this.prefWidthProperty(), 50, Interpolator.EASE_BOTH))
      );
      timeline.play();
      timeline.setOnFinished((e) -> {
        for(var child : getChildren()) {
          child.setVisible(true);
        }
      });
    }
  }
}
