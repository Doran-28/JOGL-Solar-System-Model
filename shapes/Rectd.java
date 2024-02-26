//
package shapes;

/**
 * Creates a new rectangle using double coordinates with a colour
 * x1, y1 are the coordinates of the bottom-left vertex
 * x2, y2 are the coordinates of the top-right vertex
 * WARNING: THIS SUPPOSEDLY MAKES A CROSSWINDING PATTERN IN THE RECTS. CONFIRM LATER
 */
public class Rectd extends Shaped{
	protected double x1, y1;
	protected double x2, y2;
	
	public Rectd(double x1, double y1, double x2, double y2, float red, float green, float blue) {
		super();
		this.x1 = x1; this.y1 = y1;
		this.x2 = x2; this.y2 = y2;
		this.red = red; this.green = green; this.blue = blue;
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
}
