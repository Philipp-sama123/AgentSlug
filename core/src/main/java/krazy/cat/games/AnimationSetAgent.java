package krazy.cat.games;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationSetAgent {
    private static final float FRAME_DURATION = 0.1f;
    private static final int FRAME_WIDTH = 64;  // Width of each frame
    private static final int FRAME_HEIGHT = 64; // Height of each frame

    private final TextureRegion[] idleFrames;
    private final TextureRegion[] walkFrames;
    private final TextureRegion[] runFrames;

    public AnimationSetAgent(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        int spriteSheetCols = 12; // Number of columns in the sprite sheet
        int spriteSheetRows = 32; // Number of rows in the sprite sheet

        // Initialize frames
        idleFrames = setupFrames(textureRegions, 0, 5);
        walkFrames = setupFrames(textureRegions, 13, 5);
        runFrames = setupFrames(textureRegions, 18, 5);
    }

    private TextureRegion[] setupFrames(TextureRegion[][] textureRegions, int row, int count) {
        TextureRegion[] frames = new TextureRegion[count];
        System.arraycopy(textureRegions[row], 0, frames, 0, count);
        return frames;
    }

    public TextureRegion getIdleFrame(float stateTime) {
        return getFrame(idleFrames, stateTime);
    }

    public float getIdleDuration() {
        return getAnimationDuration(idleFrames);
    }

    public TextureRegion getWalkFrame(float stateTime) {
        return getFrame(walkFrames, stateTime);
    }

    public float getWalkDuration() {
        return getAnimationDuration(walkFrames);
    }

    public TextureRegion getRunFrame(float stateTime) {
        return getFrame(runFrames, stateTime);
    }

    public float getRunDuration() {
        return getAnimationDuration(runFrames);
    }

    private TextureRegion getFrame(TextureRegion[] frames, float stateTime) {
        return frames[(int) (stateTime / FRAME_DURATION) % frames.length];
    }

    private float getAnimationDuration(TextureRegion[] frames) {
        return frames.length * FRAME_DURATION;
    }

    public void dispose() {
        // Dispose of resources if necessary
    }

    // Note: If you need to access the sprite sheet, consider keeping a reference or passing it to the constructor.
    // This method is kept for demonstration purposes.
    public Texture getSpriteSheet() {
        return new Texture("GandalfHardcoreFemaleAgent/Female Agent Portrait 64x64  (4).png");
    }
}
