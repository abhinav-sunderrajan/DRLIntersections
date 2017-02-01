package ctm;

import main.SimulationMain;
import rnwmodel.Road;

/**
 * An ordinary cell has only one outgoing cell. Can have multiple incoming
 * cells.
 * 
 * 
 * @author abhinav
 * 
 */
public class OrdinaryCell extends Cell {

    public OrdinaryCell(String cellId, double length, Road road) {
	super(cellId, length, road);
    }

    @Override
    public void updateOutFlow() {
	Cell Ek = this.successors.get(0);
	if (Ek instanceof SinkCell) {
	    double temp = 0.6 + 0.3 * SimulationMain.random.nextDouble();
	    this.outflow = Math.round(temp * sendingPotential);
	} else {
	    this.outflow = Math.min(Ek.receivePotential, sendingPotential);
	}

    }
}
