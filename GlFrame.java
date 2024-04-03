import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JFrame;

import com.jogamp.opengl.math.Vec3f;

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

	private float savedRateOfTime;
	// Used for pause and play feauture
	private boolean isPlaying = true;
	
	/**
	 * Creates the GLFrame with a window tile. 
	 * The author chose to use "glf" as the standard naming convention
	 * @param title The title (standard format: title.glf)
	 */
	public GlFrame(String title) {
		super(title);
		jogl = new Jogl();
		this.setSize(jogl.getSize());
		this.add(jogl.getCanvas()); //Adds the display canvas to the frame
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		this.setUndecorated(true);
		
		jogl.getCanvas().addKeyListener(this);
		this.addWindowListener(new WindowAdapter() {
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
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_1:
			jogl.setTrackedPlanet(PlanetEnum.MERCURY);
			jogl.setRateOfTime(0.01f);
			break;
		
		case KeyEvent.VK_2:
			jogl.setTrackedPlanet(PlanetEnum.VENUS);
			jogl.setRateOfTime(0.0045f);
			break;
			
		case KeyEvent.VK_3:
			jogl.setTrackedPlanet(PlanetEnum.EARTH);
			jogl.setRateOfTime(0.5f);
			break;
			
		case KeyEvent.VK_4:
			jogl.setTrackedPlanet(PlanetEnum.MARS);
			jogl.setRateOfTime(0.5f);
			break;
			
		case KeyEvent.VK_5:
			jogl.setTrackedPlanet(PlanetEnum.SATURN);
			jogl.setRateOfTime(0.9f);
			break;
			
		case KeyEvent.VK_6:
			jogl.setTrackedPlanet(PlanetEnum.JUPITER);
			jogl.setRateOfTime(1.1f);
			break;
			
		case KeyEvent.VK_7:
			jogl.setTrackedPlanet(PlanetEnum.URANUS);
			jogl.setRateOfTime(1.0f);
			break;
			
		case KeyEvent.VK_8:
			jogl.setTrackedPlanet(PlanetEnum.NEPTUNE);
			jogl.setRateOfTime(1.0f);
			break;
			
		case KeyEvent.VK_0:
			jogl.setTrackedPlanet(null);
			jogl.setRateOfTime(100.0f);
			break;
			
		case KeyEvent.VK_UP:
			if (isPlaying)
				jogl.increaseRateOfTime();
			break;
		
		case KeyEvent.VK_DOWN:
			if (isPlaying)
				jogl.decreaseRateOfTime();
			break;


		// Hitting the space bar will pause and play the animatuion
		case KeyEvent.VK_SPACE:
			if (isPlaying) {
				savedRateOfTime = jogl.getRateOfTime();
				jogl.setRateOfTime(0);
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
		// TODO Auto-generated method stub
		
	}	
}
