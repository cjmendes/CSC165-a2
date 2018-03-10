package a2;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;

import myGameEngine.BounceNodeController;
import myGameEngine.Camera3Pcontroller;
import myGameEngine.MoveBackwardAction;
import myGameEngine.MoveForwardAction;
import myGameEngine.MoveLeftAction;
import myGameEngine.MoveRightAction;
import myGameEngine.QuitGameAction;
import myGameEngine.RideDolphinAction;
import myGameEngine.RotateCameraDown;
import myGameEngine.RotateCameraLeft;
import myGameEngine.RotateCameraRight;
import myGameEngine.RotateCameraUp;
import myGameEngine.SprintAction;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rage.util.Configuration;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.rendersystem.shader.*;

public class MyGame extends VariableFrameRateGame {

	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, counterStr2, dispStr;
	int elapsTimeSec, counter = 0, counter2 = 0;
	
	// Variable for changing different game values
	private int NUM_OF_COINS = 30;
	private int NUM_OF_EXTRA_OBJECTS = 10;
	private int SIZE_OF_SPACE = 38;
	private float MAX_SPEED = 0.5f;
	
	//Entity dolphinE;
	private SceneNode activeNode;
	
	private Camera3Pcontroller orbitController1, orbitController2;
	//***** Input Devices and Actions *****
	private InputManager im;
	private Action quitGameAction, moveForwardActionD, moveForwardActionD2,
			moveBackwardActionD, moveBackwardActionD2, moveRightActionD, 
			moveRightActionD2, moveLeftActionD, moveLeftActionD2, rotateCameraRightD,
			rotateCameraRightD2, rotateCameraLeftD, rotateCameraLeftD2, 
			rideDolphinAction, sprintActionD, sprintActionD2;
	//***** End Input Devices and Actions *****
	
	private SceneNode onDolphinNode;
	private boolean sprint = true, sprint2 = true;
	
	// Variable for collision with coins
	private int[] coinList = new int[NUM_OF_COINS];
	private int position = 0;
	
	//Variable for the sprint function
	private int[] speedBar = new int[] {1,0,0,0,0};
	private int[] speedBar2 = new int[] {1,0,0,0,0};
	private int positionBoost = 1, positionBoost2 = 1;
	private int speedTimer = 0, speedTimer2 = 0;
	
    public MyGame() {
        super();
		System.out.println("press W to move forward");
		System.out.println("press S to move backward");
		System.out.println("press A to move right");
		System.out.println("press D to move left");
		System.out.println("press RIGHT ARROW to turn camera right");
		System.out.println("press LEFT ARROW to turn camera left");
		System.out.println("press SHIFT for temporary boost when a coin is collected");
    }

    public static void main(String[] args) {
        Game game = new MyGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
//******************************************************************************************************************
    
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}
	
	// Create Multiple Viewports
	@Override
	protected void setupWindowViewports(RenderWindow rw) {
		rw.addKeyListener(this);
		
		Viewport topViewport = rw.getViewport(0);
		topViewport.setDimensions(.51f, .01f, .99f, .49f);	//B,L,W,H
		topViewport.setClearColor(new Color(1.0f, 0.7f, .7f));
		
		Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
		botViewport.setClearColor(new Color(.5f, 1.0f, .5f));
	}

//******************************************************************************************************************

	
    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        
        // Setup Camera 1 & Camera 1 Node
        Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
        
        SceneNode cameraN = rootNode.createChildSceneNode("MainCameraNode");
        cameraN.attachObject(camera);
        camera.setMode('n');
        camera.getFrustum().setFarClipDistance(1000.0f);
        
        // Setup Camera 2 & Camera 2 Node
        Camera camera2 = sm.createCamera("MainCamera2", Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(camera2);
        
        SceneNode cameraN2 = rootNode.createChildSceneNode("MainCamera2Node");
        cameraN2.attachObject(camera2);
        camera2.setMode('n');
        camera2.getFrustum().setFarClipDistance(1000.0f);
		
		camera2.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera2.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera2.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));

    }

//******************************************************************************************************************
//************************ Game Engine SETUP Function **************************************************************
//******************************************************************************************************************
   
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	
    	// Create Dolphin
        makeDolphin(eng, sm);
        
        // Create Earth
        //makeEarth(eng, sm);
   
        // Create the Axis
        setupAxis(eng, sm);
        
        // Create Floor
        createFloor(eng, sm);
        
		activeNode = this.getEngine().getSceneManager().getSceneNode("dolphinNode");
        
        // Set Rotation for the Diamonds
		SceneNode DiamondParentNG = sm.getSceneNode("dolphinNodeG").createChildSceneNode("diamondParentNodeG");
		BounceNodeController bnc = new BounceNodeController();
        for( int i = 0; i < NUM_OF_EXTRA_OBJECTS; i++)
        	createDiamond(eng, sm, i);
       
        bnc.addNode(sm.getSceneNode("diamondParentNodeG"));
        
        sm.addController(bnc);
    	
        // Set Bounce for the Coins
        RotationController rc = new RotationController(Vector3f.createUnitVectorZ(), 0.05f);
        for( int i = 0; i < NUM_OF_COINS; i++)
        	rc.addNode(makeCoin(eng, sm, i));
        
        sm.addController(rc);
        

        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
        //***** Light Node *****
        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(40f);
		
        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        
    	// Setup the input actions
    	setupInputs(sm);
    	
        setupOrbitCameras(eng, sm);
        sm.getSceneNode("dolphinNode").yaw(Degreef.createFrom(45.0f));
    }

//******************************************************************************************************************
//************************ Game Engine UPDATE Function *************************************************************
//******************************************************************************************************************
    
    @Override
    protected void update(Engine engine) {
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		counterStr2 = Integer.toString(counter2);
		
		dispStr = "Dolphin2 Time = " + elapsTimeStr + "   Coins Picked Up = " + counterStr2 
				+ "   Speed Boost = " + printPowerUp(speedBar2);
		rs.setHUD(dispStr, 15, 15);
		
		dispStr = "Dolphin Time = " + elapsTimeStr + "   Coins Picked Up = " + counterStr 
				+ "   Speed Boost = " + printPowerUp(speedBar);
		rs.setHUD2(dispStr, 15, rs.getCanvas().getHeight()/2 + 15);
			
		// Tell the input manager to process the inputs
		im.update(elapsTime);
		orbitController1.updateCameraPosition();
		orbitController2.updateCameraPosition();
		
		engine.getSceneManager().getSceneNode("MainCameraNode").setLocalRotation(engine.getSceneManager().getSceneNode("dolphinNode").getLocalRotation());
		
		
		//******* For Dolphin #1 *************
		// Check collision of camera and coins
		for(int i = 0; i < NUM_OF_COINS; i++) {
			if(checkCollision(engine.getSceneManager().getSceneNode("coin" + Integer.toString(i) + "Node"), engine.getSceneManager().getSceneNode("dolphinNode")) < 0.5) {
				int temp = i;
				if(!IntStream.of(coinList).anyMatch(x -> x == temp)) {
					engine.getSceneManager().getSceneNode("coin" + Integer.toString(i) + "Node").detachAllObjects();
					coinList[position] = i;
					addBoost(1);
					counter++;
					position++;
				}
			}
		}
				
		// Sets sprint boost for 3 seconds and then turns off
		if(sprint == false && speedTimer == 0)
			speedTimer = elapsTimeSec;
		else if(sprint == false && (elapsTimeSec - speedTimer) >= 3) {
			sprint = true;
			speedTimer = 0;
		}
		
		
		//******* For Dolphin #2 *************
		// Check collision of camera and coins
		for(int i = 0; i < NUM_OF_COINS; i++) {
			if(checkCollision(engine.getSceneManager().getSceneNode("coin" + Integer.toString(i) + "Node"), engine.getSceneManager().getSceneNode("dolphin2Node")) < 0.5) {
				int temp = i;
				if(!IntStream.of(coinList).anyMatch(x -> x == temp)) {
					engine.getSceneManager().getSceneNode("coin" + Integer.toString(i) + "Node").detachAllObjects();
					coinList[position] = i;
					addBoost(2);
					counter2++;
					position++;
				}
			}
		}
		
		// Sets sprint boost for 3 seconds and then turns off
		if(sprint2 == false && speedTimer2 == 0)
			speedTimer2 = elapsTimeSec;
		else if(sprint2 == false && (elapsTimeSec - speedTimer2) >= 3) {
			sprint2 = true;
			speedTimer2 = 0;
		}
		
	}
    
//******************
    
    protected void setupOrbitCameras(Engine eng, SceneManager sm) {
    	SceneNode dolphinN = sm.getSceneNode("dolphinNode");
    	SceneNode cameraN = sm.getSceneNode("MainCameraNode");
    	Camera camera = sm.getCamera("MainCamera");
    	String kbName = im.getKeyboardName();
    	orbitController1 = new Camera3Pcontroller(camera, cameraN, dolphinN, kbName, im);
    	
    	SceneNode earthN = sm.getSceneNode("dolphin2Node");
    	SceneNode cameraN2 = sm.getSceneNode("MainCamera2Node");
    	Camera camera2 = sm.getCamera("MainCamera2");
    	String msName = im.getMouseName();
    	orbitController2 = new Camera3Pcontroller(camera2, cameraN2, earthN, msName, im);
    }

//******************************************************************************************************************
//************************ Setup the Keyboard and Gamepad Inputs ***************************************************
//******************************************************************************************************************
   
    protected void setupInputs(SceneManager sm) {
    	im = new GenericInputManager();
    	
    	SceneNode dolphinN = sm.getSceneNode("dolphinNode");
    	SceneNode earthN = sm.getSceneNode("dolphin2Node");
    	
    	// Build some action objects for doing things in response to user input
    	quitGameAction = new QuitGameAction(this);
    	
    	// Actions for dolphin doing things in response to user input
    	moveForwardActionD = new MoveForwardAction(dolphinN, this);
    	moveBackwardActionD = new MoveBackwardAction(dolphinN, this);
    	moveLeftActionD = new MoveLeftAction(dolphinN, this);
    	moveRightActionD = new MoveRightAction(dolphinN, this);
    	rotateCameraRightD = new RotateCameraRight(dolphinN);
    	rotateCameraLeftD = new RotateCameraLeft(dolphinN);
    	sprintActionD = new SprintAction(dolphinN, this);
    	
    	// Actions for earth doing things in response to user input
    	moveForwardActionD2 = new MoveForwardAction(earthN, this);
    	moveBackwardActionD2 = new MoveBackwardAction(earthN, this);
    	moveLeftActionD2 = new MoveLeftAction(earthN, this);
    	moveRightActionD2 = new MoveRightAction(earthN, this);
    	rotateCameraRightD2 = new RotateCameraRight(earthN);
    	rotateCameraLeftD2 = new RotateCameraLeft(earthN);
    	sprintActionD2 = new SprintAction(earthN, this);
    	
    	// Attach the action objects to keyboard and gamepad components

    	// Gamepad Action 
    	if(im.getFirstGamepadName() == null) {
    		
    		// Keyboard Action
        	if(im.getKeyboardName() != null) {
        		String kbName = im.getKeyboardName();
        		
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, 
    	    			quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	    	
    	    	//Dolphin 1
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, 
    				    moveForwardActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, 
    	    			moveLeftActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, 
    	    			moveBackwardActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, 
    	    			moveRightActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.G, 
    	    			rotateCameraRightD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.H, 
    	    			rotateCameraLeftD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.LSHIFT, 
    	    			sprintActionD, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	    	
    	    	//Dolphin 2
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.UP, 
    				    moveForwardActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.LEFT, 
    	    			moveLeftActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.DOWN, 
    	    			moveBackwardActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.RIGHT, 
    	    			moveRightActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.NUMPAD4, 
    	    			rotateCameraRightD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.NUMPAD6, 
    	    			rotateCameraLeftD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	    	
    	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.NUMPAD8, 
    	    			sprintActionD2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	    	
        	}
    	}
    	else {
	    	String gpName = im.getFirstGamepadName();
	    	String kbName = im.getKeyboardName();
    		
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, 
	    			quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	    	
	    	//Dolphin 1
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, 
				    moveForwardActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, 
	    			moveLeftActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, 
	    			moveBackwardActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, 
	    			moveRightActionD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.G, 
	    			rotateCameraRightD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.H, 
	    			rotateCameraLeftD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	
	    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.LSHIFT, 
	    			sprintActionD, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	    		
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, 
					moveForwardActionD2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		    	
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, 
		    		moveLeftActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    	
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, 
		    		moveBackwardActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    	
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, 
		    		moveRightActionD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    	
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RX, 
		    		rotateCameraRightD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    	
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RX, 
		    		rotateCameraLeftD2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._1, 
	    			sprintActionD2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	}

    }
 
//******************************************************************************************************************
//************************ Custom Functions ************************************************************************
//******************************************************************************************************************
    
    // Get Active Node (MainCameraNode or myDolphinNode)
    public SceneNode getActiveNode() {
    	return activeNode;
    }
    
    // Set Active Node (MainCameraNode or myDolphinNode)
    public void setActiveNode(SceneNode sn) {
    	activeNode = sn;
    }
    
    // Create a random float variable
    public static float randInRangeFloat(int min, int max) {
        return min + (float) (Math.random() * ((1 + max) - min));
    }
    
    // Change whether sprint is on or off
    public void changeSprint(boolean s) {
    	if(s)
    		s = false;
    	else
    		s = true;
    }
    
    // Get sprint status
    public boolean getSprint(int i) {
    	if (i == 1)
    		return sprint;
    	else
    		return sprint2;
    }
    
    // Get the current speed
    public float getSpeed() {
    	return MAX_SPEED;
    }
    
    // Collision Detection Algorithm
    public float checkCollision(SceneNode a, SceneNode b) {
    	float ax = a.getLocalPosition().x();
    	float ay = a.getLocalPosition().y();
    	float az = a.getLocalPosition().z();
    	float bx = b.getLocalPosition().x();
    	float by = b.getLocalPosition().y();
    	float bz = b.getLocalPosition().z();
    	
    	return (float)Math.sqrt((double)(ax - bx) * (ax - bx) + (ay-by) * (ay - by) + (az - bz) * (az - bz));
    }
    
    // Get Boost status
    public int getBoost(int i) {
    	if (i == 1)
    		return positionBoost;
    	else
    		return positionBoost2;
    }
    
    // Add Boost to SprintBar
    private void addBoost(int i) {
    	if (i == 1) {
	    	if(positionBoost < 5) {
	    		speedBar[positionBoost] = 1;
	    		positionBoost++;
	    	}
    	}
    	else {
    		if(positionBoost2 < 5) {
	    		speedBar2[positionBoost2] = 1;
	    		positionBoost2++;
	    	}
    	}
    }
    
    // Remove Boost from SprintBar
    public void consumeBoost(int i) {
    	if (i == 1) {
	    	if(positionBoost > 0) {
	    		speedBar[positionBoost - 1] = 0;
	    		positionBoost--;
	    	}
    	}
    	else {
    		if(positionBoost2 > 0) {
	    		speedBar2[positionBoost2 - 1] = 0;
	    		positionBoost2--;
	    	}
    	}
    }
    
    // Print out SpeedBar for HUD
    private String printPowerUp(int[] num) {
    	String s = "|";
    	for(int i = 0; i < num.length; i++) {
    		if(num[i] == 1)
    			s += "=";
    		else
    			s += "  ";
    	}
    	return s + "|";
    }
    
//******************************************************************************************************************
//********************** Create Objects in Game Section ************************************************************
//******************************************************************************************************************
    
    //***** Make Dolphins *****
    private void makeDolphin(Engine eng, SceneManager sm) throws IOException {
    	Entity dolphinE = sm.createEntity("dolphin", "dolphinHighPoly.obj");
    	dolphinE.setPrimitive(Primitive.TRIANGLES);
    	
    	SceneNode dolphinNG = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "NodeG");

    	SceneNode dolphinN = dolphinNG.createChildSceneNode(dolphinE.getName() + "Node");
    	dolphinN.moveUp(0.4f);
    	dolphinN.attachObject(dolphinE);
    	
    	Entity dolphin2E = sm.createEntity("dolphin2", "dolphinHighPoly.obj");
    	dolphin2E.setPrimitive(Primitive.TRIANGLES);
    	
    	SceneNode dolphin2N = dolphinNG.createChildSceneNode("dolphin2Node");
    	dolphin2N.moveUp(0.4f);
    	dolphin2N.attachObject(dolphin2E);
    }
    
    //***** Make Earth *****
    private void makeEarth(Engine eng, SceneManager sm) throws IOException {
    	Entity earthE = sm.createEntity("earth", "earth.obj");
	    earthE.setPrimitive(Primitive.TRIANGLES);
	    
	    SceneNode earthNG = sm.getSceneNode("dolphinNodeG").createChildSceneNode("earthNodeG");
	    
	    SceneNode earthN = earthNG.createChildSceneNode(earthE.getName() + "Node");
	    earthN.attachObject(earthE);
	    earthN.setLocalPosition(0.0f, 0.5f, 0.0f);
	    earthN.setLocalScale(0.2f, 0.2f, 0.2f);
    }
    
    //***** Make Coins *****
    private SceneNode makeCoin(Engine eng, SceneManager sm, int num) throws IOException {
    	Entity coinE = sm.createEntity("coin" + Integer.toString(num),	"coin.obj");
    	coinE.setPrimitive(Primitive.TRIANGLES);
    	
    	SceneNode coinN = sm.getSceneNode("dolphinNodeG").createChildSceneNode(coinE.getName() + "Node");
    	coinN.moveForward(randInRangeFloat(-SIZE_OF_SPACE, SIZE_OF_SPACE));
    	coinN.moveUp(0.5f);
    	coinN.moveRight(randInRangeFloat(-SIZE_OF_SPACE, SIZE_OF_SPACE));
    	coinN.rotate(Degreef.createFrom(90f), Vector3f.createUnitVectorX());
    	coinN.rotate(Degreef.createFrom(180f), Vector3f.createUnitVectorZ());
    	coinN.attachObject(coinE);
    	coinN.scale(0.25f, 0.25f, 0.25f);

    	return coinN;
    }
    
    protected ManualObject makeFloor(Engine eng, SceneManager sm) throws IOException {
    	ManualObject fl = sm.createManualObject("Floor");
    	ManualObjectSection flSec = fl.createManualSection("FloorSection");
    	fl.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	float[] vertices = new float[] {
    			-40.0f, 0.0f, 40.0f, 40.0f, 0.0f, 40.0f, -40.0f, 0.0f, -40.0f,
    			40.0f, 0.0f, -40.0f, -40.0f, 0.0f, -40.0f, 40.0f, 0.0f, 40.0f
    	};
    	
    	float[] texcoords = new float[] {
    		0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
    		0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f
    	};
    	
    	float[] normals = new float[] {
    		1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
    		1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f
    	};
    	
    	int[] indices = new int[] { 0,1,2,3,4,5 };
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		flSec.setVertexBuffer(vertBuf);
		flSec.setTextureCoordsBuffer(texBuf);
		flSec.setNormalsBuffer(normBuf);
		flSec.setIndexBuffer(indexBuf);
		
		Material matX = sm.getMaterialManager().getAssetByPath("silver.mtl");
	    Texture tex = sm.getTextureManager().getAssetByPath(matX.getTextureFilename());
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    fl.setDataSource(DataSource.INDEX_BUFFER);
		fl.setRenderState(texState);
	    fl.setMaterial(matX);
		
		return fl;
    }
    
    private SceneNode createFloor(Engine eng, SceneManager sm) throws IOException {
    	ManualObject fl = makeFloor(eng, sm);
        SceneNode flN = sm.getRootSceneNode().createChildSceneNode("FloorNode");
        flN.scale(0.75f, 0.75f, 0.75f);
        flN.attachObject(fl);
        
        return flN;
    }
    
    //***** Make Pyramids *****
    protected ManualObject makeDiamond(Engine eng, SceneManager sm, int num) throws IOException { 
		ManualObject pyr = sm.createManualObject("Diamond" + Integer.toString(num));
		ManualObjectSection pyrSec = pyr.createManualSection("DiamondSection");
		pyr.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.0f, 0.5f, 0.0f, //front
				0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, //right
				0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, //back
				-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.0f, 0.5f, 0.0f, //left
				0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f,0.0f, -1.5f, 0.0f, //front t2
				0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.0f, -1.5f, 0.0f, //right t2
				-0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.0f, -1.5f, 0.0f, //back t2
				-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.0f, -1.5f, 0.0f //left t2
		};
		
		float[] texcoords = new float[] { 
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f
		};
		
		float[] normals = new float[] { 
			0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
			-1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
		};
		
		int[] indices = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24 };
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		pyrSec.setVertexBuffer(vertBuf);
		pyrSec.setTextureCoordsBuffer(texBuf);
		pyrSec.setNormalsBuffer(normBuf);
		pyrSec.setIndexBuffer(indexBuf);
		
		Material matX = sm.getMaterialManager().getAssetByPath("silver.mtl");
	    Texture tex = sm.getTextureManager().getAssetByPath(matX.getTextureFilename());
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    pyr.setDataSource(DataSource.INDEX_BUFFER);
		pyr.setRenderState(texState);
	    pyr.setMaterial(matX);
		
		return pyr;
    }
    
    private SceneNode createDiamond(Engine eng, SceneManager sm, int num) throws IOException {
    	ManualObject dia = makeDiamond(eng, sm, num);
        //SceneNode diaN = sm.getRootSceneNode().createChildSceneNode("Diamond" + Integer.toString(num) + "Node");
        SceneNode diaN = sm.getSceneNode("diamondParentNodeG").createChildSceneNode(dia.getName() + "Node");
        diaN.scale(0.75f, 0.75f, 0.75f);
        diaN.moveForward(randInRangeFloat(-SIZE_OF_SPACE, SIZE_OF_SPACE));
        diaN.moveUp(randInRangeFloat(0, 1));
        diaN.moveRight(randInRangeFloat(-SIZE_OF_SPACE, SIZE_OF_SPACE));
        diaN.attachObject(dia);
        
        return diaN;
    }
    
    //***** Make Axis *****
    private void setupAxis(Engine eng, SceneManager sm) throws IOException {
		ManualObject vertexX = sm.createManualObject("VecX");
		ManualObjectSection vertexSecX = vertexX.createManualSection("VertexSectionX");
		vertexX.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		ManualObject vertexY = sm.createManualObject("VecY");
		ManualObjectSection vertexSecY = vertexX.createManualSection("VertexSectionY");
		vertexX.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		ManualObject vecZ = sm.createManualObject("VecZ");
		ManualObjectSection vertexSecZ = vertexX.createManualSection("VertexSectionZ");
		vertexX.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        
        float[] verticesX = new float[] { 
    			-1000.0f, 0.0f, 0.0f,
    			1000.0f, 0.0f, 0.0f, 
		};
        float[] verticesY = new float[] { 
        		0.0f, -1000.0f, 0.0f,
        		0.0f, 1000.0f, 0.0f,
		};
        float[] verticesZ = new float[] { 
        		0.0f, 0.0f, -1000.0f,
        		0.0f, 0.0f, 1000.0f,
		};     
		
		int[] indicesX = new int[] { 0,1 };
		int[] indicesY = new int[] { 0,1 };
		int[] indicesZ = new int[] { 0,1 };
		
		vertexSecX.setPrimitive(Primitive.LINES);
		vertexSecY.setPrimitive(Primitive.LINES);
		vertexSecZ.setPrimitive(Primitive.LINES);
		
		FloatBuffer vertBufX = BufferUtil.directFloatBuffer(verticesX);
		IntBuffer indexBufX = BufferUtil.directIntBuffer(indicesX);
		FloatBuffer vertBufY = BufferUtil.directFloatBuffer(verticesY);
		IntBuffer indexBufY = BufferUtil.directIntBuffer(indicesY);
		FloatBuffer vertBufZ = BufferUtil.directFloatBuffer(verticesZ);
		IntBuffer indexBufZ = BufferUtil.directIntBuffer(indicesZ);
		
		vertexSecX.setVertexBuffer(vertBufX);
		vertexSecX.setIndexBuffer(indexBufX);
		vertexSecY.setVertexBuffer(vertBufY);
		vertexSecY.setIndexBuffer(indexBufY);
		vertexSecZ.setVertexBuffer(vertBufZ);
		vertexSecZ.setIndexBuffer(indexBufZ);
		
		Material matX = sm.getMaterialManager().getAssetByPath("defaultX.mtl");
	    matX.setEmissive(Color.BLUE);
	    Texture texX = sm.getTextureManager().getAssetByPath(matX.getTextureFilename());
	    TextureState tstateX = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstateX.setTexture(texX);
	    vertexSecX.setRenderState(tstateX);
	    vertexSecX.setMaterial(matX);
	    
	    SceneNode vertexXNode = sm.getRootSceneNode().createChildSceneNode("XNode");
	    vertexXNode.attachObject(vertexX);
	    
	    Material matY = sm.getMaterialManager().getAssetByPath("defaultY.mtl");
	    matY.setEmissive(Color.RED);
	    Texture texY = sm.getTextureManager().getAssetByPath(matY.getTextureFilename());
	    TextureState tstateY = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstateY.setTexture(texY);
	    vertexSecY.setRenderState(tstateY);
	    vertexSecY.setMaterial(matY);
	    
	    SceneNode vertexYNode = sm.getRootSceneNode().createChildSceneNode("YNode");
	    vertexYNode.attachObject(vertexY);
	    
	    Material matZ = sm.getMaterialManager().getAssetByPath("defaultZ.mtl");
	    matZ.setEmissive(Color.YELLOW);
	    Texture texZ = sm.getTextureManager().getAssetByPath(matZ.getTextureFilename());
	    TextureState tstateZ = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstateZ.setTexture(texZ);
	    vertexSecZ.setRenderState(tstateZ);
	    vertexSecZ.setMaterial(matZ);
	    
	    SceneNode vertexZNode = sm.getRootSceneNode().createChildSceneNode("ZNode");
	    vertexZNode.attachObject(vecZ);
    }
}
