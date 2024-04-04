import java.awt.*;
import java.util.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.math.Vec3f;
import com.jogamp.opengl.util.*;

import solarModel.*;
/**
 * An implementation of jogl that simulates a solar system
 */
public class Jogl implements GLEventListener {
	private GL2 gl; //interface to OpenGL (C) functions
	private GLU glu; //graphics library utilities
	private GLUquadric quad; //used for drawing spheres
	private ArrayList<Planet> artbook;
	private GLCanvas canvas;
	private Animator director;
	private float timeElapsed, rateOfTime; //time since sim began and it's rate
	private PlanetEnum trackedPlanet; //the planet currently being tracked
	private Camera camera; 
	private boolean isFreeOrbit; //Flag for if the user is controlling the camera in-orbit
	private int freeOrbitDirection; //0 (no orbit), 1 (right orbit), or -1 (left orbit)
	
	/**
	 * Creates a new instance of the jogl implementation with a defined camera
	 */
	public Jogl () {
		canvas = makeCanvas(500,500);
		director = new Animator(canvas);
		this.artbook = new ArrayList<Planet>();
		this.trackedPlanet = null;
		this.timeElapsed = 0;
		this.rateOfTime = 1;
		this.isFreeOrbit = false;
		this.freeOrbitDirection = 0;
		this.camera = new Camera();
	}
	
	/**
	 * Driven by the Animator director and draws everything in the artbook
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
		
		/*
		 * The model is placed, the camera is oriented in the canvas
		 * and the viewport is filled with the camera.
		 * now we begin drawing planets
		 */
		
		for(Planet p : this.artbook) {
			//popping and pushing starts the canvas at the origin for every planet
			gl.glPushMatrix();
				//go to this planet's orbital position and rotate it
				float rotationRadians = (float) Math.toRadians((p.getRotation()));
				if(p.getEnum() != null) gl.glTranslatef(p.getX(), 0.0f, p.getZ());
				gl.glRotatef(rotationRadians, 0.0f, 1.0f, 0.0f);
				
				//set the color of the planet and draw it
				gl.glColor3f(p.getRed(), p.getGreen(), p.getBlue());
				glu.gluSphere(quad, 0.5f * (p.getRadius()), 30, 30);
			gl.glPopMatrix();
		}
		
		timeElapsed+= rateOfTime;
	}
	
	/**
	 * Initialises the specified JOGL matrix
	 * @param drawable The canvas
	 * @param matrix The matrix to be initialised (GL.GL_MODELVIEW, GL.GL_PROJECTION)
	 * @param clearString The bitstring to clear the buffers
	 * @return the matrix for the canvas now interfaced to OpenGL
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
		if(this.rateOfTime <= 1) this.rateOfTime *= 2;
		else this.rateOfTime += 0.25f;
	}
	
	/**
	 * Dncreases this.rateOfTime by a constant value.
	 * To prevent -ve values, decreasing when the rate = 1 halves the rate. Inversely, increasing doubles.
	 */
	public void decreaseRateOfTime() {
		if(this.rateOfTime <= 1) this.rateOfTime /= 2;
		else this.rateOfTime -= 0.25f;
	}
	
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
		final int DRAWSTYLES[] = {GLU.GLU_FILL, GLU.GLU_LINE, GLU.GLU_SILHOUETTE, GLU.GLU_POINT};
		glu.gluQuadricDrawStyle(quad, DRAWSTYLES[1]);
				
		//clear the canvas to a solid colour
		float red = 0.0f, green = 0.0f, blue = 0.0f, alpha = 1.0f;
		gl.glClearColor(red, green, blue, alpha);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {		
		//Setup the viewport
		gl = drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();
		
		//setup the projection matrix (perspective)
		float VERTICAL_FOV = 60.0f;
		float aspectRatio = (float) width/height;
		float nearClip = .01f, farClip = 1000.0f;
		glu.gluPerspective(VERTICAL_FOV, aspectRatio, nearClip, farClip);
	}

	/**
	 * Finds the polar coordinates of the planet with the sun as the origin.
	 * @param art The planet at it's previous position
	 * @return the angle of the planet's orbit with respect to the sun in degrees
	 */
	private float orbitPlanet(Planet art) {
		float sunRadius = artbook.get(0).getRadius(),
				offset = art.getOffset() + sunRadius;
		
		//Get the new orbital position of the planet
		float orbit = art.orbit(timeElapsed) % 360,
				orbitRadians = (float) Math.toRadians(orbit);
		
		//set the position of the planet
		float planetX = (float) (offset * Math.cos(orbitRadians));
		float planetZ = (float) (offset * Math.sin(orbitRadians));
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
		canvas.setSize(width, height);
		canvas.addGLEventListener(this);
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
	 * Moves the camera left or right by adding the unit left vector multiplied by the distance to be moved
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
	 * Determines the new orbital position and rotational angle each planet makes with the sun and it's own axis, respectively
	 */
	public void calculatePlanetProperties() {
		for(Planet art : artbook) {
			//Calculate planet orbit 
			float orbit = orbitPlanet(art);
			float rotation = art.rotate(timeElapsed);
			
			//don't update the rotation automatically if the camera is in free orbit
			art.setOrbit(orbit);
			if(art.getEnum() == this.trackedPlanet && this.isFreeOrbit) continue;
			art.setRotation(rotation);
		}
	}
	
	/**
	 * Determines the position of the camera relative to the specified planet it's orbiting
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
		if(this.isFreeOrbit) pr += this.freeOrbitDirection;
		
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
	 * @param freeOrbitDirection the direction of orbit
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
	 */
	class Camera {
		public final int FREE = 0, //viewing from a static or dynamic position
						 ORBIT = 1; //camera is orbiting a planet
		
		//For rotation direction
		public final int MOVING_LEFT = -1,
				 		 MOVING_NONE = 0,
						 MOVING_RIGHT = 1;
		
		private int mode;
		
		private Vec3f position, lookAtDirection, left, up; 
		private Vec3f focalPoint;
		
		public Camera() {
			setFixedMode();
		}
		
		public void setFixedMode() {
			this.position = new Vec3f(0,0,200);
			this.focalPoint = new Vec3f(0,0,0);
			
			this.lookAtDirection = new Vec3f(new Vec3f(this.focalPoint).sub(this.position)).normalize();
			
			this.up = new Vec3f(0,1,0);
			this.left = this.lookAtDirection.cross(this.up).normalize().mul(-1);
			this.mode = FREE;
		}
		
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

