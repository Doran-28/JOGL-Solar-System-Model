import java.awt.*;
import java.awt.event.*;
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
public class Jogl implements GLEventListener, MouseListener, KeyListener {
	private GL2 gl; //interface to OpenGL (C) functions
	private GLU glu; //graphics library utilities
	private GLUquadric quad; //used for drawing spheres
	private ArrayList<Planet> artbook;
	private GLCanvas canvas;
	private Animator director;
	private float frameCount = 0;
	// Textures for each planet
	private ArrayList<Texture> planetTextures;

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
	public Jogl (float camX, float camY, float camZ, float focusX, float focusY, float focusZ) {
		canvas = makeCanvas(500,500);
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
		director = new Animator(canvas);
		this.artbook = new ArrayList<Planet>();
		this.planetTextures = new ArrayList<Texture>(8);
	}
	
	/**
	 * Driven by the Animator director and draws everything in the artbook
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		//initialise the display matrix
		gl = initMatrix(drawable, GL2.GL_MODELVIEW);
		
		//get earth position
		float earthX = artbook.get(3).getX(),
				earthZ = artbook.get(3).getZ(),
				earthRotation = artbook.get(3).getRotation();
		
		//Draw each planet
		Planet art;
		for(int i = 0; i < artbook.size(); i++) {
			returnToOrigin();
			//apply camera transformations first
			gl.glRotatef(-(earthRotation), 0, 1, 0);
			gl.glTranslatef(-earthX, 1, -earthZ-1);
			
			//then draw the planets
			art = artbook.get(i);
			drawPlanet(art, planetTextures.get(i));
		}
		frameCount += 1;
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {}
	
	/**
	 * Initialises the display canvas
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(1); //updates display once every vertical refresh
		//setup OpenGl interface (gl) and utilities library (glu)
		gl = drawable.getGL().getGL2();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		glu = new GLU(); //library of drawable functions
		
		//quad is used for drawing spheres
		quad = glu.gluNewQuadric();
		final int DRAWSTYLES[] = {GLU.GLU_FILL, GLU.GLU_LINE, GLU.GLU_SILHOUETTE, GLU.GLU_POINT};
		glu.gluQuadricDrawStyle(quad, DRAWSTYLES[1]);
				
		//clear the canvas to a solid colour
		float red = 0.0f, green = 0.0f, blue = 0.0f, alpha = 1.0f;
		gl.glClearColor(red, green, blue, alpha);

		// Intialize all the Texture variable with their textures
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
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		//Setup the viewport
		gl = drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();
		
		//setup the perspective projection to the viewport
		float VERTICAL_FOV = 60.0f;
		float aspectRatio = (float) width/height;
		float nearClip = .1f, farClip = 1000.0f;
		glu.gluPerspective(VERTICAL_FOV, aspectRatio, nearClip, farClip);
//		camera.setCamera();
	}

	private Texture loadTexture(String textureFileName) {
		Texture texture = null;
		try {
			File textureFile = new File(textureFileName); // Load your JPG image
			texture = TextureIO.newTexture(textureFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return texture;
	}
	/**
	 * Orients and draws the given planet
	 * @param art
	 */
	private void drawPlanet(Planet art, Texture texture) {
		if(art.getEnum() != null)
			orbitPlanet(art);


		rotatePlanet(art);
		texture.bind(gl);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		texture.enable(gl);

		GLUquadric quad1 = glu.gluNewQuadric();
		glu.gluQuadricTexture(quad1, true);

		// Render the textured object
		glu.gluSphere(quad1, art.getRadius(), 10, 10);

		// Disable texture and reset state
		texture.disable(gl);
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}
	
	/**
	 * Finds the orbital angle of the planet on it's orbit and translate the planet there 
	 * @param art
	 * @return the actual offset of the planet
	 */
	private void orbitPlanet(Planet art) {
		float sunRadius = 109.3f,
				radius = art.getRadius(),
				offset = art.getOffset(),
				orbit;
		
		//if the planet isn't the sun, skew the offset
		float skew = sunRadius + radius;
		if(radius != sunRadius) offset += skew;
		
		//Get the new orbital position of the planet
		orbit = art.orbit(frameCount) % 360;
		
		//set the position of the planet
		float planetX = (float) (offset * Math.cos(Math.toRadians(orbit)));
		float planetZ = (float) (offset * Math.sin(Math.toRadians(orbit)));
		gl.glTranslatef(planetX, 0, planetZ);
		art.setX(planetX);
		art.setZ(planetZ);
	}
	
	/**
	 * Rotates the planet on it's axis
	 * @param art
	 */
	private void rotatePlanet(Planet art) {
		float rotation = art.rotate(frameCount);
		gl.glRotatef(-rotation, 0.0f, 1.0f, 0.0f);
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
	 * Initialises the specified JOGL matrix
	 * @param drawable The canvas
	 * @param matrix The matrix to be initialised (GL.GL_MODELVIEW, GL.GL_PROJECTION)
	 * @param clearString The bitstring to clear the buffers
	 * @return the matrix for the canvas now interfaced to OpenGL
	 */
	private GL2 initMatrix(GLAutoDrawable drawable) {
		int clearString = (GL.GL_COLOR_BUFFER_BIT  | GL.GL_DEPTH_BUFFER_BIT);
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
	 * Returns the current matrix pointer to the identity (1,1,1)
	 */
	private void returnToOrigin() {
		gl.glLoadIdentity();
		gl.glTranslatef(-1.0f, -1.0f, -1.0f);
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

