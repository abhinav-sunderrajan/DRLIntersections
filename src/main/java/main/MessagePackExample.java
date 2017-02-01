package main;

import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;

import rl.StateRewardTuple;

public class MessagePackExample {

    public static void main(String args[]) throws IOException {
	double a[] = { 1.99, 4.5, 8.7, 9.98 };

	MessagePack msgpack = new MessagePack();
	msgpack.register(StateRewardTuple.class);
	StateRewardTuple srt = new StateRewardTuple();
	srt.setReward(3.33);
	srt.setState(a);
	srt.setTerminalState(true);

	// Serialize
	byte[] raw = msgpack.write(srt);

	// Deserialize
	Value dynamic = msgpack.read(raw);
	ArrayValue val = dynamic.asArrayValue();
	System.out.println(val.get(0).asBooleanValue() + " <<>> " + val.get(1).asArrayValue().get(1) + " <<>> "
		+ val.get(2).asFloatValue());

	// System.out.println(v + " <<>> " + v.get(0));
    }

}
