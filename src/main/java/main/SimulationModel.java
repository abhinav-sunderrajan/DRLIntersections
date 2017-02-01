package main;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ctm.Cell;
import ctm.CellNetwork;
import ctm.SinkCell;
import ctm.SourceCell;
import intersections.Phase;
import utils.SimulationConstants;
import viz.DummyNetworkViewer;

public class SimulationModel implements Runnable {

    private AtomicInteger simulationTime;
    private DummyNetworkViewer viewer;
    private CellNetwork cellNetwork;
    private Map<Cell, Phase> phaseMap;
    private Object mainMonitor;
    private Object intersectionControlMonitor;
    private static final boolean haveVisualization = false;

    public SimulationModel(SimulationMain main, Object controllerMonitor) {

	this.cellNetwork = main.getCellNetwork();
	simulationTime = new AtomicInteger(0);
	this.phaseMap = main.getPhaseMap();
	this.mainMonitor = main.getMonitor();
	this.intersectionControlMonitor = controllerMonitor;
	// Create intersections

	// create visualization
	if (haveVisualization) {
	    viewer = DummyNetworkViewer.getViewerInstance("Cell-Sim", main.getRoadNetwork());
	}

    }

    @Override
    public void run() {

	while (simulationTime.get() <= SimulationConstants.FINISH_TIME) {

	    // Notify intersection controller that simulation has started.
	    synchronized (intersectionControlMonitor) {
		intersectionControlMonitor.notifyAll();
	    }

	    if (simulationTime.get() > 0 && simulationTime.get() % SimulationConstants.MIN_PHASE_TIME == 0) {
		synchronized (intersectionControlMonitor) {
		    try {
			// System.out.println(
			// simulationTime.get() + " SimulationModel:Requesting
			// traffic lights from controller.");
			intersectionControlMonitor.wait();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }

	    // Logic get the phase configuration from the traffic light
	    // controllers

	    // Update the sending potential of cells
	    for (Cell cell : cellNetwork.getCellMap().values()) {
		if (!(cell instanceof SinkCell || cell instanceof SourceCell)) {
		    cell.determineSendingPotential();
		    cell.determineReceivePotential();
		}
	    }

	    // Update out-flow
	    for (Cell cell : cellNetwork.getCellMap().values()) {
		if (phaseMap.containsKey(cell)) {
		    Phase phase = phaseMap.get(cell);
		    if (phase.isGreen()) {
			cell.updateOutFlow();
		    } else {
			cell.setOutflow(0);
		    }
		} else {
		    cell.updateOutFlow();
		}

	    }

	    // Now update the number of vehicles in each cell.

	    for (Cell cell : cellNetwork.getCellMap().values())
		cell.updateNumberOfVehiclesInCell();

	    // Now update the number of vehicles in each cell.
	    for (Cell cell : cellNetwork.getCellMap().values())
		cell.updateAnticipatedDensity();

	    // Now update the mean speed.
	    for (Cell cell : cellNetwork.getCellMap().values()) {
		if (!(cell instanceof SinkCell || cell instanceof SourceCell))
		    cell.updateMeanSpeed();
	    }

	    if (haveVisualization) {
		cellNetwork.getCellMap().values().parallelStream().forEach(cell -> {
		    if (!(cell instanceof SinkCell || cell instanceof SourceCell)) {
			Color c = Color.getHSBColor((float) (cell.getMeanSpeed() * 0.4f / cell.getFreeFlowSpeed()),
				0.9f, 0.9f);
			viewer.getRoadColorMap().put(cell.getRoad(), c);
		    }
		});
		viewer.getMapFrame().repaint();
		try {
		    Thread.sleep(50);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	    simulationTime.addAndGet(SimulationConstants.TIME_STEP);

	}
	synchronized (mainMonitor) {
	    // notify main class that this iteration is complete.
	    mainMonitor.notifyAll();
	}

    }

    public AtomicInteger getSimulationTime() {
	return simulationTime;
    }

}
