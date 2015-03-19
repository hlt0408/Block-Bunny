package com.hlt.blockbunny.handlers;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;

/**
 * Created by hlt04 on 3/13/15.
 */
public class MyInputProcessor extends InputAdapter {

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        MyInput.x = screenX;
        MyInput.y = screenY;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        MyInput.x = screenX;
        MyInput.y = screenY;
        MyInput.down = true;
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        MyInput.x = screenX;
        MyInput.y = screenY;
        MyInput.down = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        MyInput.x = screenX;
        MyInput.y = screenY;
        MyInput.down = false;
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.Z) {
            MyInput.setKey(MyInput.BUTTON1, true);
        }
        if (keycode == Keys.X) {
            MyInput.setKey(MyInput.BUTTON2, true);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.Z) {
            MyInput.setKey(MyInput.BUTTON1, false);
        }
        if (keycode == Keys.X) {
            MyInput.setKey(MyInput.BUTTON2, false);
        }
        return true;
    }

}
