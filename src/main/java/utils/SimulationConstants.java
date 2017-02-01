package utils;

/**
 * All private/public static final primitives should go here.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class SimulationConstants {

    // Simulation constants
    public static final Integer FINISH_TIME = 1800;
    public static final Integer START_TIME = 0;
    public static final Integer TIME_STEP = 4;

    public static final int MIN_PHASE_TIME = 20;

    // CTM parameters

    public static final double LEFF = 5.0;
    public static Double TIME_GAP = 1.25;

    public static final double ALPHA_ANTIC = 0.75;

    // From the meta-net paper.
    public static double AM = 2.34;

    // The rate at which vehicles move at the down stream section of a
    // congested region.
    public static double V_OUT_MIN = 1.6;

    // have DRL or not
    public static boolean HAVE_DRL = true;

    // Neural network file.
    public static final String NN_PATH = "src/main/resources/";

    // NN hyper parameters

    public static final double LEARNING_RATE = 1.0e-3;

    public static final double REGULARIZATION_PARAM = 1.0e-4;

    // Experience replay
    public static final int REPLAY_SIZE = 5000;

    public static final int BATCH_SIZE = 32;

}
