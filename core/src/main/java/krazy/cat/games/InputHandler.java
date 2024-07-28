package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class InputHandler implements InputProcessor {

    private final AgentSlug game;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean runLeftPressed = false;
    private boolean runRightPressed = false;
    private boolean attackPressed = false;

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

    public boolean isAttackPressed() {
        return attackPressed;
    }

    public boolean isRunLeftPressed() {
        return runLeftPressed;
    }

    public boolean isRunRightPressed() {
        return runRightPressed;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Define screen regions
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        // Regions (Assuming screen is divided into 3x2 grid)
        float leftWidth = width / 3;
        float rightWidth = 2 * width / 3;
        float topHeight = height / 2;
        float bottomHeight = height;

        if (screenX < leftWidth && screenY < topHeight) {
            // Top-left: Run Left
            runLeftPressed = true;
        } else if (screenX >= leftWidth && screenX < rightWidth && screenY < topHeight) {
            // Top-center: Jump
            jumpPressed = true;
        } else if (screenX >= rightWidth && screenY < topHeight) {
            // Top-right: Run Right
            runRightPressed = true;
        } else if (screenX < leftWidth && screenY >= topHeight) {
            // Bottom-left: Walk Left
            leftPressed = true;
        } else if (screenX >= leftWidth && screenX < rightWidth && screenY >= topHeight) {
            // Bottom-center: Attack
            attackPressed = true;
        } else if (screenX >= rightWidth && screenY >= topHeight) {
            // Bottom-right: Walk Right
            rightPressed = true;
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Define screen regions
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        // Regions (Assuming screen is divided into 3x2 grid)
        float leftWidth = width / 3;
        float rightWidth = 2 * width / 3;
        float topHeight = height / 2;
        float bottomHeight = height;

        if (screenX < leftWidth && screenY < topHeight) {
            // Top-left: Run Left
            runLeftPressed = false;
        } else if (screenX >= leftWidth && screenX < rightWidth && screenY < topHeight) {
            // Top-center: Jump
            jumpPressed = false;
        } else if (screenX >= rightWidth && screenY < topHeight) {
            // Top-right: Run Right
            runRightPressed = false;
        } else if (screenX < leftWidth && screenY >= topHeight) {
            // Bottom-left: Walk Left
            leftPressed = false;
        } else if (screenX >= leftWidth && screenX < rightWidth && screenY >= topHeight) {
            // Bottom-center: Attack
            attackPressed = false;
        } else if (screenX >= rightWidth && screenY >= topHeight) {
            // Bottom-right: Walk Right
            rightPressed = false;
        }

        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Optionally handle touch dragged events if needed
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
