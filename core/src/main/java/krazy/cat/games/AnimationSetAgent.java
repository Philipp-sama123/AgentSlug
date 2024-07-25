package krazy.cat.games;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.EnumMap;
import java.util.Map;

public class AnimationSetAgent {
    private static final float FRAME_DURATION = 0.1f;
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;

    public enum AnimationType {
        IDLE, WALK, RUN, FALLING, JUMPING
    }

    private final Map<AnimationType, Animation<TextureRegion>> animations;
    private boolean flipped = false;

    public AnimationSetAgent(Texture spriteSheet) {
        TextureRegion[][] textureRegions = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);
        animations = new EnumMap<>(AnimationType.class);

        animations.put(AnimationType.IDLE, createAnimation(textureRegions, 0, 5));
        animations.put(AnimationType.WALK, createAnimation(textureRegions, 11, 8));
        animations.put(AnimationType.RUN, createAnimation(textureRegions, 18, 5));
        animations.put(AnimationType.FALLING, createAnimation(textureRegions, 25, 5));
        animations.put(AnimationType.JUMPING, createAnimation(textureRegions, 22, 5));
    }

    private Animation<TextureRegion> createAnimation(TextureRegion[][] textureRegions, int row, int count) {
        TextureRegion[] frames = new TextureRegion[count];
        System.arraycopy(textureRegions[row], 0, frames, 0, count);
        return new Animation<>(FRAME_DURATION, frames);
    }

    public TextureRegion getFrame(AnimationType type, float stateTime, boolean looping) {
        return animations.get(type).getKeyFrame(stateTime, looping);
    }

    public void flipFramesHorizontally() {
        for (Animation<TextureRegion> animation : animations.values()) {
            for (TextureRegion frame : animation.getKeyFrames()) {
                frame.flip(true, false);
            }
        }
        flipped = !flipped;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void dispose() {
        // Dispose resources if necessary
    }
}
