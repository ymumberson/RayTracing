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
	private int x_axis = 640; private int y_axis = 480; private int z_axis = 400;
//	private int x_axis = 1920; private int y_axis = 1080; private int z_axis = 1000;
	
	private ArrayList<Tracable> tracableObjects = new ArrayList<Tracable>();
	private Point light;
    private Point camera;
    private AABB bb; //Bounding box surrounding the scene used as background (well it will be anyway)
    private int MAX_RECURSIVE_DEPTH = 7;
    private KdTree tree;
    private long startTime;
    private long lastDuration = 0;
	
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
		tree = new KdTree(bb.getMin(),bb.getMax());
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
        
//        traceSampleScene(img,0);
        traceBunny(img, 0);
        //renderExperimental(img, 0);
        
        Scene scene = new Scene(root, x_axis, y_axis+20);
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	
	public void traceBunny(WritableImage img, int recursiveCutoff) {
		double lightX = (x_axis-1)*0.9;
		initialiseBunnyTracables();
		
		
		startTimer("Building Tree.");
		tree.build(tracableObjects, 20, 3);
		finishTimer();
		System.out.println(tree.getNumTracables());
		
		startTimer("Rendering Scene.");
		render(img,(int)lightX);
		finishTimer();
	}
	
	public void initialiseBunnyTracables() {
		//File f = new File("BunnyTest.ply");
		File f = new File("dragon_vrip.ply"); // @ 871414 triangles
//		File f = new File("bun_zipper.ply"); //Very high res @ 69451 triangles
//		File f = new File("bun_zipper_res2.ply"); //High res @ 16301 triangles
//		File f = new File("bun_zipper_res3.ply"); //Medium res @ 3851 triangles
//		File f = new File("bun_zipper_res4.ply"); //Low res @ 948 triangles
		Scanner in;
		try {
			in = new Scanner(f);
			
			//Skip 17 lines
			int numLinesSkip = 3;
			for (int i=0; i<numLinesSkip; i++) {
				in.nextLine();
			}
			int numVertex = Integer.valueOf(in.nextLine().split(" ")[2]);
			System.out.println("number of Vertices: " + numVertex);
			
			//Skip 6 lines
//			int numLinesSkip2 = 5; //For bunny
			int numLinesSkip2 = 3; //For dragon
			for (int i=0; i<numLinesSkip2; i++) {
				in.nextLine();
			}
			
			int numTriangles = Integer.valueOf(in.nextLine().split(" ")[2]);
			System.out.println("Number of Triangles: " + numTriangles);
			
			int numLinesSkip3 = 2;
			for (int i=0; i<numLinesSkip3; i++) {
				in.nextLine();
			}
			
			Point[] pointList = new Point[numVertex];
			double[] colourList = new double[numVertex];
			float scalar = 2000;
			for (int i=0; i<(numVertex); i++) {
				String[] str = in.nextLine().split(" ");	
//				pointList[i] = new Point(
//					Float.parseFloat(str[0])*scalar + x_axis/2,
//					Float.parseFloat(str[1])*scalar,
//					Float.parseFloat(str[2])*scalar + z_axis/2);
				
//				colourList[i] = Double.valueOf(str[3]); //Only 
				
				Point p = new Point(
						Float.parseFloat(str[2])*scalar,
						Float.parseFloat(str[1])*scalar,
						Float.parseFloat(str[0])*scalar);
//				double px = p.z() * -1 + 300;
//				double py = p.y();
//				double pz = p.x() * 1;
				
//				double px = p.x() * 1 + 300;
//				double py = p.y() * -1 + 400;
//				double pz = p.z() * 1 + 200;
				
				double px = p.z()*1 + 300;
				double py = p.y() * -1 + 400;
				double pz = p.x() * -1 + 200;
				
				Point p2 = new Point(px,py,pz);
				//System.out.println(p + " -> " + p2);
				pointList[i] = p2;
			}
			System.out.println("Point List Created!");
			
			for (int i=0; i<numTriangles; i++) {
				String[] str = in.nextLine().split(" ");
				Triangle t = new Triangle(pointList[Integer.valueOf(str[3])],
						pointList[Integer.valueOf(str[2])],
						pointList[Integer.valueOf(str[1])]);
//				double red = Math.random();
//				double green = Math.random();
//				double blue = Math.random();
//				t.setColor(Color.color(red, green, blue));
				
				
				double col = (colourList[Integer.valueOf(str[1])] + colourList[Integer.valueOf(str[2])] + colourList[Integer.valueOf(str[3])])/3f;
				t.setColor(Color.ALICEBLUE.interpolate(Color.SADDLEBROWN, col));
				
				t.setSpecular(Color.WHITE);
//				t.setReflectedPercent(0.2);
//				t.setRefractedPercent(0.2);
//				t.setRefractiveIndex(1.5);
				tracableObjects.add(t);
			}
			System.out.println("Triangles Added To Scene!");
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void traceSampleScene(WritableImage img, int recursiveCutoff) {
		initialiseSampleSceneTracables();
//		initialiseSampleSceneTracables2();
//		initialiseTestScene();
//		initialiseTestScene2();
//		initialisePhong();
//		initialiseTriangles();
		long start = System.currentTimeMillis();
		render(img,recursiveCutoff);
		long finish = System.currentTimeMillis();
		long duration = finish - start;
		System.out.println("Time taken: " + duration + "ms.");
	}
	
	public void initialiseTriangles() {
		double width = 50;
		double depth = 100;
		Point p1 = new Point(x_axis/2, y_axis/2, depth);
		Point p2 = new Point(x_axis/2, y_axis/2+width, depth);
		Point p3 = new Point(x_axis/2+width, y_axis/2+width/2, depth);
		Triangle t1 = new Triangle(p1,p2,p3);
		t1.setColor(Color.RED);
		tracableObjects.add(t1);
	}
	
	public void initialisePhong() {
		float rad1 = 200;
		Sphere s = new Sphere(new Point(x_axis/2,y_axis/2,200),rad1);
        s.setColor(Color.RED);
        s.setColor(Color.RED, Color.RED, Color.WHITE);
        tracableObjects.add(s);
	}
	
	public void initialiseTestScene() {
		float rad1 = 200;
		Sphere test = new Sphere(new Point(x_axis/2,y_axis-rad1,-2000),rad1);
        test.setColor(Color.RED);
        tracableObjects.add(test);
        
        float rad2 = 300;
		Sphere sphere2 = new Sphere(new Point(x_axis-rad2,y_axis-rad2,-300),rad2);
		sphere2.setColor(Color.WHITE);
        tracableObjects.add(sphere2);
        
        Rectangle r1 = new Rectangle(new Point(500,30,-10), new Point(500,30,-100), new Point(500,100,-100), new Point(500,100,-10));
        r1.setColor(Color.GREEN);
        tracableObjects.add(r1);
        
        float floorWidth = x_axis*2;
        float floorHeight = y_axis;
        Point p1 = new Point(-floorWidth/2 + x_axis/2,floorHeight,floorWidth/2);
        Point p2 = new Point(-floorWidth/2 + x_axis/2,floorHeight,-floorWidth/2);
        Point p3 = new Point(floorWidth/2 + x_axis/2,floorHeight,-floorWidth/2);
        Point p4 = new Point(floorWidth/2 + x_axis/2,floorHeight,floorWidth/2);
        Rectangle floor = new Rectangle(p4, p3, p2, p1);
        floor.setColor(Color.GREEN);
        tracableObjects.add(floor);
	}
	
	public void initialiseTestScene2() {
		float rad1 = 150;
		Sphere s = new Sphere(new Point(x_axis/2,y_axis/2,500),rad1);
        s.setColor(Color.RED);
        s.setColor(Color.RED, Color.RED, Color.WHITE);
        s.setRefractiveIndex(1);
        s.setRefractedPercent(0.9);
        tracableObjects.add(s);
        
        double z = 1000;
        Point p1 = new Point(x_axis/2-500,y_axis/2,z);
        Point p2 = new Point(x_axis/2+500,y_axis/2,z);
        Point p3 = new Point(x_axis/2,y_axis/2+1000,z);
        Triangle triangle = new Triangle(p1,p2,p3);
        triangle.setColor(Color.WHITE);
        triangle.setReflectedPercent(0.8);
        tracableObjects.add(triangle);
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
        
        Rectangle floor = new Rectangle(new Point(0,floorHeight,z_axis), 
				new Point(x_axis,floorHeight,z_axis),
				new Point(x_axis,floorHeight,0),
				new Point(0,floorHeight,0));
        floor.setColor(Color.WHITE);
        floor.setReflectedPercent(0.5);
        
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
	
	public void initialiseSampleSceneTracables2() {
		int w = x_axis;
		int h = y_axis;
		
		int floorHeight = y_axis;
		Point p1 = new Point(0,y_axis,0);
		Point p2 = new Point(0,y_axis,z_axis);
		Point p3 = new Point(x_axis,y_axis,0);
		Point p4 = new Point(x_axis,y_axis,z_axis);
		
		Triangle t1 = new Triangle(p2,p3,p1);
		Triangle t2 = new Triangle(p2,p4,p3);
		
        Rectangle floor = new Rectangle(t1,t2);
        floor.setColor(Color.ANTIQUEWHITE);
        tracableObjects.add(floor);
        
        float rad1 = 300;
        Sphere s1 = new Sphere(new Point(x_axis/2,floorHeight-rad1,1000), rad1);
        s1.setColor(Color.color(0.5,0.5,0.5));
        s1.setReflectedPercent(0.2);
        tracableObjects.add(s1);
        
        float rad2 = 150;
        Sphere s2 = new Sphere(new Point(x_axis/4,floorHeight-rad2,500), rad2);
        s2.setColor(Color.LIGHTGREY);
        s2.setReflectedPercent(0.2);
        tracableObjects.add(s2);
        
        float rad3 = 200;
        Sphere s3 = new Sphere(new Point(x_axis*3/4,floorHeight-rad3,500), rad3);
        s3.setColor(Color.color(1, 1, 1));
        s3.setReflectedPercent(0.5);
        tracableObjects.add(s3);
        
        float rad4 = 50;
        Sphere s4 = new Sphere(new Point(x_axis*5/8,floorHeight-rad4,500), rad4);
        s4.setColor(Color.color(0.8, 0.8, 0.8));
        s4.setReflectedPercent(0.9);
        tracableObjects.add(s4);
	}
	
	public void render(WritableImage img, int lightX) {
		int w=(int) img.getWidth(), h=(int) img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
        
        light = new Point(lightX,0,0);
        camera = new Point(w/2.0f,h/2.0f,-1000f);
        
        //For test camera model//
        float imgWidth = 4f;
        float imgHeight = 3f;
        float imgAspectRatio = (imgWidth/imgHeight);
        float fov = 45;
        
        for (int j=0; j<h; j++) {
        	double pixelY = 1-2*((j+0.5)/h);
        	double camY = (2*pixelY-1) * Math.tan(fov/2 * Math.PI/180);
        	for (int i=0; i<w; i++) {
        		
        		//Playing around with new camera model//
        		double pixelX = 1-2*((i+0.5)/w);
//        		double camX = (2*pixelX-1) * imgAspectRatio * Math.tan(fov/2 * Math.PI/180);
//        		Point orig = new Point(0,0,0);
//        		Vector dir = new Vector(new Point(0,0,0).subtract(new Point(camX,camY,-1)));
//        		dir.normalise();
//        		Ray r2 = new Ray(orig,dir);
        		//System.out.println(r);
        		
//        		Camera c = new Camera(new Point(w/2,h/2,-1), new Vector(0,0,1), new Vector(0,-1,0), fov, imgAspectRatio);
//        		Ray r = c.getRay(new Point(pixelX,pixelY,0));
//        		image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH));
        		
        		//Super Sampling (Not adaptive so quite slow) -> Value for n => n*n samples
        		int n = 3;
        		if (n > 1) { //ie if super sampling
            		float step = 1f/n;
            		float redAcc = 0;
            		float greenAcc = 0;
            		float blueAcc = 0;
            		float upperBound = n/2f;
            		for (float x=-upperBound; x<upperBound; x++) {
            			double i2 = i+x*step;
            			for (float y=-upperBound; y<upperBound; y++) {
            				double j2 = j+y*step;
            				
            				Vector rayVec2 = new Vector(i2+0.5-camera.x(), j2+0.5-camera.y(), -camera.z());
                    		rayVec2.normalise();
                    		Ray r2 = new Ray(new Point(i2,j2,-1), rayVec2);
                    		Color c = trace(r2, MAX_RECURSIVE_DEPTH);
                    		redAcc += c.getRed();
                    		greenAcc += c.getGreen();
                    		blueAcc += c.getBlue();
            			}
            		}
            		redAcc /= n*n; redAcc = (redAcc > 1) ? 1 : (redAcc < 0) ? 0 : redAcc;
            		greenAcc /= n*n; greenAcc = (greenAcc > 1) ? 1 : (greenAcc < 0) ? 0 : greenAcc;
            		blueAcc /= n*n; blueAcc = (blueAcc > 1) ? 1 : (blueAcc < 0) ? 0 : blueAcc;
            		image_writer.setColor(i, j, Color.color(redAcc, greenAcc, blueAcc));
//            		image_writer.setColor(i, h-1-j, Color.color(redAcc, greenAcc, blueAcc));
        		} else { //Just 1 ray so no super sampling
        			// Creating the ray //
            		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
            		rayVec.normalise();
            		Ray r = new Ray(new Point(i,j,-1), rayVec); //Persepective projection
            		//Ray r = new Ray(new Point(i,j,0), new Vector(0,0,1)); //Authnographic projection
            		
            		image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH));
//            		image_writer.setColor(i, j, traceTest(r));
            		
//            		image_writer.setColor(i, h-1-j, trace(r,MAX_RECURSIVE_DEPTH)); //Flipped in y-axis
        		}
        	}
//        	System.out.println("Row " + (j+1) + "/" + h + " completed!");
        }
	}
	
	public Color trace(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {
			System.out.println("Recursive depth cut-off reached"); 
			return Color.BLUE;
		} //Default colour for reaching max recursive depth
		
		Point intersection;
		double ambient = 0.3;
		Vector lnorm;
        Vector snorm;
		
		// Finding the first object intersected //
		Tracable currentTracable = tracableObjects.get(0);
		double t = -1;
//		for (Object o: tracableObjects) {
//			Tracable temp = (Tracable) o;
//			double tempT = temp.getIntersect(r)[0];
////			if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
//			if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
//				//System.out.println("replacing t:" + t + " with tempT:" + tempT);
//				t = tempT;
//				currentTracable = temp;
//			}
//		}
		
		Tracable first = tree.getTracable(r);
		if (first != null) {
			t = first.getIntersect(r)[0];
			currentTracable = first;
		}
		
		// Ray tracing //
		if (t >= 0) {//Accounting for if rays intersect no objects
			intersection = r.getPoint(t);

			double diffuse = 0;
			double specular = 0;
			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
			toLightV.normalise();
			Ray toLightR = new Ray(intersection, toLightV);
			
			boolean inShadow = false;
			//Without kd-tree
//			for (Object o: tracableObjects) {
//    			if (o != currentTracable) {
//    				Tracable temp = (Tracable) o;
//    				if (temp.getIntersect(toLightR)[0] >= 0) {
//    					//System.out.println("In shadow!" + o);
//    					inShadow = true;
//    				}
//    			}
//    		}
			
			//With kd-tree
			Tracable inShadowFrom = tree.getTracable(toLightR);
			if (inShadowFrom != null && inShadowFrom != currentTracable) {
				inShadow = true;
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
				Float n = 25f;
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
				
				Point reflectOrig = intersection.add(new Point(reflectedDir));
				Ray reflectedRay = new Ray(reflectOrig, reflectedDir);
				Color reflectionCol = trace(reflectedRay, recursiveDepth-1);
				
				red += reflectiveAmount * reflectionCol.getRed();
				green += reflectiveAmount * reflectionCol.getGreen();
				blue += reflectiveAmount * reflectionCol.getBlue();
			}
			
			//Refraction
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

					Color refractionColor = trace(refractedRay,recursiveDepth-1);
					
					double mattPerc = currentTracable.getMattPercent();
					double refracPerc = currentTracable.getRefractedPercent();
					
					red = red*mattPerc + refracPerc*refractionColor.getRed();
					green = green*mattPerc + refracPerc*refractionColor.getGreen();
					blue = blue*mattPerc + refracPerc*refractionColor.getBlue();
				}
			} //End of refraction
			
			//Colour normalisation between 0 and 1
			if (green>1) {green=1;} if (green<0) {green=0;} 
			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
			if (red>1) {red=1;} if (red<0) {red=0;}
			
			return Color.color(red,green,blue,1.0);
		} else {
			return Color.BLACK; //ie no intersections
			//return Color.SKYBLUE;
//			double[] temp = bb.getIntersect(r);
//			t = (temp[0] < 0 && temp.length > 1 && temp[1] >= 0) ? temp[1] : temp[0];
//			if (t >= 0) {
//				//System.out.println("Hit the box " + t);
//				Point intersect = r.getPoint((float)t);
//				return bb.getColor(intersect);
//			} else {
//				System.out.println("uhhh I guess it missed the box?" + t + " : " + r);
//				return Color.BLACK;
//			}
		}		
	}
	
	public Color traceTest(Ray r) {
		Tracable trac = tree.getTracable(r);
		if (trac != null) {
			return Color.RED;
		} else {
			return Color.BLACK;
		}
	}
	
	public void startTimer() {
		startTimer("");
	}
	
	public void startTimer(String startMessage) {
		System.out.println("Starting timer: " + startMessage);
		startTime = System.currentTimeMillis();
	}
	
	public void finishTimer() {
		long finishTime = System.currentTimeMillis();
		lastDuration = finishTime - startTime;
		System.out.println("Duration: " + lastDuration + "ms.");
	}
}

