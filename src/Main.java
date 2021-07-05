import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
	private int x_axis = 640;
	private int y_axis = 480;
	private int z_axis = 400;
	private ArrayList<Tracable> tracableObjects = new ArrayList<Tracable>();
	private Point light;
    private Point camera;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		WritableImage img = new WritableImage(x_axis, y_axis);
		ImageView imgView = new ImageView(img);
		
		FlowPane root = new FlowPane();
		root.setVgap(8);
        root.setHgap(4);
        
        VBox vb = new VBox();
        Slider lightSlider = new Slider(0,x_axis,0);
        vb.getChildren().addAll(imgView,lightSlider);
        root.getChildren().addAll(vb);
        
        lightSlider.valueProperty().addListener( 
    	        new ChangeListener<Number>() { 
    				public void changed(ObservableValue <? extends Number >  
    					observable, Number oldValue, Number newValue) 
    	        { 
    				render(img,newValue.intValue());
    	        } 
    	    });
        
        render(img,0);
        
        Scene scene = new Scene(root, x_axis, y_axis+20);
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	public void render(WritableImage img, int lightX) {
		int w=(int) img.getWidth(), h=(int) img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
        
        tracableObjects.add(new Sphere(new Point(w/4, h/2, 100), 100));
        tracableObjects.add(new Sphere(new Point((w/4)*3, h/2, 100), 100));
        int floorHeight = y_axis;
        tracableObjects.add(new Rectangle(new Point(0,floorHeight,z_axis+1000), 
				new Point(x_axis,floorHeight,z_axis+1000),
				new Point(x_axis,floorHeight,0),
				new Point(0,floorHeight,0)));
        //tracableObjects.add(new Sphere(new Point((w/2), (h/3)*1.5f, 300), 100));
        tracableObjects.add(new Sphere(new Point((w/2), (h/3)*1f, 350), 120));
        
        tracableObjects.get(0).setColor(Color.color(0.5,0.5,0),Color.color(1,1,0),Color.color(1,1,0));
        tracableObjects.get(1).setColor(Color.color(0,0.67,0),Color.color(0,1,0),Color.color(1,0.3,0));
        tracableObjects.get(2).setColor(Color.CADETBLUE,Color.CADETBLUE,Color.CADETBLUE);
        //tracableObjects.get(3).setColor(Color.color(0.4,0,0),Color.color(0.6,0,0),Color.color(0.5,0,0));
        tracableObjects.get(3).setColor(Color.color(1,1,1),Color.color(1,1,1),Color.color(1,1,1));
        
        tracableObjects.get(0).setReflectedAmount(0.2);
        tracableObjects.get(1).setReflectedAmount(0.2);
        tracableObjects.get(3).setReflectedAmount(0.5);
        
        light = new Point(lightX,0,0);
        camera = new Point(w/2.0f,h/2.0f,-1000f);
        
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
        		
        		// Creating the ray //
        		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
        		rayVec.normalise();
        		Ray r = new Ray(new Point(i,j,0), rayVec); //Persepective projection
        		//Ray r = new Ray(new Point(i,j,0), new Vector(0,0,1)); //Authnographic projection
        		image_writer.setColor(i, j, trace(r,4));
        	}
        }
	}
	
	public Color trace(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {return Color.BLUE;} //Default colour for reaching max recursive depth
		
		Point intersection;
		double ambient = 0.3;
		Vector lnorm;
        Vector snorm;
		
		// Finding the first object intersected //
		Tracable currentTracable = tracableObjects.get(0);
		double t = -1;
		for (Object o: tracableObjects) {
			Tracable temp = (Tracable) o;
			double tempT = temp.getIntersect(r);
//			if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
			if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
				//System.out.println("replacing t:" + t + " with tempT:" + tempT);
				t = tempT;
				currentTracable = temp;
			}
		}
		
		// Ray tracing //
		if (t >= 0) {//Accounting for if rays intersect no objects
			intersection = r.getPoint((float)t);
			double diffuse = 0;
			double specular = 0;
			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
			toLightV.normalise();
			Ray toLightR = new Ray(intersection, toLightV);
			
			boolean inShadow = false;
			for (Object o: tracableObjects) {
    			if (o != currentTracable) {
    				Tracable temp = (Tracable) o;
    				if (temp.getIntersect(toLightR) >= 0) {
    					//System.out.println("In shadow!" + o);
    					inShadow = true;
    				}
    			}
    		}
			
			if (!inShadow) {
				//System.out.println("Not in shadow!");
				lnorm = new Vector(light.subtract(intersection));
				lnorm.normalise();
				snorm = currentTracable.getNormal(intersection);
				snorm.normalise();
				diffuse = (float) Math.max(0.0, snorm.dot(lnorm));
				
				Vector r1 = snorm.multiply(2*snorm.dot(lnorm.multiply(1))).subtract(lnorm.multiply(1));
				Vector e = new Vector(r.o().subtract(intersection)); e.normalise();
				Float n = 100f;
				double cosTheta = r1.dot(e);
				specular = (cosTheta > 0.0) ? Math.max(0.0, Math.pow((r1.dot(e)), n)) : 0;
			}
			
			Color traceAmb = currentTracable.getAmbient();
			Color traceDif = currentTracable.getDiffuse();
			Color traceSpec = currentTracable.getSpecular();
			
			double red = traceAmb.getRed()*ambient 
					+ traceDif.getRed()*diffuse + traceSpec.getRed()*specular;
			double green = traceAmb.getGreen()*ambient 
					+ traceDif.getGreen()*diffuse + traceSpec.getGreen()*specular;
			double blue = traceAmb.getBlue()*ambient 
					+ traceDif.getBlue()*diffuse + traceSpec.getBlue()*specular;
			
			// Calculating Reflections //
			if (currentTracable.isReflective()) {
				//System.out.println("There's a reflection!!!!");
				double reflectiveAmount = currentTracable.getReflectedAmount();
				double mattPercent = 1.0 - reflectiveAmount;
				red *= mattPercent;
				green *= mattPercent;
				blue *= mattPercent;
				
				Vector n = currentTracable.getNormal(intersection);
				Vector l = r.d();
				Vector reflectedDir = l.subtract(n.multiply(2*l.dot(n)));
				reflectedDir.normalise();
				
				//System.out.println(reflectedDir);
				
				Ray reflectedRay = new Ray(intersection, reflectedDir);
				Color reflectionCol = trace(reflectedRay, recursiveDepth-1);
				
				red += reflectiveAmount * reflectionCol.getRed();
				green += reflectiveAmount * reflectionCol.getGreen();
				blue += reflectiveAmount * reflectionCol.getBlue();
			}
			
			if (green>1) {green=1;} if (green<0) {green=0;} 
			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
			if (red>1) {red=1;} if (red<0) {red=0;}
			
			return Color.color(red,green,blue,1.0);
		} else {
			return Color.BLACK; //ie no intersections
		}		
	}
	
	/**
	 * Old code
	 * -> Just for testing / reference
	 * @param img
	 * @param lightX
	 */
	public void renderExperimental(WritableImage img, int lightX) {
		int w=(int) img.getWidth(), h=(int) img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
        
        Sphere s1 = new Sphere(new Point(w/4, h/2, 100), 100);
        Sphere s2 = new Sphere(new Point((w/4)*3, h/2, 100), 100);
        //Triangle t1 = new Triangle(new Point(10,20,50), new Point(200,10,50), new Point(200,400,50));
        Triangle t1 = new Triangle(new Point(10,20,20), new Point(200,10,50), new Point(200,400,50));
        //Triangle t1 = new Triangle(new Point(0,300,400), new Point(640,300,0), new Point(0,300,0));
        
        //Rectangle rect1 = new Rectangle(new Point(500,400,100), new Point(600,450,100));
        //Rectangle rect1 = new Rectangle(new Point(480,400,50), new Point(600,450,100));
        int floorHeight = y_axis;
        Rectangle rect1 = new Rectangle(new Point(0,floorHeight,z_axis+1000), 
        								new Point(x_axis,floorHeight,z_axis+1000),
        								new Point(x_axis,floorHeight,0),
        								new Point(0,floorHeight,0)); //floor
        
        Point intersection;
        Point light = new Point(lightX,0,0);
        Vector lnorm;
        Vector snorm;
        Point camera = new Point(w/2.0f,h/2.0f,-1000f);
        
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
        		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
        		rayVec.normalise();
        		//System.out.println(rayVec);
        		
        		Ray r = new Ray(new Point(i,j,0), rayVec); //Persepective projection
        		//Ray r = new Ray(new Point(i,j,0), new Vector(0,0,1)); //Authnographic projection
        		
        		double col = 0;
        		double ambient = 0.3;
        		if (s1.doesIntersect(r)) {
        			intersection = r.getPoint((float)s1.getIntersect(r));
        			double diffuse = 0;
        			double specular = 0;
        			
        			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
        			toLightV.normalise();
        			Ray toLightR = new Ray(intersection, toLightV);
        			if (!s2.doesIntersect(toLightR) || s2.getIntersect(toLightR) < 0 ) {
        				lnorm = new Vector(light.subtract(intersection));
        				lnorm.normalise();
        				snorm = new Vector(intersection.subtract(s1.c()));
        				snorm.normalise();
        				diffuse = (float) Math.max(0.0, snorm.dot(lnorm));
        				//col = snorm.dot(lnorm) + ambient;
        			
        				//Vector r1 = new Vector(r.o().subtract(intersection)); r1.normalise();
        				//Vector r1 = lnorm.subtract(snorm.multiply(lnorm.multiply(1).dot(snorm)*2)); r1.normalise();
        				//Vector r1 = lnorm.subtract(snorm.multiply((2*(lnorm.multiply(-1).dot(snorm))))); r1.normalise();
        				Vector r1 = snorm.multiply(2*snorm.dot(lnorm.multiply(1))).subtract(lnorm.multiply(1));
        				//Vector r1 = lnorm.subtract(snorm.multiply(2*snorm.dot(lnorm.multiply(1))));
        				Vector e = new Vector(r.o().subtract(intersection)); e.normalise();
        				Float n = 10f;
        				double cosTheta = r1.dot(e);
        				specular = (cosTheta > 0.0) ? Math.max(0.0, Math.pow((r1.dot(e)), n)) : 0;
        			}
        			
        			double red = ambient + diffuse + specular;
        			double green = specular;
        			double blue = specular;
        			if (green>1) {green=1;} if (green<0) {green=0;} 
        			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
        			if (red>1) {red=1;} if (red<0) {red=0;}
        			
        			//image_writer.setColor(i, j, Color.color(col,0,0,1.0));
        			image_writer.setColor(i, j, Color.color(red,green,blue,1.0));
        		} else if (s2.doesIntersect(r)) {
        			intersection = r.getPoint((float)s2.getIntersect(r));
        			double diffuse = 0;
        			double specular = 0;
        			
        			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
        			toLightV.normalise();
        			Ray toLightR = new Ray(intersection, toLightV);
        			if (!s1.doesIntersect(toLightR) || s1.getIntersect(toLightR) < 0 ) {
        				lnorm = new Vector(light.subtract(intersection));
        				lnorm.normalise();
        				snorm = new Vector(intersection.subtract(s2.c()));
        				snorm.normalise();
        				diffuse = (float) Math.max(0.0, snorm.dot(lnorm));
        			
        				Vector r1 = snorm.multiply(2*snorm.dot(lnorm.multiply(1))).subtract(lnorm.multiply(1));
        				Vector e = new Vector(r.o().subtract(intersection)); e.normalise();
        				Float n = 10f;
        				double cosTheta = r1.dot(e);
        				specular = (cosTheta > 0.0) ? Math.max(0.0, Math.pow((r1.dot(e)), n)) : 0;
        			}
        				
        			double red = specular;
        			double green = ambient + diffuse + specular;
        			double blue = ambient + diffuse + specular;
        			if (green>1) {green=1;} if (green<0) {green=0;} 
        			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
        			if (red>1) {red=1;} if (red<0) {red=0;}
        			image_writer.setColor(i, j, Color.color(red,green,blue,1.0));
        		} else if (t1.doesIntersect(r)) {
        			image_writer.setColor(i, j, Color.BLUE);
        		} else if (rect1.doesIntersect(r)) {
        			image_writer.setColor(i, j, Color.YELLOW);
        		} else {
        			image_writer.setColor(i, j, Color.BLACK);
        		}
        	}
        }
        //System.out.println(s1);
        
//        System.out.println("\nTesting:");
//        //Ray r = new Ray(new Point(200,400,0), new Vector(0,0,1));
//        
//        System.out.println("Test 1");
//        Ray r1 = new Ray(new Point(10,20,0), new Vector(0,0,1));
//        t1.doesIntersect(r1);
//        
//        System.out.println("\nTest 2");
//        Ray r2 = new Ray(new Point(200,10,0), new Vector(0,0,1));
//        t1.doesIntersect(r2);
//        
//        System.out.println("\nTest 3");
//        Ray r3 = new Ray(new Point(200,400,0), new Vector(0,0,1));
//        t1.doesIntersect(r3);
//        
//        System.out.println("\nTest 4");
//        Ray r4 = new Ray(new Point(210,410,0), new Vector(0,0,1));
//        t1.doesIntersect(r4);
	}
}

