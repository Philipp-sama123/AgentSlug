package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class InputHandler implements GestureDetector.GestureListener {

    private final AgentSlug game;

    public InputHandler(AgentSlug game) {
        this.game = game;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (game.getGameState() == 1) {
            if (x < Gdx.graphics.getWidth() / 2) {
                game.moveCharacterLeft();
            } else {
                game.moveCharacterRight();
            }
        }
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (game.getGameState() == 1) {
            if (Math.abs(velocityY) > Math.abs(velocityX) && velocityY < 0) {
                // Swipe up detected
                game.setVelocity(-AgentSlug.PLAYER_JUMP_HEIGHT);
            } else if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX > 0) {
                    game.moveCharacterRight();
                } else {
                    game.moveCharacterLeft();
                }
            }
        }
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
    }
}
