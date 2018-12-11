package org.datavyu.plugins.javafx;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.models.Identifier;
import org.datavyu.plugins.StreamViewerDialog;

import java.awt.*;
import java.io.File;

public class JavaFxStreamViewer extends StreamViewerDialog {

    private static Logger logger = LogManager.getFormatterLogger(JavaFxStreamViewer.class);

    JavaFxApplication mediaPlayer;

    /**
     * Constructs a base data video viewer.
     *
     * @param identifier
     * @param parent
     * @param modal
     */
    public JavaFxStreamViewer(Identifier identifier, File mediaFile, final Frame parent, final boolean modal) {
        super(identifier, parent, modal);
        this.mediaPlayer = new JavaFxApplication(mediaFile);

        Platform.runLater(() -> {
            try {
                mediaPlayer.start(new Stage());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });

        synchronized (mediaPlayer.getJavaFxReadyLock()){
            try {
                mediaPlayer.getJavaFxReadyLock().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        setSourceFile(mediaFile);
    }

    @Override
    protected void setPlayerVolume(float volume) {
        mediaPlayer.setVolume(volume);
    }

    @Override
    protected Dimension getOriginalVideoSize() {
        return mediaPlayer.getOriginalVideoSize();
    }

    @Override
    public void setCurrentTime(long time) {
        mediaPlayer.setCurrentTime(time / 1000.0);
    }

    @Override
    public void setRate(final float rate) {
        playBackRate = rate;
        if(isSeekPlaybackEnabled()){
            mediaPlayer.setMute(true);
        }else{
            mediaPlayer.setMute(false);
        }
        if (rate == 0) {
            mediaPlayer.stop();
        } else {
            mediaPlayer.setPlaybackSpeed(rate);
        }
    }

    @Override
    public boolean isSlavePlayer() {
        return false;
    }

    @Override
    protected float getPlayerFramesPerSecond() {
        return (float) mediaPlayer.getFPS();
    }

    @Override
    public long getDuration() {
        return (long) (mediaPlayer.getDuration() * 1000);
    }


    @Override
    public long getCurrentTime() {
        return (long) (mediaPlayer.getCurrentTime() * 1000);
    }


    @Override
    public void start() {
        if(!isPlaying()){
            mediaPlayer.play();
        }
    }

    @Override
    public void stop() {
        if(!isPlaying()){
            mediaPlayer.stop();
        }
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    protected void cleanUp() {
        mediaPlayer.cleanUp();
    }

    @Override
    protected void resizeVideo(final float scale) {
        logger.info("Resize with scale %2.2f", scale);
        mediaPlayer.setScale(scale);
        notifyChange();
    }

    @Override
    public boolean isSeekPlaybackEnabled() {
        return playBackRate > 2F || playBackRate < 0F;
    }

    @Override
    public void setViewerVisible(final boolean isVisible) {
        mediaPlayer.setVisible(isVisible);
        this.isVisible = isVisible;
    }
}
