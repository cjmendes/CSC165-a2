package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Degreef;

public class RotateCameraRight extends AbstractInputAction {

	private Node avN;
	
	public RotateCameraRight(Node n) {
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		avN.yaw(Degreef.createFrom(-3f));
	}
}
