package intersections;

import utils.SimulationConstants;

/**
 * Dynamic implementation takes the number of vehicles in the phase and its
 * preceeding cells.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class DynamicIntersectionController extends IntersectionController {

    public DynamicIntersectionController() {
    }

    @Override
    protected void nextPhase(Intersection intersection) {

	double totalDensity = 0.0;
	for (Phase phase : intersection.getPhases()) {
	    totalDensity += phase.getPhaseDensity();
	}

	int currentActive = intersection.getActivePhase();
	intersection.getPhases().get(currentActive);
	int nextActive = currentActive + 1;

	nextActive = (nextActive > intersection.getPhases().size() - 1) ? 0 : nextActive;
	intersection.getPhases().get(currentActive).setGreen(false);
	intersection.setActivePhase(nextActive);
	Phase phaseGreen = intersection.getPhases().get(nextActive);
	int greenDuration = (int) (phaseGreen.getPhaseDensity() * intersection.getCycleTime() / totalDensity);
	if (greenDuration < SimulationConstants.MIN_PHASE_TIME)
	    greenDuration = SimulationConstants.MIN_PHASE_TIME;

	phaseGreen.setGreen(true);
	phaseGreen.setGreenDuration(greenDuration);

	intersection.setCurrentGreenTime(simulationTime.get() + phaseGreen.getGreenDuration());
	System.out.println(simulationTime + ": " + intersection.getPhases().get(nextActive).getPhaseCell().getCellId()
		+ " active for " + phaseGreen.getGreenDuration());
    }

}
