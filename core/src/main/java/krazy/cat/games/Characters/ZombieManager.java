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

import krazy.cat.games.Characters.AnimationSets.AnimationSetZombie;
import krazy.cat.games.Characters.AnimationSets.AnimationSetZombie.ZombieAnimationType;
import krazy.cat.games.Bullet;

public class ZombieManager {
    public static final float MOVE_SPEED = 100.f;
    public static final float RUN_SPEED = 300.f;
    public static final float JUMP_SPEED = 1000.f;
    public static final float GRAVITY = -1000.f;
    public static final float SCALE = 5.0f; // Adjust based on your specific scale

    private final AnimationSetZombie animationSetZombie;
    private Vector2 mainCharacter = new Vector2();
    private Vector2 velocity = new Vector2();
    private float stateTime = 0f;
    private boolean facingRight = false;
    private ZombieAnimationType currentAnimationState = ZombieAnimationType.IDLE;
    private boolean attacking = false;

    private Sound attackSound;
    private Sound hitSound;
    private boolean isHit = false;

    private int health = 100; // Initial health
    private boolean isDead = false; // To track if the zombie is dead
    private boolean isDisposable = false;


    public ZombieManager(Texture spriteSheet) {
        animationSetZombie = new AnimationSetZombie(spriteSheet);
        resetCharacterPosition();
        initializeSounds();
    }

    private void initializeSounds() {
        attackSound = Gdx.audio.newSound(Gdx.files.internal("SFX/ZombieAttack.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Hit.wav"));
    }

    public void resetCharacterPosition() {
        mainCharacter.set(Gdx.graphics.getWidth() / 2f - getCurrentFrameWidth() / 2f, Gdx.graphics.getHeight());
        stateTime = 0f;
        currentAnimationState = ZombieAnimationType.IDLE;
        velocity.set(0, 0);
        attacking = false;
    }

    public void dispose() {
        animationSetZombie.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationSetZombie.getFrame(currentAnimationState, stateTime, true);
        adjustFrameOrientation();
        return frame;
    }

    public Vector2 getMainCharacter() {
        return mainCharacter;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        applyGravity(deltaTime);
        updateAnimationState();
    }

    public void updateAnimationState() {
        if (isDead) {
            currentAnimationState = ZombieAnimationType.DEATH;
            Animation<TextureRegion> deathAnimation = animationSetZombie.getAnimation(currentAnimationState);
            if (deathAnimation.isAnimationFinished(stateTime)) {
                // Here we can mark the zombie for removal
                isDisposable = true;
            }
            return;
        }
        if (isHit) {
            currentAnimationState = ZombieAnimationType.HIT;
            Animation<TextureRegion> hitAnimation = animationSetZombie.getAnimation(currentAnimationState);
            if (hitAnimation.isAnimationFinished(stateTime)) {
                stateTime = 0f;
                isHit = false;
            } else {
                return;
            }
        }

        if (attacking) {
            // Check if the attacking animation has finished
            Animation<TextureRegion> attackAnimation = animationSetZombie.getAnimation(currentAnimationState);
            if (attackAnimation.isAnimationFinished(stateTime)) {
                attacking = false;
                stateTime = 0f;
            }
        }

        if (velocity.x != 0) {
            currentAnimationState = attacking ? ZombieAnimationType.WALK_ATTACK : ZombieAnimationType.WALK;
        } else {
            currentAnimationState = attacking ? ZombieAnimationType.ATTACK : ZombieAnimationType.IDLE;
        }

        // Reset horizontal velocity after applying
        velocity.x = 0;
    }

    private void applyGravity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;

        if (mainCharacter.y < 0.f) {
            landOnGround();
        }
    }

    public void handleCollisions(List<Rectangle> platforms, List<Rectangle> rectangles) {
        handleRectangleCollisions(platforms);
        handleRectangleCollisions(rectangles);
    }

    private void handleRectangleCollisions(List<Rectangle> rectangles) {
        Rectangle characterRect = getMainZombieRectangle();

        for (Rectangle rectangle : rectangles) {
            if (characterRect.overlaps(rectangle)) {
                if (velocity.y < 0 && mainCharacter.y + characterRect.height / 2 >= (rectangle.y + rectangle.height)) {
                    mainCharacter.y = rectangle.y + rectangle.height;
                    velocity.y = 0;
                }
            }
        }
    }

    private boolean canAttack() {
        return currentAnimationState != ZombieAnimationType.ATTACK;
    }

    public boolean isAttacking() {
        return attacking;
    }

    private void landOnGround() {
        mainCharacter.y = 0.f;
        velocity.y = 0;
        if (!attacking) {
            currentAnimationState = ZombieAnimationType.IDLE;
        }
        // Jump if under the floor
        velocity.y += JUMP_SPEED;
    }

    private void adjustFrameOrientation() {
        if (facingRight && !animationSetZombie.isFlipped()) {
            animationSetZombie.flipFramesHorizontally();
        } else if (!facingRight && animationSetZombie.isFlipped()) {
            animationSetZombie.flipFramesHorizontally();
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    private float getCurrentFrameWidth() {
        TextureRegion currentFrame = getCurrentFrame();
        return currentFrame != null ? currentFrame.getRegionWidth() : 0;
    }

    public Rectangle getMainZombieRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        return new Rectangle(facingRight ? mainCharacter.x + 100 : mainCharacter.x + 50, mainCharacter.y, currentFrame.getRegionWidth() * SCALE - 150, currentFrame.getRegionHeight() * SCALE - 100);
    }

    public void checkBulletCollisions(List<Bullet> bullets) {
        if (isDead) return;

        Rectangle characterRect = getMainZombieRectangle();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (characterRect.overlaps(bullet.getBoundingRectangle())) {
                bulletIterator.remove();
                hitSound.play();
                isHit = true;
                stateTime = 0f;
                //ToDo:     reduceHealth(bullet.getDamage()); // Assuming Bullet has a getDamage method
                reduceHealth(25);
            }
        }
    }

    public void renderCharacter(Batch batch) {
        batch.draw(getCurrentFrame(), getMainCharacter().x, getMainCharacter().y, 64 * SCALE, 64 * SCALE);
    }

    public void moveZombieTowardsCharacter(CharacterManager characterManager, float deltaTime) {
        if (isHit) {
            return;
        }

        Vector2 enemyPosition = getMainCharacter();
        Vector2 characterPosition = characterManager.getMainCharacter();

        float distanceToCharacter = enemyPosition.dst(characterPosition);
        // If the character is within 75 pixels, attack
        if (distanceToCharacter <= 100) {
            if (!attacking) {
                attacking = true;
                stateTime = 0f;
                currentAnimationState = ZombieAnimationType.ATTACK; // Assuming GRAB2 is the attack animation
                attackSound.play();
            }
            return; // Exit the method early to stop movement while attacking
        }


        boolean isCharacterRight = characterPosition.x > enemyPosition.x;

        // Update facing direction based on character's position
        if (isCharacterRight) {
            setFacingRight(true);
            velocity.x = MOVE_SPEED;
        } else {
            setFacingRight(false);
            velocity.x = -MOVE_SPEED;
        }

        // Update position based on velocity
        mainCharacter.x += velocity.x * deltaTime;
        mainCharacter.y += velocity.y * deltaTime;
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
        currentAnimationState = ZombieAnimationType.DEATH;
        stateTime = 0f;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isDisposable() {
        return isDisposable;
    }
}
