package krazy.cat.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import krazy.cat.games.Characters.BatManager;
import krazy.cat.games.Characters.CharacterManager;
import krazy.cat.games.Characters.ZombieManager;

public class GameLoop {
    public static final int SCALE = 5;
    public static final float MAP_SCALE = 5.f; // Scaling factor for the map
    public boolean isDebugging = false;

    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private BitmapFont textToShow;
    private int score = 0;
    private boolean isPaused = false; // Pause state flag
    private CharacterManager characterManager;
    private InputHandler inputHandler;
    private ShapeRenderer shapeRenderer;
    private List<Bullet> bullets = new ArrayList<>();
    private List<ZombieManager> zombies = new ArrayList<>();
    private List<BatManager> bats = new ArrayList<>();
    private List<Rectangle> platforms = new ArrayList<>();
    private List<Rectangle> tiledRectangles = new ArrayList<>();

    public void create() {
        batch = new SpriteBatch();
        // Load the Tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("TiledMapEditing/AgentSlug_Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, MAP_SCALE);

        setupCamera();

        createTextToShow();
        inputHandler = new InputHandler();
        Gdx.input.setInputProcessor(inputHandler);

        shapeRenderer = new ShapeRenderer();
        parseCollisionLayer();
        createPlatforms();

        // Clear existing lists
        bullets.clear();
        zombies.clear();
        bats.clear();

        spawnCharacter();
        spawnZombies();
        spawnBats();
    }

    public void render() {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Update camera and map renderer
        camera.update();
        mapRenderer.setView(camera);
        // Render the map
        mapRenderer.render();

        if (!isPaused) {
            updateGameState(Gdx.graphics.getDeltaTime());
        }

        renderGameObjects();
    }

    public void dispose() {
        batch.dispose();
        characterManager.dispose();
        textToShow.dispose();
        shapeRenderer.dispose();
        mapRenderer.dispose();

        for (ZombieManager zombie : zombies) {
            zombie.dispose();
        }

        for (BatManager bat : bats) {
            bat.dispose();
        }
    }

    private void renderGameObjects() {
        batch.setProjectionMatrix(camera.combined); // Use the camera's combined matrix for the batch
        batch.begin();
        renderBullets(batch);
        renderZombies(batch);
        renderBats(batch);
        characterManager.renderCharacter(batch);
        renderScore(batch);
        batch.end();

        // debugRendering
        if (isDebugging) {
            renderCharacterRectangle();
            renderZombieRectangles();
            renderBatRectangles();
        }
        renderPlatforms();
    }

    private void parseCollisionLayer() {
        tiledRectangles = new ArrayList<>();
        Gdx.app.log("Collision", "Parsing collision layer");

        var collisionLayer = tiledMap.getLayers().get("Collision");

        if (collisionLayer == null) {
            Gdx.app.log("Collision", "Collision layer not found");
            return;
        }

        if (!(collisionLayer instanceof TiledMapTileLayer)) {
            Gdx.app.log("Collision", "Layer is not a TiledMapTileLayer");
            return;
        }

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;
        int layerWidth = tileLayer.getWidth();
        int layerHeight = tileLayer.getHeight();
        float tileWidth = tileLayer.getTileWidth();
        float tileHeight = tileLayer.getTileHeight();

        for (int x = 0; x < layerWidth; x++) {
            for (int y = 0; y < layerHeight; y++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    Rectangle rect = new Rectangle(x * tileWidth * MAP_SCALE, y * tileHeight * MAP_SCALE, tileWidth * MAP_SCALE, tileHeight * MAP_SCALE);
                    tiledRectangles.add(rect);
                    Gdx.app.log("Collision", "Parsed rectangle: " + rect);
                }
            }
        }
    }

    public void setupCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
        mapRenderer.setView(camera);
    }

    private void updateGameState(float deltaTime) {
        updateCharacter(deltaTime);
        updateBullets(deltaTime);
        updateZombies(deltaTime);
        updateBats(deltaTime);
        updateCamera();
    }

    private void updateCharacter(float deltaTime) {
        // The row really matters (!)
        // 1.) handleInput
        // 2.) handleCollisions
        // 3.) updateAnimationState
        characterManager.update(deltaTime);
        characterManager.handleInput(deltaTime, inputHandler.isLeftPressed(), inputHandler.isRightPressed(), inputHandler.isRunLeftPressed(), inputHandler.isRunRightPressed(), inputHandler.isJumpPressed());
        characterManager.handleCollisions(platforms, tiledRectangles, zombies, bats, bullets);
        characterManager.updateAnimationState();

        // Attack Input
        if (inputHandler.isAttackPressed() && !characterManager.isShooting()) {
            Bullet bullet = characterManager.shoot();
            bullets.add(bullet);
        }
    }

    private void updateBullets(float deltaTime) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(deltaTime);
        }
    }

    private void updateZombies(float deltaTime) {
        // With the iterator to prevent : -->  ConcurrentModificationException <--
        Iterator<ZombieManager> zombieIterator = zombies.iterator();
        while (zombieIterator.hasNext()) {
            ZombieManager zombie = zombieIterator.next();
            zombie.update(deltaTime);
            zombie.moveZombieTowardsCharacter(characterManager, deltaTime);
            zombie.handleCollisions(platforms, tiledRectangles);
            zombie.updateAnimationState();
            zombie.checkBulletCollisions(bullets);
            if (zombie.isDead() && zombie.isDisposable()) {
                //   zombie.dispose(); // Clean up resources if necessary ToDo: not the spritesheet
                zombieIterator.remove(); // Remove from the list
            }
        }
    }

    private void updateBats(float deltaTime) {
        // With the iterator to prevent : -->  ConcurrentModificationException <--
        Iterator<BatManager> batIterator = bats.iterator();
        while (batIterator.hasNext()) {
            BatManager bat = batIterator.next();
            bat.update(deltaTime);
            bat.moveBatTowardsCharacter(characterManager, deltaTime);
            bat.handleCollisions(platforms, tiledRectangles);
            bat.updateAnimationState();
            bat.checkBulletCollisions(bullets);
            if (bat.isDead() && bat.isDisposable()) {
                //   zombie.dispose(); // Clean up resources if necessary ToDo: not the spritesheet
                batIterator.remove(); // Remove from the list
            }
        }
    }

    private void renderBats(Batch batch) {
        for (BatManager bat : bats) {
            bat.renderCharacter(batch);
        }
    }

    private void updateCamera() {
        // Update the camera position to follow the character
        Vector2 characterPosition = characterManager.getMainCharacter();
        float cameraX = characterPosition.x + (float) characterManager.getCurrentUpperBodyFrame().getRegionWidth() / 2;
        float cameraY = characterPosition.y + (float) characterManager.getCurrentUpperBodyFrame().getRegionHeight() / 2;

        // Keep the camera within the bounds of the map
        camera.position.set(Math.max(camera.viewportWidth / 2, Math.min(cameraX, tiledMap.getProperties().get("width", Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class) * MAP_SCALE - camera.viewportWidth / 2)), Math.max(camera.viewportHeight / 2, Math.min(cameraY, tiledMap.getProperties().get("height", Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class) * MAP_SCALE - camera.viewportHeight / 2)), 0);
        camera.update();
    }

    private void renderBullets(Batch batch) {
        for (Bullet bullet : bullets) {
            bullet.render(batch);
        }
    }

    private void renderZombies(Batch batch) {
        for (ZombieManager zombie : zombies) {
            zombie.renderCharacter(batch);
        }
    }

    private void renderScore(Batch batch) {
        textToShow.draw(batch, String.valueOf(score), 0, 0);
    }

    private void renderCharacterRectangle() {
        Rectangle characterRect = characterManager.getMainCharacterRectangle();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(characterRect.x, characterRect.y, characterRect.width, characterRect.height);
        shapeRenderer.end();
    }

    private void renderZombieRectangles() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        for (ZombieManager zombie : zombies) {
            Rectangle zombieRect = zombie.getMainZombieRectangle();
            shapeRenderer.rect(zombieRect.x, zombieRect.y, zombieRect.width, zombieRect.height);
        }
        shapeRenderer.end();
    }

    private void renderBatRectangles() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);
        for (BatManager bat : bats) {
            Rectangle batRect = bat.getBatRectangle();
            shapeRenderer.rect(batRect.x, batRect.y, batRect.width, batRect.height);
        }
        shapeRenderer.end();
    }

    private void renderPlatforms() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        for (Rectangle platform : platforms) {
            shapeRenderer.rect(platform.x, platform.y, platform.width, platform.height);
        }
        shapeRenderer.end();
    }

    private void createPlatforms() {
        platforms.add(new Rectangle(100, 150, 200, 20));  // Platform 1
        platforms.add(new Rectangle(400, 300, 200, 20));  // Platform 2
        platforms.add(new Rectangle(700, 450, 200, 20));  // Platform 3
        // Add more platforms as needed
        platforms.add(new Rectangle(2300, 600, 200, 20));    // Platform 4
        platforms.add(new Rectangle(3100, 750, 200, 20));    // Platform 5
        platforms.add(new Rectangle(3900, 900, 200, 20));    // Platform 6
        platforms.add(new Rectangle(4700, 1050, 200, 20));   // Platform 7
        platforms.add(new Rectangle(5500, 1200, 200, 20));   // Platform 8
        platforms.add(new Rectangle(6300, 1350, 200, 20));   // Platform 9
        platforms.add(new Rectangle(7100, 1500, 200, 20));   // Platform 10
    }

    private void createTextToShow() {
        textToShow = new BitmapFont();
        textToShow.setColor(Color.WHITE);
        textToShow.getData().setScale(10);
    }

    private void spawnCharacter() {
        Texture upperBodyCharacterTexture = new Texture("Character/FemaleAgent/Body/Red.png");
        Texture lowerBodyCharacterTexture = new Texture("Character/FemaleAgent/Feet/Red.png");
        characterManager = new CharacterManager(upperBodyCharacterTexture, lowerBodyCharacterTexture);
    }

    private void spawnZombies() {
        Texture zombieTexture = new Texture("Zombies/GandalfHardcore Zombie v1 sheet.png"); // Replace with your enemy sprite sheet

        for (Rectangle platform : platforms) {
            ZombieManager zombie = new ZombieManager(zombieTexture);
            zombie.getMainCharacter().set(platform.x, platform.y + platform.height);
            zombies.add(zombie);
        }
    }

    private void spawnBats() {
        Texture batTexture = new Texture("Bat_ver20230918/Bat_v1/Sprite Sheet/Bat_v1_Sheet.png"); // Replace with your bat sprite sheet

        for (Rectangle platform : platforms) {
            BatManager bat = new BatManager(batTexture);
            bat.getBatPosition().set(platform.x, platform.y + platform.height);
            bats.add(bat);
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }
}
