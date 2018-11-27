package org.datavyu.util;


import org.datavyu.plugins.ffmpeg.MediaPlayer;

public interface Subject {

    /**
     * method to register an observer, in our case is a MediaPlayer
     * @param mediaPlayer the media Player to be registered
     */
    void register(MediaPlayer mediaPlayer);

    /**
     * method to unregister an observer, in our case is a MediaPlayer
     * @param mediaPlayer The Media Player to be unregistered
     */
    void unregister(MediaPlayer mediaPlayer);

    /**
     * Method to notify registered media players of a change
     * @param type the type of the change
     */
    void notifyObservers(ClockTimer.EventType type);

    /**
     * Method to get time updates from the Clock timer
     * @param mediaPlayer
     * @return Object
     */
    Object getTimeUpdate(MediaPlayer mediaPlayer);

    /**
     * Method to get min time updates from the Clock timer
     * @param mediaPlayer
     * @return Object
     */
    Object getMinTimeUpdate(MediaPlayer mediaPlayer);

    /**
     * Method to get max time updates from the Clock timer
     * @param mediaPlayer
     * @return Object
     */
    Object getMaxTimeUpdate(MediaPlayer mediaPlayer);
}
