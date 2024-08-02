package krazy.cat.games;


import static krazy.cat.games.GameLoop.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    public static final int BULLET_WIDTH = 16;
    public static final int BULLET_HEIGHT = 12;
    private static final float BULLET_SPEED = 100;

    private Vector2 position;
    private Vector2 velocity;

    private TextureRegion[] bulletFrames;
    private float stateTime;
    private int currentFrameIndex;
    Texture bulletTexture;

    public Bullet(Vector2 position, boolean facingRight) {
        bulletTexture = new Texture("Pixel Bullet 16x16.png");

        this.position = position;
        this.velocity = new Vector2(facingRight ? BULLET_SPEED : -BULLET_SPEED, 0);
        this.stateTime = 0;

        initializeTextures(facingRight);
        currentFrameIndex = 0;
    }

    private void initializeTextures(boolean facingRight) {
        bulletFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            bulletFrames[i] = new TextureRegion(bulletTexture, i * BULLET_WIDTH, 0, BULLET_WIDTH, BULLET_HEIGHT);
            if (facingRight)
                bulletFrames[i].flip(true, true); // Flip horizontally and vertically for 180-degree rotation
        }
    }

    public void update(float deltaTime) {
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        stateTime += deltaTime;

        if (stateTime >= 0.1f) { // Change frame every 0.1 second
            stateTime = 0;
            currentFrameIndex = (currentFrameIndex + 1) % bulletFrames.length;
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public TextureRegion getCurrentFrame() {
        return bulletFrames[currentFrameIndex];
    }

    public Rectangle getBoundingRectangle() {
        return new Rectangle(position.x, position.y, BULLET_WIDTH, BULLET_HEIGHT);
    }

    public void render(Batch batch) {
        batch.draw(getCurrentFrame(), getPosition().x, getPosition().y,
            Bullet.BULLET_WIDTH * SCALE, Bullet.BULLET_HEIGHT * SCALE);
    }
}
