import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.paint.Color;

public class Testing {
	private static Point min = new Point(1,1,1);
	private static Point max = new Point(3,3,3);
	
	public static void main(String[] args) {
//		testBoundingBoxes();
		//testVectors();
		//testTriangleAABB();
		//testSphereAABB();
		//randomTesting();
//		triangleTesting();
//		kdTreeTesting();
//		Testing t = new Testing();
//		t.concurrentTesting();
		vectorTesting();
	}
	
	public static void vectorTesting() {
		Vector v1 = new Vector(0,0,0);
		Vector v2 = new Vector(2,2,2);
//		Vector v3 = v1 + v2;
	}
	
	public void concurrentTesting() {
		ExecutorService EXEC = Executors.newCachedThreadPool();
		CompletionService<Void> compService = new ExecutorCompletionService<>(EXEC);
//        ArrayList<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
        for (int i=0; i<10; i++) {
        	Task task = new Task(i);
        	compService.submit(task);
        }
        EXEC.shutdown();
	}
	
	private final class Task implements Callable<Void> {
		private int x;
		Task(int x) {
			this.x = x;
		}
		
		@Override public Void call() {
			return printLoop(x);
		}
	}
	
	private Void printLoop(int x) {
		for (int y=0; y<10; y++) {
			System.out.println(x);
		}
		return null;
	}
	
	public static void kdTreeTesting() {
		Point min = new Point(0,0,0);
		Point max = new Point(10,10,10);
		KdTree tree = new KdTree(min,max);
		ArrayList<Tracable> ls = new ArrayList<Tracable>();
		for (int i=0; i<10; i++) {
			Point p1 = new Point(1,10,0);
			Point p2 = new Point(1,5,0);
			Point p3 = new Point(5,5,0);
			Triangle t = new Triangle(p1,p2,p3);
			ls.add(t);
		}
		tree.build(ls, 1, 1);
		System.out.println(tree.getLeft());
		System.out.println(tree.getRight());
		System.out.println(tree.isLeaf());
		System.out.println(tree.getNumTracables());
	}
	
	public static void triangleTesting() {
		Point p1 = new Point(1,10,0);
		Point p2 = new Point(1,5,0);
		Point p3 = new Point(5,5,0);
		Triangle t = new Triangle(p1,p2,p3);
		Ray r = new Ray(new Point(1,10,-10), new Vector(0,0,1));
		double inter = t.getIntersect(r);
		System.out.println(inter);
	}
	
	public static void randomTesting() {
		double x_axis = 10;
		double z_axis = 10;
		double y_axis = 10;
		 double z = 1000;
	        Point p1 = new Point(x_axis/2-500,y_axis/2,z);
	        Point p2 = new Point(x_axis/2+500,y_axis/2,z);
	        Point p3 = new Point(x_axis/2,y_axis/2+1000,z);
	        Triangle triangle = new Triangle(p1,p2,p3);
	        triangle.setColor(Color.WHITE);
	        triangle.setReflectedPercent(0.8);
	        
	        System.out.println(triangle.getNormal());
	        Ray r = new Ray(new Point(x_axis/2-500,y_axis/2,0), new Vector(0,0,1));
	        
	        System.out.println(triangle.doesIntersect(r));
	        
	        Point intersection = r.getPoint(triangle.getIntersect(r));
	        System.out.println(intersection + " " + p1);
	        
	        Vector n = triangle.getNormal(intersection);
			Vector l = r.d();
			Vector reflectedDir = l.subtract(n.multiply(2*l.dot(n)));
			reflectedDir.normalise();
			
			Ray r2 = new Ray(intersection.add(new Point(reflectedDir)), reflectedDir);
			System.out.println(triangle.doesIntersect(r2) + " " + r2);
			
			Sphere s = new Sphere(new Point(x_axis/2-500,y_axis/2,-200), 10);
			System.out.println(s.doesIntersect(r2));
	}
	
	public static void testSphereAABB() {
		Point c = new Point(10,10,-10);
		double r = 5;
		Sphere s = new Sphere(c,(float)r);
		
		min = new Point(5,5,-15);
		max = new Point(15,15,-5);
		
		Ray r1 = new Ray(new Point(5,10,0), new Vector(0,0,-1));
		System.out.println(bigDumb(r1) + " : " + s.doesIntersect(r1));
	}
	
	public static void testTriangleAABB() {
		Point p1 = new Point(1,-30,0);
		Point p2 = new Point(4,-10,-3);
		Point p3 = new Point(5,-25,0);
		Triangle t1 = new Triangle(p1, p2, p3);
		
		float padding = 0.0001f;
//		Point min = new Point(1-padding,-10-padding,0-padding);
//		Point max = new Point(5+padding,-30+padding,-3+padding);
		min = new Point(1,-10,0);
		max = new Point(5,-30,-3);
		AABB box = new AABB(min,max);
		
		Ray r1 = new Ray(new Point(4,-13,-3), new Vector(0,0,1));
		System.out.println(bigDumb(r1) + " : " + t1.doesIntersect(r1));
	}
	
	public static void testVectors() {
		Vector up = new Vector(0,-1,0);
		Vector forward = new Vector(0,0,-1);
		
		Vector left = forward.cross(up);
		System.out.println(left);
		
		Vector up2 = left.cross(forward);
		System.out.println(up2);
		
		Vector right = up.cross(forward);
		System.out.println(up);
		
		
	}
	
	public static void testBoundingBoxes() {
		float zero = -0f;
        float f1 = 7f / zero;
        float f2 = -8f / zero;
        
        float divx = 1f / zero;
        float f3 = 7f * divx;
        float f4 = -8f * divx;
        
//        System.out.println(zero);
//        System.out.println(f1);
//        System.out.println(f2);
//        System.out.println(zero == 0);
//        System.out.println(zero == 0f);
        if (f1 > f2) {
        	System.out.println("Min: " + f2);
        	System.out.println("Max: " + f1);
        } else {
        	System.out.println("Min: " + f1);
        	System.out.println("Max: " + f2);
        }
        
        System.out.println();
        
//        System.out.println(divx);
//        System.out.println(f3);
//        System.out.println(f4);
//        System.out.println(divx == 0);
//        System.out.println(divx == 0f);
        if (divx >= 0) {
        	System.out.println("Min: " + f3);
        	System.out.println("Max: " + f4);
        } else {
        	System.out.println("Min: " + f4);
        	System.out.println("Max: " + f3);
        }
        
        System.out.println("\nIntersection Tests: (Everything should be true)");
        Ray r = new Ray(new Point(2,2,4), new Vector(-0f,-0f,-1f));
        Ray r2 = new Ray(new Point(2,2,-1), new Vector(-0f,-0f,1f));
        Ray r3 = new Ray(new Point(2,2,4), new Vector(0f,0f,-1f));
        Ray r4 = new Ray(new Point(2,2,-1), new Vector(0f,0f,1f));
        System.out.println("Naive: " + naive(r) + ", " + naive(r2) + ", " + naive(r3) + ", " + naive(r4));
        System.out.println("Smart: " + smart(r) + ", " + smart(r2) + ", " + smart(r3) + ", " + smart(r4));
        System.out.println("Big Dumb: " + bigDumb(r) + ", " + bigDumb(r2) + ", " + bigDumb(r3) + ", " + bigDumb(r4));
	}
	
	/**
	 * Scratch pixel method (Not actually naive as it turns out)
	 * @param r
	 * @return
	 */
	public static boolean naive(Ray r) {
		float tmin = (float) ((min.x() - r.o().x()) / (float)r.d().dx());
		float tmax = (float) ((max.x() - r.o().x()) / (float)r.d().dx());
		
		if (tmin > tmax) {
			float temp = tmax;
			tmax = tmin;
			tmin = temp;
		}
		
		float tymin = (float) ((min.y() - r.o().y()) / (float)r.d().dy());
		float tymax = (float) ((max.y() - r.o().y()) / (float)r.d().dy());
		
		if (tymin > tymax) {
			float temp = tymax;
			tymax = tymin;
			tymin = temp;
		}
		
		if ((tmin > tymax) || (tymin > tmax)) {return false;}
		
		if (tymin > tmin) {
			tmin = tymin;
		}
		
		if (tymax < tmax) {
			tmax = tymax;
		}
		
		float tzmin = (float) ((min.z() - r.o().z()) / (float)r.d().dz());
		float tzmax = (float) ((max.z() - r.o().z()) / (float)r.d().dz());
		
		if (tzmin > tzmax) {
			float temp = tzmax;
			tzmax = tzmin;
			tzmin = temp;
		}
		
		if ((tmin > tzmax) || (tzmin > tmax)) {return false;}
		
		if (tzmin > tmin) {
			tmin = tzmin;
		}
		
		if (tzmax < tmax) {
			tmax = tzmax;
		}
		
		return true;
	}
	
	/**
	 * From paper
	 * @param r
	 * @return
	 */
	public static boolean smart(Ray r) {
		float divx = 1f / (float) r.d().dx();
		float tmin, tmax;
		if (divx >= 0) {
			tmin = (float) ((min.x() - r.o().x()) * divx);
			tmax = (float) ((max.x() - r.o().x()) * divx);
		} else {
			tmax = (float) ((min.x() - r.o().x()) * divx);
			tmin = (float) ((max.x() - r.o().x()) * divx);
		}
		
		float divy = 1f / (float) r.d().dy();
		float tymin, tymax;
		if (divy >= 0) {
			tymin = (float) ((min.y() - r.o().y()) * divy);
			tymax = (float) ((max.y() - r.o().y()) * divy);
		} else {
			tymax = (float) ((min.y() - r.o().y()) * divy);
			tymin = (float) ((max.y() - r.o().y()) * divy);
		}
		
		if (tymin > tymax) {
			float temp = tymax;
			tymax = tymin;
			tymin = temp;
		}
		
		if ((tmin > tymax) || (tymin > tmax)) {return false;}
		
		if (tymin > tmin) {
			tmin = tymin;
		}
		
		if (tymax < tmax) {
			tmax = tymax;
		}
		
		float divz = 1f / (float) r.d().dz();
		float tzmin, tzmax;
		if (divz >= 0) {
			tzmin = (float) ((min.z() - r.o().z()) * divz);
			tzmax = (float) ((max.z() - r.o().z()) * divz);
		} else {
			tzmax = (float) ((min.z() - r.o().z()) * divz);
			tzmin = (float) ((max.z() - r.o().z()) * divz);
		}
		
		if ((tmin > tzmax) || (tzmin > tmax)) {return false;}
		
		if (tzmin > tmin) {
			tmin = tzmin;
		}
		
		if (tzmax < tmax) {
			tmax = tzmax;
		}
		
		return true;
	}
	 /**
	  * Also from paper but with problems addressed in paper
	  * @param r
	  * @return
	  */
	public static boolean bigDumb(Ray r) {
		float tmin, tmax;
		if ((float) r.d().dx() >= 0) {
			tmin = (float) ((min.x() - r.o().x()) / (float) r.d().dx());
			tmax = (float) ((max.x() - r.o().x()) / (float) r.d().dx());
		} else {
			tmax = (float) ((min.x() - r.o().x()) / (float) r.d().dx());
			tmin = (float) ((max.x() - r.o().x()) / (float) r.d().dx());
		}
		
		float tymin, tymax;
		if ((float) r.d().dy() >= 0) {
			tymin = (float) ((min.y() - r.o().y()) / (float) r.d().dy());
			tymax = (float) ((max.y() - r.o().y()) / (float) r.d().dy());
		} else {
			tymax = (float) ((min.y() - r.o().y()) / (float) r.d().dy());
			tymin = (float) ((max.y() - r.o().y()) / (float) r.d().dy());
		}
		
		if (tymin > tymax) {
			float temp = tymax;
			tymax = tymin;
			tymin = temp;
		}
		
		if ((tmin > tymax) || (tymin > tmax)) {return false;}
		
		if (tymin > tmin) {
			tmin = tymin;
		}
		
		if (tymax < tmax) {
			tmax = tymax;
		}
		
		float tzmin, tzmax;
		if ((float) r.d().dz() >= 0) {
			tzmin = (float) ((min.z() - r.o().z()) / (float) r.d().dz());
			tzmax = (float) ((max.z() - r.o().z()) / (float) r.d().dz());
		} else {
			tzmax = (float) ((min.z() - r.o().z()) / (float) r.d().dz());
			tzmin = (float) ((max.z() - r.o().z()) / (float) r.d().dz());
		}
		
		if ((tmin > tzmax) || (tzmin > tmax)) {return false;}
		
		if (tzmin > tmin) {
			tmin = tzmin;
		}
		
		if (tzmax < tmax) {
			tmax = tzmax;
		}
		
		return true;
	}
}
