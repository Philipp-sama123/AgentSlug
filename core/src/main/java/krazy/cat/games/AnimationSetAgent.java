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
    private final TextureRegion[] fallingFrames;
    private final TextureRegion[] jumpingFrames;
    private boolean flipped = false;

    public AnimationSetAgent(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);

        // Initialize frames
        idleFrames = setupFrames(textureRegions, 0, 5);
        walkFrames = setupFrames(textureRegions, 11, 8);
        runFrames = setupFrames(textureRegions, 18, 5);
        fallingFrames = setupFrames(textureRegions, 25, 5);  // Example indices
        jumpingFrames = setupFrames(textureRegions, 22, 5);  // Example indices
    }

    private TextureRegion[] setupFrames(TextureRegion[][] textureRegions, int row, int count) {
        TextureRegion[] frames = new TextureRegion[count];
        System.arraycopy(textureRegions[row], 0, frames, 0, count);
        return frames;
    }

    public TextureRegion getIdleFrame(float stateTime) {
        return getFrame(idleFrames, stateTime);
    }

    public TextureRegion getWalkFrame(float stateTime) {
        return getFrame(walkFrames, stateTime);
    }

    public TextureRegion getRunFrame(float stateTime) {
        return getFrame(runFrames, stateTime);
    }

    public TextureRegion getFallingFrame(float stateTime) {
        return getFrame(fallingFrames, stateTime);
    }

    public TextureRegion getJumpingFrame(float stateTime) {
        return getFrame(jumpingFrames, stateTime);
    }

    private TextureRegion getFrame(TextureRegion[] frames, float stateTime) {
        return frames[(int) (stateTime / FRAME_DURATION) % frames.length];
    }

    public void dispose() {
        // Dispose of resources if necessary
    }

    public void flipFramesHorizontally() {
        flipArray(idleFrames);
        flipArray(walkFrames);
        flipArray(runFrames);
        flipArray(fallingFrames);
        flipArray(jumpingFrames);
        flipped = !flipped;
    }

    private void flipArray(TextureRegion[] frames) {
        for (TextureRegion frame : frames) {
            frame.flip(true, false);
        }
    }

    public boolean isFlipped() {
        return flipped;
    }
}
