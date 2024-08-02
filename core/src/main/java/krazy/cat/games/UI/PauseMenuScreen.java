package krazy.cat.games.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import krazy.cat.games.AgentSlug;
import krazy.cat.games.GameLoop;

public class PauseMenuScreen implements Screen {
    private final AgentSlug game;
    private final GameLoop gameLoop;
    private Stage stage;

    public PauseMenuScreen(final AgentSlug game, final GameLoop gameLoop) {
        this.game = game;
        this.gameLoop = gameLoop;
        this.stage = new Stage();

        Gdx.input.setInputProcessor(stage);

        createPauseMenu();
    }

    private void createPauseMenu() {
        Texture resumeTextureUp = new Texture(Gdx.files.internal("UI/GrayButtons/Resume.png"));
        Texture resumeTextureDown = new Texture(Gdx.files.internal("UI/YellowButtons/Resume.png"));
        ImageButton.ImageButtonStyle buttonStyleResume = new ImageButton.ImageButtonStyle();

        buttonStyleResume.up = new TextureRegionDrawable(resumeTextureUp);
        buttonStyleResume.down = new TextureRegionDrawable(resumeTextureDown);

        ImageButton resumeButton = new ImageButton(buttonStyleResume);

        Texture restartTextureUp = new Texture(Gdx.files.internal("UI/GrayButtons/Repeat.png"));
        Texture restartTextureDown = new Texture(Gdx.files.internal("UI/YellowButtons/Repeat.png"));
        ImageButton.ImageButtonStyle buttonStyleRestart = new ImageButton.ImageButtonStyle();

        buttonStyleRestart.up = new TextureRegionDrawable(restartTextureUp);
        buttonStyleRestart.down = new TextureRegionDrawable(restartTextureDown);

        ImageButton restartButton = new ImageButton(buttonStyleRestart);

        Texture quitTextureUp = new Texture(Gdx.files.internal("UI/GrayButtons/Menu.png"));
        Texture quitTextureDown = new Texture(Gdx.files.internal("UI/YellowButtons/Menu.png"));
        ImageButton.ImageButtonStyle buttonStyleQuit = new ImageButton.ImageButtonStyle();

        buttonStyleQuit.up = new TextureRegionDrawable(quitTextureUp);
        buttonStyleQuit.down = new TextureRegionDrawable(quitTextureDown);

        // Create buttons using the loaded textures
        ImageButton quitButton = new ImageButton(buttonStyleQuit);

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameLoop.setPaused(false);
                game.setScreen(new GameScreen(game, gameLoop));
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameLoop.setPaused(false);
                gameLoop.create(); // Restart the game
                game.setScreen(new GameScreen(game, gameLoop));
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // Arrange buttons in a table
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(resumeButton).size(250, 250).pad(10);
        table.add(restartButton).size(250, 250).pad(10);
        table.add(quitButton).size(250, 250).pad(10);

        stage.addActor(table);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
        stage.dispose();
    }
}
