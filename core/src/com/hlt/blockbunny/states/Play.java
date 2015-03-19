package com.hlt.blockbunny.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.hlt.blockbunny.entities.Crystal;
import com.hlt.blockbunny.entities.HUD;
import com.hlt.blockbunny.entities.Player;
import com.hlt.blockbunny.entities.Spike;
import com.hlt.blockbunny.handlers.Background;
import com.hlt.blockbunny.handlers.BoundedCamera;
import com.hlt.blockbunny.handlers.GameStateManager;
import com.hlt.blockbunny.handlers.MyContactListener;
import com.hlt.blockbunny.handlers.MyInput;
import com.hlt.blockbunny.main.Game;

import static com.hlt.blockbunny.handlers.B2DVars.BIT_BLUE;
import static com.hlt.blockbunny.handlers.B2DVars.BIT_CRYSTAL;
import static com.hlt.blockbunny.handlers.B2DVars.BIT_GREEN;
import static com.hlt.blockbunny.handlers.B2DVars.BIT_PLAYER;
import static com.hlt.blockbunny.handlers.B2DVars.BIT_RED;
import static com.hlt.blockbunny.handlers.B2DVars.BIT_SPIKE;
import static com.hlt.blockbunny.handlers.B2DVars.PPM;

/**
 * Created by hlt04 on 3/11/15.
 */
public class Play extends GameState {

    private boolean debug = false;

    private World world;
    private Box2DDebugRenderer b2dr;
    private MyContactListener cl;
    private BoundedCamera b2dCam;

    private Player player;

    private TiledMap tileMap;
    private int tileMapWidth;
    private int tileMapHeight;
    private int tileSize;
    private OrthogonalTiledMapRenderer tmr;

    private Array<Crystal> crystals;
    private Array<Spike> spikes;

    private Background[] backgrounds;
    private HUD hud;

    public static int level;

    public Play(GameStateManager gsm) {

        super(gsm);

        // set up box2d world and contact listener
        world = new World(new Vector2(0, -9.81f), true);
        cl = new MyContactListener();
        world.setContactListener(cl);
        b2dr = new Box2DDebugRenderer();

        // create player
        createPlayer();

        // create tiles
        createTiles();
        cam.setBounds(0, tileMapWidth * tileSize, 0, tileMapHeight * tileSize);

        // create crystals
        createCrystals();
        player.setTotalCrystals(crystals.size);

        // create spikes
        createSpikes();

        // create backgrounds
        Texture bgs = Game.res.getTexture("bgs");
        TextureRegion sky = new TextureRegion(bgs, 0, 0, 320, 240);
        TextureRegion clouds = new TextureRegion(bgs, 0, 240, 320, 240);
        TextureRegion mountains = new TextureRegion(bgs, 0, 480, 320, 240);
        backgrounds = new Background[3];
        backgrounds[0] = new Background(sky, cam, 0f);
        backgrounds[1] = new Background(clouds, cam, 0.1f);
        backgrounds[2] = new Background(mountains, cam, 0.2f);

        // set up hud
        hud = new HUD(player);

        // set up box2d cam
        b2dCam = new BoundedCamera();
        b2dCam.setToOrtho(false, Gdx.graphics.getWidth() / 2 / PPM, Gdx.graphics.getHeight() / 2 / PPM);
        b2dCam.setBounds(0, tileMapWidth * tileSize / PPM,
                         0, tileMapHeight * tileSize / PPM);
    }

    @Override
    public void handleInput() {

        // keyboard input
        if (MyInput.isPressed(MyInput.BUTTON1)) {
            playerJump();
        }

        // switch block color
        if (MyInput.isPressed(MyInput.BUTTON2)) {
            switchBlocks();
        }

        // mouse/touch input for android
        // left side of screen to switch blocks
        // right side of screen jump
        if (MyInput.isPressed()) {
            if (MyInput.x < Gdx.graphics.getWidth() / 2) {
                switchBlocks();
            }
            else {
                playerJump();
            }
        }
    }

    @Override
    public void update(float dt) {

        // check input
        handleInput();

        // update box2d world
        world.step(Game.STEP, 1, 1);

        // check for collected crystals and remove
        Array<Body> bodies = cl.getBodiesToRemove();
        for (int i = 0; i < bodies.size; i++) {
            Body b = bodies.get(i);
            crystals.removeValue((Crystal) b.getUserData(), true);
            world.destroyBody(b);
            player.collectCrystal();
            Game.res.getSound("crystal").play();
        }
        bodies.clear();

        // update player
        player.update(dt);

        // check player win
        if (player.getBody().getPosition().x * PPM > tileMapWidth * tileSize) {
            Game.res.getSound("levelselect").play();
            gsm.setState(GameStateManager.LEVEL_SELECT);
        }

        // check player failure
        if ((player.getBody().getPosition().y < 0) ||
                (player.getBody().getLinearVelocity().x < 0.001f)
                || (cl.isPlayerDead())) {
            Game.res.getSound("hit").play();
            gsm.setState(GameStateManager.MENU);
        }

        // update crystals
        for (int i = 0; i < crystals.size; i++) {
            crystals.get(i).update(dt);
        }

        // update spikes
        for (int i = 0; i < spikes.size; i++) {
            spikes.get(i).update(dt);
        }
    }

    @Override
    public void render() {

        // set camera to follow player
        cam.position.set(player.getPosition().x * PPM + Gdx.graphics.getWidth() / 8,
                Gdx.graphics.getHeight() / 4,
                0
        );
        cam.update();

        // draw bgs
        sb.setProjectionMatrix(hudCam.combined);
        for (int i = 0; i < backgrounds.length; i++) {
            backgrounds[i].render(sb);
        }

        // draw tile map
        tmr.setView(cam);
        tmr.render();

        // draw player
        sb.setProjectionMatrix(cam.combined);
        player.render(sb);

        // draw crystals
        for (int i = 0; i < crystals.size; i++) {
            crystals.get(i).render(sb);
        }

        // draw spikes
        for (int i = 0; i < spikes.size; i++) {
            spikes.get(i).render(sb);
        }

        // draw hud
        sb.setProjectionMatrix(hudCam.combined);
        hud.render(sb);

        // debug draw box2D world
        if (debug) {
            b2dCam.setPosition(player.getPosition().x
                            + Gdx.graphics.getWidth() / 8 / PPM,
                    Gdx.graphics.getHeight() / 4 / PPM
                    );
            b2dCam.update();
            b2dr.render(world, b2dCam.combined);
        }

        sb.begin();
        sb.end();
    }

    @Override
    public void dispose() {

    }

    /**
     * Creates the player.
     * Sets up the box2d body and sprites.
     */
    public void createPlayer() {

        // create bodydef
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(60 / PPM, 120 / PPM);
        bdef.fixedRotation = true;
        bdef.linearVelocity.set(1f, 0f);

        // create body from bodydef
        Body body = world.createBody(bdef);

        // create box shape for player collision box
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(13 / PPM, 13 / PPM);

        // create fixturedef for player collision box
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = BIT_PLAYER;
        fdef.filter.maskBits = BIT_RED | BIT_CRYSTAL | BIT_SPIKE;
        //fdef.restitution = 0.5f;

        // create player collision box fixture
        body.createFixture(fdef);
        shape.dispose();

        // create box shape for player foot
        shape = new PolygonShape();
        shape.setAsBox(13 / PPM, 3 / PPM, new Vector2(0, -13 /PPM), 0);

        // create fixturedef for player foot
        fdef.shape = shape;
        fdef.filter.categoryBits = BIT_PLAYER;
        fdef.filter.maskBits = BIT_RED;
        fdef.isSensor = true;

        // create player foot fixture
        body.createFixture(fdef).setUserData("foot");
        shape.dispose();

        // create player
        player = new Player(body);

        body.setUserData(player);
    }

    /*
     * Sets up the tile map collidable tiles.
     * Reads in tile map layers and sets up box2d bodies.
     */
    public void createTiles() {

        // load tiled map
        try {
            tileMap = new TmxMapLoader().load("maps/level" + level + ".tmx");}
        catch (Exception e) {
            System.out.println("Cannot find files: maps/level" + level + ".tmx");
            Gdx.app.exit();
        }
        tileMapWidth = tileMap.getProperties().get("width", Integer.class);
        tileMapHeight = tileMap.getProperties().get("height", Integer.class);
        tileSize = tileMap.getProperties().get("tilewidth", Integer.class);

        tmr = new OrthogonalTiledMapRenderer(tileMap);

        TiledMapTileLayer layer;

        layer = (TiledMapTileLayer) tileMap.getLayers().get("red");
        createLayer(layer, BIT_RED);

        layer = (TiledMapTileLayer) tileMap.getLayers().get("green");
        createLayer(layer, BIT_GREEN);

        layer = (TiledMapTileLayer) tileMap.getLayers().get("blue");
        createLayer(layer, BIT_BLUE);

    }

    private void createLayer(TiledMapTileLayer layer, short bits) {

        // go through all the cells in the layer
        for (int row = 0; row < layer.getHeight(); row++) {
            for (int col = 0; col < layer.getWidth(); col++) {

                // get cell
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);

                // check if cell exists
                if (cell == null) continue;
                if (cell.getTile() == null) continue;

                // create a body + fixture from cell
                BodyDef bdef = new BodyDef();
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set(
                        (col + 0.5f) *tileSize / PPM,
                        (row + 0.5f) * tileSize / PPM
                );

                ChainShape cs = new ChainShape();
                Vector2[] v = new Vector2[3];
                v[0] = new Vector2(
                        -tileSize / 2 / PPM, -tileSize / 2 / PPM);
                v[1] = new Vector2(
                        -tileSize / 2 /PPM, tileSize / 2 / PPM);
                v[2] = new Vector2(
                        tileSize / 2 / PPM, tileSize / 2 / PPM);
                cs.createChain(v);
                FixtureDef fdef = new FixtureDef();
                fdef.friction = 0;
                fdef.shape = cs;
                fdef.filter.categoryBits = bits;
                fdef.filter.maskBits = BIT_PLAYER;
                fdef.isSensor = false;
                world.createBody(bdef).createFixture(fdef);
                cs.dispose();
            }
        }
    }

    private void createCrystals() {

        crystals = new Array<Crystal>();

        MapLayer layer = tileMap.getLayers().get("crystals");

        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();

        for (MapObject mo : layer.getObjects()) {

            bdef.type = BodyDef.BodyType.StaticBody;

            float x = mo.getProperties().get("x", Float.class) / PPM;
            float y = mo.getProperties().get("y", Float.class) / PPM;

            bdef.position.set(x, y);

            CircleShape cshape = new CircleShape();
            cshape.setRadius(8 / PPM);

            fdef.shape = cshape;
            fdef.isSensor = true;
            fdef.filter.categoryBits = BIT_CRYSTAL;
            fdef.filter.maskBits = BIT_PLAYER;

            Body body = world.createBody(bdef);
            body.createFixture(fdef).setUserData("crystal");

            Crystal c = new Crystal(body);
            crystals.add(c);

            body.setUserData(c);
            cshape.dispose();
        }

    }

    private void createSpikes() {

        spikes = new Array<Spike>();

        MapLayer ml = tileMap.getLayers().get("spikes");
        if (ml == null) return;

        for (MapObject mo : ml.getObjects()) {
            BodyDef cdef = new BodyDef();
            float x = mo.getProperties().get("x", Float.class) / PPM;
            float y = mo.getProperties().get("y", Float.class) / PPM;
            cdef.position.set(x, y);
            Body body = world.createBody(cdef);
            FixtureDef cfdef = new FixtureDef();
            CircleShape cshape = new CircleShape();
            cshape.setRadius(5 / PPM);
            cfdef.shape = cshape;
            cfdef.isSensor = true;
            cfdef.filter.categoryBits = BIT_SPIKE;
            cfdef.filter.maskBits = BIT_PLAYER;
            body.createFixture(cfdef).setUserData("spike");
            Spike s = new Spike(body);
            body.setUserData(s);
            spikes.add(s);
            cshape.dispose();
        }
    }

    private void playerJump() {
        if (cl.isPlayerOnGround()) {
            Game.res.getSound("jump").play();
            player.getBody().applyForceToCenter(0, 250, true);
        }
    }

    private void switchBlocks() {

        Filter filter = player.getBody().getFixtureList().first().getFilterData();
        short bits = filter.maskBits;

        // switch to next color
        // red -> green -> blue -> red
        if ((bits & BIT_RED) != 0) {
            bits &= ~BIT_RED;
            bits |= BIT_GREEN;
        }
        else if ((bits & BIT_GREEN) != 0) {
            bits &= ~BIT_GREEN;
            bits |= BIT_BLUE;
        }
        else if ((bits & BIT_BLUE) != 0) {
            bits &= ~BIT_BLUE;
            bits |= BIT_RED;
        }

        // set new mask bits
        filter.maskBits = bits;
        player.getBody().getFixtureList().first().setFilterData(filter);

        // set new mask bits for foot
        filter = player.getBody().getFixtureList().get(1).getFilterData();
        bits &= ~BIT_CRYSTAL;
        filter.maskBits = bits;
        player.getBody().getFixtureList().get(1).setFilterData(filter);

        // play sound
        Game.res.getSound("changeblock").play();
    }
}
