package krazy.cat.games.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

        // Create the pause button
        createPauseButton();
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
    public void show() {}

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
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        gameLoop.dispose();
        stage.dispose();
    }
}
