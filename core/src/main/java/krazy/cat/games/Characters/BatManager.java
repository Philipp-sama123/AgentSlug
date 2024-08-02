package krazy.cat.games.Characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;
import java.util.List;

import krazy.cat.games.Bullet;
import krazy.cat.games.Characters.AnimationSets.AnimationSetBat;

public class BatManager {
    public static final float MOVE_SPEED = 150.f;
    public static final float FLY_SPEED = 200.f;
    public static final float GRAVITY = -500.f;
    public static final float SCALE = 5.0f;

    private final AnimationSetBat animationSetBat;
    private Vector2 batPosition = new Vector2();
    private Vector2 velocity = new Vector2();
    private float stateTime = 0f;
    private AnimationSetBat.BatAnimationType currentAnimationState = AnimationSetBat.BatAnimationType.IDLE1;

    private Sound attackSound;
    private Sound hitSound;

    private boolean isHit = false;
    private boolean attacking = false;
    private boolean facingRight = false;

    private int health = 100; // Initial health
    private boolean isDead = false; // To track if the zombie is dead
    private boolean isDisposable = false;

    public BatManager(Texture spriteSheet) {
        animationSetBat = new AnimationSetBat(spriteSheet);
        resetBatPosition();
        initializeSounds();
    }

    private void initializeSounds() {
        attackSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Hit.wav"));
    }

    public void resetBatPosition() {
        batPosition.set(Gdx.graphics.getWidth() / 2f - getCurrentFrameWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        stateTime = 0f;
        currentAnimationState = AnimationSetBat.BatAnimationType.IDLE1;
        velocity.set(0, 0);
        attacking = false;
    }

    public void dispose() {
        animationSetBat.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationSetBat.getFrame(currentAnimationState, stateTime, true);
        adjustFrameOrientation(frame);
        return frame;
    }

    public Vector2 getBatPosition() {
        return batPosition;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        applyGravity(deltaTime);
    }

    public void updateAnimationState() {
        if (isDead) {
            currentAnimationState = AnimationSetBat.BatAnimationType.DEATH2;
            Animation<TextureRegion> deathAnimation = animationSetBat.getAnimation(currentAnimationState);
            if (deathAnimation.isAnimationFinished(stateTime)) {
                // Here we can mark the zombie for removal
                isDisposable = true;
            }
            return;
        }
        if (isHit) {
            currentAnimationState = AnimationSetBat.BatAnimationType.HIT;
            Animation<TextureRegion> hitAnimation = animationSetBat.getAnimation(currentAnimationState);
            if (hitAnimation.isAnimationFinished(stateTime)) {
                stateTime = 0f;
                isHit = false;
            } else {
                return;
            }
        }

        if (attacking) {
            Animation<TextureRegion> attackAnimation = animationSetBat.getAnimation(currentAnimationState);
            if (attackAnimation.isAnimationFinished(stateTime)) {
                attacking = false;
                stateTime = 0f;
            }
        } else if (velocity.y != 0) {
            currentAnimationState = AnimationSetBat.BatAnimationType.MOVE2;
        } else if (velocity.x != 0) {
            currentAnimationState = AnimationSetBat.BatAnimationType.MOVE1;
        } else {
            currentAnimationState = AnimationSetBat.BatAnimationType.IDLE1;
        }

        velocity.x = 0;
    }

    private void applyGravity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;

        if (batPosition.y < 0.f) {
            landOnGround();
        }
    }

    public void handleCollisions(List<Rectangle> platforms, List<Rectangle> rectangles) {
        handleRectangleCollisions(platforms);
        handleRectangleCollisions(rectangles);
    }

    private void handleRectangleCollisions(List<Rectangle> rectangles) {
        Rectangle batRect = getBatRectangle();

        for (Rectangle rectangle : rectangles) {
            if (batRect.overlaps(rectangle)) {
                if (velocity.y < 0 && batPosition.y + batRect.height / 2 >= (rectangle.y + rectangle.height)) {
                    batPosition.y = rectangle.y + rectangle.height;
                    velocity.y = 0;
                }
            }
        }
    }

    private boolean canAttack() {
        return currentAnimationState != AnimationSetBat.BatAnimationType.GRAB2;
    }

    public boolean isAttacking() {
        return attacking;
    }

    private void landOnGround() {
        batPosition.y = 0.f;
        velocity.y = 0;
        if (!attacking) {
            currentAnimationState = AnimationSetBat.BatAnimationType.IDLE1;
        }
    }

    private void adjustFrameOrientation(TextureRegion frame) {
        if (facingRight && !animationSetBat.isFlipped()) {
            animationSetBat.flipFramesHorizontally();
        } else if (!facingRight && animationSetBat.isFlipped()) {
            animationSetBat.flipFramesHorizontally();
        }
    }

    public void moveBatTowardsCharacter(CharacterManager characterManager, float deltaTime) {
        if (isHit)
            return;
        Vector2 batPosition = getBatPosition();
        Vector2 characterPosition = characterManager.getMainCharacter();

        float distanceToCharacter = batPosition.dst(characterPosition);
        // If the character is within 20 pixels, attack
        if (distanceToCharacter <= 50) {
            if (!attacking) {
                attacking = true;
                stateTime = 0f;
                currentAnimationState = AnimationSetBat.BatAnimationType.GRAB; // Assuming GRAB2 is the attack animation
                attackSound.play();
            }
            return; // Exit the method early to stop movement while attacking
        }

        Vector2 direction = characterPosition.cpy().sub(batPosition).nor();
        getVelocity().set(direction.scl(BatManager.MOVE_SPEED));

        getBatPosition().add(getVelocity().scl(deltaTime));

        boolean isCharacterRight = characterPosition.x > getBatPosition().x;

        // Update facing direction based on character's position
        if (isCharacterRight) {
            setFacingRight(false);
        } else {
            setFacingRight(true);
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    private float getCurrentFrameWidth() {
        TextureRegion currentFrame = getCurrentFrame();
        return currentFrame != null ? currentFrame.getRegionWidth() : 0;
    }

    public Rectangle getBatRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        return new Rectangle(batPosition.x, batPosition.y, currentFrame.getRegionWidth() * SCALE, currentFrame.getRegionHeight() * SCALE);
    }

    public void checkBulletCollisions(List<Bullet> bullets) {
        if(isDead) return;

        Rectangle batRect = getBatRectangle();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (batRect.overlaps(bullet.getBoundingRectangle())) {
                bulletIterator.remove();
                hitSound.play();
                isHit = true;
                stateTime = 0f;
                reduceHealth(25);
            }
        }
    }

    public void renderCharacter(Batch batch) {
        batch.draw(getCurrentFrame(), batPosition.x, batPosition.y, 40 * SCALE, 42 * SCALE);
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

    private void triggerDeath() {
        isDead = true;
        currentAnimationState = AnimationSetBat.BatAnimationType.DEATH2;
        stateTime = 0f;
    }

    public boolean isDisposable() {
        return isDisposable;
    }

    public boolean isDead() {
        return isDead;
    }
}
