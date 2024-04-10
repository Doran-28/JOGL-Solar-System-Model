

import java.util.ArrayList;

import solarModel.Planet;
import solarModel.PlanetEnum;

/**
 * This is where the solar system is run
 * We create each planet with their appropriate data
 * We create an instance of GLFrame, which will also create an instance of Jogl
 */
public class solarSystem{
	final static int PLANETCOUNT = 8; // Number of planets in the solar system (excluding pluto)
	static ArrayList<Planet> planets = new ArrayList<Planet>(PLANETCOUNT); // intialize ArrayList to have size of 9

	/**
	 * This will create each planet, with the appropriate property values
	 * Then add the planet to the ArrayList planets
	 * @return boolean status
	 */
	public static boolean setPlanets() {
		//first add the sun
		float[] color = {1.0f,0.55f,0.0f},
				position = {0.0f, 0.0f, 0.0f};
		Planet p = new Planet(position, 109.3f, 0.0f, 0.0f, 0.8800f, color);  // Properties set for the sun
		planets.add(p);
		//add the other planets
		for(PlanetEnum planetEnum : PlanetEnum.values()) {
			p = new Planet(position, planetEnum);	
			planets.add(p);
		}
		return true;
	}

	/*
	Main method that runs the program
	 */
	public static void main(String[] args) throws Exception {
		setPlanets();								 // Sets up the ArrayList planets, with planets and their data
		GlFrame glf = new GlFrame("Solar.glf"); // Sets up the main window, and user controls
		glf.loadArtbook(planets);					 // Loads the ArrayList planets into the Jogl class
		glf.action();								 // Starts the program, and animation
	}

}
