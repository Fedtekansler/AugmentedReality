package com.mygdx.game;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.ApplicationListener;

import java.awt.image.BufferedImage;

import jdk.nashorn.internal.runtime.FindProperty;

public class Milestone2 implements ApplicationListener {

	VideoCapture webCam;
	Mat webcam_image = new Mat();
	Mat grey_image = new Mat();
	MatOfPoint2f mat2f = new MatOfPoint2f();
	
	@Override
	public void create() {
		
		webCam = new VideoCapture(0);
		
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
		webCam.read(webcam_image);
			    
		Imgproc.cvtColor(webcam_image, grey_image, Imgproc.COLOR_BGR2GRAY);
		
		mat2f.alloc(54);
		
		boolean foundCorners = Calib3d.findChessboardCorners(grey_image, new Size(9,6), mat2f);
		if (foundCorners) {
			System.out.println(mat2f.height());
			Calib3d.drawChessboardCorners(webcam_image, new Size(9,6), mat2f, foundCorners);
		}
		
		
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
