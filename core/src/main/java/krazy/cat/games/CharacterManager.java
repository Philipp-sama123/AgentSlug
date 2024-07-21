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

    public CharacterManager(Texture spriteSheet) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        resetCharacterPosition();
    }

    public void resetCharacterPosition() {
        mainCharacterY = Gdx.graphics.getHeight() / 2;
        mainCharacterX = Gdx.graphics.getWidth() / 2 - (getCurrentFrame() != null ? getCurrentFrame().getRegionWidth() / 2 : 0);
        stateTime = 0f;
    }

    public void dispose() {
        animationSetAgent.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationSetAgent.getIdleFrame(stateTime);
        if (frame == null) {
            Gdx.app.log("CharacterManager", "Current frame is null");
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

    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    public Rectangle getMainCharacterRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        if (currentFrame != null) {
            return new Rectangle(
                mainCharacterX,
                mainCharacterY,
                currentFrame.getRegionWidth(),
                currentFrame.getRegionHeight()
            );
        } else {
            // Handle the case where the frame is null
            return new Rectangle(mainCharacterX, mainCharacterY, 0, 0);
        }
    }

    public Texture getCharacterTexture() {
        return animationSetAgent.getSpriteSheet();
    }
}
