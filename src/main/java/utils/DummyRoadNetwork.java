package utils;

import com.vividsolutions.jts.geom.Coordinate;

import rnwmodel.Road;
import rnwmodel.RoadNetworkModel;
import rnwmodel.RoadNode;

public class DummyRoadNetwork extends RoadNetworkModel {

    private static final double cellLength = 80.0;
    private static Coordinate begin = new Coordinate(103.817932, 1.3341);
    private static int nodeId = 1;
    private static int roadId = 1;
    private static final double[] SPEED_LIMIT = { 54.0, 54.0 };

    /**
     * Create a dummy road network which is hard coded
     */
    public DummyRoadNetwork() {
	loadNodesAndRoads();
    }

    @Override
    protected void loadNodesAndRoads() {
	RoadNode prev = null;
	for (int i = 0; i < 10; i++) {
	    Coordinate next = EarthFunctions.getPointAtDistanceAndBearing(begin, cellLength * i, Math.PI / 2.0);
	    RoadNode node = new RoadNode(nodeId++, next.x, next.y);
	    allNodesMap.put(node.getNodeId(), node);
	    beginAndEndNodes.add(node);
	    if (prev != null) {
		Road road = new Road(roadId++);
		road.setBeginNode(prev);
		prev.getOutRoads().add(road);
		road.setEndNode(node);
		node.getInRoads().add(road);
		road.getRoadNodes().add(prev);
		road.getRoadNodes().add(node);
		road.setOneWay(true);
		road.setLaneCount(1);
		road.setSpeedLimit(SPEED_LIMIT);
		allRoadsMap.put(road.getRoadId(), road);
	    }
	    prev = node;

	}

	int intersectionNodes[] = { 4, 7 };

	for (int id : intersectionNodes) {
	    prev = allNodesMap.get(id);
	    for (int i = 1; i <= 3; i++) {
		Coordinate next = EarthFunctions.getPointAtDistanceAndBearing(prev.getPosition(), cellLength, Math.PI);
		RoadNode node = new RoadNode(nodeId++, next.x, next.y);
		allNodesMap.put(node.getNodeId(), node);
		beginAndEndNodes.add(node);
		Road road = new Road(roadId++);
		road.setBeginNode(node);
		node.getOutRoads().add(road);
		road.setEndNode(prev);
		prev.getInRoads().add(road);
		road.getRoadNodes().add(node);
		road.getRoadNodes().add(prev);
		road.setOneWay(true);
		road.setLaneCount(1);
		road.setSpeedLimit(SPEED_LIMIT);
		allRoadsMap.put(road.getRoadId(), road);
		prev = node;
	    }
	}

	for (int id : intersectionNodes) {
	    prev = allNodesMap.get(id);
	    for (int i = 1; i <= 3; i++) {
		Coordinate next = EarthFunctions.getPointAtDistanceAndBearing(prev.getPosition(), cellLength, 0.0);
		RoadNode node = new RoadNode(nodeId++, next.x, next.y);
		allNodesMap.put(node.getNodeId(), node);
		beginAndEndNodes.add(node);
		Road road = new Road(roadId++);
		road.setBeginNode(prev);
		prev.getOutRoads().add(road);
		road.setEndNode(node);
		node.getInRoads().add(road);
		road.getRoadNodes().add(prev);
		road.getRoadNodes().add(node);
		road.setOneWay(true);
		road.setLaneCount(1);
		road.setSpeedLimit(SPEED_LIMIT);
		allRoadsMap.put(road.getRoadId(), road);
		prev = node;
	    }
	}

    }

}
