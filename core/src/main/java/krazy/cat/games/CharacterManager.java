package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class CharacterManager {
    private final AnimationSetAgent animationSetAgent;
    private float mainCharacterY;
    private float mainCharacterX;
    private float stateTime;
    private boolean isWalking;
    private boolean facingRight;
    private boolean isJumping;
    private boolean isFalling;

    public CharacterManager(Texture spriteSheet) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        resetCharacterPosition();
        facingRight = false; // Character starts facing left
    }

    public void resetCharacterPosition() {
        mainCharacterY = Gdx.graphics.getHeight() / 2f;
        mainCharacterX = Gdx.graphics.getWidth() / 2f - (getCurrentFrame() != null ? getCurrentFrame().getRegionWidth() / 2f : 0f);
        stateTime = 0f;
        isWalking = false;
        isJumping = false;
        isFalling = false;
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

    public float getMainCharacterY() {
        return mainCharacterY;
    }

    public void setMainCharacterY(float mainCharacterY) {
        this.mainCharacterY = mainCharacterY;
    }

    public float getMainCharacterX() {
        return mainCharacterX;
    }

    public void setMainCharacterX(float mainCharacterX) {
        this.mainCharacterX = mainCharacterX;
    }

    public void update(float deltaTime, boolean isWalking, boolean isJumping, boolean isFalling) {
        stateTime += deltaTime;
        this.isWalking = isWalking;
        this.isJumping = isJumping;
        this.isFalling = isFalling;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public Rectangle getMainCharacterRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        return new Rectangle(
            mainCharacterX,
            mainCharacterY,
            currentFrame.getRegionWidth(),
            currentFrame.getRegionHeight()
        );
    }
}
