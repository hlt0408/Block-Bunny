package com.hlt.blockbunny.handlers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

/**
 * Created by hlt04 on 3/12/15.
 */
public class MyContactListener implements ContactListener {

    private int numFootContacts;
    private Array<Body> bodiesToRemove;
    private boolean playerDead;

    public MyContactListener() {
        super();
        bodiesToRemove = new Array<Body>();
    }

    // called when two fixtures start to collide
    @Override
    public void beginContact(Contact contact) {

        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) return;

        //System.out.println(fa.getUserData() + " " + fb.getUserData());

        if (fa.getUserData() != null && fa.getUserData().equals("foot")) {
            numFootContacts++;
        }

        if (fb.getUserData() != null && fb.getUserData().equals("foot")) {
            numFootContacts++;
        }

        if (fa.getUserData() != null && fa.getUserData().equals("crystal")) {
            // remove crystal
            bodiesToRemove.add(fa.getBody());
        }

        if (fb.getUserData() != null && fb.getUserData().equals("crystal")) {
            bodiesToRemove.add(fb.getBody());
        }

        if(fa.getUserData() != null && fa.getUserData().equals("spike")) {
            playerDead = true;
        }
        if(fb.getUserData() != null && fb.getUserData().equals("spike")) {
            playerDead = true;
        }


    }

    // called when no longer collide
    @Override
    public void endContact(Contact contact) {

        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) return;

        if (fa.getUserData() != null && fa.getUserData().equals("foot")) {
            numFootContacts--;
        }

        if (fb.getUserData() != null && fb.getUserData().equals("foot")) {
            numFootContacts--;
        }

    }

    public boolean isPlayerOnGround() { return numFootContacts > 0; }

    public Array<Body> getBodiesToRemove() { return bodiesToRemove; }

    public boolean isPlayerDead() { return playerDead; }

    // collision detection
    // presolve
    // collision handling
    // postsolve
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
