import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;

public class Main extends Application {
	//Dimensions used to be 640,480,400
	private int x_axis = 1920;
	private int y_axis = 1080;
	private int z_axis = 1000;
	private ArrayList<Tracable> tracableObjects = new ArrayList<Tracable>();
	private Point light;
    private Point camera;
    private AABB bb; //Bounding box surrounding the scene used as background (well it will be anyway)
    private int MAX_RECURSIVE_DEPTH = 8;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		WritableImage img = new WritableImage(x_axis, y_axis);
		ImageView imgView = new ImageView(img);
		
		Point min = new Point(-1,-1,-1);
		Point max = new Point(x_axis+1, y_axis+1, z_axis+1);
		bb = new AABB(min,max);
		//System.out.println(bb);
		
		FlowPane root = new FlowPane();
		root.setVgap(8);
        root.setHgap(4);
        
        VBox vb = new VBox();
        Slider lightSlider = new Slider(0,x_axis,0);
        Button lightButton = new Button("Start");
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
        
        lightButton.setOnMouseClicked(e -> {
        	for (int i=0; i<= x_axis; i++) {
        		render(img,i);
        	}
        });
        
        traceSampleScene(img,MAX_RECURSIVE_DEPTH);
        //traceBunny(img, MAX_RECURSIVE_DEPTH);
        
        Scene scene = new Scene(root, x_axis, y_axis+20);
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	public void traceBunny(WritableImage img, int recursiveCutoff) {
		initialiseBunnyTracables();
		render(img,0);
	}
	
	public void initialiseBunnyTracables() {
		File f = new File("BunnyTest.ply");
		Scanner in;
		try {
			in = new Scanner(f);
			
			//Skip 17 lines
			for (int i=0; i<17; i++) {
				in.nextLine();
			}
			int numVertex = Integer.valueOf(in.nextLine().split(" ")[2]);
			//System.out.println(numVertex);
			
			//Skip 6 lines
			for (int i=0; i<6; i++) {
				in.nextLine();
			}
			
			float scalar = 10000;
			if (numVertex >= 3) {
				String[] str = in.nextLine().split(" ");	
				Point p1 = new Point(
						Float.parseFloat(str[0])*scalar,
						Float.parseFloat(str[1])*scalar,
						Float.parseFloat(str[2])*scalar);
				str = in.nextLine().split(" ");	
				Point p2 = new Point(
						Float.parseFloat(str[0])*scalar,
						Float.parseFloat(str[1])*scalar,
						Float.parseFloat(str[2])*scalar);
				str = in.nextLine().split(" ");	
				Point p3 = new Point(
						Float.parseFloat(str[0])*scalar,
						Float.parseFloat(str[1])*scalar,
						Float.parseFloat(str[2])*scalar);
				Triangle previousTriangle = new Triangle(p1,p2,p3);
				Triangle temp1 = new Triangle(p1,p2,p3);
				temp1.setColor(Color.RED);
				tracableObjects.add(temp1);
				
				for (int i=0; i<(numVertex)-3; i++) {
					str = in.nextLine().split(" ");	
					Point p = new Point(
						Float.parseFloat(str[0])*scalar,
						Float.parseFloat(str[1])*scalar,
						Float.parseFloat(str[2])*scalar);
					Point[] prevPoints = previousTriangle.getLastTwoPoint();
					Triangle t1 = new Triangle(prevPoints[0], prevPoints[1], p);
					t1.setColor(Color.RED);
					tracableObjects.add(t1);
					previousTriangle = new Triangle(prevPoints[0], prevPoints[1], p);
					//System.out.println(p);
				}
				System.out.println("Triangles Added To Scene!");
			}
			
			
			
//			for (int i=0; i<numVertex; i++) {
//				String[] str = in.nextLine().split(" ");	
//				Point p = new Point(
//						Float.parseFloat(str[0]),
//						Float.parseFloat(str[1]),
//						Float.parseFloat(str[2]));
//				System.out.println(p);
//			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void traceSampleScene(WritableImage img, int recursiveCutoff) {
		initialiseSampleSceneTracables();
        render(img,recursiveCutoff);
	}
	
	public void initialiseSampleSceneTracables() {
		int w = x_axis;
		int h = y_axis;
		
		int floorHeight = y_axis;
		//Left Sphere
        Sphere s1 = new Sphere(new Point(w/4, h/2, z_axis/3), 200);
        s1.setColor(Color.color(0.5,0.5,0),Color.color(1,1,0),Color.color(1,1,0));
        s1.setReflectedPercent(0.4);
        
        //Right sphere
        Sphere s2 = new Sphere(new Point((w/4)*3, h/2, z_axis/3), 200);
        //int len = 150;
        //Box s2 = new Box(new Point((w/4)*3-len, h/2-len, z_axis/3-len), new Point((w/4)*3+len, h/2+len, z_axis/3+len));
        s2.setColor(Color.color(0,0.67,0),Color.color(0,1,0),Color.color(1,0.3,0));
        s2.setReflectedPercent(0.2);
        
        Rectangle floor = new Rectangle(new Point(0,floorHeight,z_axis+1000), 
				new Point(x_axis,floorHeight,z_axis+1000),
				new Point(x_axis,floorHeight,0),
				new Point(0,floorHeight,0));
        floor.setColor(Color.CADETBLUE,Color.CADETBLUE,Color.CADETBLUE);
        
        Sphere mirrorSphere = new Sphere(new Point((w/2), (h/3)*1f, z_axis*3/4), 300);
        mirrorSphere.setColor(Color.color(1,1,1),Color.color(1,1,1),Color.color(1,1,1));
        mirrorSphere.setReflectedPercent(0.9);
        
        Sphere refractiveSphere = new Sphere(new Point((w/2), (h/2)*1.4f, 150), 150);
        refractiveSphere.setColor(Color.color(1,1,1),Color.color(1,1,1),Color.color(1,1,1));
        refractiveSphere.setReflectedPercent(0.1);
        refractiveSphere.setRefractedPercent(0.9);
        refractiveSphere.setRefractiveIndex(1.5);
        //System.out.println(refractiveSphere);
        
        tracableObjects.add(s1);
        tracableObjects.add(s2);
        tracableObjects.add(floor);
        tracableObjects.add(mirrorSphere);
        tracableObjects.add(refractiveSphere);
	}
	
	public void render(WritableImage img, int lightX) {
		int w=(int) img.getWidth(), h=(int) img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
        
        light = new Point(lightX,0,0);
        camera = new Point(w/2.0f,h/2.0f,-1000f);
        
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
        		
        		// Creating the ray //
        		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
        		rayVec.normalise();
        		Ray r = new Ray(new Point(i,j,0), rayVec); //Persepective projection
        		//Ray r = new Ray(new Point(i,j,0), new Vector(0,0,1)); //Authnographic projection
        		image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH));
        	}
        	//System.out.println("Row " + (j+1) + "/" + h + " completed!");
        }
	}
	
	public Color trace(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {
			//System.out.println("Recursive depth cut-off reached"); 
			return Color.BLUE;
		} //Default colour for reaching max recursive depth
		
		Point intersection;
		double ambient = 0.3;
		Vector lnorm;
        Vector snorm;
		
		// Finding the first object intersected //
		Tracable currentTracable = tracableObjects.get(0);
		double t = -1;
		for (Object o: tracableObjects) {
			Tracable temp = (Tracable) o;
			double tempT = temp.getIntersect(r)[0];
//			if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
			if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
				//System.out.println("replacing t:" + t + " with tempT:" + tempT);
				t = tempT;
				currentTracable = temp;
			}
		}
		
		// Ray tracing //
		if (t >= 0) {//Accounting for if rays intersect no objects
		//if ((recursiveDepth == MAX_RECURSIVE_DEPTH && t >= 0) || (recursiveDepth != MAX_RECURSIVE_DEPTH && t > 0)) {
//			if (recursiveDepth != MAX_RECURSIVE_DEPTH && Math.abs(t) < 0.00001) {
//				System.out.println(t);
//				t = currentTracable.getIntersect(r)[1];
//			}
			intersection = r.getPoint(t);
//			if (intersection.equals(r.o())) {
//				intersection = r.getPoint((t+1)); System.out.println(String.valueOf(t));
//			}
			double diffuse = 0;
			double specular = 0;
			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
			toLightV.normalise();
			Ray toLightR = new Ray(intersection, toLightV);
			
			boolean inShadow = false;
			for (Object o: tracableObjects) {
    			if (o != currentTracable) {
    				Tracable temp = (Tracable) o;
    				if (temp.getIntersect(toLightR)[0] >= 0) {
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
				double reflectiveAmount = currentTracable.getReflectedPercent();
				double mattPercent = currentTracable.getMattPercent();
				red *= mattPercent;
				green *= mattPercent;
				blue *= mattPercent;
				
				Vector n = currentTracable.getNormal(intersection);
				Vector l = r.d();
				Vector reflectedDir = l.subtract(n.multiply(2*l.dot(n)));
				reflectedDir.normalise();
				
				//System.out.println(recursiveDepth-1);
				
				Ray reflectedRay = new Ray(intersection, reflectedDir);
				Color reflectionCol = trace(reflectedRay, recursiveDepth-1);
				
				red += reflectiveAmount * reflectionCol.getRed();
				green += reflectiveAmount * reflectionCol.getGreen();
				blue += reflectiveAmount * reflectionCol.getBlue();
			}
			
			//Calculating refraction //
//			if (currentTracable.isRefractive()) {
//				Vector n = currentTracable.getNormal(intersection);
//				Vector i = r.d();
//				double c1 = n.dot(i);
//				if (c1 < 0) c1 = -c1;
//				double snell = 1/currentTracable.getRefractiveIndex();
//				double c2 = Math.sqrt(1-(snell*snell)*((1-c1*c1)*(1-c1*c1)));
//				System.out.println(c2);
//				Vector out = i.multiply(snell).add(n.multiply(snell*c1-c2));
//				out.normalise();
//				Ray refractedRay = new Ray(intersection, out);
//				//System.out.println(refractedRay);
//				
//				Color refractionColor = trace(refractedRay,recursiveDepth-1);
//				
//				double mattPerc = currentTracable.getMattPercent();
//				double refracPerc = currentTracable.getRefractedPercent();
//				
//				red = red*mattPerc + refracPerc*refractionColor.getRed();
//				green = green*mattPerc + refracPerc*refractionColor.getGreen();
//				blue = blue*mattPerc + refracPerc*refractionColor.getBlue();
//			}
			
			//Refraction v1
			if (currentTracable.isRefractive()) {
				Vector n = currentTracable.getNormal(intersection);
				Vector i = r.d();
				double nDotI = n.dot(i);
				double cosi = Math.max(Math.min(1, nDotI),-1);
				double etai = 1, etat = currentTracable.getRefractiveIndex();
				
				if (cosi < 0) {
					cosi = -cosi;
				} else {
					n = n.multiply(-1);
					double temp = etai;
					etai = etat;
					etat = temp;
				}
				double eta = etai/etat;
				double k = 1 - eta*eta*(1-cosi*cosi);
				
				//if (recursiveDepth == 0) System.out.println(k);
				
				if (k < 0) {
					System.out.println("Total internal refraction");
				} else {
					Vector out = n.multiply((eta * cosi - Math.sqrt(k))).add(i.multiply(eta));
					out.normalise();
					
					Ray refractedRay = new Ray(intersection, out);
					//if (intersection == r.o()) { //Accounts for rounding error, advances point forward if is same as current ray origin
						Point p = intersection.add(new Point(r.d()));
						refractedRay = new Ray(p, out);
					//}
					
//					double[] tRefractArr = currentTracable.getIntersect(refractedRay);
//					double tRefract = (tRefractArr[0] < 0 && tRefractArr.length > 1 &&
//							!Double.isInfinite(tRefractArr[1]) && tRefractArr[1] >= tRefractArr[0]) ? tRefractArr[1] : tRefractArr[0];
//					if (tRefract >= 0) {
//						//System.out.println("Ray exits the object! (3D)\n-> " + tRefractArr[0] + " " + tRefractArr[1]);
//						//System.out.println("Ray exits the object! (3D)\n-> " + refractedRay);
//						Point intersectionRefract = refractedRay.getPoint((float)tRefract);
//						n = currentTracable.getNormal(intersectionRefract);
//						i = out;
//						nDotI = n.dot(i);
//						cosi = Math.max(Math.min(1, nDotI),-1);
//						etai = 1;
//						etat = currentTracable.getRefractiveIndex();
//						
//						if (cosi < 0) {
//							cosi = -cosi;
//						} else {
//							n = n.multiply(-1);
//							double temp = etai;
//							etai = etat;
//							etat = temp;
//						}
//						eta = etai/etat;
//						k = 1 - eta*eta*(1-cosi*cosi);
//						
//						if (k >= 0) {
//							Vector out2 = (n.multiply((eta * nDotI - Math.sqrt(k))).add(i.multiply(eta)));
//							out.normalise();
//							refractedRay = new Ray(intersectionRefract, out2);
//						}
//						
//					} else {
//						//System.out.println("Ray does not exit the object! (2D)\n-> " + tRefract);
//						//System.out.println("Ray does not exit the object! (2D)\n-> " + tRefractArr[0] + " " + tRefractArr[1]);
//						System.out.println("Ray does not exit the object! (2D)" + "t:" + tRefract + "\n-> " + refractedRay);
//						//System.out.println("Ray does not exit the object! (2D)" + intersection);
//					}
					
					//System.out.println(recursiveDepth-1);
					//if (recursiveDepth < MAX_RECURSIVE_DEPTH*3/4) System.out.println(r);
					Color refractionColor = trace(refractedRay,recursiveDepth-1);
					
					double mattPerc = currentTracable.getMattPercent();
					double refracPerc = currentTracable.getRefractedPercent();
					
					red = red*mattPerc + refracPerc*refractionColor.getRed();
					green = green*mattPerc + refracPerc*refractionColor.getGreen();
					blue = blue*mattPerc + refracPerc*refractionColor.getBlue();
				}
			} //End of refraction
			
			if (green>1) {green=1;} if (green<0) {green=0;} 
			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
			if (red>1) {red=1;} if (red<0) {red=0;}
			
			return Color.color(red,green,blue,1.0);
		} else {
			//return Color.BLACK; //ie no intersections
			double[] temp = bb.getIntersect(r);
			t = (temp[0] < 0 && temp.length > 1 && temp[1] >= 0) ? temp[1] : temp[0];
			if (t >= 0) {
				//System.out.println("Hit the box " + t);
				Point intersect = r.getPoint((float)t);
				return bb.getColor(intersect);
			} else {
				System.out.println("uhhh I guess it missed the box?" + t);
				return Color.BLACK;
			}
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
        			intersection = r.getPoint((float)s1.getIntersect(r)[0]);
        			double diffuse = 0;
        			double specular = 0;
        			
        			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
        			toLightV.normalise();
        			Ray toLightR = new Ray(intersection, toLightV);
        			if (!s2.doesIntersect(toLightR) || s2.getIntersect(toLightR)[0] < 0 ) {
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
        			intersection = r.getPoint((float)s2.getIntersect(r)[0]);
        			double diffuse = 0;
        			double specular = 0;
        			
        			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
        			toLightV.normalise();
        			Ray toLightR = new Ray(intersection, toLightV);
        			if (!s1.doesIntersect(toLightR) || s1.getIntersect(toLightR)[0] < 0 ) {
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

