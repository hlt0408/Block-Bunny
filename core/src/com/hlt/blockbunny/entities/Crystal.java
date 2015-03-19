package com.hlt.blockbunny.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.hlt.blockbunny.main.Game;

/**
 * Created by hlt04 on 3/14/15.
 */
public class Crystal extends B2DSprite {

    public Crystal(Body body) {

        super(body);

        Texture tex = Game.res.getTexture("crystal");
        TextureRegion[] sprites = TextureRegion.split(tex, 16, 16)[0];
        setAnimation(sprites, 1 / 30f);

        width = sprites[0].getRegionWidth();
        height = sprites[0].getRegionHeight();

    }
}
