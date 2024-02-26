//
package shapes;

/**
 * Creates a new line using double coordinates with a colour
 * x1, y1 are the coordinates of the left vertex
 * x2, y2 are the coordinates of the right vertex
 */
public class Lined extends Shaped{
	protected double x1, y1;
	protected double x2, y2;
	protected int algo;
	
	public static final int ALGO_NONE = 0,
			ALGO_DDA = 1,
			ALGO_BRESENHAM = 2;
	
	/**
	 * Creates a new Lined using one of three of the algo constants
	 * @param x1 The first endpoint x value
	 * @param y1 The first endpoint y value
	 * @param x2 The second endpoint x value
	 * @param y2 The second endpoint y value
	 * @param algo either ALGO_NONE, ALGO_DDA, or ALGO_BRESENHAM
	 */
	public Lined(double x1, double y1, double x2, double y2, int algo) {
		super();
		this.x1 = x1; this.y1 = y1;
		this.x2 = x2; this.y2 = y2;
		this.algo = algo;
		switch(algo) {
		case ALGO_NONE:
			this.red = 1.0f; this.green = 1.0f; this.blue = 1.0f;
			break;
		case ALGO_DDA:
			this.red = 1.0f; this.green = 0.0f; this.blue = 0.0f;
			break;
		case ALGO_BRESENHAM:
			this.red = 0.0f; this.green = 0.0f; this.blue = 1.0f;
			break;
		}		
	}
	
	/**
	 * Creates a new Lined with specified colour
	 * @param x1 The first endpoint x value
	 * @param y1 The first endpoint y value
	 * @param x2 The second endpoint x value
	 * @param y2 The second endpoint y value
	 * @param red
	 * @param green
	 * @param blue
	 */
	public Lined(double x1, double y1, double x2, double y2, float red, float green, float blue) {
		super();
		this.x1 = x1; this.y1 = y1;
		this.x2 = x2; this.y2 = y2;
		this.red = red; this.green = green; this.blue = blue;	
	}
	
	@Override
	public String toString() {
		return ("Line{("+getX1()+","+getY1()+"),("+getX2()+","+getY2()+")}");
	}
	
	public double getX1() {
		return x1;
	}
	public void setX1(double x1) {
		this.x1 = x1;
	}
	public double getY1() {
		return y1;
	}
	public void setY1(double y1) {
		this.y1 = y1;
	}
	public double getX2() {
		return x2;
	}
	public void setX2(double x2) {
		this.x2 = x2;
	}
	public double getY2() {
		return y2;
	}
	public void setY2(double y2) {
		this.y2 = y2;
	}

	public int getAlgo() {
		return algo;
	}

	public void setAlgo(int algo) {
		this.algo = algo;
	}
}
