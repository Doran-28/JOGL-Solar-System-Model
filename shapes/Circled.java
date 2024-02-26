package shapes;
public class Circled extends Shaped{
	private double x, y;
	private float radius;
	public Circled(double x, double y, float radius, float r, float g, float b) {
		super();
		this.x = x; this.y = y;
		this.radius = radius;
		this.red = r; this.green = g; this.blue = b;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	
	
}
