package org.openshapa.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import org.openshapa.component.model.NeedleModel;
import org.openshapa.component.model.ViewableModel;

/**
 * This class paints a timing needle.
 */
public class NeedlePainter extends Component {

    /** Polygon region for the needle marker */
    private Polygon needleMarker;

    private NeedleModel needleModel;
    private ViewableModel viewableModel;

    public NeedlePainter()  {
        super();
    }

    public NeedleModel getNeedleModel() {
        return needleModel;
    }

    public void setNeedleModel(NeedleModel needleModel) {
        this.needleModel = needleModel;
        this.repaint();
    }

    public ViewableModel getViewableModel() {
        return viewableModel;
    }

    public void setViewableModel(ViewableModel viewableModel) {
        this.viewableModel = viewableModel;
        this.repaint();
    }

    public Polygon getNeedleMarker() {
        return needleMarker;
    }

    @Override
    public boolean contains(Point p) {
        return needleMarker.contains(p);
    }

    @Override
    public boolean contains(int x, int y) {
        return needleMarker.contains(x, y);
    }

    @Override
    public void paint(Graphics g) {
        if (needleModel == null || viewableModel == null) {
            return;
        }

        final long currentTime = needleModel.getCurrentTime();
        // Don't paint if the needle is out of the current window
        if ((currentTime < viewableModel.getZoomWindowStart()) ||
                (viewableModel.getZoomWindowEnd() < currentTime)) {
            return;
        }

        Dimension size = this.getSize();

        g.setColor(new Color(250, 0, 0, 100));

        // Calculate the needle position based on the selected time
        float ratio = viewableModel.getIntervalWidth() / viewableModel.getIntervalTime();
        int pos = Math.round(currentTime * ratio
                - viewableModel.getZoomWindowStart() * ratio) + needleModel.getPaddingLeft();

        final int paddingTop = needleModel.getPaddingTop();
        needleMarker = new Polygon();
        needleMarker.addPoint(pos - 10, paddingTop);
        needleMarker.addPoint(pos + 11, paddingTop);
        needleMarker.addPoint(pos + 1, 19);
        needleMarker.addPoint(pos, 19);

        g.fillPolygon(needleMarker);
        
        g.setColor(Color.red);
        g.drawPolygon(needleMarker);

        // Draw the timing needle
        int x1 = pos;
        int y1 = paddingTop + 19;
        int x2 = pos + 1;
        int y2 = size.height;

        g.drawLine(x1, y1, x1, y2);
        g.drawLine(x2, y1, x2, y2);

    }

}
