package com.mygdx.game.desktop;

import org.opencv.core.Core;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Milestone1;
import com.mygdx.game.Milestone2;
import com.mygdx.game.Milestone3;
import com.mygdx.game.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new Milestone3(), config);
	}
}
