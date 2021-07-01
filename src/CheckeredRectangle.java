import javafx.scene.paint.Color;

public class CheckeredRectangle extends Rectangle {

	public CheckeredRectangle(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
		super(topLeft, topRight, bottomRight, bottomLeft);
	}
	
	public Color getColor(Point p) {
		//return new Color(1, 0.2, 0.3, 1);
		return Color.color(1, 0.2, 0.3, 1);
	}
	
	public static void main(String[] args) {
		CheckeredRectangle r = new CheckeredRectangle(new Point(0,0,0),new Point(0,0,0),new Point(0,0,0),new Point(0,0,0));
		System.out.println(r.getColor(null).getRed());
	}
}
