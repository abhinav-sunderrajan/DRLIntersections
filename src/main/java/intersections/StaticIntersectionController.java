package intersections;

/**
 * Fixed phase timings just change from one phase to an other
 * 
 * @author abhinav.sunderrajan
 *
 */
public class StaticIntersectionController extends IntersectionController {

    @Override
    protected void nextPhase(Intersection intersection) {
	int currentActive = intersection.getActivePhase();
	intersection.getPhases().get(currentActive);
	int nextActive = currentActive + 1;

	nextActive = (nextActive > intersection.getPhases().size() - 1) ? 0 : nextActive;
	intersection.getPhases().get(currentActive).setGreen(false);
	intersection.setActivePhase(nextActive);
	Phase phaseGreen = intersection.getPhases().get(nextActive);
	phaseGreen.setGreen(true);

	intersection.setCurrentGreenTime(simulationTime.get() + phaseGreen.getGreenDuration());
	// System.out.println(simulationTime + ": " +
	// intersection.getPhases().get(nextActive).getPhaseCell().getCellId()
	// + " active for " + phaseGreen.getGreenDuration());

    }

}
