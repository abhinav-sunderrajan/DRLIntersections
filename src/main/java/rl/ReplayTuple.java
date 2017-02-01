package rl;

import java.io.Serializable;

import org.nd4j.linalg.api.ndarray.INDArray;

public class ReplayTuple implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private INDArray oldState;
    private int action;
    private INDArray nextState;
    private double reward;
    private boolean isTerminalState;

    /**
     * A replay tuple
     * 
     * @param oldState
     *            the previous state
     * @param action
     *            the action taken
     * @param nextState
     *            the next state where transition occurs
     * @param reward
     *            the reward
     * @param isTerminalState
     */
    public ReplayTuple(INDArray oldState, int action, INDArray nextState, double reward, boolean isTerminalState) {
	this.oldState = oldState;
	this.action = action;
	this.nextState = nextState;
	this.reward = reward;
	this.isTerminalState = isTerminalState;
    }

    /**
     * @return the state
     */
    public INDArray getOldState() {
	return oldState;
    }

    /**
     * @return the trafficLights
     */
    public int getAction() {
	return action;
    }

    /**
     * @return the nextState
     */
    public INDArray getNextState() {
	return nextState;
    }

    /**
     * @return the reward
     */
    public double getReward() {
	return reward;
    }

    public boolean isTerminalState() {
	return isTerminalState;
    }

}
