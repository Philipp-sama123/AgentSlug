package krazy.cat.games.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.InputMultiplexer;

import krazy.cat.games.AgentSlug;
import krazy.cat.games.GameLoop;

public class GameScreen implements Screen {
    private final AgentSlug game;
    private final GameLoop gameLoop;
    private Stage stage;
    private ImageButton pauseButton;

    private InputMultiplexer inputMultiplexer;

    private Touchpad movementJoystick; // Joystick instance
    private Touchpad shootingJoystick; // Joystick instance

    public GameScreen(final AgentSlug game, final GameLoop gameLoop) {
        this.game = game;
        this.gameLoop = gameLoop;

        // Initialize the stage and set it as the input processor
        stage = new Stage(new ScreenViewport());

        // Initialize InputMultiplexer
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gameLoop.getInputHandler());
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Create UI Elements
        createPauseButton();
        createMovementJoystick();
        createShootingJoystick();
    }

    private void createMovementJoystick() {
        // Load textures for the joystick background and knob
        Texture joystickBackground = new Texture(Gdx.files.internal("UI/Joystick_Background_Round.png"));
        Texture joystickKnob = new Texture(Gdx.files.internal("UI/Joystick_Knubble.png"));

        // Check if textures are loaded
        if (!joystickBackground.getTextureData().isPrepared()) {
            joystickBackground.getTextureData().prepare();
        }
        if (!joystickKnob.getTextureData().isPrepared()) {
            joystickKnob.getTextureData().prepare();
        }

        // Create joystick style
        Touchpad.TouchpadStyle movementJoystickStyle = new Touchpad.TouchpadStyle();

        // Set background and knob using TextureRegionDrawable
        movementJoystickStyle.background = new TextureRegionDrawable(new TextureRegion(joystickBackground));
        movementJoystickStyle.knob = new TextureRegionDrawable(new TextureRegion(joystickKnob));

        // Adjust knob size relative to background
        TextureRegionDrawable knobDrawable = (TextureRegionDrawable) movementJoystickStyle.knob;
        float knobWidth = joystickKnob.getWidth();
        float knobHeight = joystickKnob.getHeight();

        knobDrawable.setMinWidth(knobWidth);  // Adjust knob size if necessary
        knobDrawable.setMinHeight(knobHeight);

        movementJoystick = new Touchpad(10, movementJoystickStyle);

        // Place the joystick in the bottom left corner
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();

        // Use the size of the background texture itself instead of manually setting a size
        table.add(movementJoystick).size(joystickBackground.getWidth(), joystickBackground.getHeight()).pad(25);

        stage.addActor(table);
    }

    private void createShootingJoystick() {
        Texture joystickBackground = new Texture(Gdx.files.internal("UI/Joystick_Background_Round.png"));
        Texture joystickKnob = new Texture(Gdx.files.internal("UI/Joystick_Knubble_Gun.png"));

        if (!joystickBackground.getTextureData().isPrepared()) {
            joystickBackground.getTextureData().prepare();
        }
        if (!joystickKnob.getTextureData().isPrepared()) {
            joystickKnob.getTextureData().prepare();
        }

        Touchpad.TouchpadStyle shootingJoystickStyle = new Touchpad.TouchpadStyle();

        // Set background and knob using TextureRegionDrawable
        shootingJoystickStyle.background = new TextureRegionDrawable(new TextureRegion(joystickBackground));
        shootingJoystickStyle.knob = new TextureRegionDrawable(new TextureRegion(joystickKnob));

        // Adjust knob size relative to background
        TextureRegionDrawable knobDrawable = (TextureRegionDrawable) shootingJoystickStyle.knob;
        float knobWidth = joystickKnob.getWidth();
        float knobHeight = joystickKnob.getHeight();

        knobDrawable.setMinWidth(knobWidth);  // Adjust knob size if necessary
        knobDrawable.setMinHeight(knobHeight);

        shootingJoystick = new Touchpad(10, shootingJoystickStyle);

        // Place the joystick in the bottom left corner
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().right();

        // Use the size of the background texture itself instead of manually setting a size
        table.add(shootingJoystick).size(joystickBackground.getWidth(), joystickBackground.getHeight()).pad(25);

        stage.addActor(table);
    }

    public Touchpad getMovementJoystick() {
        return movementJoystick;
    }

    public Touchpad getShootingJoystick() {
        return shootingJoystick;
    }

    private void createPauseButton() {
        // Load texture for pause button
        Texture pauseTextureUp = new Texture(Gdx.files.internal("UI/GrayButtons/Pause.png"));
        Texture pauseTextureDown = new Texture(Gdx.files.internal("UI/YellowButtons/Pause.png"));
        ImageButton.ImageButtonStyle buttonStylePause = new ImageButton.ImageButtonStyle();

        buttonStylePause.up = new TextureRegionDrawable(pauseTextureUp);
        buttonStylePause.down = new TextureRegionDrawable(pauseTextureDown);

        pauseButton = new ImageButton(buttonStylePause);

        // Add click listener to toggle pause state
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameLoop.isPaused()) {
                    game.resumeGame();
                } else {
                    game.pauseGame();
                }
            }
        });

        // Arrange button in a table
        Table table = new Table();
        table.setFillParent(true);
        table.top().right();
        table.add(pauseButton).size(150, 150).pad(10);

        stage.addActor(table);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        // Handle ESC key press (optional for mobile)
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            game.pauseGame();
        }

        // Render the game loop if not paused
        if (!gameLoop.isPaused()) {
            gameLoop.render();
        }
        gameLoop.getInputHandler().updateJoystickMovement(this);
        // gameLoop.getInputHandler().setShootPressed(shootButton.isPressed());

        // Draw the stage (buttons, etc.)
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameLoop.setupCamera();
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        gameLoop.dispose();
        stage.dispose();
    }
}
