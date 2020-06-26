package gamestate;

import menus.MenuTestState;
import animations.ImageCache;

import com.badlogic.gdx.InputProcessor;
import com.orangeegames.suikorm.SuikodenRM;

import entities.Door;
import entities.GameWorldCharacter;
import fighting.FightingState;
import fighting.FightingTestState;
import menus.ChoiceState;

public class GameStateManager implements InputProcessor{

	public static boolean PAUSED = false;
	
	GameState[] gameState;
	SuikodenRM relation;

	
	public int currentState;
	
	public static final int NUMGAMESTATES = 3;
	public static final int LEVELSTATE = 0;
	public static final int MENUSTATE = 1;
	public static final int ATTACKSTATE = 2;
			
	public GameStateManager (SuikodenRM rel) {
		gameState = new GameState[NUMGAMESTATES];
		ImageCache.load();
		relation = rel;
		
		currentState = LEVELSTATE;
		loadState(currentState);

	}
	
	private void loadState (int state) {
		if(state == LEVELSTATE) {
			gameState[state] = new BoxWorld(new Door("kanakan", 1));
		}
		if(state == ATTACKSTATE) {
			//gameState[state] = new FightingState((BoxWorld) gameState[LEVELSTATE]);
		}
	}
	
	private void unloadState(int state) {
		gameState[state] = null;
	}
	
	public void setState(int state) {
		unloadState(state);
		currentState = state;
		loadState(state);
		relation.changeScreen();
	}
	
	public void setPauseState() {
		BoxWorld oldState = (BoxWorld) gameState[currentState];
		int oldStateNumber = currentState;
		gameState[currentState].pause();
		currentState = MENUSTATE;
		PAUSED = true;
		//gameState[currentState] = new InfoState(oldState, oldStateNumber, "Paused");
		gameState[currentState] = new MenuTestState(oldState, oldStateNumber, "Paused");
		relation.changeScreen();
	}
	
	public void setFightState() {
		gameState[currentState].pause();
		currentState = ATTACKSTATE;
		gameState[currentState] = new FightingTestState();
		relation.changeScreen();
	}

    public void setChoiceState(Scriptable character, String[] choices) {
		BoxWorld oldState = (BoxWorld) gameState[currentState];
		int oldStateNumber = currentState;
		gameState[currentState].pause();
        System.out.println("paused!");
		PAUSED = true;
		currentState = MENUSTATE;
		gameState[currentState] = new ChoiceState(oldState, 0, choices, character);
		relation.changeScreen();
    }
	
	public void unpauseState(int state) {
        System.out.println("unloading state. before: " + currentState);
		unloadState(currentState);
		currentState = state;
        System.out.println("unloading state. after: " + currentState);
		PAUSED = false;
		gameState[currentState].resume();
		relation.changeScreen();
	}
	
	public void changeWorld(Door door) {
		unloadState(currentState);
		gameState[currentState] = new BoxWorld(door);
		relation.changeScreen();
	}
	
	public void setInfo(String infoString) {
		BoxWorld oldState = (BoxWorld) gameState[currentState];
		int oldStateNumber = currentState;
		gameState[currentState].pause();
		currentState = MENUSTATE;
		PAUSED = true;
		gameState[currentState] = new InfoState(oldState, oldStateNumber, infoString);
		relation.changeScreen();
	}
	
	public void setMessage(Scriptable character, String speakerOverrideName) {
		BoxWorld oldState = (BoxWorld) gameState[currentState];
		int oldStateNumber = currentState;
		gameState[currentState].pause();
		currentState = MENUSTATE;
		PAUSED = true;
		gameState[currentState] = new ChatState(oldState, oldStateNumber, character, speakerOverrideName);
		relation.changeScreen();
	}

	public void update(float delta){
        System.out.println("updating " + gameState[currentState]);
		gameState[currentState].update(delta);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		gameState[currentState].keyPressed(keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		gameState[currentState].keyReleased(keycode);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		gameState[currentState].touchDown(screenX, screenY, pointer, button);
		System.out.println(screenX + " : " + screenY + " : " + pointer + " : " + button);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		gameState[currentState].touchUp(screenX, screenY, pointer, button);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		gameState[currentState].touchDragged(screenX, screenY, pointer);
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
	public GameState getScreen() {
		return gameState[currentState];
	}
}
