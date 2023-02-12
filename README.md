# Photon Mapping
Implemented Photon Mapping from scratch in Java. My aim with this project was to push my problem solving skills by implementing a complex algorithm, so the code was quite experimental.

<b>For a clean and polished project</b> in ray tracing, look at my [Ray Tracing in C++](https://github.com/ymumberson/Ray-Tracing-C--) repository. The C++ project implements path tracing instead of photon mapping but aims more towards clean and efficient code.

## The Scene
The scene is a simple Cornell Box containing two glass spheres and an area light. The larger glass sphere is relfective while the smaller glass sphere is refractive. Because the ceiling light is an area light, the ceiling will not be directly lit by the light (Only lit by reflected rays) and the shadows from the spheres should be soft instead of hard.

Below is a render of the scene using the photon maps (and importance sampling), along with representations of the two photon maps. The images of the photon maps are more for understanding than completely correct representation, meaning I have normalised the color values of photons so that we can actually see them. The photon map on the left (Middle image) is the global photon map, which stores all photons which are randomly reflected around the scene. The photon map on the right (Right image) is the caustics photon map, and this stores photons which were specifically targeted at the refractive sphere.

![FullGlobal_3xSS_FullSize_ImportanceX100_UnitSquare50_NoGammaCorrection_10kPhotons_500NNS](https://user-images.githubusercontent.com/73796199/216315991-945467ff-3f42-4652-9d6b-947f12df8206.PNG)

### Direct Illumination Only
This image renders the scene with only direct illumination, meaning without using the photon map. As a result, the ceiling is not illuminated.

![Area Light Direct Only](https://user-images.githubusercontent.com/73796199/216316452-da501462-8e23-4592-aa2d-f882c381782d.PNG)

### Caustics Only
This image shows only the caustics component of the full render.

![Area Light Caustics Only](https://user-images.githubusercontent.com/73796199/216317452-402b6c9f-220b-4de6-9f17-567941ac9c25.PNG)

### Indirect Illumination Only (Direct Visualisation)
This shows only the indirect illumination of the full render, but uses direct visulisation of the global photon map to generate the indirect illumination.

![Area Light Indirect Only Direct Visualisation](https://user-images.githubusercontent.com/73796199/216316874-42ecc5dd-a0e3-4345-be56-2ff5bd7e6a0f.PNG)

### Indirect Illumination Only (Importance Sampling)
Same as above section, but we use importance sampling instead. This means that we take the information from the global photon map, and use it to work out 'important' directions to sample the lighting from.

![Area Light Indirect Only Importance Sampling](https://user-images.githubusercontent.com/73796199/216316865-159d4a4a-6859-433e-9011-6c7932691364.PNG)

### Full Global Illumination With Direct Visualisation Of The Photon Map
This image renders the scene using the photon map, and uses direct visulisation of the global photon map for indirect illumination.

![Area Light Global Illumination Direct Visualisation](https://user-images.githubusercontent.com/73796199/216316515-dcc5943d-8fdb-4fda-a35f-a861fde4a99e.PNG)

### Full Global Illumination With Importance Sampling Using The Photon Map
This image also renders the scene using the photon map, but uses the photon map to generate optimised sampling directions for indirect illumination.

![Area Light Global Illumination Importance Sampling](https://user-images.githubusercontent.com/73796199/216316549-0cfb984c-fda6-4265-b0d9-a12b0f8a4033.PNG)
