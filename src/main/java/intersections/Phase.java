package intersections;

import java.util.ArrayList;
import java.util.List;

import ctm.Cell;
import ctm.SourceCell;
import main.SimulationMain;
import utils.SimulationConstants;

public class Phase {

    private double phaseDensity;
    private boolean isGreen;
    private Cell phaseCell;
    private List<Cell> phaseAssociatedCells;

    // Use this method for the R.L controller.
    private int greenDuration;

    public Phase(Cell phaseCell) {
	this.phaseCell = phaseCell;
	this.phaseAssociatedCells = new ArrayList<Cell>();
	phaseAssociatedCells.add(phaseCell);
	Cell predecessor = phaseCell.getPredecessors().get(0);
	while (true) {
	    phaseAssociatedCells.add(predecessor);
	    if (predecessor.getPredecessors().size() == 0 || phaseAssociatedCells.size() == 4)
		break;
	    predecessor = predecessor.getPredecessors().get(0);
	}
	System.out.println(phaseAssociatedCells);
    }

    /**
     * Returns the number of vehicles waiting at the phase cells and its
     * predecessors excludes the source cells.
     * 
     * @return
     */
    public double getPhaseDensity() {
	phaseDensity = 0.0;
	for (Cell cell : phaseAssociatedCells) {
	    if (!(cell instanceof SourceCell))
		phaseDensity += cell.getDensity();
	}
	phaseDensity = Double.parseDouble(SimulationMain.DF.format(phaseDensity));
	return phaseDensity;
    }

    public boolean isGreen() {
	return isGreen;
    }

    public void setGreen(boolean isGreen) {
	this.isGreen = isGreen;
	if (!isGreen) {
	    greenDuration = 0;
	}
    }

    public Cell getPhaseCell() {
	return phaseCell;
    }

    public void setGreenDuration(Integer duration) {
	greenDuration = duration;
    }

    public void continueInGreen() {
	greenDuration += SimulationConstants.MIN_PHASE_TIME;
    }

    public int getGreenDuration() {
	return greenDuration;
    }

    /**
     * Returns the cells associated with this phase..
     * 
     * @return
     */
    public List<Cell> getPhaseAssociatedCells() {
	return phaseAssociatedCells;
    }

}
