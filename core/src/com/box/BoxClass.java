package com.box;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BoxClass extends ApplicationAdapter {
	BitmapFont levelFont;
	OrthographicCamera camera;
	ExtendViewport viewport;
	SpriteBatch batch;
	Sprite player;
	Rectangle playerbounds;
	float playerGravity=0f,playerAcceleration=0f,playerGravityTime=0f,playerMovementTime=0f,glideAcceleration=2f,glideAccelerationTime=0f,glideForwardAcceleration=2f,powerEffectTime=0f;
	Texture bgTexture,finishTexture,img,groundTexture,playerTexture,groundBlockTexture,bladeTexture,platformTexture,powerFlyTexture,powerIronTexture,gliderTexture,ironTexture,controlTexture;
	static Texture[] bladeBreakTexture = new Texture[7];
	Boolean playerOnGround=false,playerMotion=false,playerForwardMotion=true,gliding=false,playerGlideUp=false,powerEffect=false,nextLevel=false;
	final float playerSpeed=3.2f;
	int levelLength=8,level=1;
	Array<Ground> groundObjectArray = new Array<Ground>();
	Array<Blade> bladeObjectArray = new Array<Blade>();
	Array<Powers> powerObjectArray = new Array<Powers>();
	Array<Controls> controlObjectArray = new Array<Controls>();

	public static class console{
		public static void log(String print){
			Gdx.app.log("",print);
		}
	}

	public static class Blade{
		private final int type;
		private int brokenBladeTextureIndex=0;
		private boolean broken=false;
		private final float xindent,power;
		private float rotationTimeElapsed=0f,bladeTimeElapsed=0f;
		private Sprite blade;
		private Rectangle bladeBounds;
		public Blade(float x,float y,float rotation,Texture objTexture,int type,float xindent){
			this.type=type;
			this.xindent=xindent;
			blade=new Sprite(objTexture);
			blade.setPosition(x,y);
			blade.setOrigin(blade.getWidth()/2,blade.getHeight()/2);
			blade.setFlip(true,false);
			this.power=MathUtils.random(1,4);
		}
		public void render(SpriteBatch batch){
			bladeTimeElapsed+=Gdx.graphics.getDeltaTime();
			if(!broken){
			switch(type){
				case 1:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed*power)*200,(power+2)*20);break;}
				case 2:{blade.setPosition(blade.getX(),MathUtils.sin(bladeTimeElapsed*power)*200f);break;}
				case 3:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed+power)*200,MathUtils.sin(bladeTimeElapsed*2)*200f);break;}
				case 4:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed+power)*200,MathUtils.sin(bladeTimeElapsed)*200f);break;}
				case 5:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed*2)*200,MathUtils.sin(bladeTimeElapsed*power)*100f);break;}
				case 6:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed*2)*200+power*20,MathUtils.sin(bladeTimeElapsed*power)*100f);break;}
				case 7:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed+power*100)*(power+2)*100,MathUtils.sin(bladeTimeElapsed*power)*50f);break;}
				case 8:{blade.setPosition(xindent + MathUtils.cos(bladeTimeElapsed*power)*100,MathUtils.sin(bladeTimeElapsed)*100);break;}
				default:{break;}
			}
			float delta = Gdx.graphics.getDeltaTime();
			this.rotationTimeElapsed+=delta;
			if(rotationTimeElapsed>0.01){
				blade.rotate(5f);
				rotationTimeElapsed=0;
			}
			}else{
				if(bladeTimeElapsed>0.02 && brokenBladeTextureIndex<6){
				blade.setTexture(bladeBreakTexture[brokenBladeTextureIndex]);
				blade.setScale(blade.getScaleX()+0.05f,blade.getScaleY()+0.05f);
				brokenBladeTextureIndex++;
				bladeTimeElapsed=0f;
				}


			}

			blade.draw(batch);
		}
		public Rectangle getBladeBounds(){
			this.bladeBounds=blade.getBoundingRectangle();
			return bladeBounds;
		}
	}
	public static class Ground{
		private final Sprite ground;
		private Rectangle groundBounds;
		private final boolean finish;
		public Ground(float x, float y,Texture texture,boolean finish){
			this.finish=finish;
			this.ground=new Sprite(texture);
			ground.setPosition(x,y);
			ground.setOrigin(ground.getWidth()/2f,ground.getHeight()/2f);
			this.groundBounds=ground.getBoundingRectangle();
		}
		public void render(SpriteBatch batch){
			ground.draw(batch);
		}
		public Rectangle getGroundBounds(){
			this.groundBounds=ground.getBoundingRectangle();
			this.groundBounds.set(ground.getX(),ground.getY()+ground.getHeight()+2,ground.getWidth(),2);
			return groundBounds;
		}
	}
	public static class Powers{
		private final Sprite power;
		private float timeElapsed,scale;
		public Powers(float x,float y,Texture texture){
			this.power=new Sprite(texture);
			power.setPosition(x,y);
			power.setOrigin(power.getWidth()/2,power.getHeight()/2);
		}
		public void render(SpriteBatch batch){
			float delta = Gdx.graphics.getDeltaTime();
			timeElapsed+=delta;
			power.draw(batch);
		}
		public Rectangle getPowerBounds(){
			return power.getBoundingRectangle();
		}
	}
	public static class Controls{
		private final Sprite control;
		private String direction;
		private Rectangle controlBounds;
		public Controls(float x, float y,Texture tex, String direction){
			this.control=new Sprite(tex);
			this.direction=direction;
			control.setPosition(x,y);
			control.setOrigin(control.getWidth()/2f,control.getHeight()/2f);
			switch(direction){
				case "left":{control.setRotation(90);break;}
				case "right":{control.setRotation(-90);;break;}
				default:break;
			}
		}
		public void render(SpriteBatch batch){
			control.draw(batch);
		}
		public Rectangle getControlBounds(){
			this.controlBounds=control.getBoundingRectangle();
			return controlBounds;
		}
	}
	public void inititalizeLevel(){

		for(Blade obj : bladeObjectArray ) bladeObjectArray.removeValue(obj,true);
		for(Ground obj : groundObjectArray ) groundObjectArray.removeValue(obj,true);
		for(Powers obj : powerObjectArray ) powerObjectArray.removeValue(obj,true);

		for(int i =0;i<levelLength;i++){
			if(i<levelLength-1){
				groundObjectArray.add(new Ground(i*640,0,groundTexture,false));
			}else groundObjectArray.add(new Ground(i*640,0,finishTexture,true));
		}	//ground
		for(int p =0;p<=MathUtils.random(2,levelLength);p++) {
			int xIndent=MathUtils.random(380,640*levelLength);
			int rows = MathUtils.random(2, 8);
			for (int i = 1; i <= rows; i++) for (int k = 1; k <= i; k++) groundObjectArray.add(new Ground(xIndent+(i * 43), i * 43, groundBlockTexture,false));
		}  //ramp
		for(int p =0;p<=MathUtils.random(5,levelLength);p++){
			int xIndent=MathUtils.random(380,640*levelLength);
			bladeObjectArray.add(new Blade(xIndent,100,0,bladeTexture,MathUtils.random(0,9),xIndent));
		}  //blade
		for(int p =0;p<=MathUtils.random(7,15+levelLength);p++){
			int xIndent=MathUtils.random(380,640*levelLength);
			groundObjectArray.add(new Ground(xIndent,MathUtils.random(1,5)*86,platformTexture,false));
		}  //platform
		for(int p=1;p<=MathUtils.random(3,levelLength/2);p++){
			int xIndent=MathUtils.random(320,640*levelLength);
			boolean fly = p%2==0;
			powerObjectArray.add(new Powers(xIndent,MathUtils.random(1,4)*43,(fly)?powerIronTexture:powerFlyTexture));
		}	//power
		resetPlayerPosition();
		nextLevel=false;
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width,height,true);
	}

	
	@Override
	public void create () {
		Gdx.input.setInputProcessor(boxInput);
		Gdx.graphics.setWindowedMode(640,480);
		camera=new OrthographicCamera();
		viewport= new ExtendViewport(640,480,camera);
		camera.setToOrtho(false,640,480);
		viewport.apply();

		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		groundTexture= new Texture("ground.png");
		playerTexture=new Texture("box.png");
		groundBlockTexture=new Texture("block.png");
		bladeTexture=new Texture("blade.png");
		platformTexture=new Texture("platform.png");
		powerFlyTexture=new Texture("fly.png");
		powerIronTexture=new Texture("steel.png");
		gliderTexture=new Texture("glider.png");
		ironTexture=new Texture("box-steel.png");
		finishTexture= new Texture("finish.png");
		bgTexture= new Texture("bg.png");
		controlTexture=new Texture("control.png");

		controlObjectArray.add(new Controls(0,0,controlTexture,"up"),new Controls(0,0,controlTexture,"left"),new Controls(0,0,controlTexture,"right"));

		levelFont=new BitmapFont();
		levelFont.getData().setScale(1.5f);
		for(int p =0;p<7;p++){
			bladeBreakTexture[p]=new Texture("blade-break"+(p+1)+".png");
		}

		player=new Sprite(playerTexture);
		player.setPosition(30,100);

		inititalizeLevel();

	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);

		float delta = Gdx.graphics.getDeltaTime();

		if(powerEffect){
			powerEffectTime+=delta;
			if(powerEffectTime>11f){
				playerPowerDown();
			}
		}
		player.setX(player.getX()+playerAcceleration);
		if(!gliding){
		playerGravityTime+=delta;
		playerMovementTime+=delta;
		if(playerGravityTime>0.05){
			if(!playerOnGround && playerGravity>-9.1)playerGravity-=1;
			playerGravityTime=0;
		}
		if(!playerOnGround){
			player.setY(player.getY()+playerGravity);
		}
		}else{
			glideAccelerationTime+=delta;

			if(!playerGlideUp){
				if(glideAccelerationTime>0.01){
					if(player.getRotation()>-16)player.setRotation(player.getRotation()-0.15f);
					player.setY(player.getY()-glideAcceleration);
					glideAcceleration+=0.02;
					glideAccelerationTime=0;
					if(glideForwardAcceleration<5)glideForwardAcceleration+=0.2;
				}
			}else{
				if(glideAccelerationTime>0.01){
					if(player.getRotation()<1)player.setRotation(player.getRotation()+0.35f);
					player.setY(player.getY()+glideAcceleration);
					if(glideAcceleration>0)glideAcceleration-=0.02;
					glideAccelerationTime=0;
					if(glideForwardAcceleration<3)glideForwardAcceleration+=0.6;
				}
			}
			player.setX(player.getX()+glideForwardAcceleration);


		}

		camera.position.set(player.getX(),player.getY()+50,0);
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		playerbounds=player.getBoundingRectangle();
		playerbounds.set(player.getX(),player.getY(),player.getWidth(),10);

		batch.begin();
		batch.draw(bgTexture,player.getX()-Gdx.graphics.getWidth()/2f,player.getY()+50-Gdx.graphics.getHeight()/2f,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		boolean touchingGround=false;
		for(Ground ground : groundObjectArray){
			ground.render(batch);

			if(ground.getGroundBounds().overlaps(playerbounds) &&!playerOnGround &&playerGravity<0){ playerOnGround=true;playerGravity=0;}
			if(player.getY()<-200 && (player.getTexture()!= gliderTexture)) resetPlayerPosition();
			if(playerbounds.overlaps(ground.getGroundBounds()))touchingGround=true;
			if(ground.getGroundBounds().overlaps(playerbounds) && ground.finish) {
				level++;
				levelLength+=2;
				nextLevel=true;
			}
		}
		if(!touchingGround)playerOnGround=false;
		for(Blade blade : bladeObjectArray){
			blade.render(batch);
			if(blade.getBladeBounds().overlaps(player.getBoundingRectangle()) && !blade.broken){
				if(powerEffect){
					if(player.getTexture()!=ironTexture){
						playerPowerDown();
					}
					blade.broken=true;
					blade.bladeTimeElapsed=0f;
				}else {resetPlayerPosition();}
			}
			if(blade.brokenBladeTextureIndex==6)bladeObjectArray.removeValue(blade,true);
		}
		for(Powers power : powerObjectArray){
			power.render(batch);
			if(power.getPowerBounds().overlaps(player.getBoundingRectangle())){
			if(power.power.getTexture()==powerIronTexture){
				playerPowerDown();
				player.setTexture(ironTexture);
				player.setSize(34,34);
			}
			if(power.power.getTexture()==powerFlyTexture){
			player.setY(player.getY()+200f);
			gliding=true;
			playerGlideUp=false;
			glideAcceleration=0f;
			glideForwardAcceleration=4f;
			if(player.isFlipX())player.setFlip(false,false);
			player.setTexture(gliderTexture);
			player.setSize(71,26);
			}

			powerEffect=true;
			powerEffectTime=0;
			powerObjectArray.removeValue(power,true);
			}
		}
		for(Controls obj : controlObjectArray){
			switch(obj.direction){
				case "up":{obj.control.setPosition(player.getX()+390,player.getY()-150);break;}
				case "left":{obj.control.setPosition(player.getX()-420,player.getY()-150);break;}
				case "right":{obj.control.setPosition(player.getX()-300,player.getY()-150);break;}
				default:break;
			}

			obj.render(batch);
		}
		player.draw(batch);
		levelFont.draw(batch,"Level : "+ level,player.getX()+200f,player.getY()+250f);
		batch.end();
		if(nextLevel)inititalizeLevel();
	}

	private void playerPowerDown() {
		gliding=false;
		player.setSize(34,34);
		player.setRotation(0f);
		player.setTexture(playerTexture);
		glideForwardAcceleration=0;
		glideAcceleration=0;
		powerEffect=false;
		powerEffectTime=0f;
	}

	private void resetPlayerPosition() {
		gliding=false;
		player.setPosition(30,100);
		player.setSize(34,34);
		player.setRotation(0f);
		player.setTexture(playerTexture);
		glideForwardAcceleration=0;
		glideAcceleration=0;
		powerEffect=false;
		powerEffectTime=0f;
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		groundBlockTexture.dispose();
		playerTexture.dispose();
		bladeTexture.dispose();
		platformTexture.dispose();
		powerFlyTexture.dispose();
		powerIronTexture.dispose();
		groundTexture.dispose();
		gliderTexture.dispose();
		ironTexture.dispose();
		finishTexture.dispose();
		bgTexture.dispose();
		levelFont.dispose();
		controlTexture.dispose();
		for(Texture tex : bladeBreakTexture) tex.dispose();
	}
	InputProcessor boxInput =new InputProcessor() {
		@Override
		public boolean keyDown(int keycode) {
			if(keycode== Input.Keys.UP || keycode== Input.Keys.W){
				handleUpMovement();
			}
			if(keycode==Input.Keys.RIGHT || keycode== Input.Keys.D){

				handleRightMovement();

			}
			if(keycode==Input.Keys.LEFT || keycode== Input.Keys.A){

				handleLeftMovement();

			}
			return false;
		}

		private void handleLeftMovement() {
			if(!gliding){
				playerForwardMotion=false;
				if(!player.isFlipX())player.flip(true,false);
				playerAcceleration=-playerSpeed;
				playerMotion=true;}
		}

		private void handleRightMovement() {
			if(!gliding) {
				if (player.isFlipX()) player.flip(true, false);
				playerForwardMotion = true;
				playerAcceleration = playerSpeed;
				playerMotion = true;
			}
		}

		private void handleUpMovement() {
			if(!gliding){
				if(playerOnGround && player.getY()>35){
					playerOnGround=false;
					playerGravity=9.8f;
				}
			}else playerGlideUp=true;
		}

		@Override
		public boolean keyUp(int keycode) {
			if(keycode== Input.Keys.UP || keycode== Input.Keys.W){
				unhandleUpMovement();
			}
			if(keycode==Input.Keys.RIGHT || keycode== Input.Keys.D){

				unhandleRightMovement();
			}
			if(keycode==Input.Keys.LEFT || keycode== Input.Keys.A){
				unhandleLeftMovement();
			}

			return false;
		}

		private void unhandleLeftMovement() {
			playerForwardMotion=false;
			playerMotion=false;
			playerAcceleration=0;
		}

		private void unhandleRightMovement() {
			playerForwardMotion=true;
			playerAcceleration=0;
			playerMotion=false;
		}

		private void unhandleUpMovement() {
			if(gliding){
				playerGlideUp=false;
			}
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			Vector3 touchPoint = new Vector3(screenX, screenY, 0);
			camera.unproject(touchPoint);
			for(Controls obj : controlObjectArray){
			if (obj.getControlBounds().contains(touchPoint.x, touchPoint.y)) {
				switch(obj.direction){
					case "up":{handleUpMovement();break;}
					case "left":{handleLeftMovement();break;}
					case "right":{handleRightMovement();break;}
					default:break;
				}
			}
			}

			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			Vector3 touchPoint = new Vector3(screenX, screenY, 0);
			camera.unproject(touchPoint);
			for(Controls obj : controlObjectArray){
				if (obj.getControlBounds().contains(touchPoint.x, touchPoint.y)) {
					switch(obj.direction){
						case "up":{unhandleUpMovement();break;}
						case "left":{unhandleLeftMovement();break;}
						case "right":{unhandleRightMovement();break;}
						default:break;
					}
				}
			}
			return false;
		}

		@Override
		public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(float amountX, float amountY) {
			return false;
		}
	};
}
