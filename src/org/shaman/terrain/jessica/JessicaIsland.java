/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.shaman.terrain.jessica;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LodControl;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import java.awt.Color;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.LodGenerator;
import net.sourceforge.arbaro.export.ExporterFactory;
import net.sourceforge.arbaro.export.Progress;
import net.sourceforge.arbaro.gui.ShieldedGUITreeGenerator;
import net.sourceforge.arbaro.mesh.MeshGenerator;
import net.sourceforge.arbaro.mesh.MeshGeneratorFactory;
import net.sourceforge.arbaro.params.AbstractParam;
import net.sourceforge.arbaro.params.Params;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.tree.TreeGenerator;
import net.sourceforge.arbaro.tree.TreeGeneratorFactory;
import org.shaman.terrain.ArbaroToJmeExporter;
import org.shaman.terrain.CustomFlyByCamera;

/**
 * Demonstrates how to use terrain.
 * The base terrain class it uses is TerrainQuad, which is a quad tree of actual
 * meshes called TerainPatches.
 * There are a couple options for the terrain in this test:
 * The first is wireframe mode. Here you can see the underlying trianglestrip structure.
 * You will notice some off lines; these are degenerate triangles and are part of the
 * trianglestrip. They are only noticeable in wireframe mode.
 * Second is Tri-Planar texture mode. Here the textures are rendered on all 3 axes and
 * then blended together to reduce distortion and stretching.
 * Third, which you have to modify the code to see, is Entropy LOD calculations.
 * In the constructor for the TerrainQuad, un-comment the final parameter that is
 * the LodPerspectiveCalculatorFactory. Then you will see the terrain flicker to start
 * while it calculates the entropies. Once it is done, it will pick the best LOD value
 * based on entropy. This method reduces "popping" of terrain greatly when LOD levels
 * change. It is highly suggested you use it in your app.
 *
 * @author bowens
 */
public class JessicaIsland extends SimpleApplication {
	private static final Logger LOG = Logger.getLogger(JessicaIsland.class.getName());
	private Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
	private final Random rand = new Random(1);
//	private static final String[] TREES = new String[] {
//		"Models/Doodads/Trees/GnarlyTree.j3o",
//		"Models/Doodads/Trees/OakTree.j3o",
//		"Models/Doodads/Trees/RedSpruce.j3o",
//		"Models/Doodads/Trees/GnarlyTree.j3o",
//		"Models/Doodads/Trees/ScotsTree.j3o",
//		"Models/Doodads/Trees/ScotsTree2.j3o",
//		"Models/Doodads/Trees/Sequoia.j3o",
//		"Models/Doodads/Trees/Sonnerat.j3o",
//		"Models/Doodads/Trees/TallPine.j3o",
//		"Models/Doodads/Trees/Walnut.j3o"
//	};
	private static final Object[][] PALMS = new Object[][] {
		{"C:\\Users\\Sebastian\\Documents\\Java\\ProceduralTerrain\\trees\\palm.xml",
			new ColorRGBA(64/255f, 0, 0, 1),
			true
		}
	};
	private static final Object[][] BUSHES = new Object[][] {
		{"C:\\Users\\Sebastian\\Documents\\Java\\ProceduralTerrain\\trees\\desert_bush.xml",
			new ColorRGBA(118/255f, 78/255f, 48/255f, 1),
			false
		}
	};
	private static final Object[][] TREES = new Object[][] {
		{"C:\\Users\\Sebastian\\Documents\\Java\\ProceduralTerrain\\trees\\european_larch.xml",
			new ColorRGBA(200f / 255f, 180f / 255f, 160f / 255f, 1f),
			true
		},
		{"C:\\Users\\Sebastian\\Documents\\Java\\ProceduralTerrain\\trees\\quaking_aspen.xml",
			new ColorRGBA(200f / 255f, 180f / 255f, 160f / 255f, 1f),
			true
		}/*,
		{"C:\\Users\\Sebastian\\Documents\\Java\\ProceduralTerrain\\trees\\weeping_willow.xml",
			new ColorRGBA(200f / 255f, 180f / 255f, 160f / 255f, 1f),
			true
		}*/
	};

    private TerrainQuad terrain;
    Material matRock;
    Material matWire;
    boolean wireframe = false;
    boolean triPlanar = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    private float grassScale = 64;
    private float dirtScale = 16;
    private float rockScale = 128;
	
	private boolean render = false; //total time: 133 minutes 2 seconds
	ScreenshotAppState screenShotState = new ScreenshotAppState();

	CustomFlyByCamera camera;
	boolean firstInit = false;
	
	ArrayList<Vector3f> cameraPositions;
	ArrayList<Quaternion> cameraRotations;
	boolean recording = false;
	boolean playing = false;
	int playStep = 0;
	
    public static void main(String[] args) {
        JessicaIsland app = new JessicaIsland();
//		AppSettings settings = new AppSettings(true);
//        settings.setResolution(1920, 1080);
//		settings.setFullscreen(true);
//		settings.setFrameRate(60);
//        app.setSettings(settings);
//        app.start(JmeContext.Type.OffscreenSurface);
		app.start();
    }

    @Override
    public void initialize() {
        super.initialize();
		cam.setFrustumFar(10000);
//		flyCam.setEnabled(false);
//		camera = new CustomFlyByCamera(cam);
//		camera.registerWithInput(inputManager);
//		camera.setMoveSpeed(50);
//		cam.setLocation(new Vector3f(0, 10, -10));
//        cam.lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);
//        loadHintText();
    }

	@Override
	public void simpleUpdate(float tpf) {
		if (render) {
			playing = true;
			screenShotState.takeScreenshot();
			LOG.info("Frame");
			StatsAppState s = stateManager.getState(StatsAppState.class);
			if (s != null)
				getStateManager().detach(s);
		}
		if (!playing) {
			Ray ray = new Ray(cam.getLocation(), new Vector3f(0, -1, 0).interpolateLocal(cam.getDirection(), 0.8f));
			CollisionResults results = new CollisionResults();
			float minDist = 100;
			terrain.collideWith(ray, results);
			if (results.size()>0) {
				CollisionResult r = results.getClosestCollision();
				minDist = Math.min(minDist, r.getDistance());
			}
			results.clear();
			ray.setDirection(cam.getDirection());
			terrain.collideWith(ray, results);
			if (results.size()>0) {
				CollisionResult r = results.getClosestCollision();
				minDist = Math.min(minDist, r.getDistance());
			}
			flyCam.setMoveSpeed(minDist);
			
			if (recording) {
				cameraPositions.add(cam.getLocation().clone());
				cameraRotations.add(cam.getRotation().clone());
			}
		} else {
			if (playStep >= cameraPositions.size()) {
				playing = false;
				flyCam.setEnabled(true);
				if (render) {
					stop();
				}
			} else {
				cam.setLocation(cameraPositions.get(playStep).clone());
				cam.setRotation(cameraRotations.get(playStep).clone());
				playStep++;
			}
		}
	}

//	@Override
//	public void simpleUpdate(float tpf) {
//		if (firstInit) {
//			camera.setEnabled(true);
//			firstInit = false;
//		}
//	}
	

	private void initAlphaMap(Image image) {
		Texture2D a1 = (Texture2D) assetManager.loadTexture("org/shaman/terrain/jessica/TexAlpha1.png");
		Texture2D a2 = (Texture2D) assetManager.loadTexture("org/shaman/terrain/jessica/TexAlpha2.png");
		Texture2D a3 = (Texture2D) assetManager.loadTexture("org/shaman/terrain/jessica/TexAlpha3.png");
		Texture2D a4 = (Texture2D) assetManager.loadTexture("org/shaman/terrain/jessica/TexAlpha4.png");
		ByteBuffer d1 = a1.getImage().getData(0);
		ByteBuffer d2 = a2.getImage().getData(0);
		ByteBuffer d3 = a3.getImage().getData(0);
		ByteBuffer d4 = a4.getImage().getData(0);
		System.out.println("size d1="+d1.capacity()+" d2="+d2.capacity()+" d3="+d3.capacity()+" d4="+d4.capacity());
		ByteBuffer data = image.getData(0);
		if (data == null) {
			data = BufferUtils.createByteBuffer(512*512*4);
		}
		System.out.println("output size="+data.capacity());
		data.rewind();
		d1.rewind();
		d2.rewind();
		d3.rewind();
		d4.rewind();
		for (int x=0; x<512; ++x) {
			for (int y=0; y<512; ++y) {
				float r = d1.get(); d1.get(); d1.get(); float ra = d1.get();
				float g = d2.get(); d2.get(); d2.get(); float ga = d2.get();
				float b = d3.get(); d3.get(); d3.get(); float gb = d3.get();
				float a = d4.get(); d4.get(); d4.get(); float aa = d4.get();
				r *= ra;
				g *= ga;
				b *= gb;
				a *= aa;
				float sum = r+g+b+a;
				sum /= 255;
				r /= sum;
				g /= sum;
				b /= sum;
				a /= sum;
				data.put((byte) r).put((byte) g).put((byte) b).put((byte) a);
			}
		}
		data.rewind();
		image.setFormat(Image.Format.RGBA8);
		image.setWidth(512);
		image.setHeight(512);
		image.setData(0, data);
	}
    @Override
    public void simpleInitApp() {
        setupKeys();

        // First, we load up our textures and the heightmap texture for the terrain

        // TERRAIN TEXTURE material
        Material matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matTerrain.setBoolean("useTriPlanarMapping", true);

		Texture heightMapImage = assetManager.loadTexture("org/shaman/terrain/jessica/Heightmap2.png");
		
        // ALPHA map (for splat textures)
		Texture2D alphaMap = new Texture2D(heightMapImage.getImage().getWidth(), 
				heightMapImage.getImage().getHeight(), Image.Format.ABGR8);
        
        // GRASS texture
        Texture grass = assetManager.loadTexture("org/shaman/terrain/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_1", grass);
        matTerrain.setFloat("DiffuseMap_1_scale", 32/256f);
		
		// DARK ROCK texture
        Texture darkRock = assetManager.loadTexture("org/shaman/terrain/rock2.jpg");
        darkRock.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap", darkRock);
        matTerrain.setFloat("DiffuseMap_0_scale", 16/256f);
		
		// SAND texture
        Texture sand = assetManager.loadTexture("org/shaman/terrain/jessica/sand.jpg");
        sand.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("DiffuseMap_2", sand);
        matTerrain.setFloat("DiffuseMap_2_scale", 16/256f);
		
		// GRAVEL texture
//        Texture gravel = assetManager.loadTexture("org/shaman/terrain/jessica/gravel.jpg");
//        gravel.setWrap(WrapMode.Repeat);
//        matTerrain.setTexture("DiffuseMap_3", gravel);
//        matTerrain.setFloat("DiffuseMap_3_scale", 16/256f);
        
        // NORMAL MAPS
        Texture normalMapRock = assetManager.loadTexture("org/shaman/terrain/rock_normal.png");
        normalMapRock.setWrap(WrapMode.Repeat);
        Texture normalMapGrass = assetManager.loadTexture("org/shaman/terrain/grass_normal.jpg");
        normalMapGrass.setWrap(WrapMode.Repeat);
        matTerrain.setTexture("NormalMap", normalMapRock);
        matTerrain.setTexture("NormalMap_1", normalMapGrass);


        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }
		
//		Image image = alphaMap.getImage();
//		initAlphaMap(image);
		alphaMap = (Texture2D) assetManager.loadTexture("org/shaman/terrain/jessica/Alpha.png");
		alphaMap.setMagFilter(Texture.MagFilter.Bilinear);
		matTerrain.setTexture("AlphaMap", alphaMap);

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
        /**
         * Optimal terrain patch size is 65 (64x64).
         * The total size is up to you. At 1025 it ran fine for me (200+FPS), however at
         * size=2049, it got really slow. But that is a jump from 2 million to 8 million triangles...
         */
		heightmap.erodeTerrain();
        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
//        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
//        control.setLodCalculator( new DistanceLodCalculator(65, 2.7f) ); // patch size, and a multiplier
//        terrain.addControl(control);
        terrain.setMaterial(matTerrain);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 0.5f, 2f);
        rootNode.attachChild(terrain);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(lightDir);
        sun.setColor(ColorRGBA.White.clone().multLocal(1f));
        rootNode.addLight(sun);
        
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        rootNode.addLight(al);

		addPlants(heightmap);
		
        cam.setLocation(new Vector3f(0, 10, -10));
        cam.lookAtDirection(new Vector3f(0, -1.5f, -1).normalizeLocal(), Vector3f.UNIT_Y);
		
		Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", false);
        sky.setLocalScale(350);
		rootNode.attachChild(sky);
		
		//Water Filter
        WaterFilter water = new WaterFilter(rootNode, lightDir);
        water.setWaterColor(new ColorRGBA().setAsSrgb(0.0078f, 0.3176f, 0.5f, 1.0f));
        water.setDeepWaterColor(new ColorRGBA().setAsSrgb(0.0039f, 0.00196f, 0.145f, 1.0f));
        water.setUnderWaterFogDistance(80);
        water.setWaterTransparency(0.12f);
        water.setFoamIntensity(0.4f);        
        water.setFoamHardness(0.3f);
        water.setFoamExistence(new Vector3f(0.8f, 8f, 1f));
        water.setReflectionDisplace(50);
        water.setRefractionConstant(0.25f);
        water.setColorExtinction(new Vector3f(30, 50, 70));
        water.setCausticsIntensity(0.4f);        
        water.setWaveScale(0.003f);
        water.setMaxAmplitude(2f);
        water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
        water.setRefractionStrength(0.2f);
        water.setWaterHeight(-50);
		cam.setFrustumFar(10000);
        
        //Bloom Filter
        BloomFilter bloom = new BloomFilter();        
        bloom.setExposurePower(55);
        bloom.setBloomIntensity(1.0f);
        
        //Light Scattering Filter
        LightScatteringFilter lsf = new LightScatteringFilter(lightDir.mult(-300));
        lsf.setLightDensity(0.5f);   
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        
        fpp.addFilter(water);
//        fpp.addFilter(bloom);
//        fpp.addFilter(lsf);
        fpp.addFilter(new FXAAFilter());
		viewPort.addProcessor(fpp);
		
		flyCam.setMoveSpeed(100);
		
		cameraPositions = new ArrayList<>();
		cameraRotations = new ArrayList<>();
		inputManager.addMapping("record", new KeyTrigger(KeyInput.KEY_RETURN));
		inputManager.addMapping("play", new KeyTrigger(KeyInput.KEY_P));
		inputManager.addListener(actionListener, "record", "play");
		loadRecording();
		
		stateManager.attach(screenShotState);
    }
	
	private void addPlants(HeightMap heightmap) {
		Texture2D tex = (Texture2D) assetManager.loadTexture("org/shaman/terrain/jessica/Plants.png");
		System.out.println("format: "+tex.getImage().getFormat());
		ImageRaster raster = ImageRaster.create(tex.getImage());
		ColorRGBA col = new ColorRGBA();
		
		Spatial[] trees = new Spatial[5];
		if (render) {
			LOG.info("create trees");
			for (int i=0; i<trees.length; ++i) {
				Object[] settings = TREES[rand.nextInt(TREES.length)];
				trees[i] = createPlant((String) settings[0], 4, (ColorRGBA) settings[1], true);
			}
		}
		
		LOG.info("plant");
		for (int x=0; x<512; ++x) {
			for (int y=0; y<512; ++y) {
				raster.getPixel(x, 511-y, col);
				float a = col.a;
				float r = col.r;
				float g = col.g;
				float b = col.b;
				r *= a;
				g *= a;
				b *= a;
				
				float ix = x + rand.nextFloat()-0.5f;
				float iy = y + rand.nextFloat()-0.5f;
				float wx = 2*(ix-256);
				float wy = -100 + heightmap.getInterpolatedHeight(ix, iy)/2;
				float wz = 2*(iy-256);
				
				//trees
				Material treeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				treeMat.setColor("Color", ColorRGBA.Brown);
				if (g>0.3 && wy>-40) {
					float prob = g * 0.005f;
					if (rand.nextFloat()<prob) {
						//plant a tree
						Spatial tree;
						if (!render) {
							Box box = new Box(new Vector3f(-1, 0, -1), new Vector3f(1, 3, 1));
							Geometry treeg = new Geometry("tree", box);
							treeg.setMaterial(treeMat);
							tree = treeg;
						} else {
							tree = trees[rand.nextInt(trees.length)].clone();
						}
						tree.setLocalScale(2+rand.nextFloat());
						tree.setLocalTranslation(wx, wy, wz);
						tree.rotate((rand.nextFloat()-0.5f)*0.1f, (rand.nextFloat()-0.5f)*0.5f, (rand.nextFloat()-0.5f)*0.1f);
						rootNode.attachChild(tree);
						System.out.println("plant tree at "+tree.getLocalTranslation());
					}
				}
				
				//dead bushes
				Material bushMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				bushMat.setColor("Color", ColorRGBA.Gray);
				if (b>0.3 && wy>-40) {
					float prob = b * 0.01f;
					if (rand.nextFloat()<prob) {
						//plant a tree
						Spatial bush;
						if (!render) {
							Box box = new Box(new Vector3f(-0.5f, 0, -0.5f), new Vector3f(0.5f, 1, 0.5f));
							Geometry bushg = new Geometry("tree", box);
							bushg.setMaterial(bushMat);
							bush = bushg;
						} else {
							bush = createPlant((String) BUSHES[0][0], 1, (ColorRGBA) BUSHES[0][1], (boolean) BUSHES[0][2]);
						}
						bush.scale(1+rand.nextFloat());
						bush.setLocalTranslation(wx, wy, wz);
						bush.rotate((rand.nextFloat()-0.5f)*0.1f, (rand.nextFloat()-0.5f)*0.5f, (rand.nextFloat()-0.5f)*0.1f);
						rootNode.attachChild(bush);
						System.out.println("plant tree at "+bush.getLocalTranslation());
					}
				}
				
				//palms
				Material palmMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				palmMat.setColor("Color", ColorRGBA.Green);
				if (r>0.3 && wy>-40) {
					float prob = r * 0.01f;
					if (rand.nextFloat()<prob) {
						//plant a tree
						Spatial palm;
						if (!render) {
							Box box = new Box(new Vector3f(-0.5f, 0, -0.5f), new Vector3f(0.5f, 3, 0.5f));
							Geometry palmg = new Geometry("tree", box);
							palmg.setMaterial(palmMat);
							palm = palmg;
						} else {
							palm = createPlant((String) PALMS[0][0], 5, (ColorRGBA) PALMS[0][1], (boolean) PALMS[0][2]);
						}
						palm.scale(0.9f+rand.nextFloat()*0.2f);
						palm.setLocalTranslation(wx, wy, wz);
						palm.rotate((rand.nextFloat()-0.5f)*0.1f, (rand.nextFloat()-0.5f)*0.5f, (rand.nextFloat()-0.5f)*0.1f);
						rootNode.attachChild(palm);
						System.out.println("plant tree at "+palm.getLocalTranslation());
					}
				}
			}
		}
	}
	private void listsGeometries(Spatial s, ArrayList<Geometry> list) {
		if (s instanceof Geometry) {
			list.add((Geometry) s);
		}
		if (s instanceof Node) {
			for (Spatial c : ((Node) s).getChildren()) {
				listsGeometries(c, list);
			}
		}
	}
	
	private void saveRecording() {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("recording.dat"))) {
			out.writeObject(cameraPositions);
			out.writeObject(cameraRotations);
		} catch (IOException ex) {
			Logger.getLogger(JessicaIsland.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	private void loadRecording() {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("recording.dat"))) {
			cameraPositions = (ArrayList<Vector3f>) in.readObject();
			cameraRotations = (ArrayList<Quaternion>) in.readObject();
		} catch (IOException ex) {
			Logger.getLogger(JessicaIsland.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(JessicaIsland.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

    public void loadHintText() {
        hintText = new BitmapText(guiFont, false);
        hintText.setSize(guiFont.getCharSet().getRenderedSize());
        hintText.setLocalTranslation(0, getCamera().getHeight(), 0);
        hintText.setText("Hit T to switch to wireframe,  P to switch to tri-planar texturing");
        guiNode.attachChild(hintText);
    }

    private void setupKeys() {
        flyCam.setMoveSpeed(50);
        inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(actionListener, "wireframe");
        inputManager.addMapping("triPlanar", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(actionListener, "triPlanar");
    }
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean pressed, float tpf) {
            if (name.equals("wireframe") && !pressed) {
                wireframe = !wireframe;
                if (!wireframe) {
                    terrain.setMaterial(matWire);
                } else {
                    terrain.setMaterial(matRock);
                }
//            } else if (name.equals("triPlanar") && !pressed) {
//                triPlanar = !triPlanar;
//                if (triPlanar) {
//                    matRock.setBoolean("useTriPlanarMapping", true);
//                    // planar textures don't use the mesh's texture coordinates but real world coordinates,
//                    // so we need to convert these texture coordinate scales into real world scales so it looks
//                    // the same when we switch to/from tr-planar mode
//                    matRock.setFloat("Tex1Scale", 1f / (float) (512f / grassScale));
//                    matRock.setFloat("Tex2Scale", 1f / (float) (512f / dirtScale));
//                    matRock.setFloat("Tex3Scale", 1f / (float) (512f / rockScale));
//                } else {
//                    matRock.setBoolean("useTriPlanarMapping", false);
//                    matRock.setFloat("Tex1Scale", grassScale);
//                    matRock.setFloat("Tex2Scale", dirtScale);
//                    matRock.setFloat("Tex3Scale", rockScale);
//                }
            } else if ("record".equals(name) && pressed) {
				if (recording) {
					recording = false;
					LOG.info("record stopped");
					saveRecording();
				} else {
					cameraPositions.clear();
					cameraRotations.clear();
					recording = true;
					LOG.info("recording");
				}
			} else if ("play".equals(name) && pressed) {
				playStep = 0;
				if (playing) {
					playing = false;
					flyCam.setEnabled(true);
				} else {
					playing = true;
					flyCam.setEnabled(false);
					LOG.info("playing");
				}
			}
        }
    };
	
	public Spatial createPlant(String file, float height, ColorRGBA barkColor, boolean leaves) {
		LOG.info("generate "+file);
		
		Params params = new Params();
		
		params.clearParams();
		File treefile = new File(file);
		try {
			// read parameters
			params.readFromXML(new FileInputStream(treefile));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(JessicaIsland.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		AbstractParam.loading=false;
		params.prepare(13);
		
		TreeGenerator treeGenerator = TreeGeneratorFactory.createTreeGenerator(params);
//			ExporterFactory exporterFactory = new ExporterFactory();
			
		try{
			treeGenerator.setSeed(rand.nextInt(Short.MAX_VALUE));
			treeGenerator.setParam("Smooth",params.getParam("Smooth").toString());
			ExporterFactory.setRenderW(1024);
			ExporterFactory.setRenderH(1024);
			ExporterFactory.setExportFormat(-1);
			ExporterFactory.setOutputStemUVs(true);
			ExporterFactory.setOutputLeafUVs(true);
			//FIXME fileChooser.getPath() ???
			//tree.setOutputPath(fileField.getText());
			
			Progress progress = new Progress();
			// create the tree
			Tree tree = treeGenerator.makeTree(progress);
//				Params params = treeGenerator.getParams();
			// export the tree
			boolean useQuads = true;
			MeshGenerator meshGenerator = MeshGeneratorFactory.createMeshGenerator(/*params,*/ useQuads);
			ArbaroToJmeExporter exporter = new ArbaroToJmeExporter(assetManager, tree,meshGenerator);
//			exporter.setBarkTexture("org/shaman/terrain/treetex/bark3.jpg");
			exporter.setBarkColor(barkColor);
			exporter.writeLeaves(leaves);
//			exporter.setLeafTexture("org/shaman/terrain/treetex/leaf1.png");
			exporter.doWrite();
			
			Spatial spatial = exporter.getSpatial();
			spatial.rotate(-FastMath.HALF_PI, 0, 0);
			BoundingBox box = (BoundingBox) spatial.getWorldBound();
			float h = box.getYExtent();
			spatial.scale(height/h);
			
			LOG.info("generated");
			return spatial;
			
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}
}
