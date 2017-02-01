package intersections;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import utils.SimulationConstants;

public abstract class IntersectionController implements Runnable {
    protected List<Intersection> intersections;
    protected AtomicInteger simulationTime;
    protected Object simulationMonitor;

    public IntersectionController() {
	this.simulationMonitor = new Object();
    }

    @Override
    public void run() {
	try {
	    // System.out.println("Waiting for the simulation to start..");

	    synchronized (simulationMonitor) {
		simulationMonitor.wait();
	    }

	    while (simulationTime.get() < SimulationConstants.FINISH_TIME) {
		for (Intersection intersection : intersections) {
		    if (simulationTime.get() > intersection.getCurrentGreenTime())
			nextPhase(intersection);

		}

		synchronized (simulationMonitor) {
		    simulationMonitor.wait();
		}

	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

    }

    public Object getControllerMonitor() {
	return simulationMonitor;
    }

    public AtomicInteger getSimulationTime() {
	return simulationTime;
    }

    public void setSimulationTime(AtomicInteger simulationTime) {
	this.simulationTime = simulationTime;
    }

    public void setIntersections(List<Intersection> intersections) {
	this.intersections = intersections;
    }

    /**
     * This method decides how to change the phase of the intersection.
     * 
     * @param intersection
     */
    protected abstract void nextPhase(Intersection intersection);

}
