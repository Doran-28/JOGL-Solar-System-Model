package solarModel;

import com.jogamp.opengl.math.Vec3f;

/**
 *  * A Planet is a 3d graphical element to be displayed on jogl.java
 * Planets have rotation, orbit, offset, and a radius
 * Planets may also have moons (not implemented)
 * Planets have rgb intensity values in the range of [0.0f,1.0f]
 */
public class Planet {
	protected float rotation; //angle of rotation on this planet's axis
	protected float orbit; //Planet's position in it's orbit (angle)
	protected float radius; //relative to earth
	protected float offset; //relative to earth's offset from the sun (1AU)
	private float rotateRate, orbitRate; //relative velocities to earth
	protected Vec3f color; // The color for each planet using RGB model
	protected Vec3f position; // Position of each planet using (x,y,z) model (Planets are drawn on the XZ plane
	protected Moon moon;	// The planets moon. (not implemented)
	protected PlanetEnum planetEnum; // The enum holding all the planets
	
	//all ratios are of earth's respective properties
	private static float[] radiusRatios = {0.382f, 0.949f, 1.000f, 0.532f, 11.209f, 9.449f, 4.007f, 3.883f};
	private static float[] rotateRatios = {58.60f, -243.0f, 1.000f, 1.030f, 0.410f, 0.430f, -0.720f, 0.670f};
	private static float[] orbitRatios = {4.1537f, 1.6254f, 1.0000f, 0.5319f, 0.0844f, 0.0339f, 0.0119f, 0.0061f};
	private static float[] offsetRatios = {0.3871f, 0.7233f, 1.000f, 1.524f, 5.203f, 9.582f, 19.22f, 30.05f};
	private static float[][] planetColors = {{0.76f,0.74f,0.74f}, {0.85f,0.70f,0.57f}, {0.39f,0.59f,0.66f}, {0.95f,0.38f,0.47f}, {0.75f,0.51f,0.22f}, {0.95f,0.81f,0.53f}, {0.58f,0.73f,0.75f}, {0.47f,0.62f,0.75f}};
	private final int EARTHINDEX = 2;

	
	/**
	 * Make a new planet based on the enum and place it at the origin
	 * @param p the enum
	 */
	public Planet(PlanetEnum p) {
		//init rotation and orbit
		this.rotation = 0; this.orbit = 0;
		PlanetEnum[] planets = PlanetEnum.values();
		// intialize Earth values first because all other planets property
		// values are calculated as a ratio from Earth's property values
		float earthRadius = radiusRatios[EARTHINDEX],
				earthOffset = offsetRatios[EARTHINDEX],
				earthRotateRate = rotateRatios[EARTHINDEX],
				earthOrbitRate = orbitRatios[EARTHINDEX];
		
		for(int planetIndex = 0; planetIndex < planets.length; planetIndex++) {
			if(planets[planetIndex] == p) { // Find the appropriate planet set values for
				//Set all the planet properties
				this.radius = earthRadius * radiusRatios[planetIndex];
				this.offset = earthOffset * offsetRatios[planetIndex];
				this.rotateRate = earthRotateRate * rotateRatios[planetIndex];
				this.orbitRate = earthOrbitRate * orbitRatios[planetIndex];
				this.color = new Vec3f(planetColors[planetIndex]);
				this.planetEnum = p;
				this.position = new Vec3f(0.0f,0.0f,0.0f); // All planets will have a postion at (0,0,0) Will be changed by the offset.
			}
		}
	}

	/**
	 * @return The planeEnum
	 */
	public PlanetEnum getEnum() {
		return planetEnum;
	}

	/**
	 * @param planetEnum Sets the planetEnum
	 */
	public void setPlanetEnum(PlanetEnum planetEnum) {
		this.planetEnum = planetEnum;
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
		float earthRadius = radiusRatios[EARTHINDEX],
				earthOffset = offsetRatios[EARTHINDEX],
				earthRotateRate = rotateRatios[EARTHINDEX],
				earthOrbitRate = orbitRatios[EARTHINDEX];
		for(int planetIndex = 0; planetIndex < planets.length; planetIndex++) {
			if(planets[planetIndex] == p) {
				this.radius = earthRadius * radiusRatios[planetIndex];
				this.offset = earthOffset * offsetRatios[planetIndex];
				this.rotateRate = earthRotateRate * rotateRatios[planetIndex];
				this.orbitRate = earthOrbitRate * orbitRatios[planetIndex];
				this.color = new Vec3f(planetColors[planetIndex]);
				this.planetEnum = p;
			}
		}
	}

	/**
	 * Create a planet with explicit values passed as arguments
	 * @param radius Radius of the planet (will determin planets size)
	 * @param offset Distance from the center of scene (center is at (0,0,0)
	 * @param orbitRate How fast the planet will rotate around the sun
	 * @param rotateRate How fast the planet wil rotate around its axis
	 * @param color The color of the planet
	 */
	public Planet(float radius, float offset, float orbitRate, float rotateRate, float[] color) {
		this.radius = radius;
		this.offset = offset;
		this.orbitRate = orbitRate;
		this.rotateRate = rotateRate;
		this.orbit = 0; this.rotation = 0;
		this.color = new Vec3f(color);
		this.position = new Vec3f(0.0f,0.0f,0.0f);
	}

	/**
	 * Create a planet with explicit values passed as arguments
	 * @param position Set the position where the planet will be located at
	 * @param radius Radius of the planet (will determin planets size)
	 * @param offset Distance from the center of scene (center is at (0,0,0)
	 * @param orbitRate How fast the planet will rotate around the sun
	 * @param rotateRate How fast the planet wil rotate around its axis
	 * @param color The color of the planet
	 */
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
	 * @param frameCount The current Frame
	 * @return The new angle the planet makes with the sun
	 */
	public float orbit(float frameCount) {
		if(this.planetEnum == null) return 0;
		float theta = frameCount * (this.orbitRate);
		return theta;
	}
	
	/**
	 * Rotates the planet on it's axis
	 * @param frameCount
	 * @return The angle the planet makes with it's axis of rotation
	 */
	public float rotate(float frameCount) {
		float theta = frameCount * this.rotateRate;
		return theta;
	}

	/*
	Series of setters and getters to provide encapsulation
	These methods will modify or return certain planet properties
	 */

	public void setX(float value) {
		this.position.setX(value);
	}
	
	public float getX() {
		return this.position.x();
	}
	
	public void setY(float value) {
		this.position.setY(value);
	}
	
	public float getY() {
		return this.position.y();
	}
	
	public float getZ() {
		return this.position.z();
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


