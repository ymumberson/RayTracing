import java.util.ArrayList;

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

public class PhotonMapper extends Application {
//	private int x_axis = 750; private int y_axis = 800; private int z_axis = 750; //For actual scene
//	private int x_axis = 750/3; private int y_axis = 800/3; private int z_axis = 750/3; //For mini scene
	private int x_axis,y_axis,z_axis;
	
	
	
	private ArrayList<Tracable> tracableObjects = new ArrayList<Tracable>();
	private Point light;
    private Point camera;
    private AABB bb; //Bounding box surrounding the scene used as background (well it will be anyway)
    private int MAX_RECURSIVE_DEPTH = 4;
    private int SAMPLES_PER_PIXEL = 1;
    private KdTree tree;
    private long startTime;
    private long lastDuration = 0;
    private long totalTime;
    private boolean USING_KD_TREES = false;
    private static int numMisses,numDiffuse,numSpecular,numRefract,numAbsorbed,numAdded;
    private float PHOTON_SEARCH_RADIUS = 10;
    private int NUM_PHOTONS_SEARCHING_FOR = 500;
    private boolean USING_MINI_SCENE = true;
    private float LIGHT_AMOUNT = 150000f;
    private int LARGE_SCENE_LIGHT_SCALAR = 10;
    private int NUM_LIGHT_RAYS = 1000000;
    
    private ArrayList<Photon> globalPhotons = new ArrayList<Photon>();
    private PhotonMap globalMap;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		if (USING_MINI_SCENE) {
			x_axis = 750/3; y_axis = 800/3; z_axis = 750/3; //For mini scene
		} else {
			x_axis = 750; y_axis = 800; z_axis = 750; //For actual scene
		}
		
		WritableImage img = new WritableImage(x_axis, y_axis);
		ImageView imgView = new ImageView(img);
		
		WritableImage globalPhotonMapImg = new WritableImage(x_axis,y_axis);
		ImageView globalPhotonMapImgView = new ImageView(globalPhotonMapImg);
		
		Point min = new Point(-1,-1,-1);
		Point max = new Point(x_axis+1, y_axis+1, z_axis+1);
		bb = new AABB(min,max);
		tree = new KdTree(bb.getMin(),bb.getMax());
		globalMap = new PhotonMap(bb.getMin(), bb.getMax());
		//System.out.println(bb);
		
		FlowPane root = new FlowPane();
		root.setVgap(8);
        root.setHgap(4);
        
        VBox vb = new VBox();
        vb.getChildren().addAll(imgView);
        root.getChildren().addAll(vb);
        
        VBox vb2 = new VBox();
        vb2.getChildren().addAll(globalPhotonMapImgView);
        root.getChildren().addAll(vb2);
        
        traceActualScene(img);
        renderGlobalPhotonMap(globalPhotonMapImg);
        
        int padding = 5;
        Scene scene = new Scene(root, x_axis*2+padding, y_axis+padding);
        primaryStage.setScene(scene);
        primaryStage.show();
	}

	
	public void traceActualScene(WritableImage img) {
//      light = new Point((x_axis-1)*0.5,0,0);
//		light = new Point((x_axis-1)*0.5,y_axis/3,-10);
		light = new Point((x_axis-1)*0.5,y_axis/3,z_axis/2);
		initialiseActualScene();
		
		if (USING_KD_TREES) {
			buildKdTree(25,3);	
		}
		
//		//Hits wall1 (The red one)
//		Point point = new Point(249.19841163362807,261.5090382336132,152.6741042830573);
//		Vector vector = new Vector(-0.8015883663719197,-0.36221992705201345,-0.47566039917532654);
//		Ray ray = new Ray(point,vector);
//		for (Tracable trac: tracableObjects) {
//			double t = trac.getIntersect(ray);
//			if (t >= 0) {
//				System.out.println(t + " -> " + trac);
//				System.out.println(ray.getPoint(t));
//			}
//		}
		
//		Point point = new Point(249.15681737608227,29.282898353260745,4.185320587752862);
//		Vector vector = new Vector(-0.843182623917666,-0.5323505424183389,-0.0751396214404917);
//		Ray ray = new Ray(point,vector);
//		for (Tracable trac: tracableObjects) {
//			double t = trac.getIntersect(ray);
//			if (t >= 0) {
//				System.out.println(t + " -> " + trac);
//				System.out.println(ray.getPoint(t));
//				System.out.println(trac.getNormal(ray.getPoint(t)));
//			}
//		}
		
		startTimer("Building photon maps");
//		buildGlobalPhotonMap(100000);
		buildGlobalPhotonMap(NUM_LIGHT_RAYS);
//		buildRandomGlobalPhotonMap(1000);
		finishTimer();
//		
		startTimer("Rendering Scene.");
		render(img);
		finishTimer();
		
		System.out.println("Total time: " + totalTime);
	}
	
	public void initialiseActualScene() {
		float diffusePercent = 0.8f;
		
		int rad1;
		if (USING_MINI_SCENE) {
			rad1 = 150/3;
		} else {
			rad1 = 150;
		}
//		int rad1 = 150/3; //For mini
//		int rad1 = 150; //For normal
		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-rad1*2),rad1);
//		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-200),rad1);
		s1.setColor(Color.WHITE);
		s1.setReflectedPercent(0.9);
		s1.setRefractiveIndex(1.5f);
		s1.setSpecular(Color.BLACK);
		tracableObjects.add(s1);
		
		int rad2,padding;
		if (USING_MINI_SCENE) {
			rad2 = 100/3;
			padding = 0;
		} else {
			rad2 = 100;
			padding = 50;
		}
//		int rad2 = 100; //For normal
//		int rad2 = 100/3; //for mini
//		int padding = 50; //for normal
//		int padding = 0; //for mini
		Sphere s2 = new Sphere(new Point(x_axis*3/4-padding,y_axis-rad2,z_axis/3-padding),rad2);
		s2.setColor(Color.WHITE);
		s2.setRefractedPercent(0.9);
		s2.setRefractiveIndex(1.5f);
		s2.setSpecular(Color.BLACK);
		tracableObjects.add(s2);
		
		Rectangle floor = new Rectangle(new Point(0,y_axis,z_axis), 
				new Point(x_axis,y_axis,z_axis),
				new Point(x_axis,y_axis,0),
				new Point(0,y_axis,0));
        floor.setColor(Color.color(0.7,0.7,0.7));
        floor.setSpecular(Color.color(0, 0, 0));
        floor.setDiffusePercent(diffusePercent);
        
        Rectangle wall1 = new Rectangle(
        		new Point(0,0,0),
        		new Point(0,0,z_axis),
        		new Point(0,y_axis,z_axis),
        		new Point(0,y_axis,0));
        wall1.setColor(Color.color(0.5, 0, 0));
        wall1.setSpecular(Color.color(0, 0, 0));
        wall1.setDiffusePercent(diffusePercent);
        
        Rectangle wall2 = new Rectangle(
        		new Point(0,0,z_axis),
        		new Point(x_axis,0,z_axis),
        		new Point(x_axis,y_axis,z_axis),
        		new Point(0,y_axis,z_axis));
//        wall2.setColor(Color.color(0.5, 0.2, 0));
        wall2.setColor(Color.color(0.7,0.7,0.7));
        wall2.setSpecular(Color.color(0, 0, 0));
        wall2.setDiffusePercent(diffusePercent);
        
        Rectangle wall3 = new Rectangle(
        		new Point(x_axis,0,z_axis),
        		new Point(x_axis,0,0),
        		new Point(x_axis,y_axis,0),
        		new Point(x_axis,y_axis,z_axis));
        wall3.setColor(Color.color(0, 0.5, 0));
        wall3.setSpecular(Color.color(0, 0, 0));
        wall3.setDiffusePercent(diffusePercent);
        
        Rectangle wall4 = new Rectangle(
        		new Point(x_axis,0,0),
        		new Point(0,0,0),
        		new Point(0,y_axis,0),
        		new Point(x_axis,y_axis,0));
        wall4.setColor(Color.color(0.7, 0.7, 0.7));
        wall4.setSpecular(Color.color(0, 0, 0));
        wall4.setDiffusePercent(diffusePercent);
        
        Rectangle ceiling = new Rectangle(
        		new Point(0,0,0),
        		new Point(x_axis,0,0),
        		new Point(x_axis,0,z_axis),
        		new Point(0,0,z_axis));
        ceiling.setColor(Color.color(0.7,0.7,0.7));
        ceiling.setSpecular(Color.color(0, 0, 0));
        ceiling.setDiffusePercent(diffusePercent);
        
        tracableObjects.add(floor);
        tracableObjects.add(wall1);
        tracableObjects.add(wall2);
        tracableObjects.add(wall3);
        tracableObjects.add(wall4);
        tracableObjects.add(ceiling);
	}
	
	public void buildKdTree(int maxDepth, int maxObjects) {
		startTimer("Building Tree.");
		tree.build(tracableObjects, maxDepth, maxObjects);
		finishTimer();
		System.out.println(tree.getNumTracables());
	}
	
	public void buildRandomGlobalPhotonMap(int numPhotons) {
		Photon[] arr = new Photon[numPhotons];
		for (int i=0; i<numPhotons; i++) {
			double x = Math.random() * x_axis;
//			double x=0;
			double y = Math.random() * y_axis;
			double z = Math.random() * z_axis;
//			double z = 250;
//			double z = z_axis;
			arr[i] = new Photon(new Point(x,y,z), new Vector(0,0,0), new Vector(Math.random(),Math.random(),Math.random()));
//			System.out.println(arr[i]);
		}
		globalMap.build(arr, 30, 1);
		
		Point point = new Point(x_axis,y_axis/2,z_axis);
		float rad = y_axis/2;
		Sphere s = new Sphere(point,rad);
		ArrayList<Photon> ls = new ArrayList<Photon>();
		globalMap.getNearestNeighbours(s, ls, numPhotons);
		System.out.println("Size of nearest neighbours list: " + ls.size());
	}
	
	/*
	 * Emits light rays into the scene randomly from the light source,
	 * not an ideal generation for the caustics map.
	 */
	public void buildGlobalPhotonMap(int numPhotons) {
		Sphere s = new Sphere(new Point(x_axis/2,y_axis/2,z_axis/2),x_axis); //Just for generating random points, should probs use a static method instead
//		double pow = 1f/numPhotons; //Assuming white light (1,1,1)
//		double pow = 1;
//		double pow = 5;
		double pow = 1; //default
		if (USING_MINI_SCENE) {
			pow = LIGHT_AMOUNT/numPhotons;
		} else {
			pow = (LIGHT_AMOUNT/numPhotons) * LARGE_SCENE_LIGHT_SCALAR; //Because normal scene is 3x bigger
		}
		for (int i=0; i<numPhotons; i++) {
			Vector dir = s.generateRandomUnitVector();
			dir.normalise();
			Ray lightRay = new Ray(light,dir);
			Vector rayPower = new Vector(pow,pow,pow);
//			System.out.println("light Ray:" + lightRay); //For testing
			traceLightRay(lightRay,rayPower,true);
		}
		System.out.println("Num Added: " + numAdded);
		System.out.println("Num Absorbed: " + numAbsorbed);
		System.out.println("Num Specular: " + numSpecular);
		System.out.println("Num Diffuse: " + numDiffuse);
		System.out.println("Num Refract: " + numRefract);
		System.out.println("Num Missed: " + numMisses);
		
		globalMap.build(globalPhotons.toArray(new Photon[globalPhotons.size()]), 30, 1);
		System.out.println("Number of photons in map: " + globalMap.getNumPhotons());
	}
	
	public void traceLightRay(Ray r, Vector rayPower) {
		traceLightRay(r, rayPower, false);
	}
	
	/*
	 * Doesn't have a recursive depth, but should eventually terminate
	 * according to russian roulette.
	 */
	public void traceLightRay(Ray r, Vector rayPower, boolean comesFromLightSource) {
		
		//Find tracable intersected by light ray
		Tracable hitTracable = tracableObjects.get(0);
		double t = -1;
		if (USING_KD_TREES) {
			Tracable first = tree.getTracable(r);
			if (first != null) {
				t = first.getIntersect(r);
				hitTracable = first;
			}
		} else {
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(r);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t = tempT;
					hitTracable = temp;
				}
			}
		}
		
		//Calculations for the intersection
		if (t >= 0) { //Then there was an intersection
			Point intersection = r.getPoint(t);
			
			//Perform russian roulette
			Vector dir;
			Ray newR;
			boolean absorbed = false;
			switch (hitTracable.russianRoulette(r, intersection)) {
				case 1: //Diffuse reflection
					numDiffuse++;
					Vector v = new Vector(hitTracable.getDiffuse());
					Vector newPower = rayPower.multiply(v);
					
					dir = calculateDiffuseDir(hitTracable,r,intersection);
					newR = new Ray(intersection,dir);
					traceLightRay(newR,newPower);
					break;
				case 2: //Specular reflection
					numSpecular++;
					dir = calculateReflectedDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir)),dir); //Think reflections were getting stuck (intersecting themselves)
					traceLightRay(newR,rayPower); //Power doesn't change
					break;
				case 3: //Refraction
					numRefract++;
					dir = calculateReflectedDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir)),dir);
					traceLightRay(newR,rayPower); //Power doesn't change
					break;
				default: //Absorption
					numAbsorbed++;
					absorbed = true;
					if (hitTracable.isDiffuse()) {
						Photon p = new Photon(intersection,hitTracable.getNormal(intersection),rayPower);
						globalPhotons.add(p);
						numAdded++;
					}
			}
//			if (!absorbed && hitTracable.isDiffuse()) {
//				//Store photon at intersection
//				Photon p = new Photon(intersection,hitTracable.getNormal(intersection),rayPower);
//				globalPhotons.add(p);
//				numAdded++;
//				
//				//TODO Store shadow photon at second intersection
//			}
			
			if (comesFromLightSource) {
				//Creating illumination photon
				Photon p = new Photon(intersection,hitTracable.getNormal(intersection),rayPower,true);
				globalPhotons.add(p);
				
				//Creating shadow photon
				Tracable nextTracable = null;
				double nextT = -1;
				for (Object o: tracableObjects) { //Find next tracable intersected by ray
					Tracable temp = (Tracable) o;
					if (temp != hitTracable) {
						double tempT = temp.getIntersect(r);
						if ((nextT < 0 && tempT > nextT) || (nextT >= 0 && tempT < nextT && tempT >= 0)) {
							//Found an intersection
							nextT = tempT;
							nextTracable = temp;
							
							//Create shadow photon at intersection point
							Point intersectionPoint = r.getPoint(nextT);
							Photon shadowP = new Photon(intersectionPoint,nextTracable.getNormal(intersectionPoint),rayPower,false);
							globalPhotons.add(shadowP);
						}
					}
				}
			} else {
				//If not directly from light source then create a normal photon at intersection
				if (!absorbed && hitTracable.isDiffuse()) {
					//Store photon at intersection
					Photon p = new Photon(intersection,hitTracable.getNormal(intersection),rayPower);
					globalPhotons.add(p);
					numAdded++;
					
					//TODO Store shadow photon at second intersection
				}
			}
			
		} else { //else no intersection
			numMisses++;
//			System.out.println("========================================================================\n"
//					+ "No intersection: " + r);
		}
	}
	
	public void render(WritableImage img) {
		int w=(int) img.getWidth(), h=(int) img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
//        light = new Point(lightX,y_axis/2,z_axis/2);
        camera = new Point(w/2.0f,h/2.0f,-1000f);
                
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
        		
//        		//Super Sampling (Not adaptive so quite slow) -> Value for n => n*n samples
        		int n = SAMPLES_PER_PIXEL;
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
            		
        		} else { //Just 1 ray so no super sampling
        			// Creating the ray //
            		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
            		rayVec.normalise();
            		Ray r = new Ray(new Point(i,j,-1), rayVec); //Persepective projection
            		
            		image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH)); //Standard render
//            		image_writer.setColor(i, j, testTrace(r,MAX_RECURSIVE_DEPTH)); //For photon mapping stuff (Direct visualisation)
        		}
        	}
        	System.out.println("Row " + (j+1) + "/" + h + " completed!");
        }
	}
	
	public Color trace(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {
			return Color.BLACK;
		} //Default colour for reaching max recursive depth
		
		Vector currentColor = new Vector(0,0,0);
		Point intersection;
		
		// Finding the first object intersected // (Without kd-trees)
		Tracable currentTracable = tracableObjects.get(0);
		double t = -1;
		if (USING_KD_TREES) {
			Tracable first = tree.getTracable(r);
			if (first != null) {
				t = first.getIntersect(r);
				currentTracable = first;
			}
		} else {
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(r);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t = tempT;
					currentTracable = temp;
				}
			}
		}
		
		// Ray tracing //
		if (t >= 0) {//Accounting for if rays intersect no objects
			intersection = r.getPoint(t);
			
			/*
			 * Part 1: Direct Illumination
			 */
			Vector direct = new Vector(0,0,0);
			
			/*
			 * Part 2: Specular & Glossy Reflections
			 */
			Vector specular = new Vector(0,0,0);
			
			/*
			 * Part 3: Caustics
			 */
			Vector caustics = new Vector(0,0,0);
			
			/*
			 * Part 4: Multiple diffuse reflections
			 */
			Vector diffuse = new Vector(0,0,0);
			
			
			//Colour is sum of components
			currentColor = direct.add(specular).add(caustics).add(diffuse);
			
			//Colour normalisation between 0 and 1
			double red = currentColor.dx();
			double green = currentColor.dy();
			double blue = currentColor.dz();
			if (green>1) {green=1;} if (green<0) {green=0;} 
			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
			if (red>1) {red=1;} if (red<0) {red=0;}
			
			return Color.color(red,green,blue,1.0);
		} else {
			return Color.BLACK; //ie no intersections
		}
	}
	
	public Color testTrace(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {
			return Color.BLACK;
		}
		
		Vector currentColor = new Vector(0,0,0);
		// Finding the first object intersected // (Without kd-trees)
		Tracable currentTracable = tracableObjects.get(0);
		double t = -1;
		if (USING_KD_TREES) {
			Tracable first = tree.getTracable(r);
			if (first != null) {
				t = first.getIntersect(r);
				currentTracable = first;
			}
		} else {
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(r);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t = tempT;
					currentTracable = temp;
				}
			}
		}
		
		if (t >= 0) {
			Point intersection = r.getPoint(t);
			ArrayList<Photon> nearestNeighbours = new ArrayList<Photon>();
			Sphere s = new Sphere(intersection,1);
//			Sphere s = new Sphere(intersection,PHOTON_SEARCH_RADIUS);
			int N = 1;
			globalMap.getNearestNeighbours(s, nearestNeighbours, N);
//			globalMap.getNearestNeighbours(s, nearestNeighbours, NUM_PHOTONS_SEARCHING_FOR);
			int length = nearestNeighbours.size();
//			System.out.println(length);
			for (int i=0; i<length; i++) {
				Vector energy = nearestNeighbours.get(i).getEnergy();
//				System.out.println(energy);
				currentColor = currentColor.add(energy);
			}
			if (length > 0) {
				currentColor = currentColor.divide(length);
			}
			
			//Giving objects some of their own color instead of direct visualisation of global photon map
//			double mattPerc = currentTracable.getMattPercent();
//			if (mattPerc == 1) {mattPerc = currentTracable.getDiffusePercent();}
//			Vector mattCol = new Vector(currentTracable.getColor()).multiply(mattPerc);
//			currentColor = currentColor.multiply(1-mattPerc).add(mattCol);
			
//			currentColor = currentColor.divide(2).add(new Vector(currentTracable.getColor()).divide(2));
//			System.out.println(currentColor);
//			return currentTracable.getColor(); //Debugging -> Just returns colour of object it intersects
		}
		
		//Colour normalisation between 0 and 1
		double red = currentColor.dx();
		double green = currentColor.dy();
		double blue = currentColor.dz();
		if (green>1) {green=1;} if (green<0) {green=0;} 
		if (blue>1) {blue=1;} if (blue<0) {blue=0;}
		if (red>1) {red=1;} if (red<0) {red=0;}
		
		return Color.color(red,green,blue,1.0);
	}
	
	/*
	 * Direct visualisation of photon map
	 * Won't work anymore because reflections call trace() and not this method
	 */
	public Color directlyVisualise(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {
			return Color.BLACK;
		} //Default colour for reaching max recursive depth
		
		Vector currentColor = new Vector(0,0,0);
		Point intersection;
		
		// Finding the first object intersected // (Without kd-trees)
		Tracable currentTracable = tracableObjects.get(0);
		double t = -1;
		if (USING_KD_TREES) {
			Tracable first = tree.getTracable(r);
			if (first != null) {
				t = first.getIntersect(r);
				currentTracable = first;
			}
		} else {
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(r);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t = tempT;
					currentTracable = temp;
				}
			}
		}
		
		// Ray tracing //
		if (t >= 0) {//Accounting for if rays intersect no objects
			intersection = r.getPoint(t);
			
			/*
			 * Frasnel
			 * &I is the incident ray = r.d()
			 * &N is the surface normal = currentTracable.getNormal()
			 * &ior is the refractive index = currentTracable.getRefractiveIndex()
			 * &kr is calculated
			 */
			if (currentTracable.isReflective() || currentTracable.isRefractive()) {
				float kr = 0;
				Vector n = currentTracable.getNormal(intersection);
				float etat = (float)currentTracable.getRefractiveIndex();
				
				float cosi = (float) Math.max(-1,Math.min(1, r.d().dot(n)));
				float etai = 1; //Refractive index of air
				if (cosi > 0) {
					float temp = etai;
					etai = etat;
					etat = temp;
				}
				
				//Compute using Snell's law
				float sint = (float) (etai / etat * Math.sqrt(Math.max(0f, 1 - cosi*cosi)));
				
				if (sint >= 1) { //Total internal reflection
					kr = 1;
				} else {
					float cost = (float) Math.sqrt(Math.max(0f, 1 - sint*sint));
					cosi = Math.abs(cosi);
					float rS = (float) (((etat*cosi) - (etai*cost)) / ((etat*cosi) + (etai * cost)));
					float rP = (float) (((etai*cosi) - (etat*cost)) / ((etai*cosi) + (etat * cost)));
//					System.out.println(rS + " " + rP + " " + cost + " " + cosi);
					kr = (rS * rS + rP * rP) / 2f;
				}
				float kt = 1-kr;
				if (currentTracable.isRefractive()) {
					float temp = kt;
					kt = kr;
					kr = temp;
				}
				if (kr < 1) {
					currentColor = refract(currentTracable,kr,r,currentColor,intersection,recursiveDepth);
				}
				currentColor = reflect(currentTracable,kt,r,currentColor,intersection,recursiveDepth);
//				System.out.println("Kr: " + kr + " | Kt: " + (1-kr));
				
			//Messing with colour bleeding via diffuse reflections	
			} else { 
				PhotonMaxHeap heap = new PhotonMaxHeap(NUM_PHOTONS_SEARCHING_FOR);
				for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
					Photon p = globalPhotons.get(i);
					heap.insert(p, intersection.euclideanDistance(p.getPosition()));
				}
				
				globalMap.getNearestNeighbours(intersection,heap);
				
				Photon[] ls = heap.getPhotons();
//				System.out.println(heap);
				Vector temp = new Vector(0,0,0);
				Vector normal = currentTracable.getNormal(intersection);
				for (Photon p: ls) {
					if (p.getIncidentDirection() == normal) {temp = temp.add(p.getEnergy());}
//					temp = temp.add(p.getEnergy());
				}
//				System.out.println(temp);
				double maxDist = heap.getMaxDistance();
				Vector vec = temp.divide(Math.PI * maxDist*maxDist);
				currentColor = vec.multiply(new Vector(currentTracable.getColor()));
			}
			
			
			//Colour normalisation between 0 and 1
			double red = currentColor.dx();
			double green = currentColor.dy();
			double blue = currentColor.dz();
			if (green>1) {green=1;} if (green<0) {green=0;} 
			if (blue>1) {blue=1;} if (blue<0) {blue=0;}
			if (red>1) {red=1;} if (red<0) {red=0;}
			
			return Color.color(red,green,blue,1.0);
		} else {
//			return computeBackground(r);
			return Color.BLACK; //ie no intersections
			//return Color.SKYBLUE;
		}		
	}
	
	public Vector reflect(Tracable currentTracable, double reflectiveAmount, Ray r, Vector c, Point intersection, int recursiveDepth) {
//		double reflectiveAmount = currentTracable.getReflectedPercent();
//		if (recursiveDepth <= 0) {
//			return c;
//		}
		double red = c.dx();
		double green = c.dy();
		double blue = c.dz();
		
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
		return new Vector(red,green,blue);
	}
	
	public Vector calculateReflectedDir(Tracable currentTracable, Ray r, Point intersection) {
		Vector n = currentTracable.getNormal(intersection);
		Vector l = r.d();
		Vector reflectedDir = l.subtract(n.multiply(2*l.dot(n)));
		reflectedDir.normalise();
		return reflectedDir;
	}
	
	public Vector refract(Tracable currentTracable, double refracPerc, Ray r, Vector c, Point intersection, int recursiveDepth) {
//		if (recursiveDepth <= 0) {
//			return c;
//		}
		double red = c.dx();
		double green = c.dy();
		double blue = c.dz();
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
//			System.out.println("Total internal refraction");
		} else {
			Vector out = n.multiply((eta * cosi - Math.sqrt(k))).add(i.multiply(eta));
			out.normalise();
			
			Ray refractedRay = new Ray(intersection, out);
			//if (intersection == r.o()) { //Accounts for rounding error, advances point forward if is same as current ray origin
				Point p = intersection.add(new Point(r.d()));
				refractedRay = new Ray(p, out);
			//}
			
			double t2 = currentTracable.getIntersect(refractedRay); 
			if (t2 >= 0) {
				Point p2 = refractedRay.getPoint(t2);
//				System.out.println("Here");
//				p = refractedRay.getPoint(t2).add(new Point(refractedRay.d()));
//				refractedRay = new Ray(p,r.d());
				return refract(currentTracable,refracPerc,refractedRay,c,p2,recursiveDepth-1);
			}
				
			Color refractionColor = trace(refractedRay,recursiveDepth-1);
			
//			double refracPerc = currentTracable.getRefractedPercent();
			
			red += refracPerc*refractionColor.getRed();
			green += refracPerc*refractionColor.getGreen();
			blue += refracPerc*refractionColor.getBlue();
		}
		return new Vector(red,green,blue);
	}
	
	public Vector calculateRefractedDir(Tracable currentTracable, Ray r, Point intersection) {
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
//			System.out.println("Total internal refraction");
			return new Vector(0,0,0); //ie null, change to null?
		} else {
			Vector out = n.multiply((eta * cosi - Math.sqrt(k))).add(i.multiply(eta));
			out.normalise();
			
			Ray refractedRay = new Ray(intersection, out);
			//if (intersection == r.o()) { //Accounts for rounding error, advances point forward if is same as current ray origin
				Point p = intersection.add(new Point(r.d()));
				refractedRay = new Ray(p, out);
			//}
			
			double t2 = currentTracable.getIntersect(refractedRay); 
			if (t2 >= 0) { //Recurse
				Point p2 = refractedRay.getPoint(t2);
//				return refract(currentTracable,refracPerc,refractedRay,c,p2,recursiveDepth-1);
				return calculateRefractedDir(currentTracable,refractedRay,p2);
			}
				
//			Color refractionColor = trace(refractedRay,recursiveDepth-1);
			return out;
		}
	}
	
	public Vector diffuseReflect(Tracable currentTracable, Vector currentColor, Point intersection, float rayScalar, int recursiveDepth) {
		Vector surfaceNorm = currentTracable.getNormal(intersection);
		float radius = 100;
		Sphere s = new Sphere(intersection.add(new Point(surfaceNorm)),radius);
		
		Vector reflectDir = new Vector(s.generateRandomUnitPoint().add(s.c()).subtract(intersection));
		reflectDir.normalise();
		Ray rayDiff = new Ray(intersection.add(new Point(reflectDir)), reflectDir);
//		Color col = trace(rayDiff, recursiveDepth-1);
		
		Tracable hitTracable = tracableObjects.get(0);
		double t = -1;
		for (Object o: tracableObjects) {
			Tracable temp = (Tracable) o;
			double tempT = temp.getIntersect(rayDiff);
//			if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
			if ((t < 0 && tempT > t) || (t >= 0 && tempT < t && tempT >= 0)) {
				//System.out.println("replacing t:" + t + " with tempT:" + tempT);
				t = tempT;
				hitTracable = temp;
			}
		}
		if (t >= 0 && hitTracable.isDiffuse()) {
			return new Vector(hitTracable.getColor()).multiply(rayScalar);
		} else {
			return currentColor.multiply(rayScalar);
		}
		
//		return new Vector(col).multiply(rayScalar);
	}
	
	public Vector calculateDiffuseDir(Tracable currentTracable, Ray r, Point intersection) {
		Vector surfaceNorm = currentTracable.getNormal(intersection);
		float radius = 100;
//		float radius = y_axis;
		Sphere s = new Sphere(intersection.add(new Point(surfaceNorm)),radius);
		
		Vector reflectDir = new Vector(s.generateRandomUnitPoint().add(s.c()).subtract(intersection));
		reflectDir.normalise();
		return reflectDir;
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
		totalTime += lastDuration;
		System.out.println("Duration: " + lastDuration + "ms.");
	}
	
	public void renderGlobalPhotonMap(WritableImage img) {
		int w=(int) img.getWidth(), h=(int) img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
//        light = new Point(lightX,y_axis/2,z_axis/2);
        camera = new Point(w/2.0f,h/2.0f,-1000f);
                
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
        		// Creating the ray //
        		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
        		rayVec.normalise();
        		Ray r = new Ray(new Point(i,j,-1), rayVec); //Persepective projection
        		
//        		image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH)); //Standard render
        		image_writer.setColor(i, j, testTrace(r,MAX_RECURSIVE_DEPTH)); //For photon mapping stuff (Direct visualisation)
        	}
//        	System.out.println("Row " + (j+1) + "/" + h + " completed!");
        }
	}
}

