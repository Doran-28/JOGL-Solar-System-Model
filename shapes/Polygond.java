package shapes;

import java.util.ArrayList;

public class Polygond extends Shaped {
	protected ArrayList<int[]> coordinates;

	public Polygond(ArrayList<int[]> coordinates) {
		super();
		this.red = 1.0f; this.green = 0.0f; this.blue = 0.0f;
		this.coordinates = coordinates;
	}
	
	public void setCoordinates(ArrayList<int[]> coordinates) {
		this.coordinates = coordinates;
	}
	
	public ArrayList<int[]> getCoordinates() {
		return coordinates;
	}
	

}
