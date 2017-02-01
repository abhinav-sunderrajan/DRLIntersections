package intersections;

import java.util.ArrayList;
import java.util.List;

import ctm.Cell;
import ctm.SourceCell;
import utils.SimulationConstants;

/**
 * 
 * @author abhinav.sunderrajan
 *
 */
public class Intersection

{

    protected int intersectionId;
    protected List<Phase> phases;
    protected double cycleTime;
    protected List<Intersection> neighbours;
    protected int numOfPhases;
    protected int maxPhaseTime;
    protected int activePhase;
    private int currentGreenTime;

    // DRL based parameters..
    private double delay;
    private double prevDelay;

    /**
     * 
     * @param cycleTime
     * @param intersectionId
     * @param numOfPhases
     */
    public Intersection(int cycleTime, int intersectionId, int numOfPhases) {
	this.cycleTime = cycleTime;
	this.phases = new ArrayList<Phase>();
	this.neighbours = new ArrayList<Intersection>();
	this.intersectionId = intersectionId;
	this.numOfPhases = numOfPhases;
	this.maxPhaseTime = cycleTime - (SimulationConstants.MIN_PHASE_TIME * (numOfPhases - 1));
	prevDelay = -1.0;
	delay = -1.0;
    }

    public void addPhaseToIntersection(Phase phase) {
	phases.add(phase);
    }

    public void addNeighbourToIntersection(Intersection neighbour) {
	neighbours.add(neighbour);
    }

    public double getCycleTime() {
	return cycleTime;
    }

    public List<Phase> getPhases() {
	return phases;
    }

    public List<Intersection> getNeighbours() {
	return neighbours;
    }

    public int getActivePhase() {
	return activePhase;
    }

    public void setActivePhase(int activePhase) {
	this.activePhase = activePhase;
    }

    public int getCurrentGreenTime() {
	return currentGreenTime;
    }

    public void setCurrentGreenTime(int tNow) {
	this.currentGreenTime = tNow;
    }

    @Override
    public boolean equals(Object other) {
	if (other instanceof Intersection) {
	    return ((Intersection) other).intersectionId == this.intersectionId;
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	return this.intersectionId;
    }

    /**
     * Get the over all delay experienced by all phases of this intersection
     * 
     * @return
     */
    public double computeIntersectionDelay() {

	if (delay > -1.0) {
	    prevDelay = delay;
	    delay = 0.0;
	}
	for (Phase phase : phases) {
	    for (Cell cell : phase.getPhaseAssociatedCells()) {
		if (cell instanceof SourceCell) {
		    delay += ((SourceCell) cell).getSourceDelay();
		} else {
		    double ff = (cell.getNumOfVehicles() * cell.getFreeFlowSpeed() * SimulationConstants.TIME_STEP)
			    / cell.getLength();
		    ff = Math.round(ff);
		    if ((ff - cell.getOutflow()) > 0)
			delay += (ff - cell.getOutflow());
		}
	    }
	}

	return delay;
    }

    public double getPrevDelay() {
	return prevDelay;
    }

    public double getDelay() {
	return delay;
    }

    public void setDelay(double delay) {
	this.delay = delay;
    }

    public void setPrevDelay(double prevDelay) {
	this.prevDelay = prevDelay;
    }

}
