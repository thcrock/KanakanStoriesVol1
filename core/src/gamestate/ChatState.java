package gamestate;

import java.util.ArrayList;

import animations.ImageCache;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.orangeegames.suikorm.SuikodenRM;

import entities.GameWorldCharacter;

public class ChatState extends GameState {

	private BoxWorld levelState;
	private BitmapFont font;
	private Skin skin;
	private SpriteBatch spriteBatch;
	private int returnState;
	
	ArrayList<String> tryText = new ArrayList<String>();
	private String stringName;
	
	float deltaText = 0;
	Label underChatWindow;
	Label leftUpperChatWindow;
	Label rightUpperChatWindow;
	
	// Text
	int STD_TEXT_SPEED = 30;
	int PRESS_BUTTON_TEXT_SPEED = 50;
	int TXT_SPEED = STD_TEXT_SPEED;
	
	// Character
	int PORTRAIT_WIDTH = 139;
	int PORTRAIT_HEIGHT = 139;
	boolean textHalt = false;
	boolean textName = false;
	int stringPosition = 0;
	Image i;
	
	private Stage stage;
    private Scriptable scriptable;
	
	
	public ChatState(BoxWorld ls, int state, Scriptable character, String speakerOverrideName, String speakerOverridePicture) {
		this.levelState = ls;
        this.scriptable = character;
		spriteBatch = (SpriteBatch) ls.mapRenderer.getBatch();
		this.returnState = state;
		for(String message : character.getMessages()) {
			tryText.add(message);
		}
        if(speakerOverrideName != null) {
			stringName = speakerOverrideName;
			textName = true;
        } else if(character.getName() != null) {
			stringName = character.getName();
			textName = true;
		}
		
        if(speakerOverridePicture != null) {
	        TextureRegion facePicture;
            i = new Image(new TextureRegion(ImageCache.getFrame(speakerOverridePicture, 1)));
        } else if(character.getFacePicture() != null) {
            System.out.println("face picture is not null!");
            i = new Image(character.getFacePicture());
        } else {
            System.out.println("face picture is null!");
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
		deltaText += delta*TXT_SPEED;
		if(deltaText < tryText.get(stringPosition).length() && !textHalt) {
			if(!(underChatWindow.getHeight() < underChatWindow.getMaxHeight())) {
				underChatWindow.setText(tryText.get(stringPosition).subSequence(0, (int) deltaText)); 
			}
            GlyphLayout layout = new GlyphLayout();
            layout.setText(
                font,
				tryText.get(stringPosition).subSequence(0, (int) deltaText), 
                Color.WHITE,
				(underChatWindow.getWidth() - underChatWindow.getStyle().background.getLeftWidth()*2 - 40),
                0,
                true
            );

            int heightToCheck = (int) layout.height;
			if((int) layout.height >= underChatWindow.getHeight()) {
				if(tryText.get(stringPosition).charAt((int) deltaText) == ' ') {
					textHalt = true;
					tryText.add(stringPosition+1, (String) tryText.get(stringPosition).subSequence((int) (deltaText), tryText.get(stringPosition).length())); 
				}
			}
			
		} else {
			if(!textHalt) {
				underChatWindow.setText(tryText.get(stringPosition));
				textHalt = true;
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		if(stage == null) {
			stage = new Stage(new ExtendViewport(width, height));
		}
		levelState.resize(width, height);
		stage.clear();
		
		TextButtonStyle style = new TextButtonStyle();
		
		style.up = skin.getDrawable("neat9patch");
		style.down = skin.getDrawable("neat9patch");
		style.font = font;
		
		if(textName) {
		
			LabelStyle underchat = new LabelStyle();
			underchat.background = skin.getDrawable("unchat");
			underchat.font = font;
			
			
			LabelStyle leftupperchat = new LabelStyle();
			leftupperchat.background = skin.getDrawable("upleftchat");
			leftupperchat.font = font;
			leftupperchat.fontColor = new Color(Color.CYAN);
			
			LabelStyle rightupperchat = new LabelStyle();
			rightupperchat.background = skin.getDrawable("uprightchat");
			rightupperchat.font = font;
			int chatWindowWidth = width - height/8;
			int chatWindowHeight = height/4;
			underChatWindow = new Label("", underchat);
			underChatWindow.addListener( new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					SuikodenRM.gsm.unpauseState(returnState);
				}
			});
			underChatWindow.setHeight(chatWindowHeight);
			underChatWindow.setWidth(chatWindowWidth);
			underChatWindow.setX(width/2 - chatWindowWidth/2);
			underChatWindow.setY(0 + chatWindowHeight/4);
			underChatWindow.setWrap(true);
			underChatWindow.setAlignment(Align.left + Align.top);
			
            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, stringName, Color.WHITE, chatWindowWidth, 0, true);

            int nameWidth = (int) layout.width + 40;
			if(SuikodenRM.debug) System.out.println(nameWidth);
			if (nameWidth < PORTRAIT_WIDTH + 10) nameWidth = PORTRAIT_WIDTH + 10;
			if(SuikodenRM.debug) System.out.println(nameWidth);
			leftUpperChatWindow = new Label(stringName, leftupperchat);
			leftUpperChatWindow.setWidth(nameWidth);
			leftUpperChatWindow.setHeight(chatWindowHeight/4);
			leftUpperChatWindow.setX(width/2 - chatWindowWidth/2);
			leftUpperChatWindow.setY(underChatWindow.getY() + underChatWindow.getHeight());
			leftUpperChatWindow.setAlignment(Align.center + Align.bottom);
			
			rightUpperChatWindow = new Label("", rightupperchat);
			rightUpperChatWindow.setWidth(chatWindowWidth - nameWidth);
			rightUpperChatWindow.setHeight(5);
			rightUpperChatWindow.setX(underChatWindow.getX() + leftUpperChatWindow.getWidth());
			rightUpperChatWindow.setY(underChatWindow.getY() + underChatWindow.getHeight());
			
			stage.addActor(underChatWindow);
			stage.addActor(leftUpperChatWindow);
			stage.addActor(rightUpperChatWindow);
			
			if( i != null) {
				i.setWidth(PORTRAIT_WIDTH);
				i.setHeight(PORTRAIT_HEIGHT);
				i.setX(leftUpperChatWindow.getX() - 8);
				i.setY(leftUpperChatWindow.getY() + leftUpperChatWindow.getHeight());
				stage.addActor(i);
			}
			
		} else {
			
			LabelStyle chat = new LabelStyle();
			chat.background = skin.getDrawable("neat9patch");
			chat.font = font;
			chat.fontColor = new Color(Color.CYAN);
			
			int chatWindowWidth = width - height/8;
			int chatWindowHeight = height/10;
			underChatWindow = new Label(tryText.get(0), chat);
			
			underChatWindow.setHeight(chatWindowHeight);
			underChatWindow.setWidth(chatWindowWidth);
			underChatWindow.setX(width/2 - chatWindowWidth/2);
			underChatWindow.setY(0 + height/2 - chatWindowHeight/2);
			underChatWindow.setAlignment(Align.center);
			
			
			stage.addActor(underChatWindow);
		}
		
	}

	@Override
	public void show() {
		skin = new Skin();
		
		font = new BitmapFont(Gdx.files.internal("data/whiteFont.fnt"), false);
		
		skin.addRegions(ImageCache.atlas);
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

		if(stringPosition == (tryText.size() - 1) && textHalt) {
            System.out.println("exiting from chat state");
			SuikodenRM.gsm.unpauseState(returnState);
            this.scriptable.hasFinishedTalking();
		}	
		if(textHalt) {
			textHalt = false;
			stringPosition++;
			if(stringPosition > tryText.size() - 1) {
				stringPosition = tryText.size() - 1;
			} else {
				deltaText = 0;
			}
		}  
		if(k == Keys.ESCAPE) SuikodenRM.gsm.unpauseState(returnState);
	}

	@Override
	public void keyReleased(int k) {
		TXT_SPEED = STD_TEXT_SPEED;
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
}
