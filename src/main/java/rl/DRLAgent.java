package rl;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IAMax;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import intersections.RLIntersectionController;
import main.SimulationMain;
import utils.SimulationConstants;

public class DRLAgent {

    private MultiLayerNetwork actionModel;
    private int numOfInputs;
    private Queue<StateRewardTuple> stateRewardQueue;
    private INDArray prevState;
    private int prevAction;
    private double epsilon;
    private ExperienceReplay expRL;
    private int drlAgentId;
    private static int id = 1;
    private static final int NUM_OUTPUTS = 2;

    public DRLAgent(int numOfInputs, double learningRate, double regularization,
	    Queue<StateRewardTuple> stateRewardQueue) throws IOException {
	this.numOfInputs = numOfInputs;
	this.stateRewardQueue = stateRewardQueue;
	drlAgentId = 39 * id++;

	if (!RLIntersectionController.isTrained) {
	    int nHidden = numOfInputs * 7;
	    actionModel = new MultiLayerNetwork(new NeuralNetConfiguration.Builder().seed(drlAgentId)
		    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
		    .learningRate(learningRate).updater(Updater.RMSPROP).rmsDecay(0.95).regularization(true)
		    .l2(regularization).list()
		    .layer(0,
			    new DenseLayer.Builder().nIn(numOfInputs).nOut(nHidden).activation("leakyrelu")
				    .weightInit(WeightInit.XAVIER).build())
		    .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation("identity")
			    .nIn(nHidden).nOut(NUM_OUTPUTS).build())
		    .pretrain(false).backprop(true).build());
	} else {
	    File tempFile = new File(SimulationConstants.NN_PATH + "drl_agent" + drlAgentId + ".tmp");
	    if (tempFile.exists() && !tempFile.isDirectory()) {
		System.out.println("Loading neural network from file");
		actionModel = ModelSerializer.restoreMultiLayerNetwork(tempFile);
	    } else {
		System.out.println("NN file not found exit..");
		System.exit(0);
	    }
	}

	actionModel.init();
	actionModel.setListeners(new ScoreIterationListener(1000));
	expRL = new ExperienceReplay(actionModel);
    }

    public int getAction(StateRewardTuple tuple) {
	INDArray state = Nd4j.create(tuple.getState(), new int[] { 1, tuple.getState().length });
	int actionNext = 0;

	if (!RLIntersectionController.isTrained) {
	    actionNext = getBestAction(state);
	    if (prevState != null) {
		ReplayTuple replay = new ReplayTuple(prevState.dup(), prevAction, state, tuple.getReward(),
			tuple.isTerminalState());
		expRL.qLearning(replay);
	    }
	    prevState = state;
	    prevAction = actionNext;
	} else {
	    // The model already trained.
	    INDArray output = actionModel.output(state, false);
	    if (SimulationMain.random.nextDouble() > 0.01)
		actionNext = Nd4j.getExecutioner().execAndReturn(new IAMax(output)).getFinalResult();
	    else
		actionNext = SimulationMain.random.nextInt(output.length());
	}

	return actionNext;
    }

    /**
     * Get the best action as determined by the neural-net for the provided
     * state s.
     * 
     * @param s
     *            the current traffic state.
     * @return
     */
    private int getBestAction(INDArray s) {
	INDArray actions = actionModel.output(s, true);
	// Decide on the action to take.
	int action = -1;
	if (SimulationMain.random.nextDouble() < epsilon)
	    action = SimulationMain.random.nextInt(actions.length());
	else
	    action = Nd4j.getExecutioner().execAndReturn(new IAMax(actions)).getFinalResult();

	return action;
    }

    public Queue<StateRewardTuple> getStateRewardQueue() {
	return stateRewardQueue;
    }

    /**
     * @return the epsilon
     */
    public double getEpsilon() {
	return epsilon;
    }

    /**
     * @param epsilon
     *            the epsilon to set
     */
    public void setEpsilon(double epsilon) {
	this.epsilon = epsilon;
    }

    public int getNumOfInputs() {
	return numOfInputs;
    }

    public ExperienceReplay getExpRL() {
	return expRL;
    }

    /**
     * Get the DRL agent's ID.
     * 
     * @return
     */
    public int getDrlAgentId() {
	return drlAgentId;
    }

    public MultiLayerNetwork getModel() {
	return actionModel;
    }

    public void setModel(MultiLayerNetwork model) {
	this.actionModel = model;
    }

}
