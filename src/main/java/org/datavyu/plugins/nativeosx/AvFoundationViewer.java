package org.datavyu.plugins.nativeosx;

import org.datavyu.models.Identifier;
import org.datavyu.plugins.StreamViewerDialog;

import java.awt.*;
import java.io.File;


public class AvFoundationViewer extends StreamViewerDialog {

  AvFoundationPlayer player;

  AvFoundationViewer(final Identifier identifier, final File sourceFile, final Frame parent, final boolean modal) {
    super(identifier, parent, modal);
    player = new AvFoundationPlayer(this, sourceFile);
    setSourceFile(sourceFile);
  }

  @Override
  protected void setPlayerVolume(final float volume) {
    player.setVolume(volume);
  }

  @Override
  protected Dimension getOriginalVideoSize() {
    return player.getOriginalVideoSize();
  }

  @Override
  public void setCurrentTime(long time) {
    player.setCurrentTime(time / 1000.0);
  }

  @Override
  public void setRate(final float rate) {
    playBackRate = rate;
    if(isSeekPlaybackEnabled()){
      player.setMute(true);
    }else{
      player.setMute(false);
    }
    if (rate == 0) {
      player.stop();
    } else {
      player.setPlaybackSpeed(rate);
    }
  }

  @Override
  protected float getPlayerFramesPerSecond() {
    return (float) player.getFPS();
  }

  @Override
  public long getDuration() {
    return (long) (player.getDuration() * 1000);
  }

  @Override
  public long getCurrentTime() {
    return (long) (player.getCurrentTime() * 1000);
  }

  @Override
  public void start() {
    if(!isPlaying()){
      player.play();
    }
  }

  @Override
  public void stop() {
    if(!isPlaying()){
      player.stop();
    }
  }

  @Override
  public boolean isPlaying() {
    return player.isPlaying();
  }

  @Override
  protected void cleanUp() {
    player.cleanUp();
  }

  @Override
  public void stepForward() {
    player.stepForward();
  }

  @Override
  public void stepBackward() {
    player.stepBackward();
  }

  @Override
  public boolean isSeekPlaybackEnabled() { return player.isSeekPlaybackEnabled(); }

  @Override
  public boolean isStepEnabled() { return true; }
}
