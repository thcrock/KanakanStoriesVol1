package menus;

import entities.GameCharacter;
import gamestate.BoxWorld;
import gamestate.GameState;
import gamestate.Scriptable;

import java.util.ArrayList;

import utilities.Manager;
import animations.ImageCache;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.orangeegames.suikorm.SuikodenRM;

public class ChoiceState extends GameState {

	private BoxWorld levelState;
	private BitmapFont font;
	private Skin skin;
	private SpriteBatch spriteBatch;
	private int returnState;
    private Scriptable character;
	
	Container<Table> mainContainer;
	LabelStyle origButton;
	LabelStyle hoverButton;
	
	int mainWindowRow = 0;
	int mainWindowColumn = 0;
	
	ArrayList<String> choices = new ArrayList<String>();
	
	float deltaText = 0;
	Label infoWindow;
	Label leftUpperInfoWindow;
	Label rightUpperInfoWindow;
	
	// Text
	int STD_TEXT_SPEED = 10;
	int PRESS_BUTTON_TEXT_SPEED = 50;
	int TXT_SPEED = STD_TEXT_SPEED;
	
	// Character
	int PORTRAIT_WIDTH = 56*2;
	int PORTRAIT_HEIGHT = 64*2;
	boolean b = false;
	int stringPosition = 0;
	
	private Stage stage;
	
	CharacterWindow cw;
	
	Image characterImage;
	
	public ChoiceState(BoxWorld ls, int state, String[] myChoices, Scriptable myCharacter) {
		this.levelState = ls;
        this.character = myCharacter;
		spriteBatch = (SpriteBatch) ls.mapRenderer.getBatch();
		this.returnState = state;
        for (String s : myChoices) {
            choices.add(s);
        }
	}

	@Override
	public void render(float delta) {
		levelState.render(delta);
		spriteBatch.setProjectionMatrix(levelState.camera.combined);
		stage.act(delta);
		spriteBatch.begin();
		stage.draw();
		spriteBatch.end();
	}

	@Override
	public void resize(int width, int height) {
		if(stage == null) {
			stage = new Stage(new ExtendViewport(width, height));
		}
		levelState.resize(width, height);
		stage.clear();
		
		hoverButton = new LabelStyle();
		
		//style.up = skin.getDrawable("neat9patch");
		hoverButton.background = skin.getDrawable("menuSelected");
		//hoverButton.down = skin.getDrawable("neat9patch");
		hoverButton.font = font;
		
		origButton = new LabelStyle();
		origButton.font = font;

		
		Table table1 = new Table();
		table1.setFillParent(false);

		table1.setHeight(height/3);
		table1.setWidth(width/3);
		
		
		mainContainer = new Container<Table>(table1);
        int chatWindowWidth = width - height/8;
        int chatWindowHeight = height/4;
		mainContainer.setWidth(chatWindowWidth);
		mainContainer.setHeight(chatWindowHeight);
		mainContainer.setX(width/2 - chatWindowWidth/2);
		mainContainer.setY(0 + chatWindowHeight/4);
		mainContainer.setBackground(skin.getDrawable("unchat"));
		
        LabelStyle leftupperchat = new LabelStyle();
        leftupperchat.background = skin.getDrawable("upleftchat");
        leftupperchat.font = font;
        leftupperchat.fontColor = new Color(Color.CYAN);
        
        LabelStyle rightupperchat = new LabelStyle();
        rightupperchat.background = skin.getDrawable("uprightchat");
        rightupperchat.font = font;
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "Camila", Color.WHITE, chatWindowWidth, 0, true);
        int nameWidth = (int) layout.width + 40;
        if(SuikodenRM.debug) System.out.println(nameWidth);
        if (nameWidth < PORTRAIT_WIDTH + 10) nameWidth = PORTRAIT_WIDTH + 10;
        if(SuikodenRM.debug) System.out.println(nameWidth);
        leftUpperInfoWindow = new Label("Camila", leftupperchat);
        leftUpperInfoWindow.setWidth(nameWidth);
        leftUpperInfoWindow.setHeight(chatWindowHeight/4);
        leftUpperInfoWindow.setX(width/2 - chatWindowWidth/2);
        leftUpperInfoWindow.setY(mainContainer.getY() + mainContainer.getHeight());
        leftUpperInfoWindow.setAlignment(Align.center + Align.bottom);
        
        rightUpperInfoWindow = new Label("", rightupperchat);
        rightUpperInfoWindow.setWidth(chatWindowWidth - nameWidth);
        rightUpperInfoWindow.setHeight(5);
        rightUpperInfoWindow.setX(mainContainer.getX() + leftUpperInfoWindow.getWidth());
        rightUpperInfoWindow.setY(mainContainer.getY() + mainContainer.getHeight());
		table1.pad(10);
        for(String s : choices) {
            table1.row();

            Label choice = new Label(s, origButton);
            //Label items = new Label("Atem", suikoButton);
            table1.add(choice).width((mainContainer.getWidth()-40)/2).height((mainContainer.getHeight()-20)/3).pad(3);
        }

		stage.addActor(mainContainer);
        stage.addActor(rightUpperInfoWindow);
        stage.addActor(leftUpperInfoWindow);
		
		
		((Label) mainContainer.getActor().getChildren().items[0]).setStyle(hoverButton);
		
		//characterImage = new Image();
		//characterImage.setWidth(PORTRAIT_WIDTH);
		//characterImage.setHeight(PORTRAIT_HEIGHT);
		//characterImage.setX(itemContainer.getX() + itemContainer.getWidth() - PORTRAIT_WIDTH - 20);
		//characterImage.setY(itemContainer.getY() + itemContainer.getHeight() - 20);
	}

	@Override
	public void show() {
		skin = new Skin();
		font = new BitmapFont(Gdx.files.internal("data/whiteFont.fnt"), false);
		
		skin.addRegions(ImageCache.atlas);
		skin.add("fontNormal", font);
	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		skin.dispose();
		font.dispose();
		stage.dispose();
	}

	@Override
	public void init() {
		
	}

	@Override
	public void update(float delta) {
		
	}

	@Override
	public void keyPressed(int k) {
		TXT_SPEED = PRESS_BUTTON_TEXT_SPEED;
		if(k == Keys.Q) SuikodenRM.gsm.unpauseState(returnState);
		if(k == Keys.ESCAPE) SuikodenRM.gsm.unpauseState(returnState);
		
	}

	@Override
	public void keyReleased(int k) {
		TXT_SPEED = STD_TEXT_SPEED;
		updateMainMenu(k);
	}

	@Override
	public void touchDown(int screenX, int screenY, int pointer, int button) {
		stage.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public void touchUp(int screenX, int screenY, int pointer, int button) {
		stage.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public void touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateMainMenu(int k) {
		((Label) mainContainer.getActor().getChildren().items[mainWindowRow + mainWindowColumn]).setStyle(origButton);
		
		if(k == Keys.UP) {
			mainWindowRow = mainWindowRow - 1;
		}
		if(k == Keys.DOWN) {
			mainWindowRow = mainWindowRow + 1;
		}
		
		int choice = mainWindowRow + mainWindowColumn;

		if(k == Keys.ENTER) {
			System.out.println(choice);
            this.character.setCurrentChoice(choice);
            System.out.println("exiting from choice state");
			SuikodenRM.gsm.unpauseState(returnState);
		} else {
			((Label) mainContainer.getActor().getChildren().items[choice]).setStyle(hoverButton);
		}
		
	}
	
}
