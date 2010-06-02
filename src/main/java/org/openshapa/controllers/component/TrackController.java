package org.openshapa.controllers.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import net.miginfocom.swing.MigLayout;

import org.openshapa.event.component.CarriageEvent;
import org.openshapa.event.component.CarriageEventListener;
import org.openshapa.event.component.TrackMouseEventListener;
import org.openshapa.event.component.CarriageEvent.EventType;

import org.openshapa.models.component.TrackModel;
import org.openshapa.models.component.ViewableModel;
import org.openshapa.models.component.TrackModel.TrackState;

import org.openshapa.views.component.TrackPainter;
import org.openshapa.views.continuous.CustomActionListener;


/**
 * TrackPainterController is responsible for managing a TrackPainter.
 */
public final class TrackController {

    /** Track panel border color. */
    private static final Color BORDER_COLOR = new Color(73, 73, 73);

    /** Main panel holding the track UI. */
    private final JPanel view;

    /** Track label. */
    private final JLabel trackLabel;

    /** Label holding the icon. */
    private final JLabel iconLabel;

    /** Component that paints the track. */
    private final TrackPainter trackPainter;

    /** Right click menu. */
    private final JPopupMenu menu;

    /** Button for (un)locking the track. */
    private final JButton lockUnlockButton;

    /** Unlock icon. */
    private final ImageIcon unlockIcon = new ImageIcon(getClass().getResource(
                "/icons/track-unlock.png"));

    /** Lock icon. */
    private final ImageIcon lockIcon = new ImageIcon(getClass().getResource(
                "/icons/track-lock.png"));

    private final JButton actionButton1;

    private final JButton actionButton2;

    private final JButton actionButton3;

    /** Viewable model. */
    private final ViewableModel viewableModel;

    /** Track model. */
    private final TrackModel trackModel;

    /**
     * Listeners interested in custom playback region events and mouse events on
     * the track.
     */
    private final EventListenerList listenerList;

    /** Listeners interested in custom action button events. */
    private final List<CustomActionListener> buttonListeners;

    /** States. */
    // can the carriage be moved using the mouse when snap is switched on
    private boolean isMoveable;

    /**
     * Creates a new TrackController.
     *
     * @param trackPainter the track painter for this controller to manage.
     */
    public TrackController(final TrackPainter trackPainter) {
        isMoveable = true;

        view = new JPanel();
        view.setLayout(new MigLayout("ins 0", "[]0[]"));
        view.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        this.trackPainter = trackPainter;

        viewableModel = new ViewableModel();
        trackModel = new TrackModel();
        trackModel.setState(TrackState.NORMAL);
        trackModel.setBookmark(-1);
        trackModel.setLocked(false);

        trackPainter.setViewableModel(viewableModel);
        trackPainter.setTrackModel(trackModel);

        listenerList = new EventListenerList();

        buttonListeners = new LinkedList<CustomActionListener>();

        final TrackPainterListener painterListener = new TrackPainterListener();
        trackPainter.addMouseListener(painterListener);
        trackPainter.addMouseMotionListener(painterListener);

        menu = new JPopupMenu();

        JMenuItem setBookmarkMenuItem = new JMenuItem("Set bookmark");
        setBookmarkMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    TrackController.this.setBookmarkAction();
                }
            });

        JMenuItem clearBookmarkMenuItem = new JMenuItem("Clear bookmark");
        clearBookmarkMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    TrackController.this.clearBookmarkAction();
                }
            });
        menu.add(setBookmarkMenuItem);
        menu.add(clearBookmarkMenuItem);

        menu.setName("trackPopUpMenu");

        trackPainter.add(menu);

        // Create the Header panel and its components
        trackLabel = new JLabel("", SwingConstants.CENTER);
        iconLabel = new JLabel("", SwingConstants.CENTER);

        trackLabel.setName("trackLabel");

        final JPanel header = new JPanel(new MigLayout("ins 0, wrap 4"));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        header.setBackground(Color.LIGHT_GRAY);
        header.add(trackLabel, "w 96!, span 4");
        header.add(iconLabel, "span 4, w 96!, h 32!");

        // Set up the button used for locking/unlocking track movement
        lockUnlockButton = new JButton(unlockIcon);
        lockUnlockButton.setContentAreaFilled(false);
        lockUnlockButton.setBorderPainted(false);
        lockUnlockButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    handleLockUnlockButtonEvent(e);
                }
            });
        header.add(lockUnlockButton, "w 20!, h 20!");
        lockUnlockButton.setName("lockUnlockButton");

        // Set up the custom actions buttons
        actionButton1 = new JButton();
        actionButton1.setName("actionButton1");
        actionButton1.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {

                    for (CustomActionListener listener : buttonListeners) {
                        listener.handleActionButtonEvent1(e);
                    }
                }
            });
        header.add(actionButton1, "w 20!, h 20!");

        actionButton2 = new JButton();
        actionButton2.setName("actionButton2");
        actionButton2.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {

                    for (CustomActionListener listener : buttonListeners) {
                        listener.handleActionButtonEvent2(e);
                    }
                }
            });
        header.add(actionButton2, "w 20!, h 20!");

        actionButton3 = new JButton();
        actionButton3.setName("actionButton3");
        actionButton3.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {

                    for (CustomActionListener listener : buttonListeners) {
                        listener.handleActionButtonEvent3(e);
                    }
                }
            });
        header.add(actionButton3, "w 20!, h 20!");

        view.add(header, "w 100!, h 75!");

        // Create the Carriage panel
        view.add(trackPainter, "w 662!, h 75!");
    }

    /**
     * Sets the track information to use.
     * @param icon
     *            Icon to use with this track. {@code null} if no icon.
     * @param trackName
     *            Name of this track
     * @param trackId
     *            Absolute path to the track's data feed
     * @param duration
     *            Duration of the data feed in milliseconds
     * @param offset
     *            Offset of the data feed in milliseconds
     */
    public void setTrackInformation(final ImageIcon icon,
        final String trackName, final String trackId, final long duration,
        final long offset) {

        if (icon != null) {
            iconLabel.setIcon(icon);
        }

        trackModel.setTrackName(trackName);
        trackModel.setTrackId(trackId);
        trackModel.setDuration(duration);
        trackModel.setOffset(offset);
        trackModel.setErroneous(false);
        trackLabel.setText(trackName);
        trackLabel.setToolTipText(trackName);
        trackPainter.setTrackModel(trackModel);
    }

    /**
     * Sets the track offset in milliseconds.
     *
     * @param offset
     *            Offset of the data feed in milliseconds
     */
    public void setTrackOffset(final long offset) {
        trackModel.setOffset(offset);
        trackPainter.setTrackModel(trackModel);
    }

    /**
     * Indicate that the track's information cannot be resolved.
     *
     * @param erroneous true if the data is erroneous, false otherwise.
     */
    public void setErroneous(final boolean erroneous) {
        trackModel.setErroneous(erroneous);
        trackPainter.setTrackModel(trackModel);
    }

    /**
     * Add a bookmark location to the track. Does not take track offsets into
     * account.
     *
     * @param bookmark
     *            bookmark position in milliseconds
     */
    public void addBookmark(final long bookmark) {

        if ((0 <= bookmark) && (bookmark <= trackModel.getDuration())) {
            trackModel.setBookmark(bookmark);
            trackPainter.setTrackModel(trackModel);
        }
    }

    /**
     * Add a bookmark location to the track. Track offsets are taken into
     * account. This call is the same as addBookmark(position - offset).
     *
     * @param position
     *            temporal position in milliseconds to bookmark.
     */
    public void addTemporalBookmark(final long position) {
        addBookmark(position - trackModel.getOffset());
    }

    /**
     * Sets the state of the track model.
     *
     * @param state the new state to set.
     */
    private void setState(final TrackState state) {
        trackModel.setState(state);
        trackPainter.setTrackModel(trackModel);
    }

    /**
     * @return True if the track is selected, false otherwise.
     */
    public boolean isSelected() {
        return trackModel.isSelected();
    }

    /**
     * @return True if the track is locked, false otherwise.
     */
    public boolean isLocked() {
        return trackModel.isLocked();
    }

    /**
     * @return Offset in milliseconds.
     */
    public long getOffset() {
        return trackModel.getOffset();
    }

    /**
     * @return Returns the duration of the track in milliseconds. Does not take
     *         into account any offsets.
     */
    public long getDuration() {
        return trackModel.getDuration();
    }

    /**
     * @return Bookmarked position in milliseconds. Does not take into account
     *         any offsets.
     */
    public long getBookmark() {
        return trackModel.getBookmark();
    }

    /**
     * @return track name, i.e. file name.
     */
    public String getTrackName() {
        return trackLabel.getText();
    }

    /**
     * @return View used by the controller
     */
    public JComponent getView() {
        return view;
    }

    /**
     * @return a clone of the viewable model used by the controller
     */
    public ViewableModel getViewableModel() {

        // return a clone to avoid model tainting
        return viewableModel.clone();
    }

    /**
     * Copies the given viewable model.
     *
     * @param newModel the viewable model to copy settings from.
     */
    public void setViewableModel(final ViewableModel newModel) {

        /*
         * Just copy the values, do not spread references all over the place to
         * avoid model tainting.
         */
        this.viewableModel.setEnd(newModel.getEnd());
        this.viewableModel.setIntervalTime(newModel.getIntervalTime());
        this.viewableModel.setIntervalWidth(newModel.getIntervalWidth());
        this.viewableModel.setZoomWindowEnd(newModel.getZoomWindowEnd());
        this.viewableModel.setZoomWindowStart(newModel.getZoomWindowStart());
        trackPainter.setViewableModel(this.viewableModel);
        view.repaint();
    }

    /**
     * @return a clone of the track model used by the controller
     */
    public TrackModel getTrackModel() {
        return trackModel.clone();
    }

    /**
     * Set if the track carriage can be moved while the snap functionality is
     * switched on.
     *
     * @param canMove true if the carriage can be moved, false otherwise.
     */
    public void setMoveable(final boolean canMove) {
        isMoveable = canMove;
    }

    /**
     * Set if the track carriage can be moved.
     *
     * @param lock true if the carriage is locked, false otherwise.
     */
    public void setLocked(final boolean lock) {
        trackModel.setLocked(lock);

        if (lock) {
            lockUnlockButton.setIcon(lockIcon);
        } else {
            lockUnlockButton.setIcon(unlockIcon);
        }
    }

    /**
     * Used to request bookmark saving.
     */
    public void saveBookmark() {
        fireCarriageBookmarkSaveEvent();
    }

    public void deselect() {
        trackModel.setSelected(false);
        trackPainter.setTrackModel(trackModel);
    }

    /**
     * Set up the UI for the first custom action button.
     *
     * @param show true if the button should be shown.
     * @param icon icon to use for the button.
     */
    public void setActionButtonUI1(final boolean show, final ImageIcon icon) {

        if (show) {
            actionButton1.setVisible(true);
            actionButton1.setEnabled(true);

            if (icon != null) {
                actionButton1.setIcon(icon);
                actionButton1.setContentAreaFilled(false);
                actionButton1.setBorderPainted(false);
            } else {
                actionButton1.setContentAreaFilled(true);
                actionButton1.setBorderPainted(true);
            }
        } else {
            actionButton1.setVisible(false);
            actionButton1.setEnabled(false);
        }
    }

    /**
     * Set up the UI for the second custom action button.
     *
     * @param show true if the button should be shown.
     * @param icon icon to use for the button.
     */
    public void setActionButtonUI2(final boolean show, final ImageIcon icon) {

        if (show) {
            actionButton2.setVisible(true);
            actionButton2.setEnabled(true);

            if (icon != null) {
                actionButton2.setIcon(icon);
                actionButton2.setContentAreaFilled(false);
                actionButton2.setBorderPainted(false);
            } else {
                actionButton2.setContentAreaFilled(true);
                actionButton2.setBorderPainted(true);
            }
        } else {
            actionButton2.setVisible(false);
            actionButton2.setEnabled(false);
        }
    }

    /**
     * Set up the UI for the third custom action button.
     *
     * @param show true if the button should be shown.
     * @param icon icon to use for the button.
     */
    public void setActionButtonUI3(final boolean show, final ImageIcon icon) {

        if (show) {
            actionButton3.setVisible(true);
            actionButton3.setEnabled(true);

            if (icon != null) {
                actionButton3.setIcon(icon);
                actionButton3.setContentAreaFilled(false);
                actionButton3.setBorderPainted(false);
            } else {
                actionButton3.setContentAreaFilled(true);
                actionButton3.setBorderPainted(true);
            }
        } else {
            actionButton3.setVisible(false);
            actionButton3.setEnabled(false);
        }
    }

    /**
     * Request a bookmark.
     */
    private void setBookmarkAction() {
        fireCarriageBookmarkRequestEvent();
    }

    /**
     * Remove the track's bookmark.
     */
    private void clearBookmarkAction() {
        trackModel.setBookmark(-1);
        trackPainter.setTrackModel(trackModel);
    }

    /**
     * Invert selection state.
     *
     * @param hasModifiers true if modifiers were held down, false otherwise.
     */
    private void changeSelected(final boolean hasModifiers) {

        if (trackModel.isSelected()) {
            trackModel.setSelected(false);
        } else {
            trackModel.setSelected(true);
        }

        trackPainter.setTrackModel(trackModel);
        fireCarriageSelectionChangeEvent(hasModifiers);
    }

    /**
     * Handles the event for locking and unlocking the track's movement.
     *
     * @param e event to handle.
     */
    private void handleLockUnlockButtonEvent(final ActionEvent e) {
        boolean isLocked = trackModel.isLocked();
        isLocked ^= true;
        trackModel.setLocked(isLocked);

        if (isLocked) {
            lockUnlockButton.setIcon(lockIcon);
        } else {
            lockUnlockButton.setIcon(unlockIcon);
        }

        fireLockStateChangedEvent();
    }

    /**
     * Register a custom action button listener.
     *
     * @param listener listener to register.
     */
    public void addCustomActionListener(final CustomActionListener listener) {

        synchronized (this) {
            buttonListeners.add(listener);
        }
    }

    /**
     * Remove a custom action button listener.
     *
     * @param listener listener to remove.
     */
    public void removeCustomActionListener(
        final CustomActionListener listener) {

        synchronized (this) {
            buttonListeners.remove(listener);
        }
    }

    /**
     * Register a mouse listener.
     *
     * @param listener listener to register.
     */
    public void addMouseListener(final MouseListener listener) {

        synchronized (this) {
            view.addMouseListener(listener);
        }
    }

    /**
     * Remove the mouse listener.
     *
     * @param listener listener to remove.
     */
    public void removeMouseListener(final MouseListener listener) {

        synchronized (this) {
            view.removeMouseListener(listener);
        }
    }

    /**
     * Register the listener to be notified of carriage events.
     *
     * @param listener listener to register.
     */
    public void addCarriageEventListener(final CarriageEventListener listener) {

        synchronized (this) {
            listenerList.add(CarriageEventListener.class, listener);
        }
    }

    /**
     * Remove the listener from being notified of carriage events.
     *
     * @param listener listener to remove.
     */
    public void removeCarriageEventListener(
        final CarriageEventListener listener) {

        synchronized (this) {
            listenerList.remove(CarriageEventListener.class, listener);
        }
    }

    /**
     * Register the listener interested in mouse events on the track's carriage.
     *
     * @param listener listener to register.
     */
    public void addTrackMouseEventListener(
        final TrackMouseEventListener listener) {

        synchronized (this) {
            listenerList.add(TrackMouseEventListener.class, listener);
        }
    }

    /**
     * Remove the listener from being notified of mouse events on the track's
     * carriage.
     *
     * @param listener listener to remove.
     */
    public void removeTrackMouseEventListener(
        final TrackMouseEventListener listener) {

        synchronized (this) {
            listenerList.remove(TrackMouseEventListener.class, listener);
        }
    }

    /**
     * Used to inform listeners about a new carriage event.
     *
     * @param newOffset the new offset to inform listeners about.
     * @param temporalPosition
     *            the temporal position of the mouse when the new offset is
     *            triggered
     * @param hasModifiers true if modifiers were held down, false otherwise.
     */
    private void fireCarriageOffsetChangeEvent(final long newOffset,
        final long temporalPosition, final boolean hasModifiers) {

        synchronized (this) {
            final CarriageEvent e = new CarriageEvent(this,
                    trackModel.getTrackId(), newOffset,
                    trackModel.getBookmark(), trackModel.getDuration(),
                    temporalPosition, EventType.OFFSET_CHANGE, hasModifiers);
            final Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == CarriageEventListener.class) {
                    ((CarriageEventListener) listeners[i + 1]).offsetChanged(e);
                }
            }
        }
    }

    /**
     * Used to inform listeners about a bookmark request event.
     */
    private void fireCarriageBookmarkRequestEvent() {

        synchronized (this) {
            final CarriageEvent e = new CarriageEvent(this,
                    trackModel.getTrackId(), trackModel.getOffset(),
                    trackModel.getBookmark(), trackModel.getDuration(), 0,
                    EventType.BOOKMARK_REQUEST, false);
            final Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == CarriageEventListener.class) {
                    ((CarriageEventListener) listeners[i + 1]).requestBookmark(
                        e);
                }
            }
        }
    }

    /**
     * Used to inform listeners about a bookmark request event.
     */
    private void fireCarriageBookmarkSaveEvent() {

        synchronized (this) {
            final CarriageEvent e = new CarriageEvent(this,
                    trackModel.getTrackId(), trackModel.getOffset(),
                    trackModel.getBookmark(), trackModel.getDuration(), 0,
                    EventType.BOOKMARK_SAVE, false);
            final Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == CarriageEventListener.class) {
                    ((CarriageEventListener) listeners[i + 1]).saveBookmark(e);
                }
            }
        }
    }

    /**
     * Used to inform listeners about track selection event.
     *
     * @param hasModifiers true if modifiers were held down, false otherwise.
     */
    private void fireCarriageSelectionChangeEvent(final boolean hasModifiers) {

        synchronized (this) {
            final CarriageEvent e = new CarriageEvent(this,
                    trackModel.getTrackId(), trackModel.getOffset(),
                    trackModel.getBookmark(), trackModel.getDuration(), 0,
                    EventType.CARRIAGE_SELECTION, hasModifiers);
            final Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == CarriageEventListener.class) {
                    ((CarriageEventListener) listeners[i + 1]).selectionChanged(
                        e);
                }
            }
        }
    }

    /**
     * Used to inform listeners about lock state change event.
     */
    private void fireLockStateChangedEvent() {

        synchronized (this) {
            final CarriageEvent e = new CarriageEvent(this,
                    trackModel.getTrackId(), trackModel.getOffset(),
                    trackModel.getBookmark(), trackModel.getDuration(), 0,
                    EventType.CARRIAGE_LOCK, false);

            final Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == CarriageEventListener.class) {
                    ((CarriageEventListener) listeners[i + 1]).lockStateChanged(
                        e);
                }
            }
        }
    }

    /**
     * Used to inform listeners about the mouse release event on the track's
     * carriage.
     *
     * @param e the event to handle.
     */
    private void fireMouseReleasedEvent(final MouseEvent e) {

        synchronized (this) {
            final Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == TrackMouseEventListener.class) {
                    ((TrackMouseEventListener) listeners[i + 1]).mouseReleased(
                        e);
                }
            }
        }
    }

    /**
     * Inner listener used to handle mouse events.
     */
    private class TrackPainterListener extends MouseInputAdapter {

        /** Initial offset value. */
        private long offsetInit;

        /** Is the mouse in the carriage. */
        private boolean inCarriage;

        /** Initial x-coord position. */
        private int xInit;

        /** Initial track state. */
        private TrackState initialState;

        /** Mouse cursor for moving. */
        private final Cursor moveCursor = Cursor.getPredefinedCursor(
                Cursor.MOVE_CURSOR);

        /** Default mouse cursor. */
        private final Cursor defaultCursor = Cursor.getDefaultCursor();

        @Override public void mouseClicked(final MouseEvent e) {

            if (trackPainter.getCarriagePolygon().contains(e.getPoint())) {
                final boolean hasModifiers = e.isAltDown() || e.isAltGraphDown()
                    || e.isControlDown() || e.isMetaDown() || e.isShiftDown();
                changeSelected(hasModifiers);
            }
        }

        @Override public void mousePressed(final MouseEvent e) {

            if (trackPainter.getCarriagePolygon().contains(e.getPoint())) {
                inCarriage = true;
                xInit = e.getX();
                offsetInit = trackModel.getOffset();
                trackPainter.setCursor(moveCursor);
                initialState = trackModel.getState();
            }

            if (e.isPopupTrigger()) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override public void mouseDragged(final MouseEvent e) {

            if (trackModel.isLocked()) {
                return;
            }

            final boolean hasModifiers = e.isAltDown() || e.isAltGraphDown()
                || e.isControlDown() || e.isMetaDown() || e.isShiftDown();

            if (inCarriage) {
                final int xNet = e.getX() - xInit;

                // Calculate the total amount of time we offset by
                final float newOffset = ((xNet * 1F)
                        / viewableModel.getIntervalWidth()
                        * viewableModel.getIntervalTime()) + offsetInit;
                final float temporalPosition = (e.getX() * 1F)
                    / viewableModel.getIntervalWidth()
                    * viewableModel.getIntervalTime();

                if (isMoveable) {
                    fireCarriageOffsetChangeEvent((long) newOffset,
                        (long) temporalPosition, hasModifiers);
                } else {
                    final long threshold = (long) (0.05F
                            * (viewableModel.getZoomWindowEnd()
                                - viewableModel.getZoomWindowStart()));

                    if (Math.abs(newOffset - offsetInit) >= threshold) {
                        isMoveable = true;
                    }
                }
            }
        }

        @Override public void mouseReleased(final MouseEvent e) {
            isMoveable = true;
            inCarriage = false;

            final Component source = (Component) e.getSource();
            source.setCursor(defaultCursor);
            setState(initialState);

            if (e.isPopupTrigger()) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }

            fireMouseReleasedEvent(e);
        }
    }

}