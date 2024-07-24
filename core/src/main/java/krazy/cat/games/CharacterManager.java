package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CharacterManager {
    public static final float MOVE_SPEED = 200.f;
    public static final float JUMP_SPEED = 1000.f; // Positive jump speed
    public static final float GRAVITY = -1000.f; // Negative gravity to pull the character down

    private final AnimationSetAgent animationSetAgent;
    private Vector2 mainCharacter; // Position of the character
    private float stateTime;
    private boolean isWalking;
    private boolean facingRight;
    private boolean isJumping;
    private boolean isFalling;
    private Vector2 velocity;

    public CharacterManager(Texture spriteSheet) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        mainCharacter = new Vector2();
        velocity = new Vector2();
        resetCharacterPosition();
        facingRight = false; // Character starts facing left
    }

    public void resetCharacterPosition() {
        mainCharacter.set(
            Gdx.graphics.getWidth() / 2f - (getCurrentFrame() != null ? getCurrentFrame().getRegionWidth() / 2f : 0f),
            Gdx.graphics.getHeight() / 2f
        );
        stateTime = 0f;
        isWalking = false;
        isJumping = false;
        isFalling = false;
        velocity.set(0, 0);
    }

    public void dispose() {
        animationSetAgent.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame;
        if (isJumping) {
            frame = animationSetAgent.getJumpingFrame(stateTime);
        } else if (isFalling) {
            frame = animationSetAgent.getFallingFrame(stateTime);
        } else if (isWalking) {
            frame = animationSetAgent.getWalkFrame(stateTime);
        } else {
            frame = animationSetAgent.getIdleFrame(stateTime);
        }

        // Flip frames if necessary
        if (facingRight && !animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        } else if (!facingRight && animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        }

        return frame;
    }

    public Vector2 getMainCharacter() {
        return mainCharacter;
    }

    public void setMainCharacter(Vector2 mainCharacter) {
        this.mainCharacter = mainCharacter;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void update(float deltaTime, boolean moveLeft, boolean moveRight, boolean jump) {
        stateTime += deltaTime;
        velocity.y += GRAVITY * deltaTime; // Apply gravity to velocity

        if (jump && mainCharacter.y == 0.f) {
            velocity.y += JUMP_SPEED; // Set velocity for jump
        }

        if (velocity.y > 0) {
            isJumping = true;
            isFalling = false;
        } else if (velocity.y < 0.f) {
            isJumping = false;
            isFalling = true;
        }

        mainCharacter.y += velocity.y * deltaTime; // Update character's vertical position

        // Ensure the character doesn't fall below the ground
        if (mainCharacter.y < 0.f) {
            mainCharacter.y = 0.f;
            velocity.y = 0;
            isFalling = false;
            isJumping = false;
        }

        isWalking = false;
        if (moveLeft) {
            moveCharacterLeft(deltaTime);
            isWalking = true;
        } else if (moveRight) {
            moveCharacterRight(deltaTime);
            isWalking = true;
        }
    }

    private void moveCharacterLeft(float deltaTime) {
        float newMainCharacterX = mainCharacter.x - MOVE_SPEED * deltaTime;
        if (newMainCharacterX > 0) {
            mainCharacter.x = newMainCharacterX;
            setFacingRight(false);
        }
    }

    private void moveCharacterRight(float deltaTime) {
        float newMainCharacterX = mainCharacter.x + MOVE_SPEED * deltaTime;
        if (newMainCharacterX < Gdx.graphics.getWidth() - getCurrentFrame().getRegionWidth()) {
            mainCharacter.x = newMainCharacterX;
            setFacingRight(true);
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public Rectangle getMainCharacterRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        return new Rectangle(
            facingRight ? mainCharacter.x + 100 : mainCharacter.x + 150,
            mainCharacter.y,
            currentFrame.getRegionWidth() * AgentSlug.SCALE - 250,
            currentFrame.getRegionHeight() * AgentSlug.SCALE - 100
        );
    }

    /*The whole character*/
    public Rectangle getFullCharacterRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        return new Rectangle(
            mainCharacter.x,
            mainCharacter.y,
            currentFrame.getRegionWidth() * AgentSlug.SCALE,
            currentFrame.getRegionHeight() * AgentSlug.SCALE
        );
    }

}
