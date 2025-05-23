import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.math.Vec3f;
import com.jogamp.opengl.util.*;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import solarModel.*;
/**
 * An implementation of jogl that simulates a solar system
 */
public class Jogl implements GLEventListener {
	private GL2 gl; //interface to OpenGL (C) functions
	private GLU glu; //graphics library utilities
	private GLUquadric quad; //used for drawing spheres
	private ArrayList<Planet> artbook;	// List of planets to be drawn
	private GLCanvas canvas;			// The canvas to be drawn on
	private Animator director;
	private float timeElapsed, rateOfTime; //time since simulation began and it's rate.
	private PlanetEnum trackedPlanet; //the planet currently being tracked
	private Camera camera; 		// The camera which views the scene
	private boolean isFreeOrbit; //Flag for if the user is controlling the camera in-orbit
	private int freeOrbitDirection; //0 (no orbit), 1 (right orbit), or -1 (left orbit)

	private ArrayList<Texture> planetTextures; // List of all planet textures
	// Textures for every planet and the sun
	private Texture sunTexture;
	private Texture mercuryTexture;
	private Texture venusTexture;
	private Texture earthTexture;
	private Texture marsTexture;
	private Texture jupiterTexture;
	private Texture saturnTexture;
	private Texture uranusTexture;
	private Texture neptuneTexture;
	
	/**
	 * Creates a new instance of the jogl implementation with a defined camera
	 */
	public Jogl () {
		canvas = makeCanvas(500,500);
		director = new Animator(canvas);		// Sets up the animation
		this.artbook = new ArrayList<Planet>();	// Initialize the ArrayList of planets
		this.trackedPlanet = null;				// There is no set planet being tracked right away
		this.timeElapsed = 0;					// Total time elapsed equal 0 when created
		this.rateOfTime = 1;					// timeElapsed goes up by 1 every time interval
		this.isFreeOrbit = false;
		this.freeOrbitDirection = 0;
		this.camera = new Camera();				// Initialize the camera
	}
	
	/**
	 * The executive function of the simulation. This display does the following operations:
	 * 1. Determine planet coordinates and setup the modelview matrix
	 * 2. Place the camera based on whether it is in free motion or in (fixed) orbit around a planet
	 * 3. The sun will be initially drawn
	 * 4. Each planet will be drawn and textured to it's orbital position and rotational angle (with respect to it's principle x axis)
	 * 5. Increase the timeElapsed by rateOfTime
 	 */
	@Override
	public void display(GLAutoDrawable drawable) {		
//		Calculates the orbital and rotational angles of each planet
		calculatePlanetProperties();

		//set the modelview matrix (VIEW COORDINATE SYSTEM)
		initMatrix(drawable, GL2.GL_MODELVIEW);
		
		//Determine where the camera will be and place it (CAMERA COORDINATE SYSTEM)
		if(this.camera.mode == this.camera.ORBIT) trackPlanet();
		placeCamera();

		GLUquadric quad1 = glu.gluNewQuadric(); // Quadric will be used to draw spheres with textures
		//Draw sun with texuture and lighting
		drawSun(quad1);
		
		/*
		 * The model is placed, the camera is oriented in the canvas
		 * and the viewport is filled with the camera.
		 * now we begin drawing planets
		 */
		int index = 0; // index is used to track what planet is being drawn. Used to know what texure to be used
		Texture planetTexture;
		for(Planet p : this.artbook) {

			// Skip the sun, because we already drew it above using drawSun()
			if (index == 0) {
				index++;
				continue;
			}

			//popping and pushing starts the canvas at the origin for every planet
			gl.glPushMatrix();
				//go to this planet's orbital position and rotate it
				float rotationRadians = (float) Math.toRadians((p.getRotation()));
				if(p.getEnum() != null) // If not the sun
					gl.glTranslatef(p.getX(), 0.0f, p.getZ());  // translate planet to appropiate coordinates
				gl.glRotatef(rotationRadians, 0.0f, 1.0f, 0.0f);	// rotate the planet

				// Draw planet with texture
				planetTexture = planetTextures.get(index);
				planetTexture.bind(gl);					// Bind texture with drawing tool
				gl.glEnable(GL2.GL_TEXTURE_2D);			// Enable textures
				planetTexture.enable(gl);				// Enable the texture to be drawn
				glu.gluQuadricTexture(quad1, true);	// Allow quadric to be drawn with texture
				glu.gluSphere(quad1,  0.5 * (p.getRadius()), 30, 30); 	// Draw sphere with quadric that has textures on it
				// Disable textures
				planetTexture.disable(gl);
				gl.glDisable(GL2.GL_TEXTURE_2D);

			gl.glPopMatrix();
			index++; // index is incremeted
		}
		
		timeElapsed+= rateOfTime;
	}
	
	/**
	 * Draws the sun as the light source for the system.
	 * First the light source is set at the modelview origin, then the textured sun is drawn
	 * NOTE: drawSun assumes that the modelview coordinate is at the origin <0,0,0,1> 
	 */
	private void drawSun(GLUquadric quad){
		float[] lightPosition = {0.0f, 0.0f, 0.0f, 1.0f}; // position of the light source (center of the scene)
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0); // Set the properties
		gl.glDisable(GL2.GL_LIGHTING);	// Disable lighting before pushing the matrix
		gl.glPushMatrix();				// push matrix before drawing the sun
		gl.glEnable(GL2.GL_TEXTURE_2D);	// enable the lighting again
		sunTexture.enable(gl);			// bind texture with drawing tool
		sunTexture.bind(gl);
		glu.gluQuadricTexture(quad, true);	// allow textures to be drawn on quadric
		glu.gluSphere(quad, 0.5f * 109.3f, 30, 30);	// draw the sphere using quadric
		sunTexture.disable(gl);			// disable texture
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glPopMatrix(); 				// Pop matrix after done drawingin
		gl.glEnable(GL2.GL_LIGHTING);   // enable lighting after popping the matrix
	}
	
	/**
	 * Initialises the specified JOGL matrix
	 * @param drawable The canvas
	 * @param matrix The matrix to be initialised (GL.GL_MODELVIEW, GL.GL_PROJECTION)
	 * @return the openGL interface
	 */
	private GL2 initMatrix(GLAutoDrawable drawable, int matrix) {
		int clearString = (GL.GL_COLOR_BUFFER_BIT  | GL.GL_DEPTH_BUFFER_BIT);
		gl = drawable.getGL().getGL2();
		gl.glClear(clearString);
		gl.glMatrixMode(matrix);
		gl.glLoadIdentity();
		return gl;
	}
	
	/**
	 * Increases this.rateOfTime by a constant value.
	 * To prevent -ve values, decreasing when the rate = 1 halves the rate. Inversely, increasing doubles.
	 */
	public void increaseRateOfTime() {
		if(this.rateOfTime <= 1)
			this.rateOfTime *= 2;
		else
			this.rateOfTime += 0.25f;
	}
	
	/**
	 * Dncreases this.rateOfTime by a constant value.
	 * To prevent -ve values, decreasing when the rate = 1 halves the rate. When rate < 1, increasing doubles.
	 */
	public void decreaseRateOfTime() {
		if(this.rateOfTime <= 1)
			this.rateOfTime /= 2;
		else
			this.rateOfTime -= 0.25f;
	}

	/*
	 * Method exists because it must be implemented from the GLEventListener interface
	 * Method is not utilized in program
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {}
	
	/**
	 * Initialises the display canvas by setting up the gl interface, library, tools, and sets the canvas to a filled colour
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		//updates display once every vertical refresh
		drawable.getGL().setSwapInterval(1); 
	
		//OpenGl interface (gl) and utilities library (glu)
		gl = drawable.getGL().getGL2();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		glu = new GLU();
		
		//quad is used for drawing spheres
		quad = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quad, GLU.GLU_LINE);
				
		//clear the canvas to a solid colour
		float red = 0.0f, green = 0.0f, blue = 0.0f, alpha = 1.0f;
		gl.glClearColor(red, green, blue, alpha);

		// Load textures into each texture variable, and add to the list.
		this.planetTextures = new ArrayList<Texture>(8);

		sunTexture = loadTexture("src/Planet_Textures/sun.jpg");
		planetTextures.add(sunTexture);
		mercuryTexture = loadTexture("src/Planet_Textures/mercury.jpg");
		planetTextures.add(mercuryTexture);
		venusTexture = loadTexture("src/Planet_Textures/venus.jpg");
		planetTextures.add(venusTexture);
		earthTexture = loadTexture("src/Planet_Textures/earth.jpg");
		planetTextures.add(earthTexture);
		marsTexture = loadTexture("src/Planet_Textures/mars.jpg");
		planetTextures.add(marsTexture);
		jupiterTexture = loadTexture("src/Planet_Textures/jupiter.jpg");
		planetTextures.add(jupiterTexture);
		saturnTexture = loadTexture("src/Planet_Textures/saturn.jpg");
		planetTextures.add(saturnTexture);
		uranusTexture = loadTexture("src/Planet_Textures/uranus.jpg");
		planetTextures.add(uranusTexture);
		neptuneTexture = loadTexture("src/Planet_Textures/neptune.jpg");
		planetTextures.add(neptuneTexture);

		// Initialize lighting properties
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		float[] materialAmbient = {0.1f, 0.1f, 0.1f, 1.0f}; // Very low ambient light so darkside of planets cannot be seen
		float[] materialDiffuse = {0.7f, 0.7f, 0.7f, 1.0f}; // Diffuse reflection allows for un-uniform light distribution
		float[] materialSpecular = {0.4f, 0.4f, 0.4f, 1.0f}; // Low Specular reflection so the highlights aren't too bright
		float materialShininess = 5.0f;						// Low shininess to avoid fake plastic look
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, materialAmbient, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess);
	}

	/**
	 * Method used to initialize Texture types
	 * Reads local file path to store .jpg file in the variable
	 * @param textureFileName local path (Plane_Textures) to specific texture.
	 *                        Path can start from src/......
	 * @return Texture type that holds path to .jpg file
	 */
	private Texture loadTexture(String textureFileName) {
		Texture texture = null;
		// Try catch block incase the texture path is not valid
		try {
			File textureFile = new File(textureFileName); // Load your JPG image
			texture = TextureIO.newTexture(textureFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return texture;
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {		
		//Setup the viewport
		gl = drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION); // Sets matrix mode
		gl.glViewport(0, 0, width, height); // Sets viewport to new reshaped size
		gl.glLoadIdentity(); // Load indentity matrix
		
		//setup the projection matrix (perspective)
		float VERTICAL_FOV = 60.0f;
		float aspectRatio = (float) width/height;   // Uses width and height of frame to calculate aspect ratio
		float nearClip = .01f, farClip = 1000.0f;   // near and far values for perspective projection
		glu.gluPerspective(VERTICAL_FOV, aspectRatio, nearClip, farClip); // Sets up perspective projection
	}

	/**
	 * Finds the polar coordinates of the planet with the sun as the origin.
	 * @param art The planet at it's previous position
	 * @return the angle of the planet's orbit with respect to the sun in degrees
	 */
	private float orbitPlanet(Planet art) {
		float sunRadius = artbook.get(0).getRadius(),
				offset = art.getOffset() + sunRadius; // Ensure we obtain the proper offset of the planet
		
		//Get the new orbital position of the planet
		float orbit = art.orbit(timeElapsed) % 360,   // orbit represents the angle (in degrees) from the orginal planet position
				orbitRadians = (float) Math.toRadians(orbit);
		
		//Calculate the position of the planet
		float planetX = (float) (offset * Math.cos(orbitRadians));
		float planetZ = (float) (offset * Math.sin(orbitRadians));
		//Set the position of the planet
		art.setX(planetX);
		art.setZ(planetZ);
		return orbit;
	}
	
	/**
	 * Checks to see if jogl is playing or not
	 * @return
	 */
	public boolean isPlaying() {
		return director.isAnimating();
	}
	
	/**
	 * Reads the artbook to be used by this implementation
	 * @param artbook the artbook to be used
	 */
	public void loadArtbook(ArrayList<Planet> artbook) {
		this.artbook = artbook;
	}
	
	/**
	 * Requests focus and starts the animator
	 */
	public boolean startAnimator() {
		canvas.requestFocus();
		return director.start();
	}

	/**
	 * Stops the animator and closes the implementation
	 */
	public boolean stopAnimator() {
		canvas.destroy();
		artbook.clear();
		return director.stop();
	}
	
	/**
	 * Creates a new GLCanvas with specified capabilities
	 * @return A GLCanvas object with size, capabilities, and a GLEventListener
	 */
	private GLCanvas makeCanvas(int width, int height) {
		GLProfile glp = GLProfile.get("GL2"); //Gets the interface
		GLCapabilities caps = new GLCapabilities(glp); //Analog to OpenGL context
		GLCanvas canvas = new GLCanvas(caps);
		canvas.setSize(width, height);	// sets size of the canvas
		canvas.addGLEventListener(this); // Add the GLEvenetListener to the canvas
		return canvas;
	}
	
	/**
	 * Returns a reference to the canvas field
	 * @return The canvas used by the implementation.
	 */
	public GLCanvas getCanvas() {
		return canvas;
	}
	/**
	 * Returns the size of the canvas
	 * @return the dimensions of the canvas
	 */
	public Dimension getSize() {
		return canvas.getPreferredSize();
	}
	
	/**
	 * Sets the camera mode 
	 * @param mode 0 = not moving, 1 = moving right, -1 = moving left
	 */
	public void setCameraMode(int mode) {
		camera.mode = mode;
		if(mode == camera.ORBIT) this.freeOrbitDirection = camera.MOVING_NONE;
	}
	
	/**
	 * Sets the camera to <0,0,200>
	 * looking at <0,0,0>
	 * in the <0,0,-1> direction 
	 */
	public void setCameraFixed() {
		this.camera.setFixedMode();
	}
	
	/**
	 * Moves the camera towards or away from it's focal point, moving the focal point with.
	 * Does so by adding the unit lookAt vector * dz to the current camera position and focal point
	 * @param dz The difference in the z position relative to the CAMERA COORDINATE SYSTEM
	 */
	public void moveCameraInOut(float dz) {
		Vec3f dv = this.camera.lookAtDirection.mul(dz);
		this.camera.position.add(dv);
		this.camera.focalPoint.add(dv);
	}
	
	/**
	 * Rotates the camera on it's y axis
	 * @param dt The angle of rotation
	 */
	public void rotateCameraLeftRight(float dt) {		
		Vec3f v = this.camera.focalPoint;
		this.camera.focalPoint = rotateY(dt, this.camera.position, v);
		this.camera.lookAtDirection = new Vec3f(this.camera.focalPoint).sub(this.camera.position).normalize();
		//update left vector
		this.camera.left = this.camera.lookAtDirection.cross(this.camera.up).normalize().mul(-1);
	}
	
	/**
	 * Rotates the camera on it's x axis
	 * @param dt The angle of rotation
	 */
	public void rotateCameraUpDown(float dt) {
		Vec3f v = this.camera.focalPoint;
		this.camera.focalPoint = rotateX(dt, this.camera.position, v);
		this.camera.lookAtDirection = new Vec3f(this.camera.focalPoint).sub(this.camera.position).normalize();
		//update up vector
		this.camera.up = this.camera.lookAtDirection.cross(this.camera.left).normalize();
	}
	
	/**
	 * Used for pausing and playing. Limited to 0 (no change in time) or 1 
	 */
	public void setRateOfTime(float time){
		this.rateOfTime = time;
	}

	public float getRateOfTime(){
		return this.rateOfTime;
	}
	
	/**
	 * Rotates the vector from headpoint -> tailPoint by angleDegrees about the y axis
	 * If headPoint is null then the tailPoint is rotating about the origin
	 * @param angleDegrees angle of rotation 
	 * @param headPoint The head of the vector
	 * @param tailPoint The tail of the vector
	 * @return the new tailPoint vector
	 */
	private Vec3f rotateY(float angleDegrees, Vec3f headPoint, Vec3f tailPoint) {
		//check if we are rotating about the origin or an arbitrary headPoint
		if(headPoint == null) headPoint = new Vec3f(0,0,0);
		
		Vec3f dv = new Vec3f(tailPoint).sub(headPoint);
		float angleRadians = (float) Math.toRadians(angleDegrees);
		
		float newX = dv.x() * (float) Math.cos(angleRadians) + dv.z() * (float) Math.sin(angleRadians),
				newY = tailPoint.y(),
				newZ = -(dv.x()) * (float) Math.sin(angleRadians) + dv.z() * (float) Math.cos(angleRadians);
		return new Vec3f(newX, newY, newZ);
	}
	
	/**
	 * Rotates the vector from headpoint -> tailPoint by angleDegrees about the x axis
	 * If headPoint is null then the tailPoint is rotating about the origin
	 * @param angleDegrees angle of rotation 
	 * @param headPoint The head of the vector
	 * @param tailPoint The tail of the vector
	 * @return the new tailPoint vector
	 */
	private Vec3f rotateX(float angleDegrees, Vec3f headPoint, Vec3f tailPoint) {
		if(headPoint == null) headPoint = new Vec3f(0,0,0);
		
		Vec3f dv = new Vec3f(tailPoint).sub(headPoint);
		float angleRadians = (float) Math.toRadians(angleDegrees);
		
		float newX = tailPoint.x(),
				newY = dv.y() * (float) Math.cos(angleRadians) - dv.z() * (float) Math.sin(angleRadians),
				newZ = dv.y() * (float) Math.sin(angleRadians) + dv.z() * (float) Math.cos(angleRadians);
		
		return new Vec3f(newX, newY, newZ);
	}
	
	/**
	 * Get the camera mode
	 * @return current camera mode (fixed or orbit)
	 */
	public int getCameraMode() {
		return this.camera.mode;
	}
	
	/**
	 * Moves the camera left or right on it's principle x axis by adding the unit left vector multiplied by the distance to be moved
	 * @param dt the difference in the positions
	 */
	public void moveCameraLeftRight(float dt) {
		Vec3f dv = this.camera.left.mul(-dt);
		this.camera.position.add(dv);
		this.camera.focalPoint.add(dv);
	}
		
	/**
	 * Sets the camera to the planet to be tracked
	 * @param pe
	 */
	public void setTrackedPlanet(PlanetEnum pe) {
		this.trackedPlanet = pe;
		this.isFreeOrbit = false;
	}
	
	/**
	 * Determines orbital position and rotational angle as functions of timeElapsed (orbitPlanet and art.rotate)
	 */
	public void calculatePlanetProperties() {
		for(Planet art : artbook) {
			//Calculate planet orbit 
			float orbit = orbitPlanet(art);
			float rotation = art.rotate(timeElapsed);

			//set the new planet orbit and rotation
			//don't update the tracked planet's rotation if we're in FREE orbit around it
			art.setOrbit(orbit);
			if(this.isFreeOrbit && art.getEnum() == this.trackedPlanet) continue;
			art.setRotation(rotation);
		}
	}
	
	/**
	 * Updates the camera parameters to orbit the tracked planet if camera is in orbit mode
	 * If the camera is in free orbit, the planet's rotational angle is modified ONLY on user input
	 * Camera orientation is determined by the vector cp = <planet.position - camera.position> 
	 */
	private void trackPlanet() {
		//Determine which planet is being tracked
		Planet p = null;
		for(Planet pl : artbook) 
			if(pl.getEnum() == this.trackedPlanet) 
				p = pl;
		
		//get tracked planet's position, rotational angle, and find the distance the camera needs to be to orbit it
		float px = p.getX(),
				pz = p.getZ(),
				pr = p.getRotation(),
				or = p.getRadius() + 0.5f;
		if(this.isFreeOrbit)
			pr += this.freeOrbitDirection;
		
		//calculate camera position from planet position, rotation, and camera orbital radius
		float cx = or * (float) (Math.cos(Math.toRadians(-pr))) + px;
		float cz = or * (float) (Math.sin(Math.toRadians(-pr))) + pz;
		
		p.setRotation(pr);
		
		//Update camera left, up, lookAt vectors and camera position and focal point
		this.camera.position.set(cx, 0, cz);
		this.camera.focalPoint.set(px, 0, pz);
		this.camera.up.set(0, 1, 0);
		
		//cp is the vector from the camera position to the planet
		Vec3f cp = new Vec3f(px-cx, 0, pz-cz);
		
		this.camera.lookAtDirection.set(cp.x(), 0, cp.z()).normalize();
		this.camera.left = this.camera.lookAtDirection.cross(this.camera.up).normalize().mul(-1);
	}
	
	/**
	 * Sets the camera using it's position, focal point, and upVector with gluLookAt
	 */
	public void placeCamera() {	
		float cx = camera.position.x(),
				cy = camera.position.y(),
				cz = camera.position.z(),
				
				fx = camera.focalPoint.x(),
				fy = camera.focalPoint.y(),
				fz = camera.focalPoint.z(),
				
				ux = camera.up.x(),
				uy = camera.up.y(),
				uz = camera.up.z();
		
		glu.gluLookAt(cx, cy, cz,
				fx, fy, fz,
				ux, uy, uz);
	}
	
	/**
	 * Sets whether or not the camera is in free orbit around a planet
	 * @param isFreeOrbit whether the camera is in free orbit or not
	 * @param freeOrbitDirection the direction of orbit (accepted values are -1, 0, 1)
	 */
	public void setIsFreeOrbit(boolean isFreeOrbit, int freeOrbitDirection) {
		this.isFreeOrbit = isFreeOrbit;
		this.freeOrbitDirection = freeOrbitDirection; 
	}
	
	/**
	 * Get the free orbit flag
	 * @return whether the camera is in free orbit (true) or not (false)
	 */
	public boolean getIsFreeOrbit() {
		return this.isFreeOrbit;
	}
	
	/**
	 * Used to help manage the camera
  	 * Based on an up, lookAt, and left vector
         * Keeps track of it's position and focal point
         * Is either in FREE mode (0) or ORBIT mode (1) around a planet
	 */
	class Camera {
		public final int FREE = 0, //viewing from a static or dynamic position
						 ORBIT = 1; //camera is orbiting a planet
		
		//For rotation direction
		public final int MOVING_LEFT = -1,
				 		 MOVING_NONE = 0,
						 MOVING_RIGHT = 1;
		
		private int mode;	// Determines if camera is orbiting a planet, or is in free camera mode
		
		private Vec3f position, lookAtDirection, left, up; 
		private Vec3f focalPoint;
		
		public Camera() {
			setFixedMode();
		}

		/**
		 * Sets default camera values:
		 * position = <0,0,200>
		 * focalPoint = <0,0,0>
		 * lookAtDirection = <focalPoint - position>/N = <0,0,-1>
		 * left = (<lookAtDirection> X <up>)/-N = <0,0,-1>
		 * up = <0,1,0>
		 */
		public void setFixedMode() {
			this.position = new Vec3f(0,0,200);	// When user hits 0 on keyboard, camera is in there coordinates
			this.focalPoint = new Vec3f(0,0,0);	// camera looks at the center of the scene (the sun)
			
			this.lookAtDirection = new Vec3f(new Vec3f(this.focalPoint).sub(this.position)).normalize();
			
			this.up = new Vec3f(0,1,0);
			this.left = this.lookAtDirection.cross(this.up).normalize().mul(-1);
			this.mode = FREE;	// Free camera mode. mode = 1
		}

		/*
		Series of setters and getters to provide encapsulation for camera properties
		 */
		public int getMode() {
			return this.mode;
		}
		
		public void setPositionVector(float[] xyz) {
			this.position.set(xyz);
		}
		
		public void setLookAtVector(float[] xyz) {
			this.lookAtDirection.set(xyz);
		}
		
		public void setUpVector(float[] xyz) {
			this.up.set(xyz);
			this.up.normalize();
		}
		
		public void setLeftVector(float[] xyz) {
			this.left.set(xyz);
			this.left.normalize();
		}

		/**
		 * toString method that displays camera properties
		 * @return String of camera properties
		 */
		@Override
		public String toString() {
			String str = "";
			str += "Position = " + position.toString() + "\n"
					+ "Focal Point = " + focalPoint.toString() + "\n"
					+ "lookAt Vector = " + lookAtDirection.toString() + "\n"
					+ "left Vector = " + left.toString() + "\n"
					+ "up Vector = " + up.toString();
			return str;
			
		}
	}
}

