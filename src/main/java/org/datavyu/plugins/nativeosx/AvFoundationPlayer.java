package org.datavyu.plugins.nativeosx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;

import java.awt.*;
import java.io.File;

public class AvFoundationPlayer {

  private static Logger logger = LogManager.getLogger(AvFoundationPlayer.class);


  private AVFoundationMediaPlayer mediaPlayer;
  
  AvFoundationPlayer(AvFoundationViewer viewer, File mediaFile) {
    try {
      mediaPlayer = new AVFoundationMediaPlayer(mediaFile.toURI(), viewer);
      mediaPlayer.init();
      Datavyu.getVideoController().getClockTimer().registerPlayer(mediaPlayer);
    }catch (Exception e) {
      logger.error("Cannot initialize MPV player due to error: ", e);
    }
  }

  public void setVolume(float volume) {
    mediaPlayer.setVolume(volume);
  }

  public double getDuration() {
    return mediaPlayer.getDuration();
  }

  public Dimension getOriginalVideoSize() {
    return new Dimension(mediaPlayer.getImageWidth(), mediaPlayer.getImageHeight());
  }

  public void setMute(final boolean newMute) {
    mediaPlayer.setMute(newMute);
  }

  public void stop() { mediaPlayer.stop(); }

  public void setPlaybackSpeed(float rate) {
    mediaPlayer.setRate(rate);
  }

  public void setCurrentTime(double streamTime) {
    mediaPlayer.seek(streamTime);
  }

  public double getCurrentTime() {
    return mediaPlayer.getPresentationTime();
  }

  public void play() {
    mediaPlayer.play();
  }

  public void pause() { mediaPlayer.pause();}

  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  public void cleanUp() {
    mediaPlayer.dispose();
  }

  public double getFPS() {
    return mediaPlayer.getFps();
  }

  public void stepForward() {
    mediaPlayer.stepForward();
  }

  public void stepBackward() {
    mediaPlayer.stepBackward();
  }
}
