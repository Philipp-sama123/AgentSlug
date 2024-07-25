package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import krazy.cat.games.AnimationSetAgent.AnimationType;

public class CharacterManager {
    public static final float MOVE_SPEED = 200.f;
    public static final float JUMP_SPEED = 1000.f;
    public static final float GRAVITY = -1000.f;

    private final AnimationSetAgent animationSetAgent;
    private Vector2 mainCharacter = new Vector2();
    private Vector2 velocity = new Vector2();
    private float stateTime = 0f;
    ;
    private boolean facingRight = false;
    private AnimationType currentState = AnimationType.IDLE;

    public CharacterManager(Texture spriteSheet) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        resetCharacterPosition();
    }

    public void resetCharacterPosition() {
        mainCharacter.set(Gdx.graphics.getWidth() / 2f - getCurrentFrameWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        stateTime = 0f;
        currentState = AnimationType.IDLE;
        velocity.set(0, 0);
    }

    public void dispose() {
        animationSetAgent.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationSetAgent.getFrame(currentState, stateTime, true);
        adjustFrameOrientation(frame);
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

    public void update(float deltaTime, boolean moveLeft, boolean moveRight, boolean jump, List<Rectangle> platforms) {
        stateTime += deltaTime;
        applyGravity(deltaTime);

        if (jump && canJump()) {
            velocity.y += JUMP_SPEED;
        }

        mainCharacter.y += velocity.y * deltaTime;

        if (mainCharacter.y < 0.f) {
            landOnGround();
        }

        handlePlatformCollisions(platforms);
        handleMovement(deltaTime, moveLeft, moveRight);
        updateState(moveLeft, moveRight, jump);
    }

    private void applyGravity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;
    }

    private boolean canJump() {
        return currentState != AnimationType.JUMPING && currentState != AnimationType.FALLING;
    }

    private void landOnGround() {
        mainCharacter.y = 0.f;
        velocity.y = 0;
        currentState = AnimationType.IDLE;
    }

    private void handleMovement(float deltaTime, boolean moveLeft, boolean moveRight) {
        if (moveLeft) {
            moveCharacterLeft(deltaTime);
        } else if (moveRight) {
            moveCharacterRight(deltaTime);
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
        if (newMainCharacterX < Gdx.graphics.getWidth() - getCurrentFrameWidth()) {
            mainCharacter.x = newMainCharacterX;
            setFacingRight(true);
        }
    }

    private void handlePlatformCollisions(List<Rectangle> platforms) {
        Rectangle characterRect = getMainCharacterRectangle();
        boolean isOnPlatform = false;

        for (Rectangle platform : platforms) {
            if (isCollidingWithPlatform(platform, characterRect)) {
                landOnPlatform(platform);
                isOnPlatform = true;
                break;
            }
        }

        if (!isOnPlatform && mainCharacter.y > 0.f && velocity.y < 0.f) {
            currentState = AnimationType.FALLING;
        }
    }

    private boolean isCollidingWithPlatform(Rectangle platform, Rectangle characterRect) {
        float characterBottom = mainCharacter.y;
        float platformTop = platform.y + platform.height;
        float platformLeft = platform.x;
        float platformRight = platform.x + platform.width;

        return velocity.y <= 0 && characterRect.overlaps(platform) &&
            characterBottom > platformTop - 5 &&
            characterRect.x + characterRect.width / 2 > platformLeft &&
            characterRect.x + characterRect.width / 2 < platformRight;
    }

    private void landOnPlatform(Rectangle platform) {
        mainCharacter.y = platform.y + platform.height;
        velocity.y = 0;
    }

    private void adjustFrameOrientation(TextureRegion frame) {
        if (facingRight && !animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        } else if (!facingRight && animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    private float getCurrentFrameWidth() {
        TextureRegion currentFrame = getCurrentFrame();
        return currentFrame != null ? currentFrame.getRegionWidth() : 0;
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

    private void updateState(boolean moveLeft, boolean moveRight, boolean jump) {
        if (velocity.y > 0) {
            currentState = AnimationType.JUMPING;
        } else if (velocity.y < 0) {
            currentState = AnimationType.FALLING;
        } else if (moveLeft || moveRight) {
            currentState = AnimationType.WALK;
        } else {
            currentState = AnimationType.IDLE;
        }
    }
}
