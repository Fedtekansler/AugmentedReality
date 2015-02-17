package com.mygdx.game;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;


public class Milestone3 implements ApplicationListener {

	//OpenCV
	private VideoCapture webCam;
	private static int screenWidth = 640;
	private static int screenHeight = 480; 
	
	private Mat webcam_image = new Mat();
	private Mat grey_image = new Mat();
	private Mat binary_image = new Mat();
	
	private ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private Mat hierarchy = new Mat();

	@Override
	public void create() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		webCam = new VideoCapture(0);
		webCam.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, screenWidth);
		webCam.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, screenHeight);	
		
		webCam.read(webcam_image);
		
		if (webCam.isOpened()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        contours.clear();
        
        webCam.read(webcam_image);
        Imgproc.cvtColor(webcam_image, grey_image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grey_image, binary_image, 100, 250, Imgproc.THRESH_BINARY);
        Imgproc.findContours(binary_image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.drawContours(webcam_image, contours, -1, new Scalar(0,255,0));
        
        
        
        UtilAR.imDrawBackground(webcam_image);
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
	}
	
	
}
