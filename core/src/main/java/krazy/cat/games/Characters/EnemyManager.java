package krazy.cat.games.Characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;
import java.util.List;

import krazy.cat.games.Bullet;

public abstract class EnemyManager {
    protected static final float SCALE = 5.0f;

    protected Vector2 position = new Vector2();
    protected Vector2 velocity = new Vector2();
    protected float stateTime = 0f;
    protected boolean facingRight = false;
    protected boolean attacking = false;
    protected boolean isHit = false;
    protected boolean isDead = false;
    protected boolean isDisposable = false;
    protected int health = 100;

    protected Sound attackSound;
    protected Sound hitSound;

    protected abstract void initializeSounds();

    public EnemyManager() {
        initializeSounds();
        resetPosition();
    }

    protected abstract void resetPosition();

    public abstract TextureRegion getCurrentFrame();

    protected abstract void adjustFrameOrientation(TextureRegion frame);

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        applyGravity(deltaTime);
        updateAnimationState();
    }

    protected abstract void applyGravity(float deltaTime);

    public abstract void updateAnimationState();

    public void handleCollisions(List<Rectangle> platforms, List<Rectangle> rectangles) {
        handleRectangleCollisions(platforms);
        handleRectangleCollisions(rectangles);
    }

    private void handleRectangleCollisions(List<Rectangle> rectangles) {
        Rectangle characterRect = getCharacterRectangle();

        for (Rectangle rectangle : rectangles) {
            if (characterRect.overlaps(rectangle)) {
                if (velocity.y < 0 && position.y + characterRect.height / 2 >= (rectangle.y + rectangle.height)) {
                    position.y = rectangle.y + rectangle.height;
                    velocity.y = 0;
                }
            }
        }
    }

    protected abstract boolean canAttack();

    public boolean isAttacking() {
        return attacking;
    }

    protected abstract void landOnGround();

    public void checkBulletCollisions(List<Bullet> bullets) {
        if (isDead) return;

        Rectangle characterRect = getCharacterRectangle();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (characterRect.overlaps(bullet.getBoundingRectangle())) {
                bulletIterator.remove();
                hitSound.play();
                isHit = true;
                stateTime = 0f;
                reduceHealth(25); // Assuming a fixed damage value here
            }
        }
    }

    public void renderCharacter(Batch batch) {
        batch.draw(getCurrentFrame(), position.x, position.y, getCurrentFrameWidth() * SCALE, getCurrentFrameHeight() * SCALE);
    }

    public void reduceHealth(int amount) {
        if (isDead) return; // Do nothing if already dead

        health -= amount;
        if (health <= 0) {
            health = 0;
            triggerDeath();
        } else {
            isHit = true;
            stateTime = 0f;
        }
    }

    protected void triggerDeath() {
        isDead = true;
        stateTime = 0f;
    }

    public boolean isDisposable() {
        return isDisposable;
    }

    public boolean isDead() {
        return isDead;
    }

    protected abstract Rectangle getCharacterRectangle();

    protected abstract float getCurrentFrameWidth();

    protected abstract float getCurrentFrameHeight();

    public void dispose() {

    }
}
