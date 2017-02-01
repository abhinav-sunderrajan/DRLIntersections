package main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.deeplearning4j.util.ModelSerializer;

import ctm.Cell;
import ctm.CellNetwork;
import intersections.Intersection;
import intersections.IntersectionController;
import intersections.Phase;
import intersections.RLIntersectionController;
import rl.DRLAgent;
import rnwmodel.RoadNetworkModel;
import utils.DummyRoadNetwork;
import utils.SimulationConstants;
import utils.ThreadPoolExecutorService;

public class SimulationMain {

    private Object monitor;
    private RoadNetworkModel roadNetwork;
    private Map<Integer, Double> sourceFlowMap;
    private static SimulationMain simulation;
    public static Random random;
    private List<Intersection> intersections;
    private CellNetwork cellNetwork;
    private Map<Cell, Phase> phaseMap;
    private ThreadPoolExecutorService executor;
    public static final DecimalFormat DF = new DecimalFormat("#.##");

    public static SimulationMain getSimulationInstance(long seed) {
	if (simulation == null) {
	    random = new Random(seed);
	    simulation = new SimulationMain();
	}
	return simulation;
    }

    private SimulationMain() {
	roadNetwork = new DummyRoadNetwork();
	monitor = new Object();
	intersections = new ArrayList<Intersection>();
	phaseMap = new HashMap<Cell, Phase>();
	initializeSourceFlows();
	cellNetwork = new CellNetwork(roadNetwork, sourceFlowMap);
	System.out.println("finished creating cell network");
	createIntersections();
	this.executor = ThreadPoolExecutorService.getExecutorInstance();
    }

    private void initializeSourceFlows() {
	sourceFlowMap = new HashMap<Integer, Double>();
	sourceFlowMap.put(1, 1200.0);
	sourceFlowMap.put(12, 500.0);
	sourceFlowMap.put(15, 500.0);
    }

    public static void main(String args[]) throws InterruptedException, IOException {
	SimulationMain main = SimulationMain.getSimulationInstance(1008);
	// Compute the number of epochs..
	// Calculate number of s,a,r,s' pairs per game
	int tuplesPerGame = SimulationConstants.FINISH_TIME / SimulationConstants.MIN_PHASE_TIME;

	int iterations = SimulationConstants.REPLAY_SIZE / tuplesPerGame + 50;
	int iteration = 0;
	System.out.println("Number of iterations: " + iterations);

	// Initialize reinforcement learning controller.
	IntersectionController controller = new RLIntersectionController();
	if (controller instanceof RLIntersectionController)
	    ((RLIntersectionController) controller).initializeDRLAgents(main.getIntersections());

	while (iteration < iterations) {
	    // Run the simulation here
	    SimulationModel model = new SimulationModel(main, controller.getControllerMonitor());
	    controller.setSimulationTime(model.getSimulationTime());
	    Thread controllerThread = new Thread(controller);
	    controllerThread.setName("Intersection-Controller-Thread-" + iteration);
	    controllerThread.start();

	    Thread.sleep(1);
	    Thread simulationThread = new Thread(model);
	    simulationThread.setName("Simulation-Thread-" + iteration);
	    simulationThread.start();

	    iteration++;
	    synchronized (main.monitor) {
		main.monitor.wait();
	    }

	    controllerThread.interrupt();
	    main.clearEnvironmentState();
	    Thread.sleep(1);
	    System.out.println("finished iteration " + iteration);

	}
	if (controller instanceof RLIntersectionController) {
	    for (DRLAgent drlAgent : ((RLIntersectionController) controller).getDrlControllerMap().values()) {
		File tempFile = new File(SimulationConstants.NN_PATH + "drl_agent" + drlAgent.getDrlAgentId() + ".tmp");
		ModelSerializer.writeModel(drlAgent.getModel(), tempFile, true);
		System.out.format("Write to file: %s\n", tempFile.getCanonicalFile());
	    }
	}

	main.executor.getExecutor().shutdown();

    }

    /**
     * Returns the lock object for thread synchronization.
     * 
     * @return
     */
    public Object getMonitor() {
	return monitor;
    }

    /**
     * Returns the road network.
     * 
     * @return
     */
    public RoadNetworkModel getRoadNetwork() {
	return roadNetwork;
    }

    /**
     * returns the source flow in number of vehicles per hour.
     * 
     * @return
     */
    public Map<Integer, Double> getSourceFlowMap() {
	return sourceFlowMap;
    }

    private void clearEnvironmentState() {

	for (Cell cell : cellNetwork.getCellMap().values()) {
	    cell.setNt(0);
	    double freeFlowSpeed = cell.getRoad().getSpeedLimit()[1] * (5.0 / 18);
	    cell.setMeanSpeed(freeFlowSpeed);
	    cell.setOldMeanSpeed(freeFlowSpeed);
	    double meanVehicleLength = SimulationConstants.LEFF;
	    double criticalDensity = 1.0 / (SimulationConstants.TIME_GAP * freeFlowSpeed + meanVehicleLength);
	    cell.setnMax(Math.round(cell.getLength() * criticalDensity * cell.getNumOfLanes()));
	    cell.setSendingPotential(0.0);
	    cell.setReceivePotential(0.0);
	    cell.setNtOld(0.0);
	}

	for (Intersection intersection : intersections) {
	    intersection.setDelay(-1.0);
	    intersection.setPrevDelay(-1.0);
	}

    }

    public List<Intersection> getIntersections() {
	return intersections;
    }

    public CellNetwork getCellNetwork() {
	return cellNetwork;
    }

    public Map<Cell, Phase> getPhaseMap() {
	return phaseMap;
    }

    private void createIntersections() {
	Intersection intersection1 = new Intersection(120, 1, 2);
	Phase phase11 = new Phase(cellNetwork.getCellMap().get("3_0"));
	phase11.setGreen(true);
	phase11.setGreenDuration(SimulationConstants.MIN_PHASE_TIME);
	intersection1.addPhaseToIntersection(phase11);
	intersection1.setActivePhase(0);
	intersection1.setCurrentGreenTime(phase11.getGreenDuration());

	Phase phase12 = new Phase(cellNetwork.getCellMap().get("10_0"));
	phase12.setGreenDuration(SimulationConstants.MIN_PHASE_TIME);
	phase12.setGreen(false);
	intersection1.addPhaseToIntersection(phase12);

	Intersection intersection2 = new Intersection(120, 2, 2);
	Phase phase21 = new Phase(cellNetwork.getCellMap().get("6_0"));
	phase21.setGreen(true);
	phase21.setGreenDuration(SimulationConstants.MIN_PHASE_TIME);
	intersection2.addPhaseToIntersection(phase21);
	intersection2.setActivePhase(0);
	intersection2.setCurrentGreenTime(phase21.getGreenDuration());

	Phase phase22 = new Phase(cellNetwork.getCellMap().get("13_0"));
	phase22.setGreenDuration(SimulationConstants.MIN_PHASE_TIME);
	phase22.setGreen(false);
	intersection2.addPhaseToIntersection(phase22);

	intersections.add(intersection1);
	intersections.add(intersection2);

	for (Intersection intersection : intersections) {
	    for (Phase phase : intersection.getPhases())
		phaseMap.put(phase.getPhaseCell(), phase);
	}

	intersection1.addNeighbourToIntersection(intersection2);
	intersection2.addNeighbourToIntersection(intersection1);

    }

}
