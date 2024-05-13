package com.mygdx.game;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Player extends Actor {
    Rectangle bounds;
    AssetManager manager;
    float speedy, gravity;


    Player()
    {
        setX(200);
        setY(280 / 2 - 64 / 2);
        setSize(64, 45);

        speedy = 0;
        gravity = 350f; // Adjust gravity to make the bird fall slower

        bounds = new Rectangle();
    }

    @Override
    public void act(float delta)
    {
        // Update the player's position with vertical velocity
        moveBy(0, speedy * delta);
        // Update the vertical velocity with gravity
        speedy -= gravity * delta;
        bounds.set(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(manager.get("bird.png", Texture.class), getX(), getY());
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    void impulso()
    {
        speedy = 250f; // Adjust the value to control how much the bird goes up when tapped
    }
}
