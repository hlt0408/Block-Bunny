package com.hlt.blockbunny.main;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hlt.blockbunny.handlers.BoundedCamera;
import com.hlt.blockbunny.handlers.Content;
import com.hlt.blockbunny.handlers.GameStateManager;
import com.hlt.blockbunny.handlers.MyInput;
import com.hlt.blockbunny.handlers.MyInputProcessor;

public class Game implements ApplicationListener {

    public static final String TITLE = "Block Bunny";
    public static final int V_WIDTH = 320;
    public static final int V_HEIGHT = 240;
    public static final int SCALE = 2;
    public static final float STEP = 1 / 60f;

	private SpriteBatch sb;
    private BoundedCamera cam;
    private OrthographicCamera hudCam;

    private GameStateManager gsm;

    public static Content res;
	
	@Override
	public void create () {

        Gdx.input.setInputProcessor(new MyInputProcessor());

        res = new Content();
        res.loadTexture("images/menu.png");
        res.loadTexture("images/bgs.png");
        res.loadTexture("images/bunny.png");
        res.loadTexture("images/crystal.png");
        res.loadTexture("images/spikes.png");
        res.loadTexture("images/hud.png");

        res.loadSound("sfx/jump.wav");
        res.loadSound("sfx/crystal.wav");
        res.loadSound("sfx/levelselect.wav");
        res.loadSound("sfx/hit.wav");
        res.loadSound("sfx/changeblock.wav");

        res.loadMusic("music/bbsong.ogg");
        res.getMusic("bbsong").setLooping(true);
        res.getMusic("bbsong").setVolume(0.5f);
        res.getMusic("bbsong").play();

        cam = new BoundedCamera();
        cam.setToOrtho(false, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

        sb = new SpriteBatch();

        gsm = new GameStateManager(this);
	}

	@Override
	public void render () {

        Gdx.graphics.setTitle(TITLE + " -- FPS: " +
        Gdx.graphics.getFramesPerSecond());

        gsm.update(Gdx.graphics.getDeltaTime());
        gsm.render();
        MyInput.update();
	}

    @Override
    public void dispose() {
        res.removeAll();
    }

    public SpriteBatch getSpriteBatch() { return sb; }
    public BoundedCamera getCamera() { return cam; }
    public OrthographicCamera getHUDCamera() { return hudCam; }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}




}
