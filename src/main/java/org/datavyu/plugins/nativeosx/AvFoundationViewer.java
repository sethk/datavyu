package org.datavyu.plugins.nativeosx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.models.Identifier;
import org.datavyu.plugins.StreamViewerDialog;

import java.awt.*;
import java.io.File;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class AvFoundationViewer extends StreamViewerDialog  {

  /** The logger for this class */
  private static Logger logger = LogManager.getFormatterLogger(AvFoundationViewer.class);

  private AvFoundationPlayer player;
  private final long firstFrameTime;

  AvFoundationViewer(final Identifier identifier, final File sourceFile, final Frame parent, final boolean modal) {
    super(identifier, parent, modal);
    logger.info("Opening file: " + sourceFile.getAbsolutePath());
    player = new AvFoundationPlayer(this, sourceFile);
    firstFrameTime = (long) (player.getCurrentTime() * 1000);
    logger.info("first frame PTS: " + firstFrameTime);
    setSourceFile(sourceFile);
  }

  @Override
  protected void setPlayerVolume(final float volume) {
    logger.debug("Setting Volume to " + volume);
    player.setVolume(volume);
  }

  @Override
  protected Dimension getOriginalVideoSize() {
    logger.debug("Getting Image Dimension");
    return player.getOriginalVideoSize();
  }

  @Override
  public void setCurrentTime(long time) {
    logger.info("Set time to: " + time + " milliseconds.");
    // Need to force the stream to start from the first frame PTS
    // to guarantee a correct frame jogging
    if (time <= 0 && !Double.isNaN(firstFrameTime)) {
      time = firstFrameTime;
    }
    player.setCurrentTime(time / 1000.0);
  }

  @Override
  public void setCurrentFrame(int frame) {
    throw new NotImplementedException();
  }

  @Override
  public void setRate(final float rate) {
    logger.info("Setting playback speed to: " + rate + "X");
    playBackRate = rate;
    if(isSeekPlaybackEnabled()){
      player.setMute(true);
    }else{
      player.setMute(false);
    }
    if (rate == 0) {
      // AV Foundation will set the rate to 0x when stopped
      stop();
      return;
    } else if (rate == 1) {
      // Need to call start to set the State to PLAYING
      start();
      return;
    } else {
      player.setPlaybackSpeed(rate);
    }
  }

  @Override
  protected float getPlayerFramesPerSecond() {
    logger.debug("Getting the video Frame Per Second");
    return (float) player.getFPS();
  }

  @Override
  public long getDuration() {
    logger.debug("Getting video duration");
    return (long) (player.getDuration() * 1000);
  }

  @Override
  public long getCurrentTime() {
    return (long) (player.getCurrentTime() * 1000);
  }

  @Override
  public void start() {
    if(!isPlaying()){
      logger.info("Starting the video");
      player.play();
    }
  }

  @Override
  public void stop() {
    if(isPlaying()){
      logger.info("Stopping the video");
      player.stop();
    }
  }

  @Override
  public void pause() {
    if(isPlaying()){
      logger.info("Pausing the video");
      player.pause();
    }
  }

  @Override
  public boolean isPlaying() {
    return player.isPlaying();
  }

  @Override
  protected void cleanUp() {
    logger.info("Destroying the Player");
    player.cleanUp();
  }

  @Override
  public void stepForward() {
    logger.info("Step forward");
    player.stepForward();
  }

  @Override
  public void stepBackward() {
    logger.info("Step backward");
    player.stepBackward();
  }

  @Override
  public boolean isSeekPlaybackEnabled() { return player.isSeekPlaybackEnabled(); }

  @Override
  public boolean isStepEnabled() { return false; }
}
