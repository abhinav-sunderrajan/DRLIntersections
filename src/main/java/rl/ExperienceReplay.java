package rl;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.Max;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import main.SimulationMain;
import utils.SimulationConstants;

/**
 * Implements experience replay.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class ExperienceReplay {

    private List<ReplayTuple> replayList;
    private static int h = 0;
    private List<Integer> batchList;
    private MultiLayerNetwork targetModel;
    private MultiLayerNetwork actionModel;
    private long step = 0;
    private static final double DISCOUNT = 0.98;
    private boolean replayListFull = false;

    public ExperienceReplay(MultiLayerNetwork model) {
	replayList = new ArrayList<ReplayTuple>();
	batchList = new ArrayList<Integer>();
	targetModel = model.clone();
	targetModel.init();
	this.actionModel = model;
    }

    public void qLearning(ReplayTuple replay) {
	if (replayList.size() < SimulationConstants.REPLAY_SIZE) {
	    replayList.add(replay);
	} else {
	    if (!replayListFull) {
		replayListFull = true;
		System.out.println("Replay list full " + this.hashCode());
	    }
	    if (h < (replayList.size() - 1))
		h += 1;
	    else
		h = 0;
	    replayList.set(h, replay);
	    List<ReplayTuple> miniBatch = getMiniBatch();

	    List<INDArray> x = new ArrayList<INDArray>();
	    List<INDArray> y = new ArrayList<INDArray>();

	    for (ReplayTuple memory : miniBatch) {
		INDArray oldQVal = targetModel.output(memory.getOldState(), true);
		INDArray nextQVal = targetModel.output(memory.getNextState(), true);
		double maxQ = Nd4j.getExecutioner().execAndReturn(new Max(nextQVal)).getFinalResult().doubleValue();

		double update = 0.0;
		if (memory.isTerminalState())
		    update = memory.getReward();
		else
		    update = memory.getReward() + maxQ * DISCOUNT;
		INDArray newQVal = oldQVal.putScalar(0, memory.getAction(), update);
		x.add(memory.getOldState());
		y.add(newQVal);
	    }
	    INDArray oldStates = Nd4j.vstack(x);
	    INDArray newQVals = Nd4j.vstack(y);

	    DataSet dataSet = new DataSet(oldStates, newQVals);
	    List<DataSet> listDs = dataSet.asList();
	    DataSetIterator iterator = new ListDataSetIterator(listDs, SimulationConstants.BATCH_SIZE);
	    actionModel.fit(iterator);

	    step++;
	    if (step % 100 == 0) {
		targetModel = actionModel.clone();
		targetModel.init();
	    }
	}
    }

    private List<ReplayTuple> getMiniBatch() {
	List<ReplayTuple> targetList = new ArrayList<>();
	while (targetList.size() < SimulationConstants.BATCH_SIZE) {
	    int j = SimulationMain.random.nextInt(SimulationConstants.REPLAY_SIZE);
	    if (!batchList.contains(j)) {
		targetList.add(replayList.get(j));
		batchList.add(j);
	    }
	}
	batchList.clear();
	return targetList;
    }

    /**
     * @return the replayList
     */
    public List<ReplayTuple> getReplayList() {
	return replayList;
    }

    /**
     * @return the target
     */
    public MultiLayerNetwork getTargetModel() {
	return targetModel;
    }

}
