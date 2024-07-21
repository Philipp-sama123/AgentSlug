package krazy.cat.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class AgentSlug extends Game {
    public static final int SCALE = 5;
    public static final float MOVE_SPEED = 200f;
    public static final float JUMP_SPEED = -100f;
    public static final float GRAVITY = 7.5f;

    private SpriteBatch batch;
    private Texture background;
    private BitmapFont textToShow;

    private int score = 0;
    private float velocity = 0;

    private Rectangle mainCharacterRectangle;
    private CharacterManager characterManager;
    private float stateTime;

    private InputHandler inputHandler;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");

        characterManager = new CharacterManager(new Texture("GandalfHardcoreFemaleAgent/GandalfHardcore Female Agent black.png"));
        createTextToShow();

        inputHandler = new InputHandler(this);
        Gdx.input.setInputProcessor(inputHandler);
        stateTime = 0f;
    }

    @Override
    public void render() {
        batch.begin();
        renderBackground();
        updateGameState(Gdx.graphics.getDeltaTime());
        renderCharacter();
        renderScore();
        batch.end();
    }

    private void renderBackground() {
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void updateGameState(float deltaTime) {
        stateTime += deltaTime;
        velocity += GRAVITY;

        if (inputHandler.isJumpPressed() && characterManager.getMainCharacterY() == 0) {
            velocity = JUMP_SPEED;
        }

        int newMainCharacterY = characterManager.getMainCharacterY() - (int) velocity;
        if (newMainCharacterY <= 0) {
            newMainCharacterY = 0;
            velocity = 0;
        }
        characterManager.setMainCharacterY(newMainCharacterY);

        boolean isWalking = false;
        if (inputHandler.isLeftPressed()) {
            moveCharacterLeft(deltaTime);
            isWalking = true;
        } else if (inputHandler.isRightPressed()) {
            moveCharacterRight(deltaTime);
            isWalking = true;
        }

        characterManager.update(deltaTime, isWalking);
        mainCharacterRectangle = characterManager.getMainCharacterRectangle();
    }

    private void renderCharacter() {
        batch.draw(
            characterManager.getCurrentFrame(),
            characterManager.getMainCharacterX(),
            characterManager.getMainCharacterY(),
            64 * SCALE, 64 * SCALE
        );
    }

    private void renderScore() {
        textToShow.draw(batch, String.valueOf(score), 100, 200);
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        characterManager.dispose();
        textToShow.dispose();
    }

    private void createTextToShow() {
        textToShow = new BitmapFont();
        textToShow.setColor(Color.WHITE);
        textToShow.getData().setScale(10);
    }

    private void moveCharacterLeft(float deltaTime) {
        int newMainCharacterX = (int) (characterManager.getMainCharacterX() - MOVE_SPEED * deltaTime);
        if (newMainCharacterX > 0) {
            characterManager.setMainCharacterX(newMainCharacterX);
            characterManager.setFacingRight(false);
        }
    }

    private void moveCharacterRight(float deltaTime) {
        int newMainCharacterX = (int) (characterManager.getMainCharacterX() + MOVE_SPEED * deltaTime);
        if (newMainCharacterX < Gdx.graphics.getWidth() - characterManager.getCurrentFrame().getRegionWidth()) {
            characterManager.setMainCharacterX(newMainCharacterX);
            characterManager.setFacingRight(true);
        }
    }
}
