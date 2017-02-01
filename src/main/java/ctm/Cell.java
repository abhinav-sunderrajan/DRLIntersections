package ctm;

import java.util.ArrayList;
import java.util.List;

import main.SimulationMain;
import rnwmodel.Road;
import utils.SimulationConstants;

/**
 * Base class of the cell in the cell transmission model.
 * 
 * @author abhinav
 * 
 */
public abstract class Cell {

    protected String cellId;
    protected List<Cell> predecessors;
    protected List<Cell> successors;
    // Id of the road the cell belongs
    protected Road road;

    protected int numOfLanes;

    // cell capacity is veh/hour
    protected double cellCapacity;

    // model parameters

    // Cell length
    protected double length;
    // maximum number of vehicles that can be contained in a cell.
    // Number of vehicles present in the cell at time t
    protected double nt;
    // free flow speed
    protected double freeFlowSpeed;
    // backward propagation speed.

    // The number of vehicles that leave the cell in a time unit
    protected double outflow;

    protected double sendingPotential;

    protected double receivePotential;
    private double ntOld;

    protected double meanSpeed;
    protected double oldMeanSpeed;

    protected double densityAntic;
    protected double density;
    protected double criticalDensity;
    protected double nMax;
    private static boolean applyRampMetering;

    /**
     * The abstract cell class.
     * 
     * @param cellId
     * @param length
     * @param w
     */
    public Cell(String cellId, double length, Road road) {
	this.cellId = cellId;
	this.road = road;
	this.numOfLanes = road.getLaneCount();
	this.length = length;
	this.freeFlowSpeed = road.getSpeedLimit()[1] * (5.0 / 18);
	predecessors = new ArrayList<Cell>();
	successors = new ArrayList<Cell>();

	if (length > 0) {
	    this.nt = 0;
	    this.meanSpeed = freeFlowSpeed;
	    this.oldMeanSpeed = freeFlowSpeed;
	    double meanVehicleLength = SimulationConstants.LEFF;
	    criticalDensity = 1.0 / (SimulationConstants.TIME_GAP * freeFlowSpeed + meanVehicleLength);
	    this.nMax = Math.round(length * criticalDensity * numOfLanes);
	    cellCapacity = numOfLanes / (SimulationConstants.TIME_GAP + (SimulationConstants.LEFF / freeFlowSpeed));
	}

    }

    /**
     * @return the cellCapacity
     */
    public double getCellCapacity() {
	return cellCapacity;
    }

    /**
     * @return the density
     */
    public double getDensity() {
	return density;
    }

    /**
     * @param density
     *            the density to set
     */
    public void setDensity(double density) {
	this.density = density;
    }

    /**
     * @return the meanSpeed
     */
    public double getMeanSpeed() {
	return meanSpeed;
    }

    /**
     * @param meanSpeed
     *            the meanSpeed to set
     */
    public void setMeanSpeed(double meanSpeed) {
	this.meanSpeed = meanSpeed;
    }

    /**
     * The number of vehicles that moves to the next cell(s) in this current
     * time step.
     * 
     * @return the out-flow
     */
    public double getOutflow() {
	return outflow;
    }

    /**
     * @param outflow
     *            the out-flow to set
     */
    public void setOutflow(double outflow) {
	this.outflow = outflow;
    }

    /**
     * The road ID associated with this cell.
     * 
     * @return the roadId
     */
    public Road getRoad() {
	return road;
    }

    /**
     * @return the cellId
     */
    public String getCellId() {
	return cellId;
    }

    /**
     * @param cellId
     *            the cellId to set
     */
    public void setCellId(String cellId) {
	this.cellId = cellId;
    }

    /**
     * Update the number of vehicles in each cell after simulation tick. Based
     * on the law of conservation vehicles.
     */
    public void updateNumberOfVehiclesInCell() {
	double inflow = 0;
	inflow = predecessors.get(0).outflow;

	this.ntOld = nt;
	nt = nt + inflow - outflow;
	density = nt / (length * numOfLanes);

	if (nt < 0) {
	    throw new IllegalStateException(
		    "The number of vehicle in a cell cannot be less than zero.." + cellId + " " + nt);
	}

    }

    /**
     * Update the anticipated density for the next time step.
     */
    public void updateAnticipatedDensity() {

	// Update anticipated density
	if (!(this instanceof SourceCell || this instanceof SinkCell)) {
	    // update the density in the cell.
	    densityAntic = SimulationConstants.ALPHA_ANTIC * density;

	    Cell successor = successors.get(0);
	    if (successor instanceof SinkCell) {
		densityAntic += (1 - SimulationConstants.ALPHA_ANTIC) * density;
	    } else {
		densityAntic += (1 - SimulationConstants.ALPHA_ANTIC)
			* (successor.nt / (successor.length * successor.numOfLanes));
	    }

	}

    }

    /**
     * Update the mean speed of the cell.
     */
    public void updateMeanSpeed() {

	if (!(this instanceof SourceCell || this instanceof SinkCell)) {

	    double term1 = freeFlowSpeed * Math.exp(
		    (-1 / SimulationConstants.AM) * Math.pow((densityAntic / criticalDensity), SimulationConstants.AM));

	    double term2 = -1;
	    if (nt > 0) {

		double speedofIncomingVehicles = 0.0;
		for (Cell predecessor : predecessors) {
		    if (predecessor instanceof SourceCell) {
			speedofIncomingVehicles += predecessor.outflow * oldMeanSpeed;
		    } else {
			speedofIncomingVehicles += predecessor.outflow * predecessor.oldMeanSpeed;
		    }
		}

		double speedofVehiclesremaining = (ntOld - outflow) * oldMeanSpeed;
		term2 = (speedofVehiclesremaining + speedofIncomingVehicles) / nt;
	    } else {
		term2 = freeFlowSpeed;
	    }

	    term2 = Math.max(SimulationConstants.V_OUT_MIN, term2);

	    meanSpeed = 0.2 * term1 + 0.8 * term2;

	    // logic for adding noise
	    double noise = -1.1 * SimulationMain.random.nextDouble();
	    meanSpeed += noise;

	    if (meanSpeed < 0.0)
		meanSpeed = 0.0;

	    this.meanSpeed = meanSpeed > freeFlowSpeed ? freeFlowSpeed : meanSpeed;
	    double meanVehicleLength = SimulationConstants.LEFF;
	    double maxDesnsityPerlane = length / (SimulationConstants.TIME_GAP * meanSpeed + meanVehicleLength);
	    this.nMax = Math.round(maxDesnsityPerlane * numOfLanes);
	    this.oldMeanSpeed = meanSpeed;

	}

    }

    /**
     * Return the receiving potential of this cell.
     * 
     * @return
     */
    public void determineReceivePotential() {
	this.receivePotential = nMax - nt;
	if (receivePotential < 0)
	    receivePotential = 0;
    }

    /**
     * Computes and sets the sending potential of this cell.
     * 
     * @return
     */
    public void determineSendingPotential() {
	double param1 = (nt * meanSpeed * SimulationConstants.TIME_STEP) / length;
	double param2 = (nt * SimulationConstants.V_OUT_MIN * SimulationConstants.TIME_STEP) / length;
	this.sendingPotential = Math.round(Math.max(param1, param2));
	if (sendingPotential > nt)
	    throw new IllegalStateException(
		    "The sending potential cannot be greater than the number of vehicles in the cell " + cellId);
    }

    @Override
    public String toString() {
	return cellId;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof Cell) {
	    Cell c = (Cell) o;
	    if (c.cellId.equalsIgnoreCase(this.cellId)) {
		return true;
	    } else {
		return false;
	    }
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	return cellId.hashCode();
    }

    /**
     * Update the flow along all the connectors associated with cell
     */
    public abstract void updateOutFlow();

    public void setRoad(Road road) {
	this.road = road;
    }

    /**
     * Add cell to the predecessor only if its not present.
     * 
     * @param cell
     */
    public void addPredecessor(Cell cell) {
	if (!predecessors.contains(cell))
	    predecessors.add(cell);
    }

    /**
     * Add cell to the predecessor only if its not present.
     * 
     * @param cell
     */
    public void addSuccessor(Cell cell) {

	if (!successors.contains(cell)) {
	    successors.add(cell);
	}

    }

    /**
     * Set the number of vehicles in the cell.
     * 
     * @param nt
     */
    public void setNumberOfvehicles(double nt) {
	this.nt = nt;
	this.density = nt / (length * numOfLanes);
    }

    /**
     * @return the nt the number of vehicles currently in the cell.
     */
    public double getNumOfVehicles() {
	return nt;
    }

    /**
     * @return the nMax the maximum number of vehicles that can be accommodated
     *         in the cell.
     */
    public double getnMax() {
	return nMax;
    }

    /**
     * @param nMax
     *            the nMax to set
     */
    public void setnMax(double nMax) {
	this.nMax = nMax;
    }

    /**
     * @return the length of the cell.
     */
    public double getLength() {
	return length;
    }

    /**
     * @return the numOfLanes of lanes in this cell.
     */
    public int getNumOfLanes() {
	return numOfLanes;
    }

    /**
     * @param numOfLanes
     *            the numOfLanes to set
     */
    public void setNumOfLanes(int numOfLanes) {
	this.numOfLanes = numOfLanes;
    }

    /**
     * @return the freeFlowSpeed of this cell.
     */
    public double getFreeFlowSpeed() {
	return freeFlowSpeed;
    }

    /**
     * @param freeFlowSpeed
     *            the freeFlowSpeed to set
     */
    public void setFreeFlowSpeed(double freeFlowSpeed) {
	this.freeFlowSpeed = freeFlowSpeed;
	criticalDensity = 1.0 / (SimulationConstants.TIME_GAP * freeFlowSpeed + SimulationConstants.LEFF);
    }

    /**
     * @return the predecessors
     */
    public List<Cell> getPredecessors() {
	return predecessors;
    }

    /**
     * @return the successors
     */
    public List<Cell> getSuccessors() {
	return successors;
    }

    /**
     * @return the criticalDensity
     */
    public double getCriticalDensity() {
	return criticalDensity;
    }

    /**
     * @return the applyRampMetering
     */
    public static boolean isApplyRampMetering() {
	return applyRampMetering;
    }

    /**
     * @param applyRampMetering
     *            the applyRampMetering to set
     */
    public static void setApplyRampMetering(boolean applyRampMetering) {
	Cell.applyRampMetering = applyRampMetering;
    }

    public double getSendingPotential() {
	return sendingPotential;
    }

    public void setSendingPotential(double sendingPotential) {
	this.sendingPotential = sendingPotential;
    }

    public double getReceivePotential() {
	return receivePotential;
    }

    public void setReceivePotential(double receivePotential) {
	this.receivePotential = receivePotential;
    }

    public void setNt(double nt) {
	this.nt = nt;
    }

    public void setOldMeanSpeed(double oldMeanSpeed) {
	this.oldMeanSpeed = oldMeanSpeed;
    }

    public double getNtOld() {
	return ntOld;
    }

    public void setNtOld(double ntOld) {
	this.ntOld = ntOld;
    }

}
