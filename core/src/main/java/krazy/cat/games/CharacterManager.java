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
    private AnimationType currentState = AnimationType.IDLE;
    private boolean shooting = false;

    private List<Bullet> bullets;
    private Texture bulletTexture;

    private Sound jumpSound;
    private Sound shootSound;
    private Sound hitSound;

    public CharacterManager(Texture spriteSheet, Texture bulletTexture) {
        animationSetAgent = new AnimationSetAgent(spriteSheet);
        this.bulletTexture = bulletTexture;
        resetCharacterPosition();
        bullets = new ArrayList<>();
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Jump.wav"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Shoot.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("SFX/Hit.wav"));
    }

    public void resetCharacterPosition() {
        mainCharacter.set(Gdx.graphics.getWidth() / 2f - getCurrentFrameWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        stateTime = 0f;
        currentState = AnimationType.IDLE;
        velocity.set(0, 0);
        shooting = false;
    }

    public void dispose() {
        animationSetAgent.dispose();
        bulletTexture.dispose();
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion frame = animationSetAgent.getFrame(currentState, stateTime, true);
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
    private boolean isCollidingWithTriangleEdges(AgentSlug.Triangle triangle, Rectangle characterRect) {
        Vector2[] points = {
            new Vector2(characterRect.x, characterRect.y),
            new Vector2(characterRect.x + characterRect.width, characterRect.y),
            new Vector2(characterRect.x, characterRect.y + characterRect.height),
            new Vector2(characterRect.x + characterRect.width, characterRect.y + characterRect.height)
        };

        Vector2[] edges = {triangle.v1, triangle.v2, triangle.v2, triangle.v3, triangle.v3, triangle.v1};

        for (int i = 0; i < edges.length; i += 2) {
            Vector2 edgeStart = edges[i];
            Vector2 edgeEnd = edges[i + 1];
            for (Vector2 point : points) {
                if (triangle.isOnOrBelowEdge(point, edgeStart, edgeEnd) &&
                    !triangle.isOnOrBelowEdge(new Vector2(characterRect.x, characterRect.y + characterRect.height), edgeStart, edgeEnd)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void adjustPositionOnTriangleEdge(AgentSlug.Triangle triangle) {
        Rectangle characterRect = getMainCharacterRectangle();
        Vector2[] edges = {triangle.v1, triangle.v2, triangle.v2, triangle.v3, triangle.v3, triangle.v1};

        for (int i = 0; i < edges.length; i += 2) {
            Vector2 edgeStart = edges[i];
            Vector2 edgeEnd = edges[i + 1];

            if (isCollidingWithEdge(characterRect, edgeStart, edgeEnd)) {
                // Align character to the edge
                alignCharacterToEdge(characterRect, edgeStart, edgeEnd);
                break;
            }
        }
    }

    private boolean isCollidingWithEdge(Rectangle characterRect, Vector2 edgeStart, Vector2 edgeEnd) {
        Vector2[] points = {
            new Vector2(characterRect.x, characterRect.y),
            new Vector2(characterRect.x + characterRect.width, characterRect.y),
            new Vector2(characterRect.x, characterRect.y + characterRect.height),
            new Vector2(characterRect.x + characterRect.width, characterRect.y + characterRect.height)
        };

        for (Vector2 point : points) {
            if (isOnLineSegment(point, edgeStart, edgeEnd)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnLineSegment(Vector2 point, Vector2 start, Vector2 end) {
        return point.x >= Math.min(start.x, end.x) &&
            point.x <= Math.max(start.x, end.x) &&
            point.y >= Math.min(start.y, end.y) &&
            point.y <= Math.max(start.y, end.y);
    }

    private void alignCharacterToEdge(Rectangle characterRect, Vector2 edgeStart, Vector2 edgeEnd) {
        Vector2 edgeDirection = new Vector2(edgeEnd.x - edgeStart.x, edgeEnd.y - edgeStart.y).nor();
        Vector2 characterCenter = new Vector2(characterRect.x + characterRect.width / 2, characterRect.y + characterRect.height / 2);

        float distanceToEdge = (edgeDirection.x * (characterCenter.x - edgeStart.x) + edgeDirection.y * (characterCenter.y - edgeStart.y));
        Vector2 projectedPoint = new Vector2(edgeStart.x + edgeDirection.x * distanceToEdge, edgeStart.y + edgeDirection.y * distanceToEdge);

        if (projectedPoint.y > characterRect.y) {
            mainCharacter.y = projectedPoint.y - characterRect.height;
        }
        velocity.y = 0;
    }

    public void update(float deltaTime, boolean moveLeft, boolean moveRight, boolean runLeft, boolean runRight, boolean attack, boolean jump, List<Rectangle> platforms, List<Rectangle> tiledRectangles,List<AgentSlug.Triangle> triangles) {
        stateTime += deltaTime;
        applyGravity(deltaTime);

        if (jump && canJump()) {
            velocity.y += JUMP_SPEED;
            jumpSound.play();
        }

        handleMovement(deltaTime, moveLeft, moveRight, runLeft, runRight);

        mainCharacter.y += velocity.y * deltaTime;
        mainCharacter.x += velocity.x * deltaTime;

        if (mainCharacter.y < 0.f) {
            landOnGround();
        }

        handlePlatformCollisions(platforms);
        handleTiledRectangleCollisions(tiledRectangles);
        handleTriangleCollisions(triangles); // Handle triangular platform collisions
        if (attack && !shooting) {
            shoot();
        }

        updateState(moveLeft, moveRight, runLeft, runRight);
        updateBullets(deltaTime);
        checkBulletCollisions();

        // Reset horizontal velocity after applying movement
        velocity.x = 0;
    }

    private void handleTiledRectangleCollisions(List<Rectangle> tiledRectangles) {
        Rectangle characterRect = getMainCharacterRectangle();

        for (Rectangle tiledRectangle : tiledRectangles) {
            if (characterRect.overlaps(tiledRectangle)) {
                // Handle collision with the tiled rectangle
                // For example, stop movement or adjust position
                if (velocity.y < 0) {
                    mainCharacter.y = tiledRectangle.y + tiledRectangle.height;
                    velocity.y = 0;
                }
                // You can also handle other collision effects here
            }
        }
    }

    private void applyGravity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;
    }

    private boolean canJump() {
        return currentState != AnimationType.JUMP
            && currentState != AnimationType.FALL
            && currentState != AnimationType.FALL_SHOOT
            && currentState != AnimationType.JUMP_SHOOT;
    }

    private void landOnGround() {
        mainCharacter.y = 0.f;
        velocity.y = 0;
        if (!shooting) {
            currentState = AnimationType.IDLE;
        }
    }

    private void handleMovement(float deltaTime, boolean moveLeft, boolean moveRight, boolean runLeft, boolean runRight) {
        if (moveLeft || runLeft) {
            moveCharacterLeft(deltaTime, runLeft);
        } else if (moveRight || runRight) {
            moveCharacterRight(deltaTime, runRight);
        }
    }

    private void moveCharacterLeft(float deltaTime, boolean isRunning) {
        velocity.x = -(isRunning ? RUN_SPEED : MOVE_SPEED);
        setFacingRight(false);
    }

    private void moveCharacterRight(float deltaTime, boolean isRunning) {
        velocity.x = (isRunning ? RUN_SPEED : MOVE_SPEED);
        setFacingRight(true);
    }

    private void handlePlatformCollisions(List<Rectangle> platforms) {
        Rectangle characterRect = getMainCharacterRectangle();
        boolean isOnPlatform = false;

        for (Rectangle platform : platforms) {
            if (isCollidingWithPlatform(platform, characterRect)) {
                landOnPlatform(platform);
                isOnPlatform = true;
                break;
            }
        }

        if (!isOnPlatform && mainCharacter.y > 0.f && velocity.y < 0.f) {
            if (!shooting) {
                currentState = AnimationType.FALL;
            }
        }
    }
    private void handleTriangleCollisions(List<AgentSlug.Triangle> triangles) {
        Rectangle characterRect = getMainCharacterRectangle();
        boolean isOnPlatform = false;

        for (AgentSlug.Triangle triangle : triangles) {
            if (isCollidingWithTriangleEdges(triangle, characterRect)) {
                adjustPositionOnTriangleEdge(triangle);
                isOnPlatform = true;
                break;
            }
        }

        if (!isOnPlatform && mainCharacter.y > 0.f && velocity.y < 0.f) {
            if (!shooting) {
                currentState = AnimationType.FALL;
            }
        }
    }

    private boolean isCollidingWithTriangle(AgentSlug.Triangle triangle, Rectangle characterRect) {
        // Check if the character is within the bounding box of the triangle
        if (!triangle.contains(new Vector2(characterRect.x, characterRect.y)) &&
            !triangle.contains(new Vector2(characterRect.x + characterRect.width, characterRect.y)) &&
            !triangle.contains(new Vector2(characterRect.x, characterRect.y + characterRect.height)) &&
            !triangle.contains(new Vector2(characterRect.x + characterRect.width, characterRect.y + characterRect.height))) {
            return false;
        }

        // More precise triangle collision handling logic
        return true; // Placeholder for more detailed triangle collision logic
    }

    private void landOnTriangle(AgentSlug.Triangle triangle) {
        // Compute the landing position based on the triangle
        mainCharacter.y = Math.min(triangle.v1.y, Math.min(triangle.v2.y, triangle.v3.y)) + 0.1f; // Adjust as necessary
        velocity.y = 0;
    }

    private boolean isCollidingWithPlatform(Rectangle platform, Rectangle characterRect) {
        float characterBottom = characterRect.y;
        float platformTop = platform.y + platform.height;
        float platformLeft = platform.x;
        float platformRight = platform.x + platform.width;
        float characterLeft = characterRect.x;
        float characterRight = characterRect.x + characterRect.width;

        boolean isFalling = velocity.y <= 0;
        boolean isAbovePlatform = characterBottom > platformTop - 15;
        boolean isWithinHorizontalBounds = (characterLeft >= platformLeft && characterLeft <= platformRight) || (characterRight >= platformLeft && characterRight <= platformRight);

        return isFalling && isAbovePlatform && isWithinHorizontalBounds && characterRect.overlaps(platform);
    }

    private void landOnPlatform(Rectangle platform) {
        mainCharacter.y = platform.y + platform.height;
        velocity.y = 0;
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

    private void updateState(boolean moveLeft, boolean moveRight, boolean runLeft, boolean runRight) {
        if (shooting) {
            // Check if the shooting animation has finished
            Animation<TextureRegion> shootAnimation = animationSetAgent.getAnimation(currentState);
            if (shootAnimation.isAnimationFinished(stateTime)) {
                shooting = false;
                stateTime = 0f;
            }
        }
        if (velocity.y > 0) {
            currentState = shooting ? AnimationType.JUMP_SHOOT : AnimationType.JUMP;
        } else if (velocity.y < 0) {
            currentState = shooting ? AnimationType.FALL_SHOOT : AnimationType.FALL;
        } else if (velocity.x != 0) {
            currentState = shooting ? (Math.abs(velocity.x) > MOVE_SPEED ? AnimationType.RUN_SHOOT : AnimationType.WALK_SHOOT) : (Math.abs(velocity.x) > MOVE_SPEED ? AnimationType.RUN : AnimationType.WALK);
        } else {
            currentState = shooting ? AnimationType.STAND_SHOOT : AnimationType.IDLE;
        }
    }

    private void shoot() {
        float bulletOffsetY = 117.5f; // Adding an offset of 50 to the top
        float bulletOffsetX = facingRight ? 64 * AgentSlug.SCALE - 50 : -64 * AgentSlug.SCALE + 275; // Different x starting positions depending on the direction

        Vector2 bulletPosition = new Vector2(mainCharacter.x + bulletOffsetX, mainCharacter.y + getCurrentFrame().getRegionHeight() / 2 + bulletOffsetY);
        Bullet bullet = new Bullet(bulletPosition, facingRight, bulletTexture);
        bullets.add(bullet);
        shooting = true; // Set shooting flag to true
        stateTime = 0f; // Reset state time to start animation from the beginning
        shootSound.play();
    }

    private void updateBullets(float deltaTime) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(deltaTime);
            if (bullet.getPosition().x < 0 || bullet.getPosition().x > Gdx.graphics.getWidth()) {
                bulletIterator.remove();
            }
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
