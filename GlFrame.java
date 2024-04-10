import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JFrame;

import solarModel.Planet;
import solarModel.PlanetEnum;
/**
 * A JFrame handler of the Jogl implementation's GLCanvas display
 */
public class GlFrame extends JFrame implements KeyListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Jogl jogl; //the current OpenGL implementation

	// For pause and play functionality
	private boolean isPlaying;	// If value is set to true, then it allows the program to pause
	private float savedRateOfTime; // When time is paused, we sav current time in this variable

	
	/**
	 * Creates the GLFrame with a window tile. 
	 * The author chose to use "glf" as the standard naming convention
	 * @param title The title (standard format: title.glf)
	 */
	public GlFrame(String title) {
		super(title);
		jogl = new Jogl();	// Creates an instance of the Jogl class, which handles most of the logic for the solar system
		this.setSize(jogl.getSize());	// Sets size for windpw
		this.add(jogl.getCanvas()); //Adds the display canvas to the frame
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Makes the window full screen
		this.setUndecorated(true);
		
		jogl.getCanvas().addKeyListener(this); // Enables user commands from the keyboard
		this.addWindowListener(new WindowAdapter() {  // Allows for the program to terminate when closed
	        public void windowClosing(WindowEvent e) {
	            cut();
	        }
	        });
	}
	/**
	 * Starts playing the display
	 * @return true if playing successfully
	 */
	public boolean action() {
		if(jogl.isPlaying()) return false;
		this.setVisible(true);
		return jogl.startAnimator();
	}

	/**
	 * Stops playing the display
	 * @return true if stopped successfully
	 */
	public boolean cut() {
		this.dispose();
		return jogl.stopAnimator();
	}
	/**
	 * Loads the artbook into the implementation for displaying
	 * @param artbook The artbook to be loaded
	 */
	public void loadArtbook(ArrayList<Planet> artbook) {
		jogl.loadArtbook(artbook);
	}

	/**
	 * This method must be here because we implement the KeyListener interface
	 * The method will not be used as keyPressed and keyReleased will be the relevant methods.
	 * @param e the event to be processed
	 */
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Method contains switch statement which handles what code to execute depending on the user input
	 * @param e The input provided by the keyboard
	 */

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {

			/*
			For each planet, we set that planet to be tracked by the camera
			We set the camera mode to orbit
			We adjust the rate of time to ensure viewing each planet isn't too fast or too slow
			 */

			//VIEWING MERCURY
			case KeyEvent.VK_1:
				jogl.setTrackedPlanet(PlanetEnum.MERCURY);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(0.01f);
				break;

			//VIEWING VENUS
			case KeyEvent.VK_2:
				jogl.setTrackedPlanet(PlanetEnum.VENUS);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(0.0045f);
				break;

			//VIEWING EARTH
			case KeyEvent.VK_3:
				jogl.setTrackedPlanet(PlanetEnum.EARTH);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(0.5f);
				break;

			//VIEWING MARS
			case KeyEvent.VK_4:
				jogl.setTrackedPlanet(PlanetEnum.MARS);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(0.5f);
				break;

			//VIEWING JUPITER
			case KeyEvent.VK_5:
				jogl.setTrackedPlanet(PlanetEnum.JUPITER);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(0.9f);
				break;

			//VIEWING SATURN
			case KeyEvent.VK_6:
				jogl.setTrackedPlanet(PlanetEnum.SATURN);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(1.1f);
				break;

			//VIEWING URANUS
			case KeyEvent.VK_7:
				jogl.setTrackedPlanet(PlanetEnum.URANUS);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(1.0f);
				break;

			//VIEWING NEPTUNE
			case KeyEvent.VK_8:
				jogl.setTrackedPlanet(PlanetEnum.NEPTUNE);
				jogl.setCameraMode(1);
				jogl.setRateOfTime(1.0f);
				break;

			//RETURN TO STATIC VIEWPOINT
			case KeyEvent.VK_0:
				jogl.setTrackedPlanet(null);	// Not tracking a specific planet
				jogl.setCameraFixed();		// Set camera mode to static view
				jogl.setCameraMode(0);		// Set camera mode to static view
				jogl.setRateOfTime(1.0f);
				break;

			//INCREASE SIMULATION SPEED
			case KeyEvent.VK_SHIFT:
				jogl.increaseRateOfTime();
				break;

			//DECREASE SIMULATION SPEED
			case KeyEvent.VK_CONTROL:
				jogl.decreaseRateOfTime();
				break;

			//TRANSLATE CAMERA FORWARDS
			case KeyEvent.VK_UP:
				if(jogl.getCameraMode() == 0) {//fixed camera mode
					jogl.moveCameraInOut(1);
				}
				break;

			//TRANSLATE CAMERA BACKWARDS
			case KeyEvent.VK_DOWN:
				if(jogl.getCameraMode() == 0) {//fixed camera mode
					jogl.moveCameraInOut(-1);
				}
				break;

			//TRANSLATE CAMERA LEFT
			case KeyEvent.VK_LEFT:
				if(jogl.getCameraMode() == 0) //fixed camera mode
					jogl.moveCameraLeftRight(-1);
				else
					jogl.setIsFreeOrbit(true, -1);
				break;

			//TRANSLATE CAMERA RIGHT
			case KeyEvent.VK_RIGHT:
				if(jogl.getCameraMode() == 0) //fixed camera mode
					jogl.moveCameraLeftRight(1);
				else
					jogl.setIsFreeOrbit(true, 1);
				break;

			//ROTATE CAMERA LEFT
			case KeyEvent.VK_A:
				if(jogl.getCameraMode() == 0) //fixed camera mode
					jogl.rotateCameraLeftRight(1);
				break;

			//ROTATE CAMERA RIGHT
			case KeyEvent.VK_D:
				if(jogl.getCameraMode() == 0) //fixed camera mode
					jogl.rotateCameraLeftRight(-1);
				break;

			//ROTATE CAMERA DOWN
			case KeyEvent.VK_W:
				if(jogl.getCameraMode() == 0) //fixed camera mode
					jogl.rotateCameraUpDown(-1);
				break;

			//ROTATE CAMERA UP
			case KeyEvent.VK_S:
				if(jogl.getCameraMode() == 0) //fixed camera mode
					jogl.rotateCameraUpDown(1);
				break;

			// Hitting the space bar will pause and play the animation
			case KeyEvent.VK_SPACE:
			if (isPlaying) {
				savedRateOfTime = jogl.getRateOfTime();
				jogl.setRateOfTime(0.0f);
				isPlaying = false;
			}
			else{
				jogl.setRateOfTime(savedRateOfTime);
				isPlaying = true;
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//Update rotation speed to 0 when user releases keys
		if(jogl.getIsFreeOrbit() && (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)) {
			jogl.setIsFreeOrbit(true, 0);
		}
	}	
}
