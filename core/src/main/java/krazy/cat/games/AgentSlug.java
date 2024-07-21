package krazy.cat.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;

public class AgentSlug extends Game {
    public static final int PLAYER_JUMP_HEIGHT = 125;
    public static final int PLAYER_MOVE_DISTANCE = 150;

    SpriteBatch batch;

    Texture background;

    BitmapFont textToShow;

    int pause = 0;
    int gameState = 0;
    int score = 0;

    float gravity = 7.5f;
    float velocity = 0;

    Rectangle mainCharacterRectangle;

    private CharacterManager characterManager;
    private float stateTime;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");

        characterManager = new CharacterManager(new Texture("GandalfHardcoreFemaleAgent/GandalfHardcore Female Agent black.png"));
        createTextToShow();

        Gdx.input.setInputProcessor(new GestureDetector(new InputHandler(this)));
        stateTime = 0f;
    }

    @Override
    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        stateTime += Gdx.graphics.getDeltaTime();

        if (gameState == 0) {
            if (Gdx.input.justTouched()) {
                gameState = 1;
            }
        } else if (gameState == 1) {
            if (pause < 2) {
                pause++;
            } else {
                pause = 0;
                characterManager.update(stateTime);
            }

            velocity += gravity;
            int newMainCharacterY = characterManager.getMainCharacterY() - (int) velocity;

            if (newMainCharacterY <= 0) {
                newMainCharacterY = 0;
            }
            characterManager.setMainCharacterY(newMainCharacterY);

            mainCharacterRectangle = characterManager.getMainCharacterRectangle();
        } else if (gameState == 2) {
            if (Gdx.input.justTouched()) {
                restartGame();
            }
        }

        // Drawing the character based on the game state
        if (gameState == 2) {
            batch.draw(
                characterManager.getCharacterTexture(),
                characterManager.getMainCharacterX(),
                characterManager.getMainCharacterY()
            );
        } else {
            batch.draw(
                characterManager.getCurrentFrame(),
                characterManager.getMainCharacterX(),
                characterManager.getMainCharacterY()
            );
        }
        textToShow.draw(batch, String.valueOf(score), 100, 200);

        batch.end();
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

    private void restartGame() {
        score = 0;
        gameState = 1;
        velocity = 0;
        stateTime = 0f;
        characterManager.resetCharacterPosition();
    }

    public int getGameState() {
        return gameState;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public void moveCharacterLeft() {
        int newMainCharacterX = characterManager.getMainCharacterX() - PLAYER_MOVE_DISTANCE;
        if (newMainCharacterX > 0) {
            characterManager.setMainCharacterX(newMainCharacterX);
        }
    }

    public void moveCharacterRight() {
        int newMainCharacterX = characterManager.getMainCharacterX() + PLAYER_MOVE_DISTANCE;
        if (newMainCharacterX < Gdx.graphics.getWidth() - characterManager.getCurrentFrame().getRegionWidth()) {
            characterManager.setMainCharacterX(newMainCharacterX);
        }
    }
}
