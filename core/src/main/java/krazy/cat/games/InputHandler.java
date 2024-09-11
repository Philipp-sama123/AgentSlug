package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import krazy.cat.games.UI.GameScreen;

public class InputHandler implements InputProcessor {

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean runLeftPressed = false;
    private boolean runRightPressed = false;
    private boolean jumpPressed = false;
    private boolean crouchPressed = false;

    private boolean attackPressed = false;

    public void updateJoystickMovement(GameScreen gameScreen) {
        // Use joystick input to set movement flags
        if (gameScreen.getMovementJoystick() != null) {
            float joystickX = gameScreen.getMovementJoystick().getKnobPercentX(); // Knob percentage movement on the X-axis
            leftPressed = joystickX < -0.1f;   // Move left if joystick is pushed left
            rightPressed = joystickX > 0.1f;   // Move right if joystick is pushed right
            runLeftPressed = joystickX < -0.75f;
            runRightPressed = joystickX > 0.75f;

            jumpPressed = gameScreen.getMovementJoystick().getKnobPercentY() > 0.75;
            crouchPressed = gameScreen.getMovementJoystick().getKnobPercentY() < -.5;
        }
    }

    public boolean isCrouchPressed() {
        return crouchPressed;
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

    public void setShootPressed(boolean shootPressed) {
        attackPressed = shootPressed;
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

        if (screenX >= leftWidth && screenX < rightWidth && screenY < topHeight) {
            // Top-center: Jump
            jumpPressed = true;
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

        if (screenX >= leftWidth && screenX < rightWidth && screenY < topHeight) {
            // Top-center: Jump
            jumpPressed = false;
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
