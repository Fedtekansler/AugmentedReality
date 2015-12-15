package com.mygdx.game;

import java.util.ArrayList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class Milestone3 implements ApplicationListener{

	private VideoCapture webcam;
	private static int SCREEN_WIDTH = 640;
	private static int SCREEN_HEIGHT = 480;

	private Mat webcamImage = new Mat();
	private Mat warpedImage = new Mat();
	private Mat greyImage = new Mat();
	private Mat binaryImage = new Mat();
	private Mat contourHierarchy = new Mat();

	private MatOfPoint2f warpedWorld = new MatOfPoint2f();
	private int warpSize = 400;

	Mat marker = new Mat();

	private ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private ArrayList<MatOfPoint> warpedImageContours = new ArrayList<MatOfPoint>();
	private MatOfPoint2f squareContours = new MatOfPoint2f();
	private ArrayList<MatOfPoint> squareContoursList = new ArrayList<MatOfPoint>();
	
	private MatOfPoint2f warpedSquareContours = new MatOfPoint2f();
	private ArrayList<MatOfPoint> warpedSquareContoursList = new ArrayList<MatOfPoint>();
	
	private MatOfPoint3f objectPoints = new MatOfPoint3f();
	
	private Environment environment;
	private PerspectiveCamera drawingCam;
	private MatOfDouble camDistortion;
	private Mat camIntrinsics;

	private ModelInstance[][] boxes;
	private ModelInstance[][] course;
	private ModelInstance[][] collisionTest;
	private ModelBatch modelBatch;
	private Model box; 
	private ModelBuilder mb;

	private Vector3 startingPos;

	@Override
	public void create() {	
		System.out.println("Booting...");
		modelBatch = new ModelBatch();
		mb = new ModelBuilder();
		boxes = new ModelInstance[1][1];
		course = new ModelInstance[1][1];
		collisionTest = new ModelInstance[4][4];

		startingPos = new Vector3(0.05f, 0.05f, 0.05f);

		setupEnvironment();
		setupDrawingCamera();
		create3DObjects();

		camDistortion = UtilAR.getDefaultDistortionCoefficients();
		camIntrinsics = UtilAR.getDefaultIntrinsicMatrix(SCREEN_WIDTH,SCREEN_HEIGHT);

		webcam = new VideoCapture(0);
		webcam.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, SCREEN_WIDTH);
		webcam.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, SCREEN_HEIGHT);

		//Sleep to make sure camera is ready
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		warpedWorld = new MatOfPoint2f();
		warpedWorld.alloc(4);
		warpedWorld.put(0, 0, 0, 0);
		warpedWorld.put(1,0,0,warpSize);
		warpedWorld.put(2,0,warpSize,warpSize);
		warpedWorld.put(3,0,warpSize,0);
		
		System.out.println("Booting done!");
	}

	private void create3DObjects(){
		Material boxMaterial = new Material(ColorAttribute.createDiffuse(Color.CYAN));
		boxMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f));
		box = mb.createBox(0.1f, 0.1f, 0.1f, 
				boxMaterial, 
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		boxes[0][0] = new ModelInstance(box);
		
		Material planeMaterial = new Material((ColorAttribute.createDiffuse(Color.RED)));
		planeMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.95f));
		box = mb.createBox(1f, 0.05f, 1f, 
				planeMaterial, 
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		course[0][0] = new ModelInstance(box);
	}
	
	private void drawBoxes(){
		modelBatch.begin(drawingCam);
		boxes[0][0].transform.idt();
		boxes[0][0].transform.translate(startingPos);
		modelBatch.render(boxes[0][0],environment);
		
		course[0][0].transform.idt();
		course[0][0].transform.translate(0.5f, 0.025f, 0.5f);
		modelBatch.render(course[0][0], environment);
		modelBatch.end();
	}

	public void setupEnvironment(){
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	public void setupDrawingCamera(){
		drawingCam = new PerspectiveCamera(40, Gdx.graphics.getWidth(), 
				Gdx.graphics.getHeight());
		drawingCam.position.set(3f, 3f, 3f);
		drawingCam.lookAt(startingPos);
		drawingCam.up.set(0,1,0);
		drawingCam.near = 0.001f;
		drawingCam.far = 300f;
		drawingCam.update(); 
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void render() {
		//Setup til 3D Drawing
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	
		
		webcam.read(webcamImage);
		if(webcamImage.empty()){
			System.out.println("Image was empty");
			return;
		}

		//Clear contours and squares
		contours.clear();
		squareContoursList.clear();
		squareContours = new MatOfPoint2f();

		Imgproc.cvtColor(webcamImage, greyImage, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(greyImage, binaryImage, 110, 255, Imgproc.THRESH_BINARY);
		UtilAR.imShow(binaryImage);
		Imgproc.findContours(binaryImage, contours, contourHierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);

		//Draw found contours on original image
		Imgproc.drawContours(webcamImage, contours, -1, new Scalar(255,0,0), 1);
		MatOfPoint2f approxCurve = new MatOfPoint2f();

		for (int i = 0; i < contours.size(); i++) {
			//Convert contours.get(i) from MatOfPoint to MatOfPoint2f
			MatOfPoint2f mMoP2f1 = new MatOfPoint2f();
			contours.get(i).convertTo(mMoP2f1, CvType.CV_32FC2);
			Imgproc.approxPolyDP(mMoP2f1, approxCurve, Imgproc.arcLength(mMoP2f1, true)*0.12, true);

			if(approxCurve.height() == 4
					&& Math.abs(Imgproc.contourArea(approxCurve)) > 1000 // THe minimum size of the bounded area
					&& Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))){
				squareContours.push_back(approxCurve);
				squareContoursList.add(new MatOfPoint(approxCurve.toArray()));

			}
		}

		int selector = 0;
		MatOfPoint2f foundSquare = new MatOfPoint2f();
		ArrayList<MatOfPoint> foundSquareList = new ArrayList<MatOfPoint>();
		if(squareContours.height() > 0){
			//Find crossproducts
//			System.out.println("SquareContours heigh: " + squareContours.height() + "(height % 4=" + (Math.floor(squareContours.height()/4)) + ")");
			Point[] squarePoints = squareContours.toArray();
			int counter = 0;
			/*
			 * Udregning af krydsprodukt mellem punkter
			 */
			for(int i = 1; i < (Math.floor(squareContours.height()/4)+1); i++){
				float[] firstPoints = new float[3];
				firstPoints[0] = (float) (squarePoints[0+counter].x-squarePoints[1+counter].x);
				firstPoints[1] = 0.0f;
				firstPoints[2] = (float) (squarePoints[0+counter].y-squarePoints[1].y);
				
				float[] secondPoints = new float[3];
				secondPoints[0] = (float) (squarePoints[2+counter].x-squarePoints[1+counter].x);
				secondPoints[1] = 0.0f;
				secondPoints[2] = (float) (squarePoints[2+counter].y-squarePoints[1+counter].y);
				
				Vector3 vectorOne = new Vector3(firstPoints);
				Vector3 vectorTwo = new Vector3(secondPoints);
//				System.out.println("[" + vectorOne.x +" , " + vectorOne.y + " , " + vectorOne.z + "]");
//				System.out.println("[" + vectorTwo.x +" , " + vectorTwo.y + " , " + vectorTwo.z + "]");
				
				Vector3 crossProd = vectorOne.crs(vectorTwo);
				
				if(crossProd.y < 0){
					selector = counter;
					break;
				}
//				System.out.println("[" + crossProd.x +" , " + crossProd.y + " , " + crossProd.z + "]");
				counter = counter + 4;
			}
			
			foundSquare.push_back(squareContours.row(0+selector));
			foundSquare.push_back(squareContours.row(1+selector));
			foundSquare.push_back(squareContours.row(2+selector));
			foundSquare.push_back(squareContours.row(3+selector));
			foundSquareList.add(new MatOfPoint(foundSquare.toArray()));
//			System.out.println(foundSquare.dump());
		}
		
		Imgproc.drawContours(webcamImage, squareContoursList, -1, new Scalar(0,255,0), 5);
		Imgproc.drawContours(webcamImage, foundSquareList, -1, new Scalar(0,0,255), 2);
		UtilAR.imDrawBackground(webcamImage);
		
		//If squares were found
		if(squareContours.height() > 0){
			//Find warped image
			Mat perspectiveMat = Calib3d.findHomography(foundSquare, warpedWorld);
			Imgproc.warpPerspective(webcamImage, warpedImage, perspectiveMat, new Size(warpSize,warpSize));
			warpedImageContours.clear();
			warpedSquareContoursList.clear();
			Mat warpedContourHierarchy = new Mat();
			//Check warped image for orientation
			Mat warpedGreyImage = new Mat();
			Mat warpedBinaryImage = new Mat();
			Imgproc.cvtColor(warpedImage, warpedGreyImage, Imgproc.COLOR_BGR2GRAY);
			Imgproc.threshold(warpedGreyImage, warpedBinaryImage, 110, 255, Imgproc.THRESH_BINARY);
			Imgproc.findContours(warpedBinaryImage, warpedImageContours, warpedContourHierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
			
			approxCurve = new MatOfPoint2f();
			
			//Sort foundSquare points to fit orientation
			for (int i = 0; i < warpedImageContours.size(); i++) {
				//Convert contours.get(i) from MatOfPoint to MatOfPoint2f
				MatOfPoint2f mMoP2f1 = new MatOfPoint2f();
				warpedImageContours.get(i).convertTo(mMoP2f1, CvType.CV_32FC2);
				Imgproc.approxPolyDP(mMoP2f1, approxCurve, Imgproc.arcLength(mMoP2f1, true)*0.12, true);

				if(approxCurve.height() == 4
						&& Math.abs(Imgproc.contourArea(approxCurve)) > 1000 // THe minimum size of the bounded area
						&& Imgproc.isContourConvex(new MatOfPoint(approxCurve.toArray()))){
					warpedSquareContoursList.add(new MatOfPoint(approxCurve.toArray()));
				}
			}
			
			double currMin = -1;
			int smallestSquare = -1;
			
			if(warpedSquareContoursList.size() > 0){
				for(int i = 0; i< warpedSquareContoursList.size(); i++){
					double currArea = Math.abs(Imgproc.contourArea(warpedSquareContoursList.get(i)));
					if(currMin == -1 || currMin > currArea){
						currMin = currArea;
						smallestSquare = i;
					}
				}
//				Imgproc.drawContours(warpedImage, warpedSquareContoursList, smallestSquare, new Scalar(255,255,0), 6);
			}
			
			double closest = Double.MAX_VALUE;
			int index = -1;
			
			if(smallestSquare != -1){
				MatOfPoint marker = warpedSquareContoursList.get(smallestSquare);
				double[] markerPoint = marker.get(0, 0);
				for(int i = 0; i < warpedWorld.height(); i++){
					double[] currRow = warpedWorld.get(i, 0);
					double xDistance = Math.abs(markerPoint[0] - currRow[0]);
					double yDistance = Math.abs(markerPoint[1] - currRow[1]);
					double totalDist = xDistance + yDistance;
					System.out.println("Run" + i);
					System.out.println("Closests: " + closest);
					System.out.println("New dist: " + totalDist);
					
					if(closest > totalDist){
						closest = totalDist;
						index = i;
					}
				}
				
				//Check for keyinput
				checkForInput();
				objectPoints.alloc(foundSquare.height());
				
				ArrayList<MatOfPoint3f> possibleWorlds = new ArrayList<MatOfPoint3f>();
				for(int i = 0; i < 4; i++){
					MatOfPoint3f temp = new MatOfPoint3f();
					temp.alloc(4);
					temp.put(i % 4,0, 0, 0, 0);
					temp.put((i+1) % 4,0, 0, 0, 1);
					temp.put((i+2) % 4,0, 1, 0, 1);
					temp.put((i+3) % 4,0, 1, 0, 0);
					possibleWorlds.add(temp);
				}
				
				/*
				 * Build object points
				 */
				objectPoints = possibleWorlds.get(index);
				
				Mat rotation = new Mat();
				Mat translation = new Mat();
				Calib3d.solvePnP(objectPoints, foundSquare, camIntrinsics, camDistortion, rotation, translation);
				UtilAR.setCameraByRT(rotation, translation, drawingCam);
				drawBoxes();
				drawingCam.update();
				
				UtilAR.imShow(warpedImage);
			}
		}
	}

	public void checkForInput(){
		if(Gdx.input.isKeyPressed((Input.Keys.LEFT))){
			startingPos.x = startingPos.x - 0.1f;
		}
		if(Gdx.input.isKeyPressed((Input.Keys.RIGHT))){
			startingPos.x = startingPos.x + 0.1f;
		}
		if(Gdx.input.isKeyPressed((Input.Keys.UP))){
			startingPos.z = startingPos.z - 0.1f;
		}
		if(Gdx.input.isKeyPressed((Input.Keys.DOWN))){
			startingPos.z = startingPos.z + 0.1f;
		}
		if(Gdx.input.isKeyPressed((Input.Keys.Z))){
			startingPos.y = startingPos.y - 0.1f;
		}
		if(Gdx.input.isKeyPressed((Input.Keys.A))){
			startingPos.y = startingPos.y + 0.1f;
		}
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {

	}

}
