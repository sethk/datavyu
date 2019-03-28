package org.datavyu.plugins.ffmpegplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;
import org.datavyu.models.Identifier;
import org.datavyu.plugins.StreamViewerDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import org.datavyu.util.ClockTimer;

public class FFmpegStreamViewer extends StreamViewerDialog {

    /** The logger for this class */
    private static Logger logger = LogManager.getFormatterLogger(FFmpegStreamViewer.class);

    /** The player this viewer is displaying */
    private FFmpegPlayer player;

    private ClockTimer clockTimer;

    /** Currently is seeking */
    private boolean isSeeking = false;

    FFmpegStreamViewer(final Identifier identifier, final File sourceFile, final Frame parent, final boolean modal) {
        super(identifier, parent, modal);
        logger.info("Opening file: " + sourceFile.getAbsolutePath());
        player = new FFmpegPlayer(this, sourceFile);
        setSourceFile(sourceFile);
        clockTimer = Datavyu.getVideoController().getClockTimer();
        clockTimer.registerListener(this);
    }

    private void launch(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeLater(task);
            } catch (Exception e) {
                logger.error("Failed task. Error: ", e);
            }
        }
    }

    @Override
    protected void setPlayerVolume(float volume) {
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
        launch(() -> {
            try {
                if (!isSeeking) {
                    isSeeking = true;
                    logger.info("Set time to: " + time + " milliseconds.");
                    player.setCurrentTime(time / 1000.0);
                    isSeeking = false;
                }
            } catch (Exception e) {
                logger.error("Unable to set time to " + time + " milliseconds, due to error: ", e);
            }
        });
    }

    @Override
    public void setCurrentFrame(int frame) {
        launch(() -> {
            try {
                if (!isSeeking) {
                    isSeeking = true;
                    logger.info("Set Frame to: " + frame);
                    player.setCurrentFrame(frame);
                    isSeeking = false;
                }
            } catch (Exception e) {
                logger.error("Unable to set frame to " + frame + ", due to error: ", e);
            }
        });
    }

    @Override
    public void start() {
        launch(() -> {
            if (!isPlaying()) {
                logger.info("Starting the video");
                player.play();
            }
        });
    }

    @Override
    public void stop() {
        launch(() -> {
            if (isPlaying()) {
                logger.info("Stopping the video");
                player.stop();
            }
        });
    }

    @Override
    public void pause() {
        launch(() -> {
            if (isPlaying()) {
                logger.info("Pausing the video");
                player.pause();
            }
        });
    }

    @Override
    public void setRate(float speed) {
        logger.info("Setting playback speed to: " + speed + "X");
        launch(() -> {
            playBackRate = speed;
            if(isSeekPlaybackEnabled()){
                player.setMute(true);
            }else{
                player.setMute(false);
            }
            if (speed == 0) {
                player.stop();
            } else {
                player.setPlaybackSpeed(speed);
            }
        });
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
      double playerTime = player.getCurrentTime();
      if (Double.isNaN(playerTime)) {
          return -1; // return a negative value in case of NaN
      }
      return (long) (playerTime * 1000);
    }

    @Override
    protected void cleanUp() {
        logger.info("Destroying the Player");
        player.cleanUp();
    }

    @Override
    public void stepForward() {
        logger.info("Step forward");
        launch(() -> player.stepForward());
    }

    @Override
    public void stepBackward() {
        logger.info("Step backward");
        launch(() -> player.stepBackward());
    }

    @Override
    protected void resizeVideo(float scale) {
        super.resizeVideo(scale);
        logger.info("Resizing video to scale %2.2f", scale);
        player.setScale(scale);
        notifyChange();
    }

    @Override
    public boolean isStepEnabled() { return true; }

    @Override
    public boolean isPlaying() { return player != null && player.isPlaying(); }

    @Override
    public boolean isSeekPlaybackEnabled() { return getRate() < 0F
                                                || getRate() > 8F; }
}
