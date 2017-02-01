package intersections;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import rl.DRLAgent;
import rl.StateRewardTuple;
import utils.SimulationConstants;

/**
 * Intersection controller for DRL.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class RLIntersectionController extends IntersectionController {

    private Map<Intersection, DRLAgent> drlControllerMap;
    public static boolean isTrained = false;
    private double epsilon = 1.0;

    @Override
    public void run() {
	try {
	    // System.out.println("Waiting for the simulation to start..");

	    synchronized (simulationMonitor) {
		simulationMonitor.wait();
	    }

	    // System.out.println("The simulation has started, now start the
	    // intersection controller..");

	    while (!Thread.currentThread().isInterrupted()) {
		// Calculate the next action for all intersections and revert
		// back to SimulationModel

		if (simulationTime.get() > 0 && simulationTime.get() % SimulationConstants.MIN_PHASE_TIME == 0) {

		    for (Intersection intersection : intersections) {
			drlControllerMap.get(intersection).setEpsilon(epsilon);
			nextPhase(intersection);
		    }
		    if (epsilon < 0.1)
			epsilon = 0.1;
		    else
			epsilon -= 0.9 / (double) SimulationConstants.REPLAY_SIZE;
		    // Notify simulation that traffic light settings are
		    // computed.
		    synchronized (simulationMonitor) {
			simulationMonitor.notifyAll();
			// System.out.println(simulationTime.get() + "
			// RLController:Sent traffic light to simulation");
		    }
		}

	    }

	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

    }

    @Override
    protected void nextPhase(Intersection intersection) {

	DRLAgent drlc = drlControllerMap.get(intersection);
	double state[] = new double[drlc.getNumOfInputs()];

	int index = 0;
	for (Phase phase : intersection.getPhases()) {
	    state[index++] = phase.getPhaseDensity();
	}

	for (Intersection neighbour : intersection.getNeighbours()) {
	    for (Phase phase : neighbour.getPhases())
		state[index++] = phase.getPhaseDensity();
	}

	double delay = intersection.computeIntersectionDelay();
	int action = 0;
	double reward = 0.0;
	if (intersection.getPrevDelay() > 0.0) {
	    reward = (intersection.getPrevDelay() - delay) / intersection.getPrevDelay();

	    if (reward > 1.0)
		reward = 1.0;
	    if (reward < -1.0)
		reward = -1.0;

	    boolean isTerminalState = (simulationTime.get() == SimulationConstants.FINISH_TIME);
	    StateRewardTuple srt = new StateRewardTuple();
	    srt.setReward(reward);
	    srt.setState(state);
	    srt.setTerminalState(isTerminalState);
	    action = drlc.getAction(srt);
	}

	// Now you have the current and the next active phases..
	int active = intersection.getActivePhase();

	Phase currentActive = null;

	// Change phase if action is 0.
	if (action == 0) {
	    intersection.getPhases().get(active).setGreen(false);
	    active = active + 1;
	    active = (active > intersection.getPhases().size() - 1) ? 0 : active;

	    intersection.setActivePhase(active);
	    currentActive = intersection.getPhases().get(active);
	    currentActive.setGreen(true);
	    currentActive.setGreenDuration(SimulationConstants.MIN_PHASE_TIME);
	} else {
	    currentActive = intersection.getPhases().get(active);
	    currentActive.continueInGreen();
	}
    }

    /**
     * Initialize the intersections
     * 
     * @param intersections
     */
    public void initializeDRLAgents(List<Intersection> intersections) {
	try {

	    this.intersections = intersections;
	    this.drlControllerMap = new HashMap<Intersection, DRLAgent>();
	    for (Intersection intersection : intersections) {
		int statelen = intersection.getPhases().size();
		for (Intersection neighbor : intersection.getNeighbours())
		    statelen += neighbor.getPhases().size();

		DRLAgent drlAgent = new DRLAgent(statelen, SimulationConstants.LEARNING_RATE,
			SimulationConstants.REGULARIZATION_PARAM, new ConcurrentLinkedQueue<>());
		drlAgent.setEpsilon(0.95);
		drlControllerMap.put(intersection, drlAgent);

	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    public Map<Intersection, DRLAgent> getDrlControllerMap() {
	return drlControllerMap;
    }

}
