package org.datavyu.plugins.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import org.datavyu.Datavyu;
import org.datavyu.plugins.MediaPlayer;
import org.datavyu.plugins.PlayerStateEvent;
import org.datavyu.views.VideoController;

import java.awt.*;
import java.io.File;
import java.net.URI;

public class JavaFxApplication extends Application {

    private MediaPlayer mediaPlayer;
    private final Object readyLock = new Object();
    private URI mediaPath;
    private Stage stage;

    public JavaFxApplication(File mediaFile) {
        this.mediaPath = mediaFile.toURI();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        mediaPlayer = new JavaFxMediaPlayer(mediaPath, primaryStage, readyLock);
        mediaPlayer.init();
        Datavyu.getVideoController().getClockTimer().registerPlayer(mediaPlayer);
        Task<Void> waitingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                synchronized (readyLock) {
                    readyLock.wait();
                }

                Platform.runLater(() -> handler());
                return null;
            }
        };
        new Thread(waitingTask).start();
    }

    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume);
    }

    public Dimension getOriginalVideoSize() {
        return new Dimension(mediaPlayer.getImageWidth(), mediaPlayer.getImageHeight());
    }

    public void setCurrentTime(double streamTime) {
        mediaPlayer.seek(streamTime);
    }

    public double getFPS() {
        return mediaPlayer.getFps();
    }

    public double getDuration() {
        return mediaPlayer.getDuration();
    }

    public double getCurrentTime() {
        return mediaPlayer.getPresentationTime();
    }

    public void play() {
        mediaPlayer.play();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    boolean isPlaying() {
        return mediaPlayer.getState() == PlayerStateEvent.PlayerState.PLAYING;
    }

    public void cleanUp() {
        mediaPlayer.dispose();
    }

    public void setPlaybackSpeed(float rate) {
        mediaPlayer.setRate(rate);
    }

    public void setMute(final boolean newMute) {
        mediaPlayer.setMute(newMute);
    }

    public boolean isMute() {
        return mediaPlayer.getMute();
    }

    public void setScale(double scale) {
        stage.setHeight(mediaPlayer.getImageHeight() * scale);
        stage.setWidth(mediaPlayer.getImageWidth() * scale);
    }

    public void setVisible(final boolean visible) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!visible) {
                    mediaPlayer.setMute(true);
                    stage.hide();
                } else {
                    mediaPlayer.setMute(false);
                    stage.show();
                }
            }
        });
    }

    public void stepForward() {
        mediaPlayer.stepForward();
    }

    public void stepBackward() {
        mediaPlayer.stepBackward();
    }

    public Object getJavaFxReadyLock(){
        return readyLock;
    }

    private void handler(){
        stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                VideoController videoController = Datavyu.getVideoController();
                switch (event.getCode()){
                    case DIVIDE: {
                        if(Datavyu.getPlatform().equals(Datavyu.Platform.MAC)){
                            videoController.pressShowTracksSmall();
                        }else{
                            videoController.pressPointCell();
                        }
                        break;
                    }
                    case EQUALS:{
                        if(Datavyu.getPlatform().equals(Datavyu.Platform.MAC)){
                            videoController.pressPointCell();
                        }
                        break;
                    }
                    case MULTIPLY:{
                        if (!Datavyu.getPlatform().equals(Datavyu.Platform.MAC)) {
                            videoController.pressShowTracksSmall();
                        }
                        break;
                    }
                    case NUMPAD7:{
                        videoController.pressSetCellOnset();
                        break;
                    }
                    case NUMPAD8:{
                        videoController.pressPlay();
                        break;
                    }
                    case NUMPAD9:{
                        videoController.pressSetCellOffsetNine();
                        break;
                    }
                    case NUMPAD4:{
                        videoController.pressShuttleBack();
                        break;
                    }
                    case NUMPAD5:{
                        videoController.pressStop();
                        break;
                    }
                    case NUMPAD6:{
                        videoController.pressShuttleForward();
                        break;
                    }
                    case NUMPAD1:{
                        videoController.jogBackAction();
                        break;
                    }
                    case NUMPAD2:{
                        videoController.pressPause();
                        break;
                    }
                    case NUMPAD3:{
                        videoController.jogForwardAction();
                        break;
                    }
                    case NUMPAD0:{
                        videoController.pressCreateNewCellSettingOffset();
                        break;
                    }
                    case DECIMAL:{
                        videoController.pressSetCellOffsetPeriod();
                        break;
                    }
                    case SUBTRACT:{
                        if(event.getCode() == KeyCode.CONTROL){
                            videoController.clearRegionOfInterestAction();
                        } else {
                            videoController.pressGoBack();
                        }
                    }
                    case ADD:{
                        if (event.getCode() == KeyCode.SHIFT){
                            videoController.findOffsetAction();
                            videoController.pressFind();
                        } else if(event.getCode() == KeyCode.CONTROL){
                            // IMPORTANT: Don't change the order of
                            // the video controller calls, it will break
                            // the features.
                            videoController.setRegionOfInterestAction();
                            videoController.pressFind();
                        } else {
                            videoController.pressFind();
                        }
                    }
                    case ENTER:{
                        videoController.pressCreateNewCell();
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }
        });
    }
}
