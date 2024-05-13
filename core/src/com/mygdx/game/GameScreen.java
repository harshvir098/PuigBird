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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;
public class GameScreen implements Screen {
    final Bird game;

    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;

    Stage stage;
    Player player;
    boolean dead;

    Array<Pipe> obstacles;
    long lastObstacleTime;
    int score;
    private boolean paused = false;
    private Texture pauseButtonTexture;
    private Rectangle pauseButtonBounds;

    public GameScreen(final Bird gam) {
        this.game = gam;

        score = 0;

        pauseButtonTexture = new Texture(Gdx.files.internal("pause.png")); // Replace with your pause button image
        pauseButtonBounds = new Rectangle(700, 400, 50, 50);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        player = new Player();
        player.setManager(game.manager);
        stage = new Stage();
        stage.getViewport().setCamera(camera);
        stage.addActor(player);

        obstacles = new Array<Pipe>();
        spawnObstacle();
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

        stage.getBatch().setProjectionMatrix(camera.combined);
        stage.draw();

        if (!paused) {
            if (Gdx.input.justTouched()) {
                player.impulso();
                game.manager.get("flap.wav", Sound.class).play();
            }

            stage.act();

            if (player.getBounds().y > 480 - player.getHeight()) {
                player.setY(480 - player.getHeight());
            }
            if (player.getBounds().y < 0 - player.getHeight()) {
                dead = true;
            }

            if (TimeUtils.nanoTime() - lastObstacleTime > 1500000000) {
                spawnObstacle();
            }

            Iterator<Pipe> iter = obstacles.iterator();
            while (iter.hasNext()) {
                Pipe pipe = iter.next();
                // Draw score on each pipe
                font.draw(batch, String.valueOf(pipe.getPipeScore()), pipe.getX() + pipe.getWidth() / 2, pipe.getY() + pipe.getHeight() + 20);

                if (pipe.getBounds().overlaps(player.getBounds())) {
                    dead = true;
                }
                if (pipe.getX() < player.getX() && !pipe.isScored()) {
                    pipe.setScored(true);
                    pipe.incrementPipeScore(); // Increment score for this pipe
                    score += pipe.getPipeScore(); // Update overall score with score from current pipe
                    game.manager.get("flap.wav", Sound.class).play();
                }
                if (pipe.getX() < -64) {
                    obstacles.removeValue(pipe, true);
                }
            }
        }

        batch.end(); // End SpriteBatch

        batch.begin(); // Begin SpriteBatch again before drawing the pause button
        batch.draw(pauseButtonTexture, pauseButtonBounds.x, pauseButtonBounds.y, pauseButtonBounds.width, pauseButtonBounds.height);
        batch.end(); // End SpriteBatch

        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touch);
            if (pauseButtonBounds.contains(touch.x, touch.y)) {
                paused = !paused; // Toggle pause state
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


    private void spawnObstacle() {
        float holey = MathUtils.random(50, 230);

        Pipe pipe1 = new Pipe();
        pipe1.setX(800);
        pipe1.setY(holey - 230);
        pipe1.setUpsideDown(true);
        pipe1.setManager(game.manager);
        obstacles.add(pipe1);
        stage.addActor(pipe1);

        Pipe pipe2 = new Pipe();
        pipe2.setX(800);
        pipe2.setY(holey + 200);
        pipe2.setUpsideDown(false);
        pipe2.setManager(game.manager);
        obstacles.add(pipe2);
        stage.addActor(pipe2);

        lastObstacleTime = TimeUtils.nanoTime();
    }

    @Override
    public void resize(int width, int height) {
        pauseButtonBounds.set(700, 400, 50, 50);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        pauseButtonTexture.dispose();
        batch.dispose();
        font.dispose();
    }
}