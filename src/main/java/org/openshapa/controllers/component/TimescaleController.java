package org.openshapa.controllers.component;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import org.openshapa.event.component.TimescaleEvent;
import org.openshapa.event.component.TimescaleListener;

import org.openshapa.models.component.TimescaleModel;
import org.openshapa.models.component.ViewableModel;

import org.openshapa.views.component.TimescalePainter;


/**
 * Timescale controller is responsible for managing a TimescalePainter
 */
public final class TimescaleController {

    /** View */
    private final TimescalePainter view;

    /** Models */
    private final TimescaleModel timescaleModel;
    private final ViewableModel viewableModel;

    /** Listeners interested in needle painter events */
    private final EventListenerList listenerList;

    public TimescaleController() {
        view = new TimescalePainter();

        timescaleModel = new TimescaleModel();
        timescaleModel.setZoomWindowIndicatorHeight(8);
        timescaleModel.setZoomWindowToTrackTransitionHeight(20);
        timescaleModel.setHeight(50
            + timescaleModel.getZoomWindowIndicatorHeight()
            + timescaleModel.getZoomWindowToTrackTransitionHeight());
        timescaleModel.setZoomWindowIndicatorColor(new Color(192, 192, 192));
        timescaleModel.setTimescaleBackgroundColor(new Color(237, 237, 237));

        // TODO these should match up with the colors in
        // DataControllerV.static{}
        timescaleModel.setHoursMarkerColor(Color.red.darker());
        timescaleModel.setMinutesMarkerColor(Color.green.darker().darker());
        timescaleModel.setSecondsMarkerColor(Color.blue.darker().darker());
        timescaleModel.setMillisecondsMarkerColor(Color.gray.darker());

        viewableModel = new ViewableModel();

        final TimescaleEventListener listener = new TimescaleEventListener();
        view.addMouseListener(listener);
        view.addMouseMotionListener(listener);

        listenerList = new EventListenerList();
    }

    /**
     * @param start
     *            The start time, in milliseconds, of the scale to display
     * @param end
     *            The end time, in milliseconds, of the scale to display
     */
    public void setConstraints(final long start, final long end) {
        viewableModel.setZoomWindowStart(start);
        viewableModel.setZoomWindowEnd(end);

        final int effectiveWidth = view.getWidth();
        timescaleModel.setEffectiveWidth(effectiveWidth);

        viewableModel.setIntervalTime(end - start + 1);
        viewableModel.setIntervalWidth(effectiveWidth);

        view.setViewableModel(viewableModel);
        view.setTimescaleModel(timescaleModel);
    }

    public void setViewableModel(final ViewableModel viewableModel) {

        /*
         * Just copy the values, do not spread references all over the place to
         * avoid model tainting.
         */
        this.viewableModel.copyFrom(viewableModel);
        view.setViewableModel(this.viewableModel);
    }

    /**
     * @return a copy of the viewable model in use
     */
    public ViewableModel getViewableModel() {

        // return a clone to avoid model tainting
        return viewableModel.copy();
    }

    public TimescaleModel getTimescaleModel() {
        return timescaleModel;
    }

    /**
     * @return View used by the controller
     */
    public JComponent getView() {
        return view;
    }

    public void addTimescaleEventListener(final TimescaleListener listener) {

        synchronized (this) {
            listenerList.add(TimescaleListener.class, listener);
        }
    }

    public void removeTimescaleEventListener(final TimescaleListener listener) {

        synchronized (this) {
            listenerList.remove(TimescaleListener.class, listener);
        }
    }

    /**
     * Used to fire a new event informing listeners about the new needle time.
     *
     * @param newTime
     */
    private void fireJumpEvent(final long jumpTime,
        final boolean togglePlaybackMode) {

        synchronized (this) {
            TimescaleEvent e = new TimescaleEvent(this, jumpTime,
                    togglePlaybackMode);
            Object[] listeners = listenerList.getListenerList();

            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {

                if (listeners[i] == TimescaleListener.class) {
                    ((TimescaleListener) listeners[i + 1]).jumpToTime(e);
                }
            }
        }
    }

    /**
     * Inner class used to handle intercepted events.
     */
    private class TimescaleEventListener extends MouseInputAdapter {
        @Override public void mousePressed(final MouseEvent e) {

            if (e.getButton() == MouseEvent.BUTTON1) {

                if (isMouseInsideTimescale(e)) {
                    fireJumpEvent(getNewNeedlePosition(e), false);
                }
            }
        }

        @Override public void mouseReleased(final MouseEvent e) {
        }

        private boolean isMouseInsideTimescale(final MouseEvent e) {
            return (e.getX() >= 0) && ((e.getX()) < view.getSize().width);
        }

        @Override public void mouseClicked(final MouseEvent e) {

            if ((e.getButton() == MouseEvent.BUTTON1)
                    && ((e.getClickCount() % 2) == 0)) {
                fireJumpEvent(getNewNeedlePosition(e), true);
            }
        }

        @Override public void mouseDragged(final MouseEvent e) {

            if (e.getButton() == MouseEvent.BUTTON1) {
                fireJumpEvent(getNewNeedlePosition(e), false);
            }
        }

        private long getNewNeedlePosition(final MouseEvent e) {
            final int dx = Math.min(Math.max(e.getX(), 0),
                    view.getSize().width - 1);

            // Calculate the time represented by the new location
            double ratio = viewableModel.getIntervalWidth()
                / viewableModel.getIntervalTime();
            double newTime = (dx
                    + (viewableModel.getZoomWindowStart() * ratio)) / ratio;
            newTime = Math.min(Math.max(newTime, 0),
                    viewableModel.getZoomWindowEnd());

            return Math.round(newTime);
        }
    }
}
