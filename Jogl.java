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
public class Jogl implements GLEventListener, MouseListener, KeyListener {
	private static GL2 gl; //interface to OpenGL (C) functions
	private static GLU glu; //graphics library utilities
	private static GLUquadric quad; //used for drawing spheres
	private ArrayList<Planet> artbook;
	private GLCanvas canvas;
	private Animator director;
	private Camera camera; //Handler for camera control
	private float frameCount = 0;
	protected Vec3f position;
	
	/**
	 * Creates a new instance of the jogl implementation with a defined camera
	 */
	public Jogl (Vec3f position, Vec3f focus, Vec3f upDirection) {
		camera = new Camera(position, focus, upDirection);
		canvas = makeCanvas(500,500);
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		director = new Animator(canvas);
		this.artbook = new ArrayList<Planet>();
		this.position = new Vec3f();
	}
	
	/**
	 * Driven by the Animator director and draws everything in the artbook
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		//initialise the display matrix
		int clearString = (GL.GL_COLOR_BUFFER_BIT  | GL.GL_DEPTH_BUFFER_BIT);
		gl = initMatrix(drawable, GL2.GL_MODELVIEW, clearString);
		Planet art;
		
		//Display the sun
		returnToOrigin();
		art = artbook.get(0);
		drawPlanet(art);
		//drawColorFilledSphere(gl,10f, 10, 10);
		
		//Display the earth
		returnToOrigin();
		art = artbook.get(3);
		drawPlanet(art);
		
		//Track the earth with the camera
		getArtPosition();
		float[] p = {this.position.x() + 10f,1, this.position.z()},
				f = {this.position.x(), 1, this.position.z()};
		Vec3f position = new Vec3f(p), focus = new Vec3f(f);
		camera.setPosition(position);
		camera.setFocus(focus);
		camera.setCamera();
		
		//print the camera position and focal point, and the earth coordinates
		float camX = camera.getPosition().x(), camZ = camera.getPosition().z();
		float focusX = camera.getFocus().x(), focusZ = camera.getFocus().z();
		System.out.println("Camera at " + camX + " " + camZ);
		System.out.println("Camera focused at " + focusX + " " + focusZ);
		System.out.println("Earth blitted at " + this.position.toString());
		
		//the executive loop
//		for(int i = 0; i < artbook.size(); i++) {
//			returnToOrigin();
//			art = artbook.get(i);
//			drawPlanet(art);
//			if(i == 3) {
//				System.out.println("Earth blitted at " + art.getPosition().x());
//			}
//		}
		frameCount += 1;
	}

	/**
	 * Orients and draws the given planet
	 * @param art
	 */
	private void drawPlanet(Planet art) {
		orbitPlanet(art);
		rotatePlanet(art);
		setColor(art);
		glu.gluSphere(quad, art.getRadius(), 10, 10);
	}



	/**
	 * Gets the coordinates from the modelview matrix and stores it in the position property
	 */
	private void getArtPosition() {
		float[] m = new float[16];
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, m, 0);
//		for(int i = 0; i < 4; i++) {
//			for(int j = 0; j < 4; j++) {
//				System.out.print(m[i*4+j] + " ");
//			}
//			System.out.println();
//		}
		float[] p = {m[0], m[5], m[10]};
		this.position = new Vec3f(p);
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {}

	/**
	 * Sets the drawing colour to the specified planet colour
	 * @param p
	 */
	public void setColor(Planet p) {
		float red = p.getRed(), green = p.getGreen(), blue = p.getBlue();
		gl.glColor3f(red, green, blue);
	}
	
	/**
	 * Finds the orbital position of the planet on it's path (offset) and translate the planet there 
	 * @param art
	 */
	private void orbitPlanet(Planet art) {
		float sunRadius = 109.3f, radius = art.getRadius(), offset = art.getOffset(), orbit;
		
		//if the planet isn't the sun, skew the offset
		float skew = sunRadius + radius;
		if(radius != sunRadius) offset += skew;
		
		//rotate the planet
		orbit = art.orbit(frameCount, offset);
		gl.glRotatef(orbit, 0.0f, 1.0f, 0.0f);
		
		//Translate the planet to it's orbital radius
		gl.glTranslatef(offset, 0.0f, 0.0f);
	}
	
	/**
	 * Rotates the planet on it's axis
	 * @param art
	 */
	private void rotatePlanet(Planet art) {
		float rotation = art.rotate(frameCount);
		gl.glRotatef(rotation, 0.0f, 1.0f, 0.0f);
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
	 * Initialises the display canvas
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(1); //updates display once every vertical refresh
		gl = drawable.getGL().getGL2();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		glu = new GLU(); //library of drawable functions
		quad = glu.gluNewQuadric();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		final int DRAWSTYLES[] = {GLU.GLU_FILL, GLU.GLU_LINE, GLU.GLU_SILHOUETTE, GLU.GLU_POINT};
		glu.gluQuadricDrawStyle(quad, DRAWSTYLES[1]);
	}
	public boolean isPlaying() {
		return director.isAnimating();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_Q) 
			stopAnimator();
		else if(keyCode == KeyEvent.VK_ENTER) {
			Planet earth = artbook.get(0);
			
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {}	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	/**
	 * Reads the artbook to be used by this implementation
	 * @param artbook the artbook to be used
	 */
	public void loadArtbook(ArrayList<Planet> artbook) {
		this.artbook = artbook;
//		float[] position = {1.0f, 1.0f, 1.0f};
//		this.position = new Vec3f(position);
//		for(int i = 0; i < this.artbook.size(); i ++) {
//			this.planetPositions.add(new Vec3f(position));
//		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == 3) 
			stopAnimator();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		//setup the viewport
		float VERTICAL_FOV = 60.0f;
		float aspectRatio = (float) width/height;
		float nearClip = 1.0f, farClip = 1000.0f;
		
		//setup the camera
		gl = drawable.getGL().getGL2();
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION); //change the view matrix
		gl.glLoadIdentity(); //set the stored mat to the identity mat I
		
		//init the camera and viewport
		glu.gluPerspective(VERTICAL_FOV, aspectRatio, nearClip, farClip);
//		camera.setCamera();
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
	
	public Vec3f getCameraPosition() {
		return camera.getPosition();
	}
	public Vec3f getCameraFocus() {
		return camera.getFocus();
	}
	public Vec3f getCameraUpDirection() {
		return camera.getUpDirection();
	}

	public void setCameraPosition(Vec3f position) {
		camera.setPosition(position);
	}
	public void setCameraFocus(Vec3f focus) {
		camera.setFocus(focus);
	}
	public void setCameraUpDirection(Vec3f upDirection) {
		camera.setUpDirection(upDirection);
	}
	
	/**
	 * Initialises the specified JOGL matrix
	 * @param drawable The canvas
	 * @param matrix The matrix to be initialised (GL.GL_MODELVIEW, GL.GL_PROJECTION)
	 * @param clearString The bitstring to clear the buffers
	 * @return the matrix for the canvas now interfaced to OpenGL
	 */
	private GL2 initMatrix(GLAutoDrawable drawable, int matrix, int clearString) {
		gl = drawable.getGL().getGL2();
		gl.glClear(clearString);
		gl.glMatrixMode(matrix);
		return gl;
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
	 * Returns the current matrix pointer to the origin (1,1,1)
	 */
	private void returnToOrigin() {
		gl.glLoadIdentity();
	}
	
	/**
	 * Helper class to handle camera controls
	 */
	class Camera{
		private Vec3f position;
		private Vec3f focus;
		private Vec3f upDirection;
		
		/**
		 * Creates a new camera at and focused on the origin, with upDirection=<0.0f,1.0f,0.0f>
		 */
		public Camera() {
			position = new Vec3f(0.0f, 0.0f, 0.0f);
			focus = new Vec3f(0.0f, 0.0f, 0.0f);
			upDirection = new Vec3f(0.0f, 1.0f, 0.0f);
		}
		public void setPosition(float camX, float camY, float camZ) {
			position.setX(camX); position.setY(camY); position.setZ(camZ);
			
		}
		/**
		 * Creates a new camera with specified verticies
		 * @param position The camera coordinates
		 * @param focus The camera focal point
		 * @param upDirection The 'upwards orientation' of the camera relative to the projecction
		 */
		public Camera(Vec3f position, Vec3f focus, Vec3f upDirection) {
			this.position = position;
			this.focus = focus;
			this.upDirection = upDirection;
		}
		
		/**
		 * Takes the parameter vectors and uses them to set the camera position and orientation
		 * The position, focus, and upDirection params must be set as prerequsites. 
		 */
		public void setCamera() {
			//get the params
			Vec3f position = camera.getPosition(),
					focus = camera.getFocus(),
					upDirection = camera.getUpDirection();
			
//			gl.glMatrixMode(GL2.GL_PROJECTION); //change the view matrix
			
			//break down the params into each dimension
			float camX, camY, camZ,
			focusX, focusY, focusZ,
			upDirectionX, upDirectionY, upDirectionZ;
			
			camX = position.x(); camY = position.y(); camZ = position.z();
			focusX = focus.x(); focusY = focus.y(); focusZ = focus.z();
			upDirectionX = upDirection.x(); upDirectionY = upDirection.y(); upDirectionZ = upDirection.z();
//			System.out.println("Camera coords are" + camX + " " + camY + " " + camZ);
			glu.gluLookAt(camX, camY, camZ, focusX, focusY, focusZ, upDirectionX, upDirectionY, upDirectionZ);
//			gl.glMatrixMode(GL2.GL_MODELVIEW); 
		}
		/**
		 * Rotates the 3d space by the specified angle and axis
		 * @param angle The angle of rotation
		 * @param axis The axis of rotation
		 */
		public void rotate(float angle, int axis) {
			
		}
		/**
		 * Rotates the 3d space by specified angles and axis'
		 * @param angle1 First angle
		 * @param axis1 First axis
		 * @param angle2 Second angle
		 * @param axis2 Second axis
		 */
		public void rotate(float angle1, int axis1, float angle2, int axis2) {
			
		}
		
		public void rotate(Planet p) {
//			float rotation = p.getAngle() + (frame),
//					rotateRate = p.getRotateRate();
	
		}
		
		/**
		 * Translates the world in the magnitude of the specified vector
		 * @param dx x direction
		 * @param dy y direction
		 * @param dz z direction
		 */
		public void translate(float dx, float dy, float dz) {
			gl.glTranslatef(-dx, -dy, -dz);
		}
		
		public void yaw(float angle) {
			gl.glRotatef(angle, -1.0f, -1.0f, 0.0f);
		}
		public void pitch(float angle) {
			gl.glRotatef(angle, -1.0f, 0.0f, -1.0f);
		}
		public void roll(float angle) {
			gl.glRotatef(angle, 0.0f, -1.0f, -1.0f);
		}
		
		public Vec3f getPosition() {
			return position;
		}
		public void setPosition(Vec3f position) {
			this.position = position;
		}
		public Vec3f getFocus() {
			return focus;
		}
		public void setFocus(Vec3f focus) {
			this.focus = focus;
		}
		public Vec3f getUpDirection() {
			return upDirection;
		}
		public void setUpDirection(Vec3f upDirection) {
			this.upDirection = upDirection;
		}	
	}
}

