package rl;

import java.io.Serializable;

public class StateRewardTuple implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private boolean isTerminalState;
    private double[] state;
    private double reward;

    public StateRewardTuple() {

    }

    public double[] getState() {
	return state;
    }

    public void setState(double[] state) {
	this.state = state;
    }

    public double getReward() {
	return reward;
    }

    public void setReward(double reward) {
	this.reward = reward;
    }

    public boolean isTerminalState() {
	return isTerminalState;
    }

    public void setTerminalState(boolean isTerminalState) {
	this.isTerminalState = isTerminalState;
    }

}
