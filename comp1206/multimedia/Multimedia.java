package uk.ac.soton.comp1206.multimedia;

import java.util.Objects;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Multimedia class is responsible for all background music and sound playing within the actual
 * game itself.
 * <p>
 * Its methods are static so that they can be run anywhere without the need to create a Multimedia
 * class
 */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * boolean value that represents whether the game audio is enabled or not
   */
  public static SimpleBooleanProperty audioEnabled = new SimpleBooleanProperty(true);

  /**
   * MediaPlayer variable that is responsible for the playing of background music
   */
  private static MediaPlayer backgroundPlayer;

  /**
   * MediaPlayer variable that is responsible for the playing of sounds
   */
  private static MediaPlayer soundMediaPlayer;

  /**
   * boolean value that represents whether music is playing currently
   */
  private static boolean playingMusic = false;

  /**
   * Plays the requested background music from the '/music' resource folder
   *
   * @param music name of the music to be played
   */
  public static void playBackgroundMusic(String music) {

    audioEnabled.addListener(observable -> {
      backgroundPlayer.setMute(!audioEnabled.get());
      soundMediaPlayer.setMute(!audioEnabled.get());
    });

    //Get the full pathname of the requested audio
    String toPlay = Objects.requireNonNull(Multimedia.class.getResource("/music/" + music))
        .toExternalForm();

    try {
      //Try to make the requested audio a media object
      Media play = new Media(toPlay);

      //Set the backgroundPlayer MediaPlayer to the requested music
      backgroundPlayer = new MediaPlayer(play);

      //Set the backgroundPlayer to start the music over if it reaches the end
      backgroundPlayer.setOnEndOfMedia(() -> backgroundPlayer.seek(Duration.ZERO));

      //Play the requested media object
      backgroundPlayer.play();

      //Set the boolean playingMusic to true, as game is playing music
      playingMusic = true;

    } catch (Exception e) {
      //Catch any errors which may occur here
      e.printStackTrace();
      logger.error(e.toString());
    }
  }

  /**
   * Stops playing the background music and sets playingMusic to false
   */
  public static void stopBackgroundMusic() {
    backgroundPlayer.stop();
    playingMusic = false;
  }

  /**
   * Plays the requested sound from the '/sound' resource folder
   *
   * @param sound name of sound to be played
   */
  public static void playSound(String sound) {
    //Get the full pathname of the requested audio
    String toPlay = Objects.requireNonNull(Multimedia.class.getResource("/sounds/" + sound))
        .toExternalForm();

    try {
      //Try to make the requested audio a media object
      Media play = new Media(toPlay);

      //Set the soundMediaPlayer MediaPlayer to the requested sound
      soundMediaPlayer = new MediaPlayer(play);

      //Play the sound
      soundMediaPlayer.play();
    } catch (Exception e) {
      //Catch any errors which may occur here
      e.printStackTrace();
      logger.error(e.toString());
    }
  }

  /**
   * Returns the currently playing music
   *
   * @return currently playing music
   */
  public static boolean getPlayingMusic() {
    return playingMusic;
  }
}