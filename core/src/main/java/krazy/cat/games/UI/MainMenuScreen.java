package krazy.cat.games.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import krazy.cat.games.AgentSlug;

public class MainMenuScreen implements Screen {
    private final AgentSlug game;
    private Stage stage;

    public MainMenuScreen(final AgentSlug game) {
        this.game = game;
        this.stage = new Stage();

        Gdx.input.setInputProcessor(stage);
        createBackground();
        createMainMenu();
    }

    private void createBackground() {
        Texture baseBackgroundTexture = new Texture(Gdx.files.internal("UI/Background.png"));
        Image baseBackgroundImage = new Image(new TextureRegionDrawable(baseBackgroundTexture));

        baseBackgroundImage.setFillParent(true);
        stage.addActor(baseBackgroundImage);

        Texture backgroundTexture = new Texture(Gdx.files.internal("GandalfHardcoreFemaleAgent/Female Agent Portrait 640x640  (5).png"));
        Image backgroundImage = new Image(new TextureRegionDrawable(backgroundTexture));

        stage.addActor(backgroundImage);

        Texture backgroundTexture2 = new Texture(Gdx.files.internal("GandalfHardcoreFemaleAgent/Female Agent Portrait 640x640  (4).png"));
        Image backgroundImage2 = new Image(new TextureRegionDrawable(backgroundTexture2));

        backgroundImage2.setPosition(Gdx.graphics.getWidth() - backgroundImage2.getWidth(), 0);

        stage.addActor(backgroundImage2);
    }

    private void createMainMenu() {
        Texture buttonUpTexture = new Texture(Gdx.files.internal("UI/GrayButtons/Resume.png"));
        Texture buttonDownTexture = new Texture(Gdx.files.internal("UI/YellowButtons/Resume.png")); // Texture for when button is pressed
        Texture buttonOverTexture = new Texture(Gdx.files.internal("UI/GrayButtons/Resume.png")); // Texture for when button is hovered

        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();

        buttonStyle.up = new TextureRegionDrawable(buttonUpTexture);
        buttonStyle.down = new TextureRegionDrawable(buttonDownTexture);
        buttonStyle.over = new TextureRegionDrawable(buttonOverTexture);

        ImageButton startButton = new ImageButton(buttonStyle);

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.startGame();
            }
        });

        // Arrange button in a table
        Table table = new Table();
        table.setFillParent(true);
        table.bottom();
        table.add(startButton).size(250, 250).pad(10);

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
