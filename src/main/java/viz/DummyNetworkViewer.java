package viz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;

import rnwmodel.Road;
import rnwmodel.RoadNetworkModel;
import rnwmodel.RoadNode;
import utils.DummyRoadNetwork;
import utils.EarthFunctions;

/**
 * Dummy road network viewer.
 * 
 * @author abhinav
 * 
 */
public class DummyNetworkViewer extends RoadNetworkVisualizer {

    Set<Envelope> allPartitions = new HashSet<>();
    private static DummyNetworkViewer viewerInstance;
    private Map<Road, Color> roadColorMap;

    private DummyNetworkViewer(String title, RoadNetworkModel model) {
	super(title, model);
	this.roadColorMap = new HashMap<>();
    }

    /**
     * Get viewer instance.
     * 
     * @param title
     * @param model
     * @return
     */
    public static DummyNetworkViewer getViewerInstance(String title, RoadNetworkModel model) {
	if (viewerInstance == null) {
	    viewerInstance = new DummyNetworkViewer(title, model);
	}

	return viewerInstance;

    }

    @Override
    public void updateView() {

	if (model.getAllNodes().isEmpty())
	    return;

	int pwidth = panel.getWidth();
	int pheight = panel.getHeight();

	image = new BufferedImage(pwidth, pheight, BufferedImage.TYPE_INT_RGB);

	Graphics g = image.getGraphics();
	Graphics2D g2 = (Graphics2D) g;
	g2.setColor(Color.WHITE);
	g2.fillRect(0, 0, pwidth, pheight);

	synchronized (lock) {

	    g2.setColor(Color.DARK_GRAY);
	    for (Road link : visibleRoads) {
		g2.setStroke(new BasicStroke(4));
		drawRoad(link, g2);
		drawBox(link);
	    }

	}

	if (selectedRoad != null) {
	    g2.setColor(Color.PINK);
	    drawRoad(selectedRoad, g2);

	}

	synchronized (lock) {
	    for (Road link : selectedRoads) {
		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(4));
		drawRoad(link, g2);
	    }
	}

    }

    public void drawBox(Road road) {

	Color color = roadColorMap.get(road);
	Graphics g = image.getGraphics();
	Graphics2D g2d = (Graphics2D) g;

	RoadNode segmentNode1 = road.getRoadNodes().get(0);
	RoadNode segmentNode2 = road.getRoadNodes().get(1);
	g2d.setColor(color);

	int xo = panel.getWidth() / 2;
	int yo = panel.getHeight() / 2;

	int x0 = xo + (int) (zoom * (segmentNode1.getPosition().x - offset[0]));
	int y0 = yo + (int) (zoom * (-segmentNode1.getPosition().y - offset[1]));

	int x1 = xo + (int) (zoom * (segmentNode2.getPosition().x - offset[0]));
	int y1 = yo + (int) (zoom * (-segmentNode2.getPosition().y - offset[1]));

	double dx = x1 - x0;
	double dy = y1 - y0;

	double length = Math.sqrt(dx * dx + dy * dy);

	Rectangle rectangle = new Rectangle(x0, y0, (int) length, 10);

	// Compute the bearing with respect to east, that is why -Math.PI/2.
	double alpha = Math.toRadians(EarthFunctions.bearing(segmentNode1.getPosition(), segmentNode2.getPosition()))
		- Math.PI / 2.0;

	AffineTransform transform = new AffineTransform();
	transform.rotate(alpha, x0, y0);

	Shape rotatedRect = transform.createTransformedShape(rectangle);
	g2d.setColor(color);
	g2d.fill(rotatedRect);
	g2d.setColor(Color.black);
	g2d.draw(rotatedRect);
    }

    public Map<Road, Color> getRoadColorMap() {
	return roadColorMap;
    }

    public static void main(String[] args) {
	try {

	    RoadNetworkVisualizer viewer = DummyNetworkViewer.getViewerInstance("Dummy Network",
		    new DummyRoadNetwork());

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
