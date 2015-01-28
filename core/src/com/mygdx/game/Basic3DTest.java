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

public class Basic3DTest implements ApplicationListener {
	public PerspectiveCamera cam;
	public Environment environment;
    public Model model;
    public ModelBatch modelBatch;
    public ModelInstance instance;
    public CameraInputController camController;
    public SpriteBatch spriteBatch = new SpriteBatch();
	
	@Override
    public void create () {
		modelBatch = new ModelBatch();
		
		// Lighting to improve the dimension
		environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
        // Setting up the camera and where to look from and to.
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();    
        
        // Building the model
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f, 
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            Usage.Position | Usage.Normal);
        instance = new ModelInstance(model);
        
        // Control the camera
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
	}
 
    @Override
    public void render () {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 
        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
        
        camController.update();
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