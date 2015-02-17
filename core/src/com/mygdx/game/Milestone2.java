package com.mygdx.game;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import jdk.nashorn.internal.runtime.FindProperty;

public class Milestone2 implements ApplicationListener {

	//Libgdx
	private PerspectiveCamera cam;
	private Environment environment;
	private ModelBatch modelBatch;
	private ModelBuilder modelBuilder;
	private Model cube;
	private ModelInstance[][] cubes;
	
	//OpenCV
	private VideoCapture webCam;
	
	private MatOfPoint2f corners = new MatOfPoint2f();
	private MatOfPoint3f worldPoints = new MatOfPoint3f();
	
	private Mat camIntrinsic;
	private Mat webcam_image = new Mat();
	private Mat grey_image = new Mat();
	private MatOfDouble camDist;
	private Material mat;
	
	private Vector3 startingPosition;
	private Size boardSize = new Size(9,6);
	
	private static int screenWidth = 640;
	private static int screenHeight = 480;

	private double width = Math.floor(boardSize.width/2);
	private double height = boardSize.height - 1;
	
	private boolean foundCorners = false;
	
	

	@Override
	public void create() {	
		//Allocate
		corners.alloc(54);
		worldPoints.alloc(54);
		
		//Origin postion of the cubes
		startingPosition = new Vector3(0.5f, 0.5f, 0.5f);
		
		//Libgdx
		modelBatch = new ModelBatch();
		modelBuilder = new ModelBuilder();
		cubes = new ModelInstance[(int)Math.floor(boardSize.width/2)][(int)boardSize.height - 1];

		setupCube();
		setupCamera();
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		webCam = new VideoCapture(0);
		webCam.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, screenWidth);
		webCam.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, screenHeight);		

		webCam.read(webcam_image);
		camIntrinsic = UtilAR.getDefaultIntrinsicMatrix(screenWidth, screenHeight);
		camDist = UtilAR.getDefaultDistortionCoefficients();
		
		if (webCam.isOpened()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
	
	private void setupCamera() {
		cam = new PerspectiveCamera(40, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(3f, 3f, 3f);
		cam.lookAt(startingPosition);
		cam.up.set(0, 1, 0);
		cam.near = .0001f;
		cam.far = 300f;
		cam.update(); 
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.update();
	}

	@Override
	public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
		UtilAR.imDrawBackground(webcam_image);
		findChessboard();
		drawBoxes();

	}
	
	private void findChessboard() {
		webCam.read(webcam_image);

		Imgproc.cvtColor(webcam_image, grey_image, Imgproc.COLOR_BGR2GRAY);

		foundCorners = Calib3d.findChessboardCorners(grey_image, boardSize, corners, Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
		if (foundCorners) {
			
			double scale = 1.0; //change for a bigger board.
			
			for (int j = 0; j < corners.size().height; j++) {
				double row = Math.floor(j / boardSize.width);
				double col = j % boardSize.width;
				
				worldPoints.put(j, 0, scale * col, 0.0, scale * row);
			}
			
			Mat rvec = new Mat();
			Mat tvec = new Mat();
						
			Imgproc.cornerSubPix(grey_image, corners, new Size(9,6), new Size(-1,-1), new TermCriteria(TermCriteria.EPS, 0, 0.01));
			Calib3d.drawChessboardCorners(webcam_image, new Size(9,6), corners, foundCorners);	
			
			Calib3d.solvePnP(worldPoints, corners, camIntrinsic, camDist, rvec, tvec);
			UtilAR.setCameraByRT(rvec, tvec, cam);
		}
	}
	

	private void drawBoxes() {
		if (foundCorners) { 		
			modelBatch.begin(cam);
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					cubes[i][j].transform.idt();
					int xOffset = 2*i;
					if (j % 2 == 1) xOffset+= 1;
					
					Vector3 uniquePosition = new Vector3(startingPosition.x +xOffset, startingPosition.y, startingPosition.z + j);
					cubes[i][j].transform.translate(uniquePosition);
					
					modelBatch.render(cubes[i][j]);
					
				}
			}
			modelBatch.end();			
		}
	}
	
	private void setupCube() {

		// Lighting to improve the dimension
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
        // setup material with texture
        mat = new Material(ColorAttribute.createDiffuse(Color.ORANGE));
        // blending
        mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA, 0.9f));

        cube = modelBuilder.createBox(1f, 1f, 1f, mat, Usage.Position
                | Usage.Normal | Usage.TextureCoordinates);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cubes[i][j] = new ModelInstance(cube);
            }
        }
    }

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		webCam.release();
		modelBatch.dispose();

	}

}
