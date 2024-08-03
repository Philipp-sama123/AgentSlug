package krazy.cat.games.Characters;

import static krazy.cat.games.GameLoop.SCALE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;
import java.util.List;

import krazy.cat.games.Bullet;
import krazy.cat.games.Characters.AnimationSets.AnimationSetAgent;
import krazy.cat.games.Characters.AnimationSets.AnimationSetAgent.AnimationType;

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
    private int jumpCount = 0;  // Variable to track jump count
    private boolean jumpPressedLastFrame = false; // Track if jump button was pressed last frame
    private static final int MAX_JUMPS = 2; // Maximum number of jumps allowed

    private Sound jumpSound;
    private Sound shootSound;
    private Sound hitSound;
    private boolean isHit;
    private TextureRegion[] hitEffect;

    protected float hitEffectStateTime;
    protected static final float HIT_EFFECT_DURATION = 0.1f;

    private ShaderProgram redShader;

    public CharacterManager(Texture spriteSheet) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        resetCharacterPosition();
        initializeSounds();
        loadHitEffect();
        initializeShaders();
    }

    private void initializeShaders() {
        ShaderProgram.pedantic = false;
        redShader = new ShaderProgram(
            Gdx.files.internal("Shaders/vertex.glsl"),
            Gdx.files.internal("Shaders/fragment.glsl")
        );
        if (!redShader.isCompiled()) {
            Gdx.app.error("Shader Error", redShader.getLog());
        }
    }

    private void initializeSounds() {
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Jump.wav"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("SFX/PlayerHit.wav"));
    }

    public void resetCharacterPosition() {
        mainCharacter.set(Gdx.graphics.getWidth() / 2f - getCurrentFrameWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        stateTime = 0f;
        currentAnimationState = AnimationType.IDLE;
        velocity.set(0, 0);
        shooting = false;
        jumpCount = 0; // Reset jump count
    }

    public void dispose() {
        animationSetAgent.dispose();
        redShader.dispose();
    }

    public TextureRegion getCurrentFrame() {
        adjustFrameOrientation();
        return animationSetAgent.getFrame(currentAnimationState, stateTime, true);
    }

    public Vector2 getMainCharacter() {
        return mainCharacter;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void playHitEffect() {
        isHit = true;
        hitEffectStateTime = 0f;
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        applyGravity(deltaTime);

        if (isHit) {
            hitEffectStateTime += deltaTime;
            if (hitEffectStateTime > HIT_EFFECT_DURATION * hitEffect.length) {
                isHit = false; // Set isHit to false after hit effect has finished playing
            }
        }
    }

    public void updateAnimationState() {
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

    private void applyGravity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;

        if (mainCharacter.y < 0.f) {
            landOnGround();
        }
    }

    public void handleCollisions(List<Rectangle> platforms, List<Rectangle> rectangles, List<ZombieManager> zombies, List<BatManager> bats, List<Bullet> bullets) {
        handleRectangleCollisions(platforms);
        handleRectangleCollisions(rectangles);
        handleZombieCollisions(zombies);
        handleBatCollisions(bats);
        handleBulletCollisions(bullets);
    }

    private void handleRectangleCollisions(List<Rectangle> rectangles) {
        Rectangle characterRect = getMainCharacterRectangle();

        for (Rectangle rectangle : rectangles) {
            if (characterRect.overlaps(rectangle)) {
                if (velocity.y < 0 && mainCharacter.y + characterRect.height / 2 >= (rectangle.y + rectangle.height)) {
                    mainCharacter.y = rectangle.y + rectangle.height;
                    velocity.y = 0;
                    jumpCount = 0; // Reset jump count when landing on a platform
                }
            }
        }
    }

    private void handleZombieCollisions(List<ZombieManager> zombies) {
        Rectangle characterRect = getMainCharacterRectangle();

        for (ZombieManager zombie : zombies) {
            if (characterRect.overlaps(zombie.getMainZombieRectangle()) && !zombie.isDead() && zombie.isAttacking()) {
                if (!isHit) {
                    getHit();
                    playHitEffect();
                }
                // Handle collision (e.g., reduce health, trigger an effect, etc.)
                // Add any additional logic here for when the character collides with a zombie.
            }
        }
    }

    private void handleBatCollisions(List<BatManager> bats) {
        Rectangle characterRect = getMainCharacterRectangle();

        for (BatManager bat : bats) {
            if (characterRect.overlaps(bat.getBatRectangle()) && !bat.isDead() && bat.isAttacking()) {
                if (!isHit) {
                    getHit();
                    playHitEffect();
                }
                // Handle collision (e.g., reduce health, trigger an effect, etc.)
                // Add any additional logic here for when the character collides with a zombie.
            }
        }
    }

    private void loadHitEffect() {
        hitEffect = new TextureRegion[9];

        // List of file paths for the 10 images
        String[] hitEffectFilePaths = {
            "BloodEffect/B001.png",
            "BloodEffect/B002.png",
            "BloodEffect/B003.png",
            "BloodEffect/B004.png",
            "BloodEffect/B005.png",
            "BloodEffect/B006.png",
            "BloodEffect/B007.png",
            "BloodEffect/B008.png",
            "BloodEffect/B009.png",
        };

        // Load each image into the hitEffect array
        for (int i = 0; i < hitEffectFilePaths.length; i++) {
            Texture texture = new Texture(Gdx.files.internal(hitEffectFilePaths[i]));
            hitEffect[i] = new TextureRegion(texture);
        }
    }

    private void getHit() {
        isHit = true;
        hitSound.play();
    }

    public boolean isShooting() {
        return shooting;
    }

    private void landOnGround() {
        mainCharacter.y = 0.f;
        velocity.y = 0;
        jumpCount = 0; // Reset jump count when landing on the ground
        if (!shooting) {
            currentAnimationState = AnimationType.IDLE;
        }
    }

    public void handleInput(float deltaTime, boolean moveLeft, boolean moveRight, boolean runLeft, boolean runRight, boolean jump) {
        boolean isRunning = runLeft || runRight;

        if (jump && !jumpPressedLastFrame && jumpCount < MAX_JUMPS) {
            velocity.y = JUMP_SPEED;
            jumpCount++;
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

        jumpPressedLastFrame = jump; // Update jump button state
    }

    private void adjustFrameOrientation() {
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
        return new Rectangle(facingRight ? mainCharacter.x + 100 : mainCharacter.x + 150, mainCharacter.y, currentFrame.getRegionWidth() * SCALE - 250, currentFrame.getRegionHeight() * SCALE - 100);
    }

    public Bullet shoot() {
        float bulletOffsetY = 117.5f; // Adding an offset of 50 to the top
        float bulletOffsetX = facingRight ? 64 * SCALE - 50 : -64 * SCALE + 275; // Different x starting positions depending on the direction

        Vector2 bulletPosition = new Vector2(mainCharacter.x + bulletOffsetX, mainCharacter.y + (float) getCurrentFrame().getRegionHeight() / 2 + bulletOffsetY);
        shooting = true; // Set shooting flag to true
        stateTime = 0f; // Reset state time to start animation from the beginning
        shootSound.play();
        return new Bullet(bulletPosition, facingRight);
    }

    public void handleBulletCollisions(List<Bullet> bullets) {
        Rectangle characterRect = getMainCharacterRectangle();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (characterRect.overlaps(bullet.getBoundingRectangle())) {
                //TODO: think of removing or keeping
                //bulletIterator.remove();
                //hitSound.play();
                // Handle collision (e.g., reduce health, trigger an effect, etc.)
            }
        }
    }

    public void renderCharacter(Batch batch) {
        // Apply red shader if the character is hit
        if (isHit) {
            batch.setShader(redShader);
            redShader.bind();
            redShader.setUniformf("u_isHit", 1.0f);
        } else {
            batch.setShader(null);
        }

        // Render hit effect if it is playing
        if (isHit) {
            renderHitEffect(batch);
        }

        // Render the main character
        batch.draw(getCurrentFrame(), mainCharacter.x, mainCharacter.y, 64 * SCALE, 64 * SCALE);

        // Reset shader after drawing
        if (isHit) {
            batch.setShader(null);
        }
    }

    private void renderHitEffect(Batch batch) {
        int frameIndex = (int) (hitEffectStateTime / HIT_EFFECT_DURATION);
        if (frameIndex < hitEffect.length) {
            TextureRegion hitFrame = hitEffect[frameIndex];

            Rectangle characterRect = getMainCharacterRectangle();
            float hitEffectX = characterRect.x + (characterRect.width - hitFrame.getRegionWidth() * 3);
            float hitEffectY = characterRect.y + (characterRect.height - hitFrame.getRegionHeight() * 3) / 2 + 75;

            batch.draw(
                hitFrame,
                hitEffectX,
                hitEffectY,
                hitFrame.getRegionWidth() * 3,
                hitFrame.getRegionHeight() * 3
            );
        }
    }
}
