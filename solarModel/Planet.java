package solarModel;

import com.jogamp.opengl.math.Vec3f;

/**
 *  * A Planet is a 3d graphical element to be displayed on jogl.java
 * Planets have rotation, orbit, offset, and a radius
 * Planets may also have moons
 * Planets have rgb intensity values in the range of [0.0f,1.0f]
 */
public class Planet {
	protected float rotation; //angle of rotation on this planet's axis
	protected float orbit; //Planet's position in it's orbit
	protected float radius; //relative to earth
	protected float offset; //relative to earth's offset from the sun (1AU)
	private float rotateRate, orbitRate; //relative velocities to earth
	protected Vec3f color, position;
	protected Moon moon;
	
	//all ratios are of earth's respective properties
//	static float[] radiusRatios = {0.3829f, 0.9499f, 0.12742f, 0.5320f, 6.818f, 9.140f, 3.980f, 3.864f};
	static float[] radiusRatios = {0.3829f, 0.9499f, 1.12742f, 1, 6.818f, 9.140f, 3.980f, 3.864f};
	static float[] rotateRatios = {59f, 243.0f, 1.0f, 1.025f, 0.4138f, 0.4458f, 0.7180f, 0.6708f};
	static float[] orbitRatios = {0.2410f, 0.6164f, 1.0f, 1.882f, 11.8f, 29.5f, 84.1f, 165.0f};
	static float[] offsetRatios = {0.410f, 0.72f, 1f, 1.52f, 5.20f, 9.538f, 19.61f, 30.00f};
	static float[][] planetColors = {{0.76f,0.74f,0.74f}, {0.85f,0.70f,0.57f}, {0.39f,0.59f,0.66f}, {0.95f,0.38f,0.47f}, {0.75f,0.51f,0.22f}, {0.95f,0.81f,0.53f}, {0.58f,0.73f,0.75f}, {0.47f,0.62f,0.75f}};
	private final int EARTHINDEX = 2;

	
	/**
	 * Make a new planet based on the enum and place it at the origin
	 * @param p the enum
	 */
	public Planet(PlanetEnum p) {
		//init rotation and orbit
		this.rotation = 0; this.orbit = 0;
		PlanetEnum[] planets = PlanetEnum.values();
		float earthRadius = radiusRatios[EARTHINDEX], earthOffset = offsetRatios[EARTHINDEX], earthRotateRate = rotateRatios[EARTHINDEX], earthOrbitRate = orbitRatios[EARTHINDEX];
		for(int planetIndex = 0; planetIndex < planets.length; planetIndex++) {
			if(planets[planetIndex] == p) {
				this.radius = earthRadius * radiusRatios[planetIndex];
				this.offset = earthOffset * offsetRatios[planetIndex];
				this.rotateRate = earthRotateRate * rotateRatios[planetIndex];
				this.orbitRate = earthOrbitRate * orbitRatios[planetIndex];
				this.color = new Vec3f(planetColors[planetIndex]);
			}
		}
	}
	
	/**
	 * Make a new planet based on the enum
	 * @param position arr for position vertex
	 * @param p the enum
	 */
	public Planet(float[] position, PlanetEnum p) {
		//init rotation and orbit
		this.rotation = 0; this.orbit = 0;
		this.position = new Vec3f(position);
		PlanetEnum[] planets = PlanetEnum.values();
		float earthRadius = radiusRatios[EARTHINDEX], earthOffset = offsetRatios[EARTHINDEX], earthRotateRate = rotateRatios[EARTHINDEX], earthOrbitRate = orbitRatios[EARTHINDEX];
		for(int planetIndex = 0; planetIndex < planets.length; planetIndex++) {
			if(planets[planetIndex] == p) {
				this.radius = earthRadius * radiusRatios[planetIndex];
				this.offset = earthOffset * offsetRatios[planetIndex];
				this.rotateRate = earthRotateRate * rotateRatios[planetIndex];
				this.orbitRate = earthOrbitRate * orbitRatios[planetIndex];
				this.color = new Vec3f(planetColors[planetIndex]);
			}
		}
	}
	
//	/**
//	 * Create a new planet and moon from an enum 
//	 * @param p the enum
//	 * @param moonRadius
//	 * @param moonOffset The distance from the planet to the moon
//	 */
//	public Planet(PlanetEnum p, float moonRadius, float moonOffset) {
//		this.rotation = 0; this.orbit = 0;
//		PlanetEnum[] planets = PlanetEnum.values();
//		for(int planetIndex = 0; planetIndex < 9; planetIndex++) {
//			if(planets[planetIndex] == p) {
//				this.radius = earth.radius * radiusRatios[planetIndex];
//				this.offset = earth.offset * offsetRatios[planetIndex];
//				this.color = new Vec3f(planetColors[planetIndex]);
//			}
//		}
//		this.moon = new Moon(p);
//	}
	
	public Planet(float radius, float offset, float orbitRate, float rotateRate, float[] color) {
		this.radius = radius;
		this.offset = offset;
		this.orbitRate = orbitRate;
		this.rotateRate = rotateRate;
		this.orbit = 0; this.rotation = 0;
		this.color = new Vec3f(color);
	}
	
	public Planet(float[] position, float radius, float offset, float orbitRate, float rotateRate, float[] color) {
		this.radius = radius;
		this.offset = offset;
		this.orbitRate = orbitRate;
		this.rotateRate = rotateRate;
		this.orbit = 0; this.rotation = 0;
		this.color = new Vec3f(color);
		this.position = new Vec3f(position);
	}
	
	/**
	 * Finds the new orbital angle at the current frame
	 * @param frameCount
	 * @param offset depreceated
	 * @return
	 */
	public float orbit(float frameCount, float offset) {
		//find the new orbit angle theta and the delta between the current and new orbit
		float theta = frameCount * this.orbitRate;
		this.orbit = theta;
		return this.orbit;
	}
	
	/**
	 * Rotates the planet on it's axis
	 * @param frameCount
	 * @return
	 */
	public float rotate(float frameCount) {
		//find the new orbit angle theta and the delta between the current and new orbit
		float theta = frameCount * this.rotateRate;
		this.rotation = theta;
		return this.rotation;
	}
	
	public void setX(float value) {
		if(value == 0) value = 1;
		this.position.setX(value);
	}
	
	public void setZ(float value) {
		this.position.setZ(value);
	}
	
	public Vec3f getPosition() {
		return this.position;
	}
	
	/**
	 * Checks if the planet has a moon
	 * @return true if moon isn't null
	 */
	public boolean hasMoon() {
		return moon != null;
	}
	
	public float getRotation() {
		return rotation;
	}
	public void setRotation(float angle) {
		this.rotation = angle;
	}
	public Vec3f getColor() {
		return this.color;
	}
	public float getRed() {
		return this.color.x();
	}
	public void setRed(float red) {
		this.color.setX(red);
	}
	public float getGreen() {
		return this.color.y();
	}
	public void setGreen(float green) {
		this.color.setY(green);
	}
	public float getBlue() {
		return this.color.z();
	}
	public void setBlue(float blue) {
		this.color.setZ(blue);
	}
	public Moon getMoon() {
		return moon;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}
	public float getOffset() {
		return offset;
	}
	public void setOffset(float offset) {
		this.offset = offset;
	}
	public float getOrbit() {
		return orbit;
	}
	public void setOrbit(float orbit){
		this.orbit = orbit;
	}

}


