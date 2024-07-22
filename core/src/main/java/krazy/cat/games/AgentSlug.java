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
    public static final float MOVE_SPEED = 200.f;
    public static final float JUMP_SPEED = -50.f;
    public static final float GRAVITY = 2.5f;

    private SpriteBatch batch;
    private Texture background;
    private BitmapFont textToShow;

    private int score = 0;
    private float velocityY = 0.f;

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
        stateTime = 0.f;
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
        velocityY += GRAVITY;

        boolean isJumping = false;
        boolean isFalling = false;
        boolean isWalking = false;

        if (inputHandler.isJumpPressed() && characterManager.getMainCharacterY() == 0.f) {
            velocityY = JUMP_SPEED;
            isJumping = true;
        }

        if (velocityY < 0.f && characterManager.getMainCharacterY() > 0.f) {
            isFalling = true;
        }

        float newMainCharacterY = characterManager.getMainCharacterY() - velocityY;
        if (newMainCharacterY < 0.f) {
            newMainCharacterY = 0.f;
            velocityY = 0;
            isFalling = false;
            isJumping = false;
        }
        characterManager.setMainCharacterY(newMainCharacterY);

        if (inputHandler.isLeftPressed()) {
            moveCharacterLeft(deltaTime);
            isWalking = true;
        } else if (inputHandler.isRightPressed()) {
            moveCharacterRight(deltaTime);
            isWalking = true;
        }

        characterManager.update(deltaTime, isWalking, isJumping, isFalling);
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
        float newMainCharacterX = characterManager.getMainCharacterX() - MOVE_SPEED * deltaTime;
        if (newMainCharacterX > 0) {
            characterManager.setMainCharacterX(newMainCharacterX);
            characterManager.setFacingRight(false);
        }
    }

    private void moveCharacterRight(float deltaTime) {
        float newMainCharacterX = characterManager.getMainCharacterX() + MOVE_SPEED * deltaTime;
        if (newMainCharacterX < Gdx.graphics.getWidth() - characterManager.getCurrentFrame().getRegionWidth()) {
            characterManager.setMainCharacterX(newMainCharacterX);
            characterManager.setFacingRight(true);
        }
    }
}
