import java.awt.*;
import java.awt.event.*;
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
	private GLUquadric quad, sunQuad; //used for drawing spheres
	private ArrayList<Planet> artbook;
	private GLCanvas canvas;
	private Animator director;
	private float timeElapsed,
			rateOfTime;
	private PlanetEnum trackedPlanet;
	
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
	}
	
	/**
	 * Sets the camera to the planet to be tracked
	 * @param pe
	 */
	public void setTrackedPlanet(PlanetEnum pe) {
		this.trackedPlanet = pe;
	}
	
	/**
	 * Determines the position of the camera relative to the specified planet it's orbiting
	 */
	private void trackPlanet() {
		Planet p = null;
		for(Planet pl : artbook) 
			if(pl.getEnum() == this.trackedPlanet) 
				p = pl;
			
		//calculate camera position from planet position, rotation, and radius
		if(p != null) {			
			float px = p.getX(),
					py = p.getY(),
					pz = p.getZ(),
					pr = p.getRotation(),
					or = p.getRadius() + 0.5f;
			float camX = (float) (or * Math.cos(Math.toRadians(-pr)) + px);
			float camY = 0;
			float camZ = (float) (or * Math.sin(Math.toRadians(-pr)) + pz);
			glu.gluLookAt(camX, camY, camZ, px, py, pz, 0.0f, 1.0f, 0.0f);
		}
		//if we aren't tracking a planet, we're just observing the solar system
		else {
			glu.gluLookAt(-10, 10, -400, 0, 0, 0, 0, 1, 0);
		}
	}
	
	/**
	 * Driven by the Animator director and draws everything in the artbook
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
//		Calculates the orbital and rotational angles of each planet
		for(Planet art : artbook) {
			orbitPlanet(art);
			art.rotate(timeElapsed);
		}

		//set the modelview matrix
		initMatrix(drawable, GL2.GL_MODELVIEW);
		trackPlanet();
		
		/*
		 * The camera is oriented in the canvas
		 * and the viewport is filled with the camera.
		 * now we begin drawing planets
		 */
		
		for(Planet p : this.artbook) {
			//popping and pushing starts the canvas at the origin for every planet
			gl.glPushMatrix();
				//go to this planet's position and rotate by it's rotation
				gl.glTranslatef(p.getX(), 0.0f, p.getZ());
				gl.glRotatef((float) Math.toRadians((p.getRotation())), 0.0f, 1.0f, 0.0f);
				
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
	
	public void increaseRateOfTime() {
		if(this.rateOfTime <= 1) this.rateOfTime *= 2;
		else this.rateOfTime += 0.25f;
	}
	
	public void decreaseRateOfTime() {
		if(this.rateOfTime <= 1) this.rateOfTime /= 2;
		else this.rateOfTime -= 0.25f;
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {}
	
	/**
	 * Initialises the display canvas
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
	 * Finds the orbital angle of the planet on it's orbit and translate the planet there 
	 * @param art
	 * @return the actual offset of the planet
	 */
	private void orbitPlanet(Planet art) {
		float sunRadius = artbook.get(0).getRadius(),
				offset = art.getOffset(),
				orbit;
		
		//if the planet isn't the sun, skew the offset
		//skew is distance from the center of the sun (origin)
		//and a virtual skew value to make sure the planets don't cut throughthe sun
		if(art.getEnum() != null) offset += sunRadius;
		
		//Get the new orbital position of the planet
		orbit = art.orbit(timeElapsed) % 360;
		
		//set the position of the planet
		float planetX = (float) (offset * Math.cos(Math.toRadians(orbit)));
		float planetZ = (float) (offset * Math.sin(Math.toRadians(orbit)));
		art.setX(planetX);
		art.setZ(planetZ);
	}
	
	/**
	 * Sets the drawing colour to the specified planet colour
	 * @param p
	 */
	public void setColor(Planet p) {
		float red = p.getRed(), green = p.getGreen(), blue = p.getBlue();
		gl.glColor3f(red, green, blue);
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
}

