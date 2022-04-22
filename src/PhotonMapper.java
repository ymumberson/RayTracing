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
	
	private float x_axis=7.5f,y_axis=8f,z_axis=7.5f;
	private int imgX=750,imgY=800;
	
	
	
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
    private int NUM_PHOTONS_SEARCHING_FOR = 500;
    private boolean USING_MINI_SCENE = true;
    private boolean USING_AREA_LIGHT = true;
    private float AREA_LIGHT_NUM_SAMPLES = 16;
    private AreaLight areaLight;
    private boolean USING_GAMMA_CORRECTION = true;
    private float gamma = 2.2f;
//    private float LIGHT_AMOUNT = 3000f;
//    private float LIGHT_AMOUNT = 200f;
//    private float LIGHT_AMOUNT = 0.001f; //For 1,000,000 rays
//    private float LIGHT_AMOUNT = 10f; //For 100,000 rays
    private float LIGHT_AMOUNT = 2000f; //For 10,000 rays
    private int NUM_LIGHT_RAYS = 10000;
    
    //Max squared differences
    private float MAX_SEARCH_DISTANCE_DIRECT = 1f * 1f;
    private float MAX_SEARCH_DISTANCE_INDIRECT = 1f*1f;
    private float MAX_SEARCH_DISTANCE_CAUSTICS = 1f*1f;
    
    private boolean DIRECT_ILLUMINATION_ENABLED = true;
    private boolean CAUSTICS_ENABLED = true;
    private boolean SOFT_INDIRECT_ILLUMINATION_ENABLED = true;
    
    //Global photon map
    private ArrayList<Photon> globalPhotons = new ArrayList<Photon>();
    private PhotonMap globalMap;
    
    //Caustics Photon map
    private ArrayList<Photon> causticsPhotons = new ArrayList<Photon>();
    private PhotonMap causticsMap;
    private Sphere reflectiveSphere;
    private Sphere refractiveSphere;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		if (USING_MINI_SCENE) {
//			x_axis = 750/3; y_axis = 800/3; z_axis = 750/3; //For mini scene
			imgX = 750/3; imgY = 800/3;
		}
		
//		int miniFactor = 10; imgX = 750/miniFactor; imgY = 800/miniFactor; //Super mini scene
		
		WritableImage img = new WritableImage(imgX, imgY);
		ImageView imgView = new ImageView(img);
		
		WritableImage globalPhotonMapImg = new WritableImage(imgX,imgY);
		ImageView globalPhotonMapImgView = new ImageView(globalPhotonMapImg);
		
		WritableImage causticsPhotonMapImg = new WritableImage(imgX,imgY);
		ImageView causticsPhotonMapImgView = new ImageView(causticsPhotonMapImg);
		
		Point min = new Point(-1,-1,-1);
		Point max = new Point(x_axis+1, y_axis+1, z_axis+1);
		bb = new AABB(min,max);
		tree = new KdTree(bb.getMin(),bb.getMax());
		globalMap = new PhotonMap(bb.getMin(), bb.getMax());
		causticsMap = new PhotonMap(bb.getMin(),bb.getMax());
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
        
        VBox vb3 = new VBox();
        vb3.getChildren().addAll(causticsPhotonMapImgView);
        root.getChildren().addAll(vb3);
        
        traceActualScene(img);
        renderGlobalPhotonMap(globalPhotonMapImg);
        renderCausticsPhotonMap(causticsPhotonMapImg);
        
        int padding = 5;
        Scene scene = new Scene(root, imgX*3+padding*2, imgY+padding);
        primaryStage.setScene(scene);
        primaryStage.show();
	}

	
	public void traceActualScene(WritableImage img) {
//      light = new Point((x_axis-1)*0.5,0,0);
//		light = new Point((x_axis-1)*0.5,y_axis/3,-10);
		light = new Point((x_axis)*0.5,y_axis/3,z_axis/2);
		initialiseActualScene();
		
		if (USING_KD_TREES) {
			buildKdTree(25,3);	
		}
		
		startTimer("Building photon maps");
//		buildGlobalPhotonMap(100000);
		buildGlobalPhotonMap(NUM_LIGHT_RAYS);
		buildCausticsPhotonMap(NUM_LIGHT_RAYS);
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
		
		float rad1 = 1.5f;
//		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-rad1*2),rad1);
		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-rad1*2f), rad1);
//		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-200),rad1);
		s1.setColor(Color.WHITE);
		s1.setReflectedPercent(0.9);
		s1.setRefractiveIndex(1.5f);
		s1.setSpecular(Color.BLACK);
		tracableObjects.add(s1);
		reflectiveSphere = s1;
		
		float rad2 = 1f;
		float padding = 0.5f;
//		Sphere s2 = new Sphere(new Point(x_axis*3/4-padding,y_axis-rad2,z_axis/3-padding),rad2);
		Sphere s2 = new Sphere(new Point((x_axis/3)*2,y_axis-rad2,z_axis/3-1),rad2);
		s2.setColor(Color.WHITE);
		s2.setRefractedPercent(0.9);
		s2.setRefractiveIndex(1.5f);
		s2.setSpecular(Color.BLACK);
		tracableObjects.add(s2);
		refractiveSphere = s2;
		
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
//        wall1.setColor(Color.color(0.5, 0, 0));
        wall1.setColor(Color.DARKRED);
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
//        wall3.setColor(Color.color(0, 0.5, 0));
        wall3.setColor(Color.DARKGREEN);
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
        
        if (USING_AREA_LIGHT) {
        	float width = 2f;
        	Point middle = new Point(x_axis/2f,0.001f,z_axis/2f);
        	this.areaLight = new AreaLight(
        		middle.add(new Point(-width/2,0,-width/2)),
        		middle.add(new Point(width/2,0,-width/2)),
        		middle.add(new Point(width/2,0,width/2)),
        		middle.add(new Point(-width/2,0,width/2)));
        	this.areaLight.setColor(Color.WHITE);
        	tracableObjects.add(this.areaLight);
        }
        
        
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
			arr[i] = new Photon(new Point(x,y,z), new Vector(0,0,0), new Vector(0,0,0), new Vector(Math.random(),Math.random(),Math.random()));
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
		double pow = LIGHT_AMOUNT/(numPhotons);
		
		if (this.USING_AREA_LIGHT) {
			for (int i=0; i<numPhotons; i++) {
				Ray lightRay = this.areaLight.generateRandomLightRay();
				Vector rayPower = new Vector(pow,pow,pow);
//				System.out.println("light Ray:" + lightRay); //For testing
				traceLightRay(lightRay,rayPower,globalPhotons,true,false);
			}
		} else {
			for (int i=0; i<numPhotons; i++) {
				Vector dir = s.generateRandomUnitVector();
				dir.normalise();
				Ray lightRay = new Ray(light,dir);
				Vector rayPower = new Vector(pow,pow,pow);
//				System.out.println("light Ray:" + lightRay); //For testing
				traceLightRay(lightRay,rayPower,globalPhotons,true,false);
			}
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
	
	public void buildCausticsPhotonMap(int numPhotons) {
//		double pow = 1; //default
//		if (USING_MINI_SCENE) {
//			pow = LIGHT_AMOUNT/numPhotons;
//		} else {
//			pow = (LIGHT_AMOUNT/numPhotons) * LARGE_SCENE_LIGHT_SCALAR; //Because normal scene is 3x bigger
//		}
		
		double pow = LIGHT_AMOUNT/(numPhotons*1000*10);
//		double pow = 1/numPhotons;
		
		//Fire rays at reflective sphere
		Point target;
		Vector dir;
		Ray r;
		Vector rayPower = new Vector(pow,pow,pow);
		for (int i=0; i<numPhotons/2; i++) {
			target = reflectiveSphere.c().add(reflectiveSphere.generateRandomUnitPoint()
					.multiply(reflectiveSphere.getRadius()));
			dir = new Vector(target.subtract(light));
			dir.normalise();
			r = new Ray(light,dir);
			traceLightRay(r,rayPower,causticsPhotons,true,true);
		}
		
		//Fire rays at refractive sphere
		for (int i=0; i<numPhotons/2; i++) {
			target = refractiveSphere.c().add(refractiveSphere.generateRandomUnitPoint()
					.multiply(refractiveSphere.getRadius()));
//			System.out.println(target);
			dir = new Vector(target.subtract(light));
			dir.normalise();
			r = new Ray(light,dir);
			traceLightRay(r,rayPower,causticsPhotons,true,true);
		}
		
		System.out.println("Num Added: " + numAdded);
		System.out.println("Num Absorbed: " + numAbsorbed);
		System.out.println("Num Specular: " + numSpecular);
		System.out.println("Num Diffuse: " + numDiffuse);
		System.out.println("Num Refract: " + numRefract);
		System.out.println("Num Missed: " + numMisses);
		
		causticsMap.build(causticsPhotons.toArray(new Photon[causticsPhotons.size()]), 30, 1);
		System.out.println("Number of photons in map: " + causticsMap.getNumPhotons());
		System.out.println(refractiveSphere);
	}
	
	public void traceLightRay(Ray r, Vector rayPower, ArrayList<Photon> mapList, boolean isCaustics) {
		traceLightRay(r, rayPower, mapList, false, isCaustics);
	}
	
	/*
	 * Doesn't have a recursive depth, but should eventually terminate
	 * according to russian roulette.
	 */
	public void traceLightRay(Ray r, Vector rayPower, ArrayList<Photon> mapList, boolean comesFromLightSource, boolean isCaustics) {
		
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
			float padding = 0.001f;
			switch (hitTracable.russianRoulette(r, intersection)) {
				case 1: //Diffuse reflection
					if (isCaustics) break; //Caustics can't be diffusely reflected
					numDiffuse++;
					Vector v = new Vector(hitTracable.getDiffuse());
					Vector newPower = rayPower.multiply(v);
					
					dir = calculateDiffuseDir(hitTracable,r,intersection);
					newR = new Ray(intersection,dir);
					traceLightRay(newR,newPower,mapList,isCaustics);
					break;
				case 2: //Specular reflection
					numSpecular++;
					dir = calculateReflectedDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir).multiply(padding)),dir); //Think reflections were getting stuck (intersecting themselves)
					traceLightRay(newR,rayPower,mapList,isCaustics); //Power doesn't change
					break;
				case 3: //Refraction
					numRefract++;
					dir = calculateRefractedDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir).multiply(padding)),dir);
					traceLightRay(newR,rayPower,mapList,isCaustics); //Power doesn't change
					break;
				default: //Absorption
					numAbsorbed++;
					absorbed = true;
					if (hitTracable.isDiffuse() && !comesFromLightSource) {
						Photon p = new Photon(intersection,r.d(),hitTracable.getNormal(intersection),rayPower);
						mapList.add(p);
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
				//Only create illumination photon if surface is diffuse
				if (hitTracable.isDiffuse()) {
					//Creating illumination photon
					Photon p = new Photon(intersection,r.d(),hitTracable.getNormal(intersection),rayPower,true);
//					Photon p = new Photon(intersection,r.d(),hitTracable.getNormal(intersection),new Vector(0,0,0),true);
					mapList.add(p);
				}
				
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
							
							//Only create shadow photon if surface is diffuse
							if (nextTracable.isDiffuse()) {
								//Create shadow photon at intersection point with negative power
								Point intersectionPoint = r.getPoint(nextT);
								Photon shadowP = new Photon(intersectionPoint,r.d(),nextTracable.getNormal(intersectionPoint),rayPower.multiply(-1),false);
//								Photon shadowP = new Photon(intersectionPoint,r.d(),nextTracable.getNormal(intersectionPoint),new Vector(0,0,0),false);
								mapList.add(shadowP);
							}
						}
					}
				}
			} else {
				//If not directly from light source then create a normal photon at intersection, with no shadow photon created
				if (!absorbed && hitTracable.isDiffuse()) {
					//Store photon at intersection
					Photon p = new Photon(intersection,r.d(),hitTracable.getNormal(intersection),rayPower);
					mapList.add(p);
					numAdded++;
				}
			}
			
		} else { //else no intersection
			numMisses++;
//			System.out.println("========================================================================\n"
//					+ "No intersection: " + r);
		}
	}
	
	public void render(WritableImage img) {
		double w= img.getWidth(), h= img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
//        light = new Point(lightX,y_axis/2,z_axis/2);
        camera = new Point((x_axis)/2.0f,(y_axis)/2.0f,-10f);
        
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
        		
//        		//Super Sampling (Not adaptive so quite slow) -> Value for n => n*n samples
        		int n = SAMPLES_PER_PIXEL;
        		if (n > 1) { //ie if super sampling
        			double xMiddle = ((double)i/w)*x_axis;
        			double yMiddle = ((double)j/h)*y_axis;
        			float xWidth = (float) (x_axis/w);
        			float yWidth = (float) (y_axis/h);
        			float xStep = xWidth/n;
        			float yStep = yWidth/n;
//            		float step = 1f/n;
            		float redAcc = 0;
            		float greenAcc = 0;
            		float blueAcc = 0;
            		float upperBound = n/2f;
            		for (float x=-upperBound; x<upperBound; x++) {
            			double i2 = xMiddle+x*xStep;
            			for (float y=-upperBound; y<upperBound; y++) {
            				double j2 = yMiddle+y*yStep;
            				
//            				Vector rayVec2 = new Vector(i2+0.5-camera.x(), j2+0.5-camera.y(), -camera.z());
            				Vector rayVec2 = new Vector(i2-camera.x(), j2-camera.y(), -camera.z());
                    		rayVec2.normalise();
                    		Ray r2 = new Ray(new Point(i2,j2,0), rayVec2);
                    		Color c = trace(r2, MAX_RECURSIVE_DEPTH);
                    		redAcc += c.getRed();
                    		greenAcc += c.getGreen();
                    		blueAcc += c.getBlue();
            			}
            		}
            		redAcc /= n*n; redAcc = (redAcc > 1) ? 1 : (redAcc < 0) ? 0 : redAcc;
            		greenAcc /= n*n; greenAcc = (greenAcc > 1) ? 1 : (greenAcc < 0) ? 0 : greenAcc;
            		blueAcc /= n*n; blueAcc = (blueAcc > 1) ? 1 : (blueAcc < 0) ? 0 : blueAcc;
//            		image_writer.setColor(i, j, Color.color(redAcc, greenAcc, blueAcc));
            		if (USING_GAMMA_CORRECTION) {
            			Vector correctedCol = new Vector(redAcc,greenAcc,blueAcc).pow(1f/gamma);
            			Color newCol = Color.color(correctedCol.dx(), correctedCol.dy(), correctedCol.dz());
            			image_writer.setColor(i,j,newCol);
            		} else {
            			image_writer.setColor(i, j, Color.color(redAcc, greenAcc, blueAcc)); //Standard render
            		}
            		
        		} else { //Just 1 ray so no super sampling
        			// Creating the ray //
//            		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
        			double x = ((double)i/w)*x_axis;
        			double y = ((double)j/h)*y_axis;
//        			System.out.println(i + " " + j + " -> " + x + " " + y);
        			Vector rayVec = new Vector(x-camera.x(), y-camera.y(), -camera.z());
            		rayVec.normalise();
//            		Ray r = new Ray(new Point(i,j,-1), rayVec); //Persepective projection
            		Ray r = new Ray(new Point(x,y,0),rayVec);
            		
            		if (USING_GAMMA_CORRECTION) {
            			Color col = trace(r,MAX_RECURSIVE_DEPTH);
            			Vector correctedCol = new Vector(col).pow(1f/gamma);
            			Color newCol = Color.color(correctedCol.dx(), correctedCol.dy(), correctedCol.dz());
            			image_writer.setColor(i,j,newCol);
            		} else {
            			image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH)); //Standard render
            		}
        		}
        	}
        	System.out.println("Row " + (j+1) + "/" + (int)h + " completed!");
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
			 * If diffuse -> Colour = direct + caustics + indirect.
			 * If specular -> Colour = (reflect the ray)
			 */
			if (currentTracable.isDiffuse()) {
				Vector direct=new Vector(0,0,0),caustics=new Vector(0,0,0),indirect=new Vector(0,0,0);
				
				if (this.DIRECT_ILLUMINATION_ENABLED) {
					if (USING_AREA_LIGHT) {
						direct = this.calculateDirectIlluminationWithAreaLight(intersection,currentTracable); //Using shadow rays
//						direct = this.calculateRadianceFromPhotonMap(intersection, currentTracable);
//						direct = this.testFinalGather(intersection, currentTracable);
					} else {
//						direct = calculateDirectIllumination(intersection,currentTracable); //Using shadow photons
						direct = calculateDirectIlluminationWithShadowRays(intersection,currentTracable); //Using shadow rays
					}
				}
				
				if (this.CAUSTICS_ENABLED) caustics = calculateCaustics(intersection, currentTracable);
				if (this.SOFT_INDIRECT_ILLUMINATION_ENABLED) {
					/* Original importance sampling for point light, doesn't light up ceiling properly */
//					indirect = this.calculateIndirectIlluminationWithImportanceSampling
//							(intersection, currentTracable, recursiveDepth).multiply(1)
//							.multiply(new Vector(currentTracable.getColor()))
//							.multiply(currentTracable.getDiffusePercent());
					
					/* Trying to fix importance sampling */
//					indirect = this.calculateIndirectIlluminationWithImportanceSamplingV2
//							(intersection, currentTracable, recursiveDepth).multiply(1);
					
					/* Direct visualisation of indirect illumination */
					indirect = calculateIndirectIlluminationEstimate(intersection, currentTracable).multiply(500f);
					
//					indirect = this.calculateIndirectIlluminationAccurate(r, intersection, currentTracable).multiply(10f); // I think standard Monte Carlo
					
//					System.out.println(indirect);
				}
				
//				indirect = this.calculateIndirectIlluminationWithImportanceSampling(intersection, currentTracable).multiply(6);
//				indirect = calculateRadianceFromGlobalPhotonMap(intersection, currentTracable);
//				indirect = calculateIndirectIlluminationAccurate(r,intersection,currentTracable).multiply(0.2f);
//				indirect = directlyVisualiseGlobalPhotonMap(intersection,currentTracable);
				currentColor = direct.add(caustics.add(indirect));
//				currentColor = 
//						(calculateDirectIllumination(intersection,currentTracable));
//						.add(this.finalGather(r, intersection, currentTracable))).divide(2f);
//						.add(calculateCaustics(intersection, currentTracable)));
//						.add(calculateRadianceFromGlobalPhotonMap(intersection, currentTracable)));
//						.add(calculateRadianceFromGlobalPhotonMapTest(r,intersection, currentTracable)));
//						.add(calculateIndirectIlluminationAccurate(r,intersection,currentTracable)));
			} else {
				currentColor = calculateSpecular(r,intersection,currentTracable,recursiveDepth);
			}
			
			
//			/*
//			 * Part 1: Direct Illumination
//			 * TODO Currently not sending out shadow rays at borders, and instead averaging out the radiance
//			 */
//			Vector direct = calculateDirectIllumination(intersection,currentTracable);
//			
//			
//			/*
//			 * Part 2: Specular & Glossy Reflections
//			 * -> Currently just using fresnel
//			 * -> Not doing importance sampling for glossy surfaces as I don't have glossy surfaces
//			 */
//			Vector specular = new Vector(0,0,0);
//			if (currentTracable.isReflective() || currentTracable.isRefractive()) {
//				specular = calculateSpecular(r,intersection,currentTracable,recursiveDepth);
//			}
//			
//			
//			/*
//			 * Part 3: Caustics
//			 */
//			Vector caustics = calculateCaustics(intersection, currentTracable);
////			Vector caustics = new Vector(0,0,0);
//			
//			
//			
//			/*
//			 * Part 4: Multiple diffuse reflections
//			 */
//			Vector diffuse = new Vector(0,0,0);
////			Vector diffuse = calculateIndirectIlluminationEstimate(intersection,currentTracable); //For estimate
////			Vector diffuse = calculateIndirectIlluminationAccurate(r,intersection,currentTracable); //For accurate
//			
//			
//			//Colour is sum of components
//			currentColor = direct.add(specular).add(caustics).add(diffuse);
			
			currentColor = currentColor.add(currentTracable.getEmissiveValue());
			
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
	
	public Vector calculateDirectIlluminationAccurate(Point intersection, Tracable currentTracable) {
		Vector lnorm = new Vector(light.subtract(intersection));
		lnorm.normalise();
		Vector snorm = currentTracable.getNormal(intersection);
		snorm.normalise();
		float diffuseCoefficient = (float) Math.max(0.0, snorm.dot(lnorm));
		return new Vector(currentTracable.getColor()).multiply(diffuseCoefficient);
	}
	
	public Vector calculateDirectIllumination(Point intersection, Tracable currentTracable) {
		Vector direct = new Vector(0,0,0);
//		int numDirectPhotons = 100; //Using only 100 photons
		int numDirectPhotons = NUM_PHOTONS_SEARCHING_FOR;
		//long start1 = System.nanoTime();
		PhotonMaxHeap directHeap = new PhotonMaxHeap(numDirectPhotons);
		
		//Initialise heap with first N photons (shadow or illumination only)
		int count = 0;
		Photon p;
		for (int i=0; i<globalPhotons.size(); i++) {
			p = globalPhotons.get(i);
			if (p.isIlluminationPhoton() || p.isShadowPhoton()) {
				directHeap.insert(p, intersection.euclideanDistance(p.getPosition()));
				count++;
			}
			if (count >= numDirectPhotons) {
				break;
			}
		}
		
		//Find nearest N photons (shadow or illumination only)
		globalMap.getNearestNeighboursDirectIllumination(intersection, currentTracable.getNormal(intersection), directHeap, this.MAX_SEARCH_DISTANCE_DIRECT);
//		globalMap.getNearestNeighboursDirectIllumination(intersection, directHeap, this.MAX_SEARCH_DISTANCE_DIRECT);
		//long finish1 = System.nanoTime();
		//System.out.println((finish1-start1) + "ns.");
//		globalMap.getNearestNeighboursDirectIllumination(intersection, directHeap,MAX_SEARCH_DISTANCE_DIRECT);
		int nI = directHeap.getNumIlluminationPhotons();
		int nS = directHeap.getNumShadowPhotons();
//		System.out.println(nI + " | " + nS + " | " + directHeap.getSize() + " | " + directHeap.getMaxSize() + " | " + directHeap.testSize());
		
		boolean inShadow = false;
		if (nI < 0) {
			inShadow = true;
		} else if (nS > 0) {
			//Cast shadow ray (Not using kd-tree)
			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
			toLightV.normalise();
			Ray toLightR = new Ray(intersection, toLightV);
			Sphere s = new Sphere(light,0.5f);
			double tLight = s.getIntersect(toLightR);
			for (Object o: tracableObjects) {
    			if (o != currentTracable) {
    				Tracable temp = (Tracable) o;
    				double tempt = temp.getIntersect(toLightR);
    				if (tempt >= 0 && tempt < tLight) {
    					inShadow = true;
    				}
    			}
    		}
		} else {
			inShadow = false;
		}
		
		if (!inShadow) { //Only illumination photon nearby
//			float k = 0.01f;
//			direct = directHeap.getAverageColourConeFilter(k).multiply(new Vector(currentTracable.getColor())); //Cone filter
//			direct = directHeap.getAverageColour().multiply(new Vector(currentTracable.getColor())); //No filtering
			direct = this.calculateDirectIlluminationAccurate(intersection, currentTracable);
		}
		
		return direct;
	}
	
	public Vector calculateDirectIlluminationWithShadowRays(Point intersection, Tracable currentTracable) {
		Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
		toLightV.normalise();
		Ray toLightR = new Ray(intersection, toLightV);
		Sphere s = new Sphere(light,0.5f);
		double tLight = s.getIntersect(toLightR);
		boolean inShadow = false;
		for (Object o: tracableObjects) {
			if (o != currentTracable) {
				Tracable temp = (Tracable) o;
				double tempt = temp.getIntersect(toLightR);
				if (tempt >= 0 && tempt < tLight) {
					inShadow = true;
				}
			}
		}
		if (!inShadow) {
			return calculateDirectIlluminationAccurate(intersection,currentTracable);
		}
		return new Vector(0,0,0);
	}
	
	public Vector calculateDirectIlluminationWithAreaLight(Point intersection, Tracable currentTracable) {
		return new Vector(currentTracable.getColor()).multiply(calculateRadianceWithAreaLight(intersection, currentTracable)).multiply(0.6f);
	}
	
	public float calculateRadianceWithAreaLight(Point intersection, Tracable currentTracable) {
		float total = 0;
		
		float u,v;
		Point samplePoint;
		Ray shadowRay;
		boolean inShadow;
		double tLight;
		for (int i=0; i<AREA_LIGHT_NUM_SAMPLES; i++) {
			u = i/AREA_LIGHT_NUM_SAMPLES;
			for (int j=0; j<AREA_LIGHT_NUM_SAMPLES; j++) {
				v = j/AREA_LIGHT_NUM_SAMPLES;
				samplePoint = this.areaLight.getPointOnLight(u, v);
				Vector lightDir = new Vector(samplePoint.subtract(intersection));
				lightDir.normalise();
				shadowRay = new Ray(intersection,lightDir);
				inShadow = false;
				tLight = areaLight.getIntersect(shadowRay);
				
				if (tLight >= 0) { //Only sample if light ray intersects the light (ie ceiling is "behind" light)
					for (Object o: tracableObjects) {
						if (o != currentTracable) {
							Tracable temp = (Tracable) o;
							double tempt = temp.getIntersect(shadowRay);
							if (tempt >= 0 && tempt < tLight) {
								inShadow = true;
							}
						}
					}
					if (!inShadow) {
						Vector lnorm = new Vector(samplePoint.subtract(intersection));
						lnorm.normalise();
						Vector snorm = currentTracable.getNormal(intersection);
						snorm.normalise();
						float diffuseCoefficient = (float) Math.max(0.0, snorm.dot(lnorm));
						total += diffuseCoefficient;
					}
				}
			}
		}
		total /= AREA_LIGHT_NUM_SAMPLES*AREA_LIGHT_NUM_SAMPLES;
		return total;
	}
	
	/*
	 * Doesn't work :(
	 */
	public Vector calculateRadianceFromPhotonMap(Point intersection, Tracable currentTracable) {
		int numPhots = 100;
		PhotonMaxHeap heap = new PhotonMaxHeap(numPhots);
		Photon p;
		for (int i=0; i<numPhots; i++) {
			p = globalPhotons.get(i);
			heap.insert(p, intersection.euclideanDistance(p.getPosition()));
		}
		globalMap.getNearestNeighboursAll(intersection,currentTracable.getNormal(intersection), heap, this.MAX_SEARCH_DISTANCE_DIRECT*this.MAX_SEARCH_DISTANCE_DIRECT);
		return heap.getAverageColourConeFilter(1f).multiply(new Vector(currentTracable.getColor()));
	}
	
	public Vector testFinalGather(Point intersection, Tracable currentTracable) {
		int numSamples = 10;
		Vector N = currentTracable.getNormal(intersection);
		Sphere s = new Sphere(new Point(0,0,0),1f);
		
		Ray r;
		Vector d;
		float accum = 0;
		for (int i=0; i<numSamples; i++) {
			d = s.generateRandomUnitVector();
			if (d.dot(N) <= 0) {
				d = d.multiply(-1);
			}
			r = new Ray(intersection, d);
			
			Tracable hitTracable = tracableObjects.get(0);
			double t2 = -1;
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(r);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t2 < 0 && tempT > t2) || (t2 >= 0 && tempT < t2 && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t2 = tempT;
					hitTracable = temp;
				}
			}
			Point intersection2 = r.getPoint(t2);
			
			if (t2 >= 0) {
				accum += (calculateRadianceWithAreaLight(intersection2, hitTracable));
			}
		}
		
		accum /= numSamples;
		
		return new Vector(currentTracable.getColor()).multiply(accum);
	}
	
	public Vector calculateDirectIlluminationNoColour(Point intersection, Tracable currentTracable) {
		Vector direct = new Vector(0,0,0);
		int numDirectPhotons = 10; //Using only 100 photons
//		int numDirectPhotons = NUM_PHOTONS_SEARCHING_FOR;
		//long start1 = System.nanoTime();
		PhotonMaxHeap directHeap = new PhotonMaxHeap(numDirectPhotons);
		
		//Initialise heap with first N photons (shadow or illumination only)
		int count = 0;
		Photon p;
		for (int i=0; i<globalPhotons.size(); i++) {
			p = globalPhotons.get(i);
			if (p.isIlluminationPhoton() || p.isShadowPhoton()) {
				directHeap.insert(p, intersection.euclideanDistance(p.getPosition()));
				count++;
			}
			if (count >= numDirectPhotons) {
				break;
			}
		}
		
		//Find nearest N photons (shadow or illumination only)
		globalMap.getNearestNeighboursDirectIllumination(intersection, currentTracable.getNormal(intersection), directHeap, this.MAX_SEARCH_DISTANCE_DIRECT);
		int nI = directHeap.getNumIlluminationPhotons();
		if (nI > 0) { //Only illumination photon nearby
			float k = 1;
			direct = directHeap.getAverageColourConeFilter(k); //Cone filter
//			direct = directHeap.getAverageColour().multiply(new Vector(currentTracable.getColor())); //No filtering
		}
		
		return direct;
	}
	
	/*
	 * Assume currentTracable is specular (reflective or refractive)
	 */
	public Vector calculateSpecular(Ray r, Point intersection, Tracable currentTracable, int recursiveDepth) {
		Vector specular = new Vector(0,0,0);
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
//			System.out.println(rS + " " + rP + " " + cost + " " + cosi);
			kr = (rS * rS + rP * rP) / 2f;
		}
		float kt = 1-kr;
		if (currentTracable.isRefractive()) {
			float temp = kt;
			kt = kr;
			kr = temp;
		}
		if (kr < 1) {
			specular = refract(currentTracable,kr,r,specular,intersection,recursiveDepth);
		}
		specular = reflect(currentTracable,kt,r,specular,intersection,recursiveDepth);
		
		return specular;
	}
	
	public Vector calculateCaustics(Point intersection, Tracable currentTracable) {
		Vector caustics = new Vector(0,0,0);
		int numPhots = 100;
		//Initialise heap
		PhotonMaxHeap heap = new PhotonMaxHeap(numPhots);
//		for (int i=0; i<numPhots; i++) {
//			Photon p2 = causticsPhotons.get(i);
//			heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
//		}
		
		causticsMap.getNearestNeighbours(intersection,heap,MAX_SEARCH_DISTANCE_CAUSTICS);
		
		float k = 1;
		caustics = heap.getAverageColourConeFilter(k).multiply(new Vector(currentTracable.getColor()));
		
		return caustics;
	}
	
	public Vector calculateRadianceFromGlobalPhotonMap(Point intersection, Tracable currentTracable) {
		Vector radiance = new Vector(0,0,0);
		//Estimate from global photon map
		PhotonMaxHeap heap = new PhotonMaxHeap(NUM_PHOTONS_SEARCHING_FOR);
//		for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
//			Photon p2 = globalPhotons.get(i);
//			heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
//		}
//		globalMap.getNearestNeighbours(intersection,heap,MAX_SEARCH_DISTANCE_INDIRECT);
		globalMap.getNearestNeighbours(intersection,currentTracable.getNormal(intersection),heap,MAX_SEARCH_DISTANCE_INDIRECT);
		
		float k = 1;
		radiance = heap.getAverageColourConeFilter(k).multiply(new Vector(currentTracable.getColor()));
		
		return radiance.divide(4);
	}
	
	public Vector directlyVisualiseGlobalPhotonMap(Point intersection,Tracable currentTracable) {
		Vector col = new Vector(0,0,0);
		PhotonMaxHeap heap = new PhotonMaxHeap(NUM_PHOTONS_SEARCHING_FOR);
		int count=0;
//		for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
		for (int i=0; i<this.globalPhotons.size(); i++) {
			Photon p2 = globalPhotons.get(i);
			if (!(p2.isIlluminationPhoton() || p2.isShadowPhoton())) {
				heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
				count++;
			}
			if (count >= NUM_PHOTONS_SEARCHING_FOR) break;
		}
//		globalMap.getNearestNeighbours(intersection,heap,MAX_SEARCH_DISTANCE_INDIRECT);
		globalMap.getNearestNeighbours(intersection,currentTracable.getNormal(intersection),heap,MAX_SEARCH_DISTANCE_INDIRECT);
		
		float k = 1;
		col = heap.getAverageColourConeFilter(k);
		return col;
	}
	
	public Vector calculateRadianceFromGlobalPhotonMapTest(Ray r,Point intersection, Tracable currentTracable) {
		Vector radiance = new Vector(0,0,0);
		//Estimate from global photon map
		PhotonMaxHeap heap = new PhotonMaxHeap(NUM_PHOTONS_SEARCHING_FOR);
//		for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
//			Photon p2 = globalPhotons.get(i);
//			heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
//		}
//		globalMap.getNearestNeighbours(intersection,heap,MAX_SEARCH_DISTANCE_INDIRECT);
		globalMap.getNearestNeighbours(intersection,currentTracable.getNormal(intersection),heap,MAX_SEARCH_DISTANCE_INDIRECT);
		
		float k = 1;
		radiance = heap.getAverageColourConeFilter(k).multiply(new Vector(currentTracable.getColor()));
		Vector norm = currentTracable.getNormal(intersection);
		norm.normalise();
		Photon[] phots = heap.getPhotons();
		Vector lightDir = new Vector(0,0,0);
		for (int i=0; i<heap.getSize(); i++) {
			lightDir = lightDir.add(phots[i].getIncidentDirection());
		}
		lightDir = lightDir.divide(heap.getSize());
		lightDir.normalise();
		double diffCoef = Math.max(0.0, norm.dot(lightDir));
		radiance = radiance.multiply(diffCoef);
		
		return radiance;
	}
	
	public Vector finalGather(Ray r, Point intersection, Tracable currentTracable) {
		Vector finalGather = new Vector(0,0,0);
		int numRays = 10;
		Vector currentCol = new Vector(currentTracable.getColor());
		for (int i=0; i<numRays; i++) {
//			diffuse = diffuse.add(diffuseReflect(currentTracable,diffuse,intersection,diffusePerc/numRays,recursiveDepth-1));
			Vector diffuseDir = this.calculateDiffuseDir(currentTracable, r, intersection);
			Ray diffuseR = new Ray(intersection, diffuseDir);
			
			Tracable hitTracable = tracableObjects.get(0);
			double t2 = -1;
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(diffuseR);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t2 < 0 && tempT > t2) || (t2 >= 0 && tempT < t2 && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t2 = tempT;
					hitTracable = temp;
				}
			}
			
			if (t2 >= 0 && hitTracable.isDiffuse()) {
				Point intersection2 = diffuseR.getPoint(t2);
//				Vector radiance = this.calculateRadianceFromGlobalPhotonMap(intersection2, hitTracable);
				Vector radiance = this.calculateDirectIlluminationNoColour(intersection2, hitTracable);
				finalGather = finalGather.add(radiance.multiply(currentCol).divide(numRays));
//				diffuse = diffuse.add(
//						(new Vector(currentTracable.getColor()).multiply(radiance)).divide(numRays));
//				diffuse = diffuse.add(new Vector(hitTracable.getColor()).multiply(diffusePerc).divide(numRays));
			}
		}
		return finalGather;
	}
	
	public Vector calculateIndirectIlluminationAccurate(Ray r, Point intersection, Tracable currentTracable) {
		Vector diffuse = new Vector(0,0,0);
		Vector N = currentTracable.getNormal(intersection);
		int numRays = 10;
		float diffusePerc = 0.1f;
//		currentColor = currentColor.multiply(1-diffusePerc);
		double diffuseCoeff;
		for (int i=0; i<numRays; i++) {
//			diffuse = diffuse.add(diffuseReflect(currentTracable,diffuse,intersection,diffusePerc/numRays,recursiveDepth-1));
			Vector diffuseDir = this.calculateDiffuseDir(currentTracable, r, intersection);
			Ray diffuseR = new Ray(intersection, diffuseDir);
			
			Tracable hitTracable = tracableObjects.get(0);
			double t2 = -1;
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(diffuseR);
//				if (tempT > t && !Double.isNaN(tempT) && !Double.isInfinite(tempT)) {
				if ((t2 < 0 && tempT > t2) || (t2 >= 0 && tempT < t2 && tempT >= 0)) {
					//System.out.println("replacing t:" + t + " with tempT:" + tempT);
					t2 = tempT;
					hitTracable = temp;
				}
			}
			if (t2 >= 0 && hitTracable.isDiffuse()) {
				diffuseCoeff = Math.max(0.0, N.dot(diffuseDir));
				Point intersection2 = diffuseR.getPoint(t2);
//				Vector radiance = this.calculateRadianceFromGlobalPhotonMap(intersection2, hitTracable);
				Vector radiance = this.calculateDirectIlluminationWithAreaLight(intersection2, hitTracable);
				diffuse = diffuse.add(
						(new Vector(currentTracable.getColor()).multiply(radiance)).divide(numRays)).multiply(diffuseCoeff);
//				diffuse = diffuse.add(new Vector(hitTracable.getColor()).multiply(diffusePerc).divide(numRays));
			}
		}
		
		return diffuse;
	}
	
	public Vector calculateIndirectIlluminationEstimate(Point intersection, Tracable currentTracable) {
		int numPhots = 500;
		Vector diffuse = new Vector(0,0,0);
		PhotonMaxHeap heap = new PhotonMaxHeap(numPhots);
		int count=0;
		Vector norm = currentTracable.getNormal(intersection);
//		for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
		for (int i=0; i<this.globalPhotons.size(); i++) {
			Photon p2 = globalPhotons.get(i);
			if (!(p2.isIlluminationPhoton() || p2.isShadowPhoton()) && (p2.getSurfaceNormal() == norm)) {
				heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
				count++;
			}
			if (count >= numPhots) break;
		}
		this.globalMap.getNearestNeighbours(intersection, currentTracable.getNormal(intersection), heap, this.MAX_SEARCH_DISTANCE_INDIRECT); //With surface normal
		
		Photon[] ls = heap.getPhotons();
		for (int i=0; i<heap.getSize(); i++) {
			diffuse = diffuse.add(ls[i].getEnergy());
		}
		
		float fixingConstantFor10kPhotons = 0.06f;
//		float fixingConstantFor10kPhotons = 1f;
		return diffuse.divide(numPhots * Math.PI * this.MAX_SEARCH_DISTANCE_INDIRECT)
				.multiply(new Vector(currentTracable.getColor())).multiply(fixingConstantFor10kPhotons);
//		return diffuse.divide(numPhots * Math.PI * this.MAX_SEARCH_DISTANCE_INDIRECT).multiply(currentTracable.getDiffusePercent());
	}
	
	public Vector calculateIndirectIlluminationWithImportanceSampling(Point intersection, Tracable currentTracable, int recursiveDepth) {
		Vector diffuse = new Vector(0,0,0);
//		System.out.println("Intersection at: " + intersection);
		
		//Initialise heap
		PhotonMaxHeap heap = new PhotonMaxHeap(this.NUM_PHOTONS_SEARCHING_FOR);
		int count=0;
		Vector norm = currentTracable.getNormal(intersection);
//		for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
		for (int i=0; i<this.globalPhotons.size(); i++) {
			Photon p2 = globalPhotons.get(i);
			if (!(p2.isIlluminationPhoton() || p2.isShadowPhoton()) && (p2.getSurfaceNormal() == norm)) {
				heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
				count++;
			}
			if (count >= NUM_PHOTONS_SEARCHING_FOR) break;
		}
		
//		this.globalMap.getNearestNeighbours(intersection, heap, this.MAX_SEARCH_DISTANCE_INDIRECT); //Without surface normal
		this.globalMap.getNearestNeighbours(intersection, currentTracable.getNormal(intersection), heap, this.MAX_SEARCH_DISTANCE_INDIRECT); //With surface normal
//		this.globalMap.getNearestNeighbours(intersection, currentTracable.getNormal(intersection), heap, 10*10); //With surface normal
		UnitSquare unitSquare = new UnitSquare(50);
		Photon[] photons = heap.getPhotons();
		for (int i=0; i<heap.getSize(); i++) {
			unitSquare.insertVector(photons[i].getIncidentDirection().multiply(-1)); //Inverted incident direction
			
//			unitSquare.insertVector(photons[i].getIncidentDirection()); //normal incident direction
		}
//		unitSquare.sort();
		unitSquare.createCumulativeFrequencies();
		int numRays = 100;
		for (int i=0; i<numRays; i++) {
			double randomValue = Math.random();
			Vector diffuseDir = unitSquare.getDirection(randomValue);
			float scalingFactor = (float) diffuseDir.magnitude();
			diffuseDir.normalise();
			Ray diffuseR = new Ray(intersection, diffuseDir);
			
			Tracable hitTracable = tracableObjects.get(0);
			double t2 = -1;
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(diffuseR);
				if ((t2 < 0 && tempT > t2) || (t2 >= 0 && tempT < t2 && tempT >= 0)) {
					t2 = tempT;
					hitTracable = temp;
				}
			}
			if (t2 >= 0 && hitTracable.isDiffuse()) {
				Point intersection2 = diffuseR.getPoint(t2);
				diffuse = diffuse.add(
						new Vector(hitTracable.getColor()).divide(numRays).multiply(scalingFactor));
//						.multiply(this.calculateDirectIlluminationWithAreaLight(intersection2, hitTracable)));
//						.multiply(Math.abs(currentTracable.getNormal(intersection).dot(diffuseDir))
//						); //Multiply by cos
			}
		}
		
		return diffuse;
	}
	
	public Vector calculateIndirectIlluminationWithImportanceSamplingV2(Point intersection, Tracable currentTracable, int recursiveDepth) {
		Vector diffuse = new Vector(0,0,0);
		
		//Initialise heap
		PhotonMaxHeap heap = new PhotonMaxHeap(this.NUM_PHOTONS_SEARCHING_FOR);
		
		int count=0;
		Vector norm = currentTracable.getNormal(intersection);
//		for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
		for (int i=0; i<this.globalPhotons.size(); i++) {
			Photon p2 = globalPhotons.get(i);
			if (!(p2.isIlluminationPhoton() || p2.isShadowPhoton()) && (p2.getSurfaceNormal() == norm)) {
				heap.insert(p2, intersection.euclideanDistance(p2.getPosition()));
				count++;
			}
			if (count >= NUM_PHOTONS_SEARCHING_FOR) break;
		}
		
//		this.globalMap.getNearestNeighbours(intersection, heap, this.MAX_SEARCH_DISTANCE_INDIRECT); //Without surface normal
		this.globalMap.getNearestNeighbours(intersection, currentTracable.getNormal(intersection), heap, this.MAX_SEARCH_DISTANCE_INDIRECT); //With surface normal
//		this.globalMap.getNearestNeighbours(intersection, currentTracable.getNormal(intersection), heap, 10*10); //With surface normal
		UnitSquare unitSquare = new UnitSquare(30);
		Photon[] photons = heap.getPhotons();
		for (int i=0; i<heap.getSize(); i++) {
			unitSquare.insertVector(photons[i].getIncidentDirection().multiply(-1)); //Inverted incident direction
			
//			unitSquare.insertVector(photons[i].getIncidentDirection()); //normal incident direction
		}
//		unitSquare.sort();
		unitSquare.createCumulativeFrequencies();
//		unitSquare.print();
		int numRays = 100;
		Sphere s = new Sphere(new Point(0,0,0), 1f);
		for (int i=0; i<numRays; i++) {
			
			/* Generating random rays with importance sampling (Just for testing) */
			double randomValue = Math.random();
			Vector diffuseDir = unitSquare.getDirection(randomValue);
			float scalingFactor = (float) diffuseDir.magnitude();
			diffuseDir.normalise();
			
			/* Generating random rays with Monte Carlo */
//			Vector diffuseDir = s.generateRandomUnitVector();
//			if (diffuseDir.dot(currentTracable.getNormal(intersection)) < 0) {
//				diffuseDir = diffuseDir.multiply(-1);
//			}
			
			Ray diffuseR = new Ray(intersection.add(new Point(diffuseDir).multiply(0.00001f)), diffuseDir);
			Tracable hitTracable = tracableObjects.get(0);
			double t2 = -1;
			for (Object o: tracableObjects) {
				Tracable temp = (Tracable) o;
				double tempT = temp.getIntersect(diffuseR);
				if ((t2 < 0 && tempT > t2) || (t2 >= 0 && tempT < t2 && tempT >= 0)) {
					t2 = tempT;
					hitTracable = temp;
				}
			}
			double diffuseCoeff = Math.abs(norm.dot(diffuseDir));
			double f = currentTracable.getDiffusePercent();
			if (t2 >= 0 && hitTracable.isDiffuse()) {
				Point intersection2 = diffuseR.getPoint(t2);
//				
//				diffuse = diffuse.add(this.calculateRadianceFromPhotonMap(intersection2, hitTracable)).divide(scalingFactor).multiply(diffuseCoeff);
//						.multiply(diffuseCoeff*scalingFactor);
				
				diffuse = diffuse.add(new Vector(hitTracable.getColor())
						.multiply(this.calculateRadianceWithAreaLight(intersection2, hitTracable)))
						.multiply(diffuseCoeff*scalingFactor);
			}
		}
		
		diffuse = diffuse.multiply(new Vector(currentTracable.getColor())).multiply(currentTracable.getDiffusePercent());
//		System.out.println(diffuse);
		return diffuse;
//		return diffuse.multiply(new Vector(currentTracable.getColor())).multiply(currentTracable.getDiffusePercent());
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
					
			} else { 
				PhotonMaxHeap heap = new PhotonMaxHeap(NUM_PHOTONS_SEARCHING_FOR);
				for (int i=0; i<NUM_PHOTONS_SEARCHING_FOR; i++) {
					Photon p = globalPhotons.get(i);
					heap.insert(p, intersection.euclideanDistance(p.getPosition()));
				}
				
				globalMap.getNearestNeighbours(intersection,heap,MAX_SEARCH_DISTANCE_DIRECT);
				
				Photon[] ls = heap.getPhotons();
//				System.out.println(heap);
				Vector temp = new Vector(0,0,0);
				Vector normal = currentTracable.getNormal(intersection);
				for (Photon p: ls) {
					if (p.getSurfaceNormal() == normal) {temp = temp.add(p.getEnergy());}
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
		
		Point reflectOrig = intersection.add(new Point(reflectedDir).multiply(0.000001f));
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
//		double eta = currentTracable.getRefractiveIndex();
		double nDotI = n.dot(i);
//		double k = 1.0 - eta*eta*(1.0- nDotI*nDotI);
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
//			return new Vector(0,0,1);
		} else {
			Vector out = n.multiply((eta * cosi - Math.sqrt(k))).add(i.multiply(eta));
//			Vector out = i.multiply(eta).minus(n.multiply(eta*nDotI+Math.sqrt(k)));
//			Vector out = (i.minus(n.multiply(nDotI))).multiply(eta).minus(n.multiply(Math.sqrt(k)));
			out.normalise();
			
			Ray refractedRay = new Ray(intersection.add(new Point(out).multiply(0.001f)), out);
			//if (intersection == r.o()) { //Accounts for rounding error, advances point forward if is same as current ray origin
//				Point p = intersection.add(new Point(r.d()).multiply(0.001f));
//				Point p = intersection.add(new Point(r.d()));
//				refractedRay = new Ray(p, out);
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
			
			Ray refractedRay = new Ray(intersection.add(new Point(out).multiply(0.001f)), out);
//			Ray refractedRay = new Ray(intersection, out);
//			//if (intersection == r.o()) { //Accounts for rounding error, advances point forward if is same as current ray origin
//				Point p = intersection.add(new Point(r.d()).multiply(0.001f));
//				refractedRay = new Ray(p, out);
//			//}
			
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
//		Vector surfaceNorm = currentTracable.getNormal(intersection);
//		float radius = 100;
////		float radius = y_axis;
//		Sphere s = new Sphere(intersection.add(new Point(surfaceNorm)),radius);
//		
//		Vector reflectDir = new Vector(s.generateRandomUnitPoint().add(s.c()).subtract(intersection));
//		reflectDir.normalise();
//		return reflectDir;
		
		Vector N = currentTracable.getNormal(intersection);
		Sphere s = new Sphere(new Point(0,0,0),1);
		Vector d = s.generateRandomUnitVector();
		d.normalise();
		if (d.dot(N) <= 0) {
			return d.multiply(-1);
		} else {
			return d;
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
		totalTime += lastDuration;
		System.out.println("Duration: " + lastDuration + "ms.");
	}
	
	public void renderGlobalPhotonMap(WritableImage img) {
		double w= img.getWidth(), h= img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
//        light = new Point(lightX,y_axis/2,z_axis/2);
        camera = new Point((x_axis)/2.0f,(y_axis)/2.0f,-10f);
                
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
//        		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
    			double x = ((double)i/w)*x_axis;
    			double y = ((double)j/h)*y_axis;
//    			System.out.println(i + " " + j + " -> " + x + " " + y);
    			Vector rayVec = new Vector(x-camera.x(), y-camera.y(), -camera.z());
        		rayVec.normalise();
//        		Ray r = new Ray(new Point(i,j,-1), rayVec); //Persepective projection
        		Ray r = new Ray(new Point(x,y,0),rayVec);
        		
        		image_writer.setColor(i, j, testTrace(r,MAX_RECURSIVE_DEPTH)); //Standard render
        	}
//        	System.out.println("Row " + (j+1) + "/" + (int)h + " completed!");
        }
	}
	
	public void renderCausticsPhotonMap(WritableImage img) {
		double w= img.getWidth(), h= img.getHeight();
        PixelWriter image_writer = img.getPixelWriter();
//        light = new Point(lightX,y_axis/2,z_axis/2);
        camera = new Point((x_axis)/2.0f,(y_axis)/2.0f,-10f);
                
        for (int j=0; j<h; j++) {
        	for (int i=0; i<w; i++) {
//        		Vector rayVec = new Vector(i-camera.x(), j-camera.y(), -camera.z());
    			double x = ((double)i/w)*x_axis;
    			double y = ((double)j/h)*y_axis;
//    			System.out.println(i + " " + j + " -> " + x + " " + y);
    			Vector rayVec = new Vector(x-camera.x(), y-camera.y(), -camera.z());
        		rayVec.normalise();
//        		Ray r = new Ray(new Point(i,j,-1), rayVec); //Persepective projection
        		Ray r = new Ray(new Point(x,y,0),rayVec);
        		
        		image_writer.setColor(i, j, traceCausticsMap(r,MAX_RECURSIVE_DEPTH)); //Standard render
        	}
//        	System.out.println("Row " + (j+1) + "/" + (int)h + " completed!");
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
			Sphere s = new Sphere(intersection,0.05f);
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
		
		currentColor.normalise(); //Normalise so displayed nicer
		//Colour normalisation between 0 and 1
		double red = currentColor.dx();
		double green = currentColor.dy();
		double blue = currentColor.dz();
		if (green>1) {green=1;} if (green<0) {green=0;} 
		if (blue>1) {blue=1;} if (blue<0) {blue=0;}
		if (red>1) {red=1;} if (red<0) {red=0;}
		
		return Color.color(red,green,blue,1.0);
	}
	
	public Color traceCausticsMap(Ray r, int recursiveDepth) {
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
			Sphere s = new Sphere(intersection,0.05f);
//			Sphere s = new Sphere(intersection,PHOTON_SEARCH_RADIUS);
			int N = 1;
			causticsMap.getNearestNeighbours(s, nearestNeighbours, N);
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
		
		currentColor.normalise();
		//Colour normalisation between 0 and 1
		double red = currentColor.dx();
		double green = currentColor.dy();
		double blue = currentColor.dz();
		if (green>1) {green=1;} if (green<0) {green=0;} 
		if (blue>1) {blue=1;} if (blue<0) {blue=0;}
		if (red>1) {red=1;} if (red<0) {red=0;}
		
		return Color.color(red,green,blue,1.0);
	}
}

