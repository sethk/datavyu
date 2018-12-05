package org.datavyu.util;


import org.datavyu.plugins.ffmpeg.MediaPlayer;

public interface MasterClock {

    /**
     * method to register an observer, in our case is a MediaPlayer
     * @param mediaPlayer the media Player to be registered
     */
    void registerPlayer(MediaPlayer mediaPlayer);

    /**
     * method to unregister an observer, in our case is a MediaPlayer
     * @param mediaPlayer The Media Player to be unregistered
     */
    void unregisterPlayer(MediaPlayer mediaPlayer);

    /**
     * Method to notify registered media players of a change
     * @param type the type of the change
     */
    void notifyPlayers(ClockTimer.EventType type);
}
