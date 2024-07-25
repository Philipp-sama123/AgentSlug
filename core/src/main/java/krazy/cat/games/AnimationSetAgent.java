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

        // Debug: Check that all animations are created
        for (AnimationType type : AnimationType.values()) {
            if (animations.get(type) == null) {
                System.err.println("Animation " + type + " is null");
            } else {
                System.out.println("Animation " + type + " created successfully");
            }
        }
    }

    private Animation<TextureRegion> createAnimation(TextureRegion[][] textureRegions, int row, int count) {
        if (row >= textureRegions.length || count > textureRegions[row].length) {
            System.err.println("Error: Frame extraction out of bounds. Row: " + row + ", Count: " + count);
            return null;
        }
        TextureRegion[] frames = new TextureRegion[count];
        System.arraycopy(textureRegions[row], 0, frames, 0, count);
        return new Animation<>(FRAME_DURATION, frames);
    }

    public TextureRegion getFrame(AnimationType type, float stateTime, boolean looping) {
        Animation<TextureRegion> animation = animations.get(type);
        if (animation == null) {
            throw new IllegalStateException("Animation " + type + " not found");
        }
        return animation.getKeyFrame(stateTime, looping);
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
