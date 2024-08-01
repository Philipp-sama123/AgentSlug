package krazy.cat.games;

import com.badlogic.gdx.Game;
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

import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Dice Rolls
// Predefined characters
// ToDo: Do you want to make na rpg
// TODO:
//  BUILDING WORLDS (!)
//  I think this is te clue
//  Build your own world
//  but in a controlled way
//
//  ToDo:
//        People like building worlds
//            DnDConstants
//          Boh... I guess you can do this somwhow
//          like build a world to go to the boss
//              Dungeon Crawler -- Wizadry


// ToDo: what to doo ..
//      make this a "game class"
//      will handle the game loop ...
//      then we need a main screeen
//      and a pause screens
//      maybe npc`s
//      ...and multiple enemies so you will do oop
//      ...in Java.. so good (!)
//      --- generate enemies on dice rolls in duneons ----
//      abilities - at least strength-damage, agility-dodge chance, what else u can so with the sprite sheet
//         luck when u do dice rolls -what u will do




public class AgentSlug extends Game {
    public static final int SCALE = 5;
    public static final float MAP_SCALE = 5.f; // Scaling factor for the map

    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private BitmapFont textToShow;

    private int score = 0;

    private CharacterManager characterManager;
    private InputHandler inputHandler;
    private ShapeRenderer shapeRenderer;

    private List<Bullet> bullets = new ArrayList<>();
    private List<ZombieManager> zombies = new ArrayList<>();
    private List<BatManager> bats = new ArrayList<>();

    private List<Rectangle> platforms = new ArrayList<>();
    private List<Rectangle> tiledRectangles = new ArrayList<>();


    @Override
    public void create() {
        batch = new SpriteBatch();
        // Load the Tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("TiledMapEditing/AgentSlug_Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, MAP_SCALE);

        setupCamera();

        Texture characterTexture = new Texture("GandalfHardcoreFemaleAgent/GandalfHardcore Female Agent black.png");
        Texture zombieTexture = new Texture("Zombies/GandalfHardcore Zombie v1 sheet.png"); // Replace with your enemy sprite sheet
        Texture batTexture = new Texture("Bat_ver20230918/Bat_v1/Sprite Sheet/Bat_v1_Sheet.png"); // Replace with your bat sprite sheet


        characterManager = new CharacterManager(characterTexture);
        createTextToShow();

        inputHandler = new InputHandler(this);
        Gdx.input.setInputProcessor(inputHandler);

        shapeRenderer = new ShapeRenderer();
        parseCollisionLayer();
        createPlatforms();

        spawnZombies(zombieTexture);
        spawnBats(batTexture);
    }

    @Override
    public void render() {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Update camera and map renderer
        camera.update();
        mapRenderer.setView(camera);
        // Render the map
        mapRenderer.render();
        updateGameState(Gdx.graphics.getDeltaTime());
        renderGameObjects();
    }

    @Override
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
        renderScore(batch);
        characterManager.renderCharacter(batch);
        batch.end();

        // debugRendering
        renderCharacterRectangle();
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
                    Rectangle rect = new Rectangle(
                        x * tileWidth * MAP_SCALE,
                        y * tileHeight * MAP_SCALE,
                        tileWidth * MAP_SCALE,
                        tileHeight * MAP_SCALE
                    );
                    tiledRectangles.add(rect);
                    Gdx.app.log("Collision", "Parsed rectangle: " + rect);
                }
            }
        }
    }

    private void setupCamera() {
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
        characterManager.handleCollisions(platforms, tiledRectangles);
        characterManager.updateAnimationState();

        // Attack Input
        if (inputHandler.isAttackPressed() && !characterManager.isShooting()) {
            Bullet bullet = characterManager.shoot();
            bullets.add(bullet);
        }
        characterManager.checkBulletCollisions(bullets);
    }

    private void updateBullets(float deltaTime) {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(deltaTime);
        }
    }

    private void updateZombies(float deltaTime) {
        for (ZombieManager zombie : zombies) {
            zombie.update(deltaTime);
            zombie.moveZombieTowardsCharacter(characterManager, deltaTime);
            zombie.handleCollisions(platforms, tiledRectangles);
            zombie.updateAnimationState();
            zombie.checkBulletCollisions(bullets);
        }
    }

    private void updateBats(float deltaTime) {
        for (BatManager bat : bats) {
            bat.update(deltaTime);
            bat.moveBatTowardsCharacter(characterManager, deltaTime);
            bat.handleCollisions(platforms, tiledRectangles);
            bat.updateAnimationState();
            bat.checkBulletCollisions(bullets);
        }
    }

    private void renderBats(Batch batch) {
        for (BatManager bat : bats) {
            bat.renderCharacter(batch);
        }
    }

    private void spawnBats(Texture batTexture) {
        for (Rectangle platform : platforms) {
            BatManager bat = new BatManager(batTexture);
            bat.getBatPosition().set(platform.x, platform.y + platform.height);
            bats.add(bat);
        }
    }

    private void updateCamera() {
        // Update the camera position to follow the character
        Vector2 characterPosition = characterManager.getMainCharacter();
        float cameraX = characterPosition.x + characterManager.getCurrentFrame().getRegionWidth() / 2;
        float cameraY = characterPosition.y + characterManager.getCurrentFrame().getRegionHeight() / 2;

        // Keep the camera within the bounds of the map
        camera.position.set(
            Math.max(camera.viewportWidth / 2, Math.min(cameraX,
                tiledMap.getProperties().get("width", Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class) * MAP_SCALE - camera.viewportWidth / 2)),
            Math.max(camera.viewportHeight / 2, Math.min(cameraY,
                tiledMap.getProperties().get("height", Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class) * MAP_SCALE - camera.viewportHeight / 2)),
            0
        );
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
        textToShow.draw(batch, String.valueOf(score), 100, 200);
    }

    private void renderCharacterRectangle() {
        Rectangle characterRect = characterManager.getMainCharacterRectangle();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(characterRect.x, characterRect.y, characterRect.width, characterRect.height);
        shapeRenderer.end();
    }

    private void renderPlatforms() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BROWN);
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
    }

    private void createTextToShow() {
        textToShow = new BitmapFont();
        textToShow.setColor(Color.WHITE);
        textToShow.getData().setScale(10);
    }

    private void spawnZombies(Texture zombieTexture) {
        for (Rectangle platform : platforms) {
            ZombieManager zombie = new ZombieManager(zombieTexture);
            zombie.getMainCharacter().set(platform.x, platform.y + platform.height);
            zombies.add(zombie);
        }
    }
}
