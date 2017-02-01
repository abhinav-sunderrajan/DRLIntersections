package ctm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rnwmodel.Road;
import rnwmodel.RoadNetworkModel;
import rnwmodel.RoadNode;

/**
 *
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Cell_Transmission_Model">http://en
 *      .wikipedia.org/wiki/Cell_Transmission_Model</a>
 * @author abhinav
 * 
 */
public class CellNetwork {

    private Map<String, Cell> cellMap;
    private Collection<Road> roads;
    private Map<Integer, Double> sourceFlowMap;

    /**
     * Create the Cell transmission model for the road network model from the
     * {@link RoadNetworkModel}
     * 
     * @param ramps
     * 
     * @param model
     */
    public CellNetwork(RoadNetworkModel model, Map<Integer, Double> sourceFlowMap) {
	cellMap = new LinkedHashMap<String, Cell>();
	this.sourceFlowMap = sourceFlowMap;

	// Create cells and connectors to be used in the CTM model.
	this.roads = model.getAllRoadsMap().values();
	createCells();
	generatePredecessorsAndSuccessors();

    }

    /**
     * Add predecessors and successors to the cells created.
     */
    private void generatePredecessorsAndSuccessors() {
	for (Road road : roads) {
	    RoadNode beginNode = road.getBeginNode();
	    int roadId = road.getRoadId();
	    String cellId = roadId + "_" + 0;

	    Cell cell = cellMap.get(cellId);
	    List<Road> ins = new ArrayList<>();
	    for (Road inRoad : beginNode.getInRoads()) {
		if (roads.contains(inRoad))
		    ins.add(inRoad);
	    }

	    for (Road inRoad : ins) {
		String inCellId = inRoad.getRoadId() + "_" + (inRoad.getSegmentsLength().length - 1);
		Cell inCell = cellMap.get(inCellId);
		inCell.addSuccessor(cell);
		cell.addPredecessor(inCell);

	    }

	}

    }

    /**
     * Create the cells of the CTM model. You need to have one
     */
    private void createCells() {

	for (Road road : roads) {
	    int roadId = road.getRoadId();

	    RoadNode beginNode = road.getBeginNode();
	    RoadNode endNode = road.getEndNode();

	    double cellLength = road.getSegmentsLength()[0];
	    String cellId = roadId + "_" + 0;
	    Cell ordinaryCell = new OrdinaryCell(cellId, cellLength, road);
	    cellMap.put(cellId, ordinaryCell);

	    int ins = 0;
	    for (Road inRoad : beginNode.getInRoads()) {
		if (roads.contains(inRoad))
		    ins++;
	    }

	    if (ins == 0) {
		Cell sourceCell = new SourceCell(roadId + "_source", 0, road, sourceFlowMap.get(roadId));
		sourceCell.addSuccessor(ordinaryCell);
		ordinaryCell.addPredecessor(sourceCell);
		cellMap.put(roadId + "_source", sourceCell);
	    }

	    int outs = 0;
	    for (Road outRoad : endNode.getOutRoads()) {
		if (roads.contains(outRoad))
		    outs++;
	    }

	    if (outs == 0) {
		Cell sinkCell = new SinkCell(roadId + "_sink", 0, road);
		sinkCell.setRoad(road);
		ordinaryCell.addSuccessor(sinkCell);
		sinkCell.addPredecessor(ordinaryCell);
		cellMap.put(roadId + "_sink", sinkCell);
	    }

	}

    }

    /**
     * @return the cellMap
     */
    public Map<String, Cell> getCellMap() {
	return cellMap;
    }

}
