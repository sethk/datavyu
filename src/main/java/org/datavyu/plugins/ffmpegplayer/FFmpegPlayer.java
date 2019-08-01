package org.datavyu.plugins.ffmpegplayer;

import java.awt.event.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.plugins.MediaPlayer;
import org.datavyu.plugins.PlayerStateEvent;
import org.datavyu.plugins.SdlKeyEventListener;
import org.datavyu.plugins.ffmpeg.FfmpegSdlMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FFmpegPlayer extends JPanel {

  /** Identifier for object serialization */
  private static final long serialVersionUID = 5109839668203738974L;

  /** The logger for this class */
  private static Logger logger = LogManager.getFormatterLogger(FFmpegPlayer.class);
	
	/** The movie stream for this movie player */
	private MediaPlayer mediaPlayer;
	private SdlKeyEventListener keyEventListener;

	/**
	 * Construct an FFmpegPlayer by creating the underlying movie stream provider
	 * and registering stream listeners for the video and audio. The stream
	 * listener for the video will show the image in this JPanel.
	 *
	 * @param viewer The ffmpeg viewer
     * @param sourceFile The source file
	 */
	FFmpegPlayer(FFmpegStreamViewer viewer, File sourceFile) {
		setLayout(new BorderLayout());
		try {
			mediaPlayer = new FfmpegSdlMediaPlayer(sourceFile.toURI());
			mediaPlayer.init();
			keyEventListener = (source, nativeMediaRef, javaKeyCode) -> {
        logger.debug("Player dispatching event " + javaKeyCode);
        viewer.dispatchEvent(new KeyEvent(
            viewer,
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0,
            javaKeyCode,
            KeyEvent.CHAR_UNDEFINED,
            KeyEvent.KEY_LOCATION_NUMPAD));
      };
      mediaPlayer.addSdlKeyEventListener(keyEventListener);
		}catch (Exception e) {
			logger.error("Cannot initialize ffmpeg player due to error: ", e);
		}
	}

	/**
	 * Get the duration of the opened video/audio stream of this player in seconds.
	 *
	 * @return Duration of the opened stream.
	 */
	public double getDuration() {	return mediaPlayer.getDuration();	}

	/**
	 * Get the original stream size (not the size when a viewing window is used).
	 *
	 * @return Original stream size: width, height.
	 */
	public Dimension getOriginalVideoSize() {
		return new Dimension(mediaPlayer.getImageWidth(), mediaPlayer.getImageHeight());
	}

	/**
	 * Get the current time in seconds.
	 *
	 * @return Current time in seconds.
	 */
	public double getCurrentTime() { return mediaPlayer.getPresentationTime(); }

	/**
	 * Seek to the position.
	 *
	 * @param position Position in seconds.
	 */
	public void setCurrentTime(double position) { mediaPlayer.seek(position); }

	/**
	 * Seek to a frame index.
	 *
	 * @param frame index.
	 */
	public void setCurrentFrame(int frame) { mediaPlayer.seekToFrame(frame); }

	/**
	 * Clean up the player before closing.
	 */
	public void cleanUp() {
    if (keyEventListener != null) {
      mediaPlayer.removeSdlKeyEventListener(keyEventListener);
		}
		mediaPlayer.dispose();
	}

	/**
	 * Set the start back speed for this player.
	 *
	 * @param playbackSpeed The start back speed.
	 */
	public void setPlaybackSpeed(float playbackSpeed) { mediaPlayer.setRate(playbackSpeed); }

	/**
	 * Play the video/audio.
	 */
	public void play() { mediaPlayer.play(); }

	/**
	 * Stop the video/audio.
	 */
	public void stop() { mediaPlayer.stop(); }

	/**
	 * Pause the video/audio.
	 */
	public void pause() {	mediaPlayer.pause(); }

	@Deprecated
	public void setScale(float scale) {
		// TODO(fraudies): Implement me
  }

	/**
	 * Instead of isPlaying a sequence of frames just step by one frame.
	 */
	public void stepForward() { mediaPlayer.stepForward(); }

	public void stepBackward() { mediaPlayer.stepBackward(); }

	/**
	 * Set the audio volume.
	 *
	 * @param volume New volume to set.
	 */
	public void setVolume(float volume) {	mediaPlayer.setVolume(volume); }

	public void setMute(final boolean newMute) { mediaPlayer.setMute(newMute); }

	public boolean isMute() { return mediaPlayer.getMute(); }

	boolean isPlaying() { return mediaPlayer.getState() == PlayerStateEvent.PlayerState.PLAYING; }

  public double getFPS() { return mediaPlayer.getFps();	}

	public void setViewerVisible(final boolean isVisible) {
		if (isVisible) {
			mediaPlayer.showSDLWindow();
		} else {
			mediaPlayer.hideSDLWindow();
		}
	}

	public int getViewerWidth() {
		return mediaPlayer.getWindowWidth();
	}
	public int getViewerHeight() {
		return mediaPlayer.getWindowHeight();
	}

	public void setViewerSize(final int width, final int height) {
		mediaPlayer.setWindowSize(width, height);
	}
}
