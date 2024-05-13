package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Bird;
import com.mygdx.game.Pipe;
import com.mygdx.game.Player;

import java.util.Iterator;

public class EndlessGameScreen implements Screen {
    final Bird game;

    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    Player player;
    boolean dead;
    long lastObstacleTime;
    int score;
    private Texture closeButtonTexture;
    private Rectangle closeButtonBounds;

    Array<Pipe> obstacles;

    public EndlessGameScreen(final Bird gam) {
        game = gam;

        score = 0;

        closeButtonTexture = new Texture(Gdx.files.internal("close.png")); // Replace with your close button image
        closeButtonBounds = new Rectangle(700, 400, 50, 50);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        player = new Player();
        player.setManager(game.manager);

        obstacles = new Array<Pipe>();
        spawnObstacle();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.3f, 0.8f, 0.8f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin(); // Begin SpriteBatch

        // Draw background
        batch.draw(game.manager.get("background.png", Texture.class), 0, 0);

        // Draw score on the left-hand side of the screen
        font.draw(batch, "Score: " + score, 10, Gdx.graphics.getHeight() - 10);

        // Player input
        if (Gdx.input.justTouched()) {
            player.impulso();
            game.manager.get("flap.wav", Sound.class).play();
        }

        // Player update
        player.act(delta);

        // Obstacle spawning
        if (TimeUtils.nanoTime() - lastObstacleTime > 1500000000) {
            spawnObstacle();
        }

        // Obstacle update and collision detection
        Iterator<Pipe> iter = obstacles.iterator();
        while (iter.hasNext()) {
            Pipe pipe = iter.next();
            pipe.act(delta);

            // Collision detection
            if (pipe.getBounds().overlaps(player.getBounds())) {
                dead = true;
            }

            // Score update
            if (pipe.getX() < player.getX() && !pipe.isScored()) {
                pipe.setScored(true);
                score++; // Increment score
                game.manager.get("flap.wav", Sound.class).play();
            }

            // Remove obstacle if it's out of the screen
            if (pipe.getX() < -64) {
                iter.remove();
            }
        }

        batch.end(); // End SpriteBatch

        batch.begin(); // Begin SpriteBatch again before drawing the close button
        batch.draw(closeButtonTexture, closeButtonBounds.x, closeButtonBounds.y, closeButtonBounds.width, closeButtonBounds.height);
        batch.end(); // End SpriteBatch

        if (Gdx.input.justTouched()) {
            if (isTouchInBounds(closeButtonBounds, Gdx.input.getX(), Gdx.input.getY())) {
                Gdx.app.exit(); // Close the game
            }
        }

        if (dead) {
            game.lastScore = score;
            if (game.lastScore > game.topScore)
                game.topScore = game.lastScore;

            game.manager.get("fail.wav", Sound.class).play();
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private boolean isTouchInBounds(Rectangle bounds, int screenX, int screenY) {
        return bounds.contains(screenX, Gdx.graphics.getHeight() - screenY);
    }

    private void spawnObstacle() {
        float holey = MathUtils.random(50, 230);

        Pipe pipe1 = new Pipe();
        pipe1.setX(800);
        pipe1.setY(holey - 230);
        pipe1.setUpsideDown(true);
        pipe1.setManager(game.manager);
        obstacles.add(pipe1);

        Pipe pipe2 = new Pipe();
        pipe2.setX(800);
        pipe2.setY(holey + 200);
        pipe2.setUpsideDown(false);
        pipe2.setManager(game.manager);
        obstacles.add(pipe2);

        lastObstacleTime = TimeUtils.nanoTime();
    }

    // Other overrides omitted for brevity
}
