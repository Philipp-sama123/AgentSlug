package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class CharacterManager {
    private final AnimationSetAgent animationSetAgent;
    private int mainCharacterY;
    private int mainCharacterX;
    private float stateTime;
    private boolean isWalking;
    private boolean facingRight;

    public CharacterManager(Texture spriteSheet) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        resetCharacterPosition();
        facingRight = false; // Character starts facing left
    }

    public void resetCharacterPosition() {
        mainCharacterY = Gdx.graphics.getHeight() / 2;
        mainCharacterX = Gdx.graphics.getWidth() / 2 - (getCurrentFrame() != null ? getCurrentFrame().getRegionWidth() / 2 : 0);
        stateTime = 0f;
        isWalking = false;
    }

    public void dispose() {
        animationSetAgent.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame;
        if (isWalking) {
            frame = animationSetAgent.getWalkFrame(stateTime);
        } else {
            frame = animationSetAgent.getIdleFrame(stateTime);
        }
        if (facingRight && !animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        } else if (!facingRight && animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        }
        return frame;
    }

    public int getMainCharacterY() {
        return mainCharacterY;
    }

    public void setMainCharacterY(int mainCharacterY) {
        this.mainCharacterY = mainCharacterY;
    }

    public int getMainCharacterX() {
        return mainCharacterX;
    }

    public void setMainCharacterX(int mainCharacterX) {
        this.mainCharacterX = mainCharacterX;
    }

    public void update(float deltaTime, boolean isWalking) {
        stateTime += deltaTime;
        this.isWalking = isWalking;
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
