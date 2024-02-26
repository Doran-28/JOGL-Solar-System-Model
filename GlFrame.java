import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JFrame;

import com.jogamp.opengl.math.Vec3f;

import solarModel.Planet;
/**
 * A JFrame handler of the Jogl implementation's GLCanvas display
 */
public class GlFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Jogl jogl; //the current OpenGL implementation
	
	/**
	 * Creates the GLFrame with a window tile. 
	 * The author chose to use "glf" as the standard naming convention
	 * @param title The title (standard format: title.glf)
	 */
	public GlFrame(String title, Vec3f position, Vec3f focus, Vec3f upDirection) {
		super(title);
		jogl = new Jogl(position, focus, upDirection);
		this.add(jogl.getCanvas());
		this.setSize(jogl.getSize());
		this.add(jogl.getCanvas()); //Adds the display canvas to the frame
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
	
	public void setCameraPosition(Vec3f position) {
		jogl.setCameraPosition(position);
	}
	
	public void setCameraFocus(Vec3f focus) {
		jogl.setCameraFocus(focus);
	}
	
	public void setUpDirection(Vec3f upDirection) {
		jogl.setCameraUpDirection(upDirection);
	}
	
}
