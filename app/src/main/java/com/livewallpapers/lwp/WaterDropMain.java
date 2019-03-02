package com.livewallpapers.lwp;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;

import java.util.ArrayList;
import java.util.Random;

public class WaterDropMain extends BaseLiveWallpaperService implements SharedPreferences.OnSharedPreferenceChangeListener, IAccelerometerListener {
     
     /**
      * Camera Constants
      */
     public static int              CAMERA_WIDTH          = 720;
     public static int              CAMERA_HEIGHT         = 480;
     private Scene scene                 = new Scene();
     private Camera mCamera;
     private String                 orientationValue      = "PORTRAIT";
     
     /**
      * Shared Preferences
      */
     private SharedPreferences      mPrefs;
     /**
      * SET PREFERENCES
      */
     public static final String     SHARED_PREFS_NAME     = "lw_settings";
     
     private static final String    OBJECT_SIZE_SELECTION = "object_size_selection";
     private static final String    ACCELEROMETER_ON_OFF  = "accelerometer_on_off";
     private static final String    OBJECT_SELECTION      = "Object_selection";
     private static final String    FALLING_DIRECTION     = "falling_direction";
     
     /**
      * Fields
      */
     private BitmapTextureAtlas mBGTexture1, mHeartTexture1, mHeartTexture2;
     
     private int                    mObjectSizeSelection  = 1;
     private int                    mNoOfObject           = 1, fallingYPostion, removeFrom;
     private int                    mAccelerometerOnOff   = 1;
     
     private SensorManager          mSensorManager;
     private ShakeEventListener     mSensorListener;
     
     private Display                localDisplay;
     private TextureRegion mHeart4;
     private TextureRegion mHeart1;
     private TextureRegion mBg;
     private TextureRegion customizeBackground;
     
     public static boolean          flowers               = true;
     PhysicsHandler mPhysicsHandler;
     private Handler                handler               = new Handler();
     private int                    maxObject             = 0;
     private PhysicsWorld mPhysicsWorld;
     private ArrayList<Sprite>      myObject1List, myObject2List;
     
     /** The categories. */
     public static final short      CATEGORYBIT_WALL      = 1;
     public static final short      CATEGORYBIT_CIRCLE    = 4;
     
     /** And what should collide with what. */
     public static final short      MASKBITS_WALL         = CATEGORYBIT_WALL + CATEGORYBIT_CIRCLE;
     public static final short      MASKBITS_BOX          = CATEGORYBIT_WALL;                                       // Missing:
                                                                                                                     // CATEGORYBIT_CIRCLE
     public static final short      MASKBITS_CIRCLE       = CATEGORYBIT_WALL + CATEGORYBIT_CIRCLE;                  // Missing:
                                                                                                                     // CATEGORYBIT_BOX
                                                                                                                     
     public static final FixtureDef WALL_FIXTURE_DEF      = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f, false,
                                                                    CATEGORYBIT_WALL, MASKBITS_WALL, (short) 0);
     public static final FixtureDef FIXTURE_DEF           = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false,
                                                                    CATEGORYBIT_CIRCLE, MASKBITS_CIRCLE, (short) 0);
     private TimerHandler spriteTimerHandler    = null;
     private Integer                mFallingDirection     = 1;
     
     @SuppressWarnings("deprecation")
     /** 
      * SET CAMERA and INTIALIZE ENGINE
      */
     @Override
     public org.anddev.andengine.engine.Engine onLoadEngine() {
          UtilAndEngine.calculateScreenWidthAndHeight(WaterDropMain.this);
          localDisplay = ((WindowManager) getSystemService("window")).getDefaultDisplay();
          
          if (localDisplay.getWidth() == localDisplay.getHeight())
               this.orientationValue = "PORTRAIT";
          while (true) {
               this.mCamera = new Camera(0.0F, 0.0F, 480.0F, 720.0F);
               if (localDisplay.getWidth() < localDisplay.getHeight())
                    this.orientationValue = "PORTRAIT";
               else
                    this.orientationValue = "LANDSCAPE";
               EngineOptions localEngineOptions = new EngineOptions(true, EngineOptions.ScreenOrientation.PORTRAIT,
                         new RatioResolutionPolicy(480.0F, 800.0F), this.mCamera).setNeedsSound(true).setNeedsMusic(
                         true);
               localEngineOptions.getTouchOptions().setRunOnUpdateThread(true);
               return new org.anddev.andengine.engine.Engine(localEngineOptions);
          }
     }
     
     /**
      * LOAD RESOURCES FROM ASSETS FOLDER
      **/
     @Override
     public void onLoadResources() {
          BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
          
          this.mBGTexture1 = new BitmapTextureAtlas(1024, 1024, TextureOptions.NEAREST_PREMULTIPLYALPHA);
          this.mHeartTexture1 = new BitmapTextureAtlas(256, 128, TextureOptions.NEAREST_PREMULTIPLYALPHA);
          this.mHeartTexture2 = new BitmapTextureAtlas(256, 128, TextureOptions.NEAREST_PREMULTIPLYALPHA);
          
          mBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBGTexture1, this, "bgeffect3.png", 0, 0);
          mHeart1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHeartTexture1, this, "3(1).png", 0, 0);
          mHeart4 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHeartTexture2, this, "4(1).png", 0, 0);
          myObject1List = new ArrayList<Sprite>();
          myObject2List = new ArrayList<Sprite>();
          this.mEngine.getTextureManager().loadTextures(this.mBGTexture1, this.mHeartTexture1, this.mHeartTexture2);
     }
     
     /**
      * SET SCENE
      */
     @Override
     public Scene onLoadScene() {
          if (this.orientationValue == "PORTRAIT")
               this.scene.setScale(1.0F);
          else if (this.orientationValue == "LANDSCAPE")
               this.scene.setScale(2.0f);
          this.mEngine.registerUpdateHandler(new FPSLogger());
          this.scene.detachChildren();
          mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
          
          if (mFallingDirection == 1)
               this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0.1f), false);
          else if (mFallingDirection == 2)
               this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0.2f * -1), false);
          mPhysicsWorld.setVelocityIterations(50);
          
          /**
           * CREATED PHYSICS FOR LEFT AND RIGHT WALL
           */
          final Shape left = new Rectangle(0, -1500, 0, CAMERA_HEIGHT + 1500);
          final Shape right = new Rectangle(mCamera.getWidth(), -1500, 0, CAMERA_HEIGHT + 1500);
          PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, WALL_FIXTURE_DEF);
          PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, WALL_FIXTURE_DEF);
          
          scene.attachChild(left);
          scene.attachChild(right);
          
          scene.setBackground(new SpriteBackground(new Sprite(0, 0, mCamera.getWidth(), mCamera.getHeight(), mBg)));
          createFlowersSpawnTimeHandler();
          mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
          
          /**
           * START THREAD TO ADD OBJECTS CONTINOUSLY
           */
          handler.post(timedTask);
          this.scene.registerUpdateHandler(this.mPhysicsWorld);
          mEngine.enableAccelerometerSensor(WaterDropMain.this, WaterDropMain.this);
          
          /**
           * CHECK FOR THE LIST , WHEN ANY OF HEARTS WILL GO APART FROM SCREEN THEN IT WILL BE REMOVED FROM THE LIST AS
           * WELL AS SCREEN
           */
          scene.registerUpdateHandler(new IUpdateHandler() {
               
               @Override
               public void reset() {}
               
               @Override
               public void onUpdate(float arg0) {
                    if (mFallingDirection == 2) {
                         removeFrom = -100;
                         if (!myObject1List.isEmpty() && (myObject1List.get(0).getY() <= removeFrom)) {
                              scene.detachChild(myObject1List.get(0));
                              myObject1List.remove(0);
                         }
                         if (mNoOfObject == 2) {
                              if (!myObject2List.isEmpty() && (myObject2List.get(0).getY() <= removeFrom)) {
                                   scene.detachChild(myObject2List.get(0));
                                   myObject2List.remove(0);
                              }
                         }
                    } else if (mFallingDirection == 1) {
                         removeFrom = CAMERA_HEIGHT;
                         if (!myObject1List.isEmpty() && (myObject1List.get(0).getY() >= removeFrom)) {
                              scene.detachChild(myObject1List.get(0));
                              myObject1List.remove(0);
                         }
                         if (mNoOfObject == 2) {
                              if (!myObject2List.isEmpty() && (myObject2List.get(0).getY() >= removeFrom)) {
                                   scene.detachChild(myObject2List.get(0));
                                   myObject2List.remove(0);
                              }
                         }
                    }
                    
               }
          });
          
          return scene;
     }
     
     @Override
     public void onLoadComplete() {}
     
     @Override
     public void onCreate() {
          // TODO Auto-generated method stub
          super.onCreate();
          // get the system properties
          mPrefs = WaterDropMain.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
          mPrefs.registerOnSharedPreferenceChangeListener(this);
          onSharedPreferenceChanged(mPrefs, null);
          
     }
     
     @Override
     public void onPauseGame() {
          super.onPause();
          WaterDropMain.this.getEngine().onPause();
          WaterDropMain.this.onPause();
     }
     
     @Override
     public void onResumeGame() {
          super.onResume();
          WaterDropMain.this.getEngine().onResume();
          enableAccelerometerSensor(this);
     }
     
     @Override
     protected void onResume() {
          // TODO Auto-generated method stub
          super.onResume();
          handler.post(timedTask);
     }
     
     @Override
     protected void onPause() {
          super.onPause();
          handler.removeCallbacks(timedTask);
     }
     
     @Override
     public void onUnloadResources() {}
     
     public LoopEntityModifier getLoopModif(int pSpeed, float pIA, float pFA) {
          return new LoopEntityModifier(new SequenceEntityModifier(new RotationModifier(pSpeed, pIA, pFA)));
     }
     
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
          
          /**
           * SET VALUE AS PER SETTINGS CHANGE
           */
          mAccelerometerOnOff = Integer.valueOf(mPrefs.getString(ACCELEROMETER_ON_OFF, "1"));
          mNoOfObject = Integer.valueOf(mPrefs.getString(OBJECT_SELECTION, "1"));
          mObjectSizeSelection = Integer.valueOf(mPrefs.getString(OBJECT_SIZE_SELECTION, "1"));
          mFallingDirection = Integer.valueOf(mPrefs.getString(FALLING_DIRECTION, "1"));
          
          if (mFallingDirection == 2) {
               scene.detachChildren();
               myObject1List.clear();
               myObject2List.clear();
               this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0.2f * -1), false);
               
               attachWalls();
          } else if (mFallingDirection == 1) {
               scene.detachChildren();
               myObject1List.clear();
               myObject2List.clear();
               this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0.1f), false);
               this.scene.registerUpdateHandler(this.mPhysicsWorld);
               attachWalls();
          }
          
     }
     
     public void cutomizeWallpaper() {
          this.scene.detachChildren();
          scene.setBackground(new SpriteBackground(new Sprite(0, 0, mCamera.getWidth(), mCamera.getHeight(),
                    customizeBackground)));
     }
     
     public void unregisterTounch() {
          scene.detachChildren();
     }
     
     public void attachWalls() {
          
          final Shape left = new Rectangle(0, -500, 0, CAMERA_HEIGHT + 500);
          final Shape right = new Rectangle(mCamera.getWidth(), -500, 0, CAMERA_HEIGHT + 500);
          
          PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, WALL_FIXTURE_DEF);
          PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, WALL_FIXTURE_DEF);
          
          scene.attachChild(left);
          scene.attachChild(right);
          this.scene.registerUpdateHandler(this.mPhysicsWorld);
     }
     
     private Runnable timedTask = new Runnable() {
                                     
                                     @Override
                                     public void run() {
                                          // CALL BELOW METHOD AT EVERY 2 SECONDS
                                          createFlowersSpawnTimeHandler();
                                          handler.postDelayed(timedTask, 2000);
                                     }
                                };
     
     /**
      * THIS METHOD IS USED TO ADD OBJECTS ON THE SCREEN
      */
     private void addoBJECTS() {
          if (mNoOfObject == 1)
               maxObject = 20;
          else
               maxObject = 10;
          if (mFallingDirection == 2) {
               fallingYPostion = CAMERA_HEIGHT - 100;
          } else {
               fallingYPostion = -100;
          }
          
          if (myObject1List.size() <= maxObject) {
               myObject1List.add(new Sprite(new Random().nextInt((int) (this.mCamera.getWidth()
                         - (this.mHeart1.getWidth()) - 50)), fallingYPostion, this.mHeart1));
               
               if (mObjectSizeSelection == 1) {
                    myObject1List.get(myObject1List.size() - 1).setScale(0.5F);
               } else if (mObjectSizeSelection == 2) {
                    myObject1List.get(myObject1List.size() - 1).setScale(0.7F);
               } else {
                    myObject1List.get(myObject1List.size() - 1).setScale(1.0F);
               }
               Body body1 = WaterDropMain.createTriangleBody(mPhysicsWorld,
                         myObject1List.get(myObject1List.size() - 1), BodyType.DynamicBody, FIXTURE_DEF);
               this.scene.attachChild(myObject1List.get(myObject1List.size() - 1));
               mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(myObject1List.get(myObject1List.size() - 1),
                         body1, true, true));
          }
          
          if (mNoOfObject == 2) {
               if (myObject2List.size() <= maxObject) {
                    myObject2List.add(new Sprite(new Random().nextInt((int) (this.mCamera.getWidth()
                              - (this.mHeart1.getWidth()) - 50)), fallingYPostion, this.mHeart4));
                    if (mObjectSizeSelection == 1) {
                         myObject2List.get(myObject2List.size() - 1).setScale(0.5F);
                    } else if (mObjectSizeSelection == 2) {
                         myObject2List.get(myObject2List.size() - 1).setScale(0.7F);
                    } else {
                         myObject2List.get(myObject2List.size() - 1).setScale(1.0F);
                    }
                    Body body2 = WaterDropMain.createTriangleBody(mPhysicsWorld,
                              myObject2List.get(myObject2List.size() - 1), BodyType.DynamicBody, FIXTURE_DEF);
                    this.scene.attachChild(myObject2List.get(myObject2List.size() - 1));
                    mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                              myObject2List.get(myObject2List.size() - 1), body2, true, true));
               }
          }
     }
     
     private static Body createTriangleBody(final PhysicsWorld pPhysicsWorld, final Shape pShape,
                                            final BodyType pBodyType, final FixtureDef pFixtureDef) {
          /** Remember that the vertices are relative to the center-coordinates of the Shape. */
          final float halfWidth = pShape.getWidthScaled() * 0.5f / 32.0f;
          final float halfHeight = pShape.getHeightScaled() * 0.5f / 32.0f;
          
          final float top = -halfHeight;
          final float bottom = halfHeight;
          final float left = -halfHeight;
          final float centerX = 0;
          final float right = halfWidth;
          
          final Vector2[] vertices = { new Vector2(centerX, top), new Vector2(right, bottom), new Vector2(left, bottom) };
          
          return PhysicsFactory.createPolygonBody(pPhysicsWorld, pShape, vertices, pBodyType, pFixtureDef);
     }
     
     private void createFlowersSpawnTimeHandler() {
          this.spriteTimerHandler = new TimerHandler(1.0F, new ITimerCallback() {
               
               @Override
               public void onTimePassed(TimerHandler arg0) {
                    WaterDropMain.this.addoBJECTS();
               }
          });
          getEngine().registerUpdateHandler(this.spriteTimerHandler);
     }
     
     public interface IOffsetsChanged {
          
     }
     
     protected class MyBaseWallpaperGLEngine extends GLEngine {
          
          // ===========================================================
          // Fields
          // ===========================================================
          
          private org.anddev.andengine.opengl.view.GLSurfaceView.Renderer mRenderer;
          private IOffsetsChanged                                         mOffsetsChangedListener = null;
          
          // ===========================================================
          // Constructors
          // ===========================================================
          
          public MyBaseWallpaperGLEngine(IOffsetsChanged pOffsetsChangedListener) {
               this.setEGLConfigChooser(false);
               this.mRenderer = new RenderSurfaceView.Renderer(WaterDropMain.this.mEngine);
               this.setRenderer(this.mRenderer);
               this.setRenderMode(RENDERMODE_CONTINUOUSLY);
               this.mOffsetsChangedListener = pOffsetsChangedListener;
          }
          
          // ===========================================================
          // Methods for/from SuperClass/Interfaces
          // ===========================================================
          
          @Override
          public Bundle onCommand(final String pAction, final int pX, final int pY, final int pZ, final Bundle pExtras,
                    final boolean pResultRequested) {
               if (pAction.equals(WallpaperManager.COMMAND_TAP)) {
                    WaterDropMain.this.onTap(pX, pY);
               } else if (pAction.equals(WallpaperManager.COMMAND_DROP)) {
                    WaterDropMain.this.onDrop(pX, pY);
               }
               
               return super.onCommand(pAction, pX, pY, pZ, pExtras, pResultRequested);
          }
          
          @Override
          public void onResume() {
               super.onResume();
          }
          
          @Override
          public void onPause() {
               super.onPause();
               WaterDropMain.this.getEngine().onPause();
               WaterDropMain.this.onPause();
          }
          
          @Override
          public void onDestroy() {
               super.onDestroy();
               if (this.mRenderer != null) {}
               this.mRenderer = null;
          }
          
          @Override
          public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
                    int xPixelOffset, int yPixelOffset) {
               
               super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
               
               if (mCamera != null) {
                    mCamera.setCenter(mCamera.getWidth() * xOffset * 0.25f + mCamera.getWidth() * xOffset * 0.5f,
                              mCamera.getCenterY());
               }
          }
          
          @Override
          public void onTouchEvent(MotionEvent event) {
               super.onTouchEvent(event);
          }
     }
     
     /** 
      * CHECK ACCELEROMETER VALUES
      */
     @Override
     public void onAccelerometerChanged(AccelerometerData arg0) {
          Vector2 gravity = null;
          if (mFallingDirection == 2) {
               gravity = Vector2Pool.obtain(arg0.getX(), 0.2f * -1);
          } else if (mFallingDirection == 1) {
               gravity = Vector2Pool.obtain(arg0.getX(), 0.1f);
               
          }
          
          if (mAccelerometerOnOff == 1) {
               this.mPhysicsWorld.setGravity(gravity);
               Vector2Pool.recycle(gravity);
          }
          
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
          super.onConfigurationChanged(newConfig);
     }
}
