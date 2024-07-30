package krazy.cat.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.graphics.GL20;

import java.util.ArrayList;
import java.util.List;

public class AgentSlug extends Game {
    public static final int SCALE = 5;
    public static final float MAP_SCALE = 5.f; // Scaling factor for the map

    private OrthographicCamera camera;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private BitmapFont textToShow;

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private int score = 0;

    private CharacterManager characterManager;
    private float stateTime;

    private InputHandler inputHandler;
    private ShapeRenderer shapeRenderer;

    private List<Rectangle> platforms = new ArrayList<>();
    private List<Rectangle> tiledRectangles = new ArrayList<>();
    private List<Triangle> triangles = new ArrayList<>();

    @Override
    public void create() {
        Box2D.init(); // Initialize Box2D

        world = new World(new Vector2(0, -9.8f), true); // Create the world with gravity
        debugRenderer = new Box2DDebugRenderer();

        batch = new SpriteBatch();
        // Load the Tiled map
        TmxMapLoader mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("TiledMapEditing/AgentSlug_Map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, MAP_SCALE);

        setupCamera();

        Texture characterTexture = new Texture("GandalfHardcoreFemaleAgent/GandalfHardcore Female Agent black.png");
        Texture bulletTexture = new Texture("Pixel Bullet 16x16.png");
        characterManager = new CharacterManager(characterTexture, bulletTexture);
        createTextToShow();

        inputHandler = new InputHandler(this);
        Gdx.input.setInputProcessor(inputHandler);
        stateTime = 0.f;

        shapeRenderer = new ShapeRenderer();

        parseTriangleLayers();
        parseCollisionLayer();
        createPlatforms();
        createPhysicsBodies();
    }

    private void createPhysicsBodies() {
        for (Rectangle rect : tiledRectangles) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.StaticBody;
            bodyDef.position.set(rect.x, rect.y);

            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(rect.width, rect.height);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            shape.dispose();
        }

        for (Triangle triangle : triangles) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.StaticBody;
            bodyDef.position.set(0, 0);

            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.set(new float[]{
                triangle.p1.x, triangle.p1.y,
                triangle.p2.x, triangle.p2.y,
                triangle.p3.x, triangle.p3.y
            });
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            shape.dispose();
        }
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

    private void parseTriangleLayers() {
        Gdx.app.log("Triangle", "Available layers:");
        for (int i = 0; i < tiledMap.getLayers().getCount(); i++) {
            Gdx.app.log("Triangle", "Layer: " + tiledMap.getLayers().get(i).getName());
        }

        parseTriangleLayer("TriangleLeftToRight", true);
        parseTriangleLayer("TriangleRightToLeft", false);
    }

    private void parseTriangleLayer(String layerName, boolean leftToRight) {
        Gdx.app.log("Triangle", "Parsing triangle layer: " + layerName);

        var triangleLayer = tiledMap.getLayers().get(layerName);

        if (triangleLayer == null || !(triangleLayer instanceof TiledMapTileLayer)) {
            Gdx.app.log("Triangle", "Invalid triangle layer: " + layerName);
            return;
        }

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) triangleLayer;
        int layerWidth = tileLayer.getWidth();
        int layerHeight = tileLayer.getHeight();
        float tileWidth = tileLayer.getTileWidth();
        float tileHeight = tileLayer.getTileHeight();

        for (int x = 0; x < layerWidth; x++) {
            for (int y = 0; y < layerHeight; y++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    // Define vertices for the triangle based on the tile position
                    Vector2 p1 = new Vector2(x * tileWidth * MAP_SCALE, y * tileHeight * MAP_SCALE);
                    Vector2 p2 = new Vector2((x + 1) * tileWidth * MAP_SCALE, y * tileHeight * MAP_SCALE);
                    Vector2 p3 = new Vector2(x * tileWidth * MAP_SCALE, (y + 1) * tileHeight * MAP_SCALE);

                    if (leftToRight) {
                        // Triangle with vertices at the left bottom, right top, and right bottom
                        p2 = new Vector2((x + 1) * tileWidth * MAP_SCALE, (y + 1) * tileHeight * MAP_SCALE);
                        p3 = new Vector2((x + 1) * tileWidth * MAP_SCALE, y * tileHeight * MAP_SCALE);
                    } else {
                        // Triangle with vertices at the left bottom, right bottom, and left top
                        p2 = new Vector2((x + 1) * tileWidth * MAP_SCALE, y * tileHeight * MAP_SCALE);
                        p3 = new Vector2(x * tileWidth * MAP_SCALE, (y + 1) * tileHeight * MAP_SCALE);
                    }

                    triangles.add(new Triangle(p1, p2, p3));
                    Gdx.app.log("Triangle", "Parsed triangle: (" + p1 + ", " + p2 + ", " + p3 + ")");
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

    @Override
    public void render() {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the world
        world.step(Gdx.graphics.getDeltaTime(), 6, 2); // Step the world

        // Update camera and map renderer
        camera.update();
        mapRenderer.setView(camera);

        // Render the map
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined); // Use the camera's combined matrix for the batch
        batch.begin();
        updateGameState(Gdx.graphics.getDeltaTime());
        renderCharacter();
        renderBullets();
        renderScore();
        batch.end();

        renderCharacterRectangle();
        renderPlatforms();
        debugRenderer.render(world, camera.combined); // Render Box2D debug info
    }

    private void updateGameState(float deltaTime) {
        stateTime += deltaTime;
        characterManager.update(deltaTime, inputHandler.isLeftPressed(), inputHandler.isRightPressed(),
            inputHandler.isRunLeftPressed(), inputHandler.isRunRightPressed(), inputHandler.isAttackPressed(),
            inputHandler.isJumpPressed(), platforms, tiledRectangles);

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

    private void renderCharacter() {
        batch.draw(characterManager.getCurrentFrame(), characterManager.getMainCharacter().x, characterManager.getMainCharacter().y,
            64 * SCALE, 64 * SCALE);
    }

    private void renderBullets() {
        for (Bullet bullet : characterManager.getBullets()) {
            batch.draw(bullet.getCurrentFrame(), bullet.getPosition().x, bullet.getPosition().y,
                Bullet.BULLET_WIDTH * SCALE, Bullet.BULLET_HEIGHT * SCALE);
        }
    }

    private void renderScore() {
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
        shapeRenderer.setColor(Color.YELLOW);
        for (Rectangle tiledRect : tiledRectangles) {
            shapeRenderer.rect(tiledRect.x, tiledRect.y, tiledRect.width, tiledRect.height);
        }
        shapeRenderer.setColor(Color.BLUE);
        for (Triangle triangle : triangles) {
            shapeRenderer.triangle(triangle.p1.x, triangle.p1.y, triangle.p2.x, triangle.p2.y, triangle.p3.x, triangle.p3.y);
        }
        shapeRenderer.end();
    }

    private void createPlatforms() {
        platforms.add(new Rectangle(100, 150, 200, 20));  // Platform 1
        platforms.add(new Rectangle(400, 300, 200, 20));  // Platform 2
        platforms.add(new Rectangle(700, 450, 200, 20));  // Platform 3
        // Add more platforms as needed
    }

    @Override
    public void dispose() {
        batch.dispose();
        characterManager.dispose();
        textToShow.dispose();
        shapeRenderer.dispose();
        mapRenderer.dispose();
        debugRenderer.dispose();
        world.dispose();
    }

    private void createTextToShow() {
        textToShow = new BitmapFont();
        textToShow.setColor(Color.WHITE);
        textToShow.getData().setScale(10);
    }

    public static class Triangle {
        public Vector2 p1, p2, p3;

        public Triangle(Vector2 p1, Vector2 p2, Vector2 p3) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }
    }
}
