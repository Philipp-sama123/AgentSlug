package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import krazy.cat.games.AnimationSetAgent.AnimationType;

public class CharacterManager {
    public static final float MOVE_SPEED = 100.f;
    public static final float RUN_SPEED = 300.f;
    public static final float JUMP_SPEED = 1000.f;
    public static final float GRAVITY = -1000.f;

    private final AnimationSetAgent animationSetAgent;
    private Vector2 mainCharacter = new Vector2();
    private Vector2 velocity = new Vector2();
    private float stateTime = 0f;
    private boolean facingRight = false;
    private AnimationType currentAnimationState = AnimationType.IDLE;
    private boolean shooting = false;

    private List<Bullet> bullets;
    private Texture bulletTexture;

    private Sound jumpSound;
    private Sound shootSound;
    private Sound hitSound;

    public CharacterManager(Texture spriteSheet, Texture bulletTexture) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        this.bulletTexture = bulletTexture;
        bullets = new ArrayList<>();
        resetCharacterPosition();
        initializeSounds();
    }

    private void initializeSounds() {
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Jump.wav"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Hit.wav"));
    }

    public void resetCharacterPosition() {
        mainCharacter.set(Gdx.graphics.getWidth() / 2f - getCurrentFrameWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        stateTime = 0f;
        currentAnimationState = AnimationType.IDLE;
        velocity.set(0, 0);
        shooting = false;
    }

    public void dispose() {
        animationSetAgent.dispose();
        bulletTexture.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationSetAgent.getFrame(currentAnimationState, stateTime, true);
        adjustFrameOrientation(frame);
        return frame;
    }

    public Vector2 getMainCharacter() {
        return mainCharacter;
    }

    public void setMainCharacter(Vector2 mainCharacter) {
        this.mainCharacter = mainCharacter;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void update(float deltaTime, boolean moveLeft, boolean moveRight, boolean runLeft, boolean runRight, boolean attack, boolean jump, List<Rectangle> platforms, List<Rectangle> tiledRectangles) {
        stateTime += deltaTime;

        applyGravity(deltaTime);

        handleInput(deltaTime, moveLeft, moveRight, runLeft, runRight, jump, attack);

        handleCollisions(platforms, tiledRectangles);

        updateAnimationState();
        updateBullets(deltaTime);
        checkBulletCollisions();

    }

    public void handleCollisions(List<Rectangle> platforms, List<Rectangle> rectangles) {
        handleRectangleCollisions(platforms);
        handleRectangleCollisions(rectangles);
    }

    public void handleRectangleCollisions(List<Rectangle> rectangles) {
        Rectangle characterRect = getMainCharacterRectangle();

        for (Rectangle rectangle : rectangles) {
            if (characterRect.overlaps(rectangle)) {
                if (velocity.y < 0 && mainCharacter.y + characterRect.height / 2 >= (rectangle.y + rectangle.height)) {
                    mainCharacter.y = rectangle.y + rectangle.height;
                    velocity.y = 0;
                }
            }
        }
    }

    private void applyGravity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;

        if (mainCharacter.y < 0.f) {
            landOnGround();
        }
    }

    private boolean canJump() {
        return currentAnimationState != AnimationType.JUMP
            && currentAnimationState != AnimationType.FALL
            && currentAnimationState != AnimationType.FALL_SHOOT
            && currentAnimationState != AnimationType.JUMP_SHOOT;
    }

    private void landOnGround() {
        mainCharacter.y = 0.f;
        velocity.y = 0;
        if (!shooting) {
            currentAnimationState = AnimationType.IDLE;
        }
    }

    private void handleInput(float deltaTime, boolean moveLeft, boolean moveRight, boolean runLeft, boolean runRight, boolean jump, boolean attack) {
        boolean isRunning = runLeft || runRight;

        if (attack && !shooting) {
            Bullet bullet = shoot();
            bullets.add(bullet);
        }

        if (jump && canJump()) {
            velocity.y += JUMP_SPEED;
            jumpSound.play();
        }

        if (moveLeft || runLeft) {
            velocity.x = -(isRunning ? RUN_SPEED : MOVE_SPEED);
            setFacingRight(false);
        } else if (moveRight || runRight) {
            velocity.x = (isRunning ? RUN_SPEED : MOVE_SPEED);
            setFacingRight(true);
        }

        mainCharacter.y += velocity.y * deltaTime;
        mainCharacter.x += velocity.x * deltaTime;
    }

    private void adjustFrameOrientation(TextureRegion frame) {
        if (facingRight && !animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        } else if (!facingRight && animationSetAgent.isFlipped()) {
            animationSetAgent.flipFramesHorizontally();
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    private float getCurrentFrameWidth() {
        TextureRegion currentFrame = getCurrentFrame();
        return currentFrame != null ? currentFrame.getRegionWidth() : 0;
    }

    public Rectangle getMainCharacterRectangle() {
        TextureRegion currentFrame = getCurrentFrame();
        return new Rectangle(facingRight ? mainCharacter.x + 100 : mainCharacter.x + 150, mainCharacter.y, currentFrame.getRegionWidth() * AgentSlug.SCALE - 250, currentFrame.getRegionHeight() * AgentSlug.SCALE - 100);
    }

    private void updateAnimationState() {
        if (shooting) {
            // Check if the shooting animation has finished
            Animation<TextureRegion> shootAnimation = animationSetAgent.getAnimation(currentAnimationState);
            if (shootAnimation.isAnimationFinished(stateTime)) {
                shooting = false;
                stateTime = 0f;
            }
        }
        if (velocity.y > 0) {
            currentAnimationState = shooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
        } else if (velocity.y < 0) {
            currentAnimationState = shooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
        } else if (velocity.x != 0) {
            currentAnimationState = shooting ? (Math.abs(velocity.x) > MOVE_SPEED ? AnimationType.RUN_SHOOT : AnimationType.WALK_SHOOT) : (Math.abs(velocity.x) > MOVE_SPEED ? AnimationType.RUN : AnimationType.WALK);
        } else {
            currentAnimationState = shooting ? AnimationType.STAND_SHOOT : AnimationType.IDLE;
        }

        // Reset horizontal velocity after applying movement
        velocity.x = 0;
    }

    private Bullet shoot() {
        float bulletOffsetY = 117.5f; // Adding an offset of 50 to the top
        float bulletOffsetX = facingRight ? 64 * AgentSlug.SCALE - 50 : -64 * AgentSlug.SCALE + 275; // Different x starting positions depending on the direction

        Vector2 bulletPosition = new Vector2(mainCharacter.x + bulletOffsetX, mainCharacter.y + getCurrentFrame().getRegionHeight() / 2 + bulletOffsetY);
      //
        shooting = true; // Set shooting flag to true
        stateTime = 0f; // Reset state time to start animation from the beginning
        shootSound.play();
        return new Bullet(bulletPosition, facingRight, bulletTexture);

    }

    public void updateBullets(float deltaTime) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(deltaTime);
//            if (bullet.getPosition().x < 0 || bullet.getPosition().x > Gdx.graphics.getWidth()) {
//                bulletIterator.remove();
//            }
        }
    }

    private void checkBulletCollisions() {
        Rectangle characterRect = getMainCharacterRectangle();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (characterRect.overlaps(bullet.getBoundingRectangle())) {
                bulletIterator.remove();
                hitSound.play();
                // Handle collision (e.g., reduce health, trigger an effect, etc.)
            }
        }
    }

    public List<Bullet> getBullets() {
        return bullets;
    }
}
