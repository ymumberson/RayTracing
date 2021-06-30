import javafx.scene.paint.Color;

//Assuming flat in Y axis, and all 4 points are flat
public class Plane {
	private Point p1,p2,p3,p4;
	private Vector n = new Vector(0,1,0);
	
	public Plane(Point p1, Point p2, Point p3, Point p4) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
	}
	
//	public Color getColor(Point p) {
//		double length = Math.abs(p2.x() - p1.x());
//		int sqrLen = 10;
//		
//		if ((p.x()/sqrLen) % 2 == 0) {
//			if ((p.z()/sqrLen) % 2 == 0) {
//				return Color.BLACK;
//			} else {
//				return Color.WHITE;
//			}
//		} else {
//			if ((p.z()/sqrLen) % 2 == 0) {
//				return Color.WHITE;
//			} else {
//				return Color.BLACK;
//			}
//		}
//	}
	
	public boolean doesIntersect(Ray r) {
		
	}
}
