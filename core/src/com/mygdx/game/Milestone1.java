package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class Milestone1 implements ApplicationListener {
	private PerspectiveCamera cam;
	private Environment environment;
	private Model model;
	private Model xArrow;
	private Model yArrow;
	private Model zArrow;
	private Model cylinder;
	private ModelBatch modelBatch;
	private ModelInstance instance;
	private ModelInstance xInstance;
	private ModelInstance yInstance;
	private ModelInstance zInstance;
	private ModelInstance cylinderInstance;
	private CameraInputController camController;
	private Vector3 point;
	private Vector3 attach;

	@Override
	public void create () {
		modelBatch = new ModelBatch();

		// Lighting to improve the dimension
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));


		// Setting up the camera and where to look from and to.
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(5f, 2f, 5f);
		cam.lookAt(0,1,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update(); 

		// Building the model
		ModelBuilder modelBuilder = new ModelBuilder();
		
		
		model = modelBuilder.createBox(1f, 1f, 1f, 
				new Material(ColorAttribute.createDiffuse(Color.MAGENTA)),
				Usage.Position | Usage.Normal);
		instance = new ModelInstance(model);
		
		instance.transform.translate(1f, 0.5f, 0.75f);

		xArrow = modelBuilder.createArrow(0f, 0f, 0f, 1f, 0f, 0f, 
				0.1f, 0.1f, 200, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.RED)),
				Usage.Position | Usage.Normal);
		xInstance = new ModelInstance(xArrow);

		yArrow = modelBuilder.createArrow(0f, 0f, 0f, 0f, 1f, 0f, 
				0.1f, 0.1f, 200, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				Usage.Position | Usage.Normal);
		yInstance = new ModelInstance(yArrow);

		zArrow = modelBuilder.createArrow(0f, 0f, 0f, 0f, 0f, 1f, 
				0.1f, 0.1f, 200, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.BLUE)),
				Usage.Position | Usage.Normal);
		zInstance = new ModelInstance(zArrow);            

		cylinder = modelBuilder.createCylinder(1f, 1f, 1f, 100, 
				new Material(ColorAttribute.createDiffuse(Color.NAVY)), 
				Usage.Position | Usage.Normal);
		cylinderInstance = new ModelInstance(cylinder);
		
		cylinderInstance.transform.translate(3f, 0.5f, 0.75f);
		
		// Control the camera
		//camController = new CameraInputController(cam);
		//Gdx.input.setInputProcessor(camController);
		
		

	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(xInstance);
		modelBatch.render(yInstance);
		modelBatch.render(zInstance);
		modelBatch.render(instance, environment);
		modelBatch.render(cylinderInstance, environment);
		modelBatch.end();
		
		point = new Vector3(1f, 1f, 1f);
		attach = new Vector3(0f, 1f, 0f);
		cam.rotateAround(point, attach, 2);
		cam.update();
		//camController.update();
	}

	@Override
	public void dispose () {
		model.dispose();
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void pause () {
	}
}