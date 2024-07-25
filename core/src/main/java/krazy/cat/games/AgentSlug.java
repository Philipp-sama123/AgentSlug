package krazy.cat.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class AgentSlug extends Game {
    public static final int SCALE = 5;

    private SpriteBatch batch;
    private Texture background;
    private BitmapFont textToShow;

    private int score = 0;

    private CharacterManager characterManager;
    private float stateTime;

    private InputHandler inputHandler;
    private ShapeRenderer shapeRenderer;

    private List<Rectangle> platforms;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");

        characterManager = new CharacterManager(new Texture("GandalfHardcoreFemaleAgent/GandalfHardcore Female Agent black.png"));
        createTextToShow();

        inputHandler = new InputHandler(this);
        Gdx.input.setInputProcessor(inputHandler);
        stateTime = 0.f;

        shapeRenderer = new ShapeRenderer();

        createPlatforms();
    }

    @Override
    public void render() {
        batch.begin();
        renderBackground();
        updateGameState(Gdx.graphics.getDeltaTime());
        renderCharacter();
        renderScore();
        batch.end();

        renderCharacterRectangle();
        renderPlatforms();
    }

    private void renderBackground() {
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void updateGameState(float deltaTime) {
        stateTime += deltaTime;

        characterManager.update(deltaTime, inputHandler.isLeftPressed(), inputHandler.isRightPressed(), inputHandler.isJumpPressed(), platforms);

        // Log character's state for debugging
        Gdx.app.log("Character Y", String.valueOf(characterManager.getMainCharacter()));
        Gdx.app.log("Velocity ", String.valueOf(characterManager.getVelocity()));
    }

    private void renderCharacter() {
        batch.draw(
            characterManager.getCurrentFrame(),
            characterManager.getMainCharacter().x,
            characterManager.getMainCharacter().y,
            64 * SCALE, 64 * SCALE
        );
    }

    private void renderScore() {
        textToShow.draw(batch, String.valueOf(score), 100, 200);
    }

    private void renderCharacterRectangle() {
        Rectangle characterRect = characterManager.getMainCharacterRectangle();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(characterRect.x, characterRect.y, characterRect.width, characterRect.height);
        shapeRenderer.end();
    }

    private void renderPlatforms() {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BROWN);
        for (Rectangle platform : platforms) {
            shapeRenderer.rect(platform.x, platform.y, platform.width, platform.height);
        }
        shapeRenderer.end();
    }

    private void createPlatforms() {
        platforms = new ArrayList<>();
        platforms.add(new Rectangle(100, 150, 200, 20));  // Platform 1
        platforms.add(new Rectangle(400, 300, 200, 20));  // Platform 2
        platforms.add(new Rectangle(700, 450, 200, 20));  // Platform 3
        // Add more platforms as needed
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        characterManager.dispose();
        textToShow.dispose();
        shapeRenderer.dispose();
    }

    private void createTextToShow() {
        textToShow = new BitmapFont();
        textToShow.setColor(Color.WHITE);
        textToShow.getData().setScale(10);
    }
}
