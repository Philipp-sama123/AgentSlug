package krazy.cat.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import krazy.cat.games.UI.GameScreen;
import krazy.cat.games.UI.MainMenuScreen;
import krazy.cat.games.UI.PauseMenuScreen;

public class AgentSlug extends Game {
    private GameLoop gameLoop;

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    public void startGame() {
        gameLoop = new GameLoop();
        gameLoop.create();
        setScreen(new GameScreen(this, gameLoop));
    }

    public void pauseGame() {
        gameLoop.setPaused(true);
        setScreen(new PauseMenuScreen(this, gameLoop));
    }

    public void resumeGame() {
        gameLoop.setPaused(false);
        setScreen(new GameScreen(this, gameLoop));
    }

    public void restartGame() {
        gameLoop.create(); // Restart the game
        setScreen(new GameScreen(this, gameLoop));
    }

    public void quitGame() {
        Gdx.app.exit(); // Quit the game
    }
}
