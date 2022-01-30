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
	private int x_axis = 750/3; private int y_axis = 800/3; private int z_axis = 750/3; //For mini scene
	
	
	
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
    private static int numMisses,numDiffuse,numSpecular,numRefract,numAbsorbed;
    
    private ArrayList<Photon> globalPhotons = new ArrayList<Photon>();
    private PhotonMap globalMap;
	
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
//		globalMap = new PhotonMap(bb.getMax(),bb.getMax());
		globalMap = new PhotonMap(bb.getMin(), bb.getMax());
		//System.out.println(bb);
		
		FlowPane root = new FlowPane();
		root.setVgap(8);
        root.setHgap(4);
        
        VBox vb = new VBox();
        vb.getChildren().addAll(imgView);
        root.getChildren().addAll(vb);
        
        traceActualScene(img);
        
        Scene scene = new Scene(root, x_axis, y_axis+20);
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
		
		startTimer("Building photon maps");
		buildGlobalPhotonMap(100000);
		finishTimer();
		
		startTimer("Rendering Scene.");
		render(img);
		finishTimer();
		
		System.out.println("Total time: " + totalTime);
	}
	
	public void initialiseActualScene() {
		int rad1 = 150/3; //For mini
//		int rad1 = 150; //For normal
		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-rad1*2),rad1);
//		Sphere s1 = new Sphere(new Point(x_axis/3,y_axis-rad1,z_axis-200),rad1);
		s1.setColor(Color.WHITE);
		s1.setReflectedPercent(0.9);
		s1.setRefractiveIndex(1.5f);
		s1.setSpecular(Color.BLACK);
		tracableObjects.add(s1);
		
//		int rad2 = 100; //For normal
		int rad2 = 100/3; //for mini
//		int padding = 50; //for normal
		int padding = 0; //for mini
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
        floor.setDiffusePercent(0.5);
        
        Rectangle wall1 = new Rectangle(
        		new Point(0,0,0),
        		new Point(0,0,z_axis),
        		new Point(0,y_axis,z_axis),
        		new Point(0,y_axis,0));
        wall1.setColor(Color.color(0.5, 0, 0));
        wall1.setSpecular(Color.color(0, 0, 0));
        wall1.setDiffusePercent(0.5f);
        
        Rectangle wall2 = new Rectangle(
        		new Point(0,0,z_axis),
        		new Point(x_axis,0,z_axis),
        		new Point(x_axis,y_axis,z_axis),
        		new Point(0,y_axis,z_axis));
//        wall2.setColor(Color.color(0.5, 0.2, 0));
        wall2.setColor(Color.color(0.7,0.7,0.7));
        wall2.setSpecular(Color.color(0, 0, 0));
        wall2.setDiffusePercent(0.5);
        
        Rectangle wall3 = new Rectangle(
        		new Point(x_axis,0,z_axis),
        		new Point(x_axis,0,0),
        		new Point(x_axis,y_axis,0),
        		new Point(x_axis,y_axis,z_axis));
        wall3.setColor(Color.color(0, 0.5, 0));
        wall3.setSpecular(Color.color(0, 0, 0));
        wall3.setDiffusePercent(0.5f);
        
        Rectangle wall4 = new Rectangle(
        		new Point(x_axis,0,0),
        		new Point(0,0,0),
        		new Point(0,y_axis,0),
        		new Point(x_axis,y_axis,0));
        wall4.setColor(Color.color(0.7, 0.7, 0.7));
        wall4.setSpecular(Color.color(0, 0, 0));
        wall4.setDiffusePercent(0.5f);
        
        Rectangle ceiling = new Rectangle(
        		new Point(0,0,0),
        		new Point(x_axis,0,0),
        		new Point(x_axis,0,z_axis),
        		new Point(0,0,z_axis));
        ceiling.setColor(Color.color(0.7,0.7,0.7));
        ceiling.setSpecular(Color.color(0, 0, 0));
        ceiling.setDiffusePercent(0.5f);
        
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
			double y = Math.random() * y_axis;
			double z = Math.random() * z_axis;
			arr[i] = new Photon(new Point(x,y,z), new Vector(0,0,0), new Vector(Math.random(),Math.random(),Math.random()));
//			System.out.println(arr[i]);
		}
		globalMap.build(arr, 20, 1);
	}
	
	/*
	 * Emits light rays into the scene randomly from the light source,
	 * not an ideal generation for the caustics map.
	 */
	public void buildGlobalPhotonMap(int numPhotons) {
		Sphere s = new Sphere(new Point(0,0,0),1); //Just for generating random points, should probs use a static method instead
//		double pow = 1f/numPhotons; //Assuming white light (1,1,1)
		double pow = 1;
		for (int i=0; i<numPhotons; i++) {
			Vector dir = s.generateRandomUnitVector();
//			dir.normalise();
			Ray lightRay = new Ray(light,dir);
			Vector rayPower = new Vector(pow,pow,pow);
			traceLightRay(lightRay,rayPower);
//			System.out.println(lightRay); //For testing
		}
		
//		System.out.println(globalPhotons.size());
//		for (int i=0; i<globalPhotons.size(); i++) {
//			System.out.println(globalPhotons.get(i).getPosition());
//		}
		System.out.println("Num Absorbed: " + numAbsorbed);
		System.out.println("Num Specular: " + numSpecular);
		System.out.println("Num Diffuse: " + numDiffuse);
		System.out.println("Num Refract: " + numRefract);
		System.out.println("Num Missed: " + numMisses);
		
		globalMap.build(globalPhotons.toArray(new Photon[globalPhotons.size()]), 30, 1);
		System.out.println("Number of photons in map: " + globalMap.getNumPhotons());
		
		//Debugging
//		ArrayList<Photon> ls = new ArrayList<Photon>();
//		Sphere sph = new Sphere(new Point(0,y_axis/2,z_axis/2),1000);
//		globalMap.getNearestNeighbours(sph, ls, 10);
//		System.out.println(ls.size());
//		Vector col = new Vector(0,0,0);
//		for (int i=0; i<ls.size(); i++) {
//			col = col.add(ls.get(i).getEnergy());
//		}
//		System.out.println(col);
//		System.out.println(col.divide(ls.size()));
		
//		globalMap.printMap();
	}
	
	/*
	 * Doesn't have a recursive depth, but should eventually terminate
	 * according to russian roulette.
	 */
	public void traceLightRay(Ray r, Vector rayPower) {
		
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
			if (hitTracable.isDiffuse()) {
				//Store photon at intersection
//				double red = Math.random();
//				double green = Math.random();
//				double blue = Math.random();
//				Photon p = new Photon(intersection.multiply(1),r.d(),new Vector(red,green,blue));
				Photon p = new Photon(intersection.multiply(1),r.d(),rayPower);
//				System.out.println(p);
				globalPhotons.add(p);
//				System.out.println("Added photon");
				
				//TODO Store shadow photon at second intersection
			}
			
			//Perform russian roulette
			Vector dir;
			Ray newR;
			switch (hitTracable.russianRoulette(r, intersection)) {
				case 1: //Diffuse reflection
					numDiffuse++;
//					System.out.println("Diffuse reflection");
					//Color becomes reflected color from diffuse surface ie surfaceColor*rayColor -> (0.5,0.2,0)*(1,1,1)
					Vector v = new Vector(hitTracable.getDiffuse());
					Vector newPower = rayPower.multiply(v);
					
					dir = calculateDiffuseDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir)),dir);
					traceLightRay(newR,newPower);
					break;
				case 2: //Specular reflection
					numSpecular++;
//					System.out.println("Specular reflection");
					dir = calculateReflectedDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir)),dir); //Think reflections were getting stuck (intersecting themselves)
					traceLightRay(newR,rayPower); //Power doesn't change
					break;
				case 3: //Refraction
					numRefract++;
//					System.out.println("Refraction");
					dir = calculateReflectedDir(hitTracable,r,intersection);
					newR = new Ray(intersection.add(new Point(dir)),dir);
					traceLightRay(newR,rayPower); //Power doesn't change
					break;
				default: //Absorption
					numAbsorbed++;
					if (hitTracable.isDiffuse()) {
						Photon p = new Photon(intersection,r.d(),new Vector(hitTracable.getColor()));
						globalPhotons.add(p);
					}
//					System.out.println("Absorption");
					//Do nothing, we've already stored the photon above
			}
			
			
		} //else no intersection
		numMisses++;
//		System.out.println("No intersection: " + r);
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
            		
//            		image_writer.setColor(i, j, trace(r,MAX_RECURSIVE_DEPTH)); //Standard render
            		image_writer.setColor(i, j, testTrace(r,MAX_RECURSIVE_DEPTH)); //For photon mapping stuff
        		}
        	}
        	System.out.println("Row " + (j+1) + "/" + h + " completed!");
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
			Sphere s = new Sphere(intersection,50);
			int N = 500;
			globalMap.getNearestNeighbours(s, nearestNeighbours, N);
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
	
	public Color trace(Ray r, int recursiveDepth) {
		if (recursiveDepth < 0) {
//			System.out.println("Recursive depth cut-off reached"); 
//			return Color.BLUE;
			return Color.BLACK;
		} //Default colour for reaching max recursive depth
		
		Vector currentColor = new Vector(0,0,0);
		Point intersection;
		double ambient = 0.3;
		Vector lnorm;
        Vector snorm;
//        Sphere lightSphere = new Sphere(light,1);
		
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

			double diffuse = 0;
			double specular = 0;
			Vector toLightV = new Vector(light.x()-intersection.x(),light.y()-intersection.y(),light.z()-intersection.z());
			toLightV.normalise();
			Ray toLightR = new Ray(intersection, toLightV);
//			double lightSphereIntersect = lightSphere.getIntersect(toLightR);
			
			//Not working when light is in the room !!!!!!
			boolean inShadow = false;
			if (USING_KD_TREES) {
				Tracable inShadowFrom = tree.getTracableIgnoreSelf(toLightR, currentTracable);
				if (inShadowFrom != null) {
					double tInShadowFrom = inShadowFrom.getIntersect(toLightR);
//					System.out.println(inShadowFrom);
					Sphere s = new Sphere(light,0.5f);
					double tLight = s.getIntersect(toLightR);
					inShadow = (tInShadowFrom >= 0 && tInShadowFrom < tLight);
//					inShadow = true;
				}
			} else {
//				for (Object o: tracableObjects) {
//	    			if (o != currentTracable) {
//	    				Tracable temp = (Tracable) o;
//	    				if (temp.getIntersect(toLightR) >= 0) {
//	    					inShadow = true;
//	    				}
//	    			}
//	    		}
				
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
			double mattPerc = currentTracable.getMattPercent();
			
			double red = (traceAmb.getRed()*ambient 
					+ traceDif.getRed()*diffuse + traceSpec.getRed()*specular) * mattPerc;
			double green = (traceAmb.getGreen()*ambient 
					+ traceDif.getGreen()*diffuse + traceSpec.getGreen()*specular) * mattPerc;
			double blue = (traceAmb.getBlue()*ambient 
					+ traceDif.getBlue()*diffuse + traceSpec.getBlue()*specular) * mattPerc;
			currentColor = new Vector(red,green,blue);
			
//			// Calculating Reflections //
//			if (currentTracable.isReflective()) {
//				currentColor = reflect(currentTracable,currentTracable.getReflectedPercent(),r,currentColor,intersection,recursiveDepth);
//			}
//			
//			//Refraction
//			if (currentTracable.isRefractive()) {
//				currentColor = refract(currentTracable,currentTracable.getRefractedPercent(),r,currentColor,intersection,recursiveDepth);
//			} //End of refraction
			
			
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
				int numRays = 10;
				float diffusePerc = 0.35f;
				currentColor = currentColor.multiply(1-diffusePerc);
				for (int i=0; i<numRays; i++) {
					currentColor = currentColor.add(diffuseReflect(currentTracable,currentColor,intersection,diffusePerc/numRays,recursiveDepth-1));
				}
			}
			
			
			//Colour normalisation between 0 and 1
			red = currentColor.dx();
			green = currentColor.dy();
			blue = currentColor.dz();
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
}

