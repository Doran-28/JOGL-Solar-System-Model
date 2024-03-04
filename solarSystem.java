

import java.util.ArrayList;

import com.jogamp.opengl.math.Vec3f;

import solarModel.Planet;
import solarModel.PlanetEnum;


public class solarSystem{
	final static int PLANETCOUNT = 8;
	static ArrayList<Planet> planets = new ArrayList<Planet>(PLANETCOUNT);
	
	public static boolean setPlanets() {
		//first add the sun
		float[] color = {1.0f,0.55f,0.0f},
				position = {1.0f, 1.0f, 1.0f};
		Planet p = new Planet(position, 109.3f, 0.0f, 0.0f, 0.8800f, color);
		planets.add(p);
		//add the other planets
		for(PlanetEnum planetEnum : PlanetEnum.values()) {
			p = new Planet(position, planetEnum);	
			planets.add(p);
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		setPlanets();
		
		//up close camera params (earth in view at minimum
		Vec3f position = new Vec3f(-10.0f,10.0f,200.0f);
		Vec3f focus = new Vec3f(0.0f, 0.0f, 0.0f);
		Vec3f upDirection = new Vec3f(0.0f, 1.0f, 0.0f);
		float camX = -10.0f, camY = 10.0f, camZ = 300.0f;
		float focusX = 1.0f, focusY = 1.0f, focusZ = 1.0f;
		
		//whole solar system
//		Vec3f position = new Vec3f(-10.0f,10.0f,700.0f);
//		Vec3f focus = new Vec3f(0.0f, 0.0f, 0.0f);
//		Vec3f upDirection = new Vec3f(0.0f, 1.0f, 0.0f);
		
		//sun from above
//		Vec3f position = new Vec3f(0.0f,700.0f,0.0f);
//		Vec3f focus = new Vec3f(0.0f, 0.0f, 0.0f);
//		Vec3f upDirection = new Vec3f(0.0f, 0.0f, 1.0f);
		GlFrame glf = new GlFrame("Solar.glf", camX, camY, camZ, focusX, focusY, focusZ);
		glf.loadArtbook(planets);
		glf.action();
	}

}
