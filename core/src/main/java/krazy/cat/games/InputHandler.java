package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class InputHandler implements InputProcessor {

    private final AgentSlug game;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;

    public InputHandler(AgentSlug game) {
        this.game = game;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (screenX < Gdx.graphics.getWidth() / 3) {
            leftPressed = true;
            rightPressed = false;
            jumpPressed = false;
        } else if (screenX > 2 * Gdx.graphics.getWidth() / 3) {
            rightPressed = true;
            leftPressed = false;
            jumpPressed = false;
        } else {
            jumpPressed = true;
            leftPressed = false;
            rightPressed = false;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }
}
