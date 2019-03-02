package com.livewallpapers.lwp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Display;
import android.view.WindowManager;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;


public class UtilAndEngine {
	public static Camera dCamera;
//	public static SharedPrefs prefs;

	public static boolean isVisible = false;

	public static Engine loadEngine(boolean isFullScreen,
                                    ScreenOrientation screenOrientation, float startX, float startY,
                                    float width, float height) {
		return loadEngine(isFullScreen, screenOrientation, startX, startY,
				width, height, false, false, true);
	}

	public static Engine loadEngine(boolean isFullScreen,
                                    ScreenOrientation screenOrientation, float startX, float startY,
                                    float width, float height, boolean isNeedSound, boolean isNeedMusic) {
		return loadEngine(isFullScreen, screenOrientation, startX, startY,
				width, height, isNeedSound, isNeedMusic, true);
	}

	public static Engine loadEngine(boolean isFullScreen,
                                    ScreenOrientation screenOrientation, float startX, float startY,
                                    float width, float height, boolean isNeedSound,
                                    boolean isNeedMusic, boolean isUpdateThread) {

		dCamera = new Camera(0, 0, width, height);
		EngineOptions engineOptions = new EngineOptions(isFullScreen,
				screenOrientation, new RatioResolutionPolicy(width, height),
				dCamera);
		engineOptions.setNeedsSound(isNeedSound);
		engineOptions.setNeedsMusic(isNeedMusic);
		engineOptions.getTouchOptions().setRunOnUpdateThread(isUpdateThread);
		return new Engine(engineOptions);
	}

	public static TextureRegion loadTextureRegion(BaseGameActivity activity,
                                                  int width, int height, String name) {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("images/");
		BitmapTextureAtlas BitmapTextureAtlas = new BitmapTextureAtlas(width,
				height, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(BitmapTextureAtlas, activity, name, 0, 0);
		activity.getTextureManager().loadTextures(BitmapTextureAtlas);
		return textureRegion;
	}

	public static TiledTextureRegion loadTiledTextureRegion(
            BaseGameActivity activity, int width, int height, int col, int row,
            String name) {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("images/");
		BitmapTextureAtlas BitmapTextureAtlas = new BitmapTextureAtlas(width,
				height, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TiledTextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(BitmapTextureAtlas, activity, name, 0, 0,
						col, row);
		activity.getTextureManager().loadTextures(BitmapTextureAtlas);
		return textureRegion;
	}

	// Load Default Font using Typeface built in Font
	public static Font loadDefaultFont(BaseGameActivity activity, int width,
                                       int height, Typeface family, int style, int color, int size,
                                       boolean AntiAliased) {
		BitmapTextureAtlas BitmapTextureAtlas = new BitmapTextureAtlas(width,
				height, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		Font font = new Font(BitmapTextureAtlas,
				Typeface.create(family, style), size, AntiAliased, color);
		activity.getTextureManager().loadTexture(BitmapTextureAtlas);
		activity.getFontManager().loadFont(font);
		return font;
	}

	// Load Custom Font
	public static Font loadCustomFont(BaseGameActivity activity, int width,
                                      int height, String fontName, int color, int size,
                                      boolean AntiAliased) {
		FontFactory.setAssetBasePath("font/");

		BitmapTextureAtlas BitmapTextureAtlas = new BitmapTextureAtlas(width,
				height, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = FontFactory.createFromAsset(BitmapTextureAtlas, activity,
				fontName, size, AntiAliased, color);
		activity.getTextureManager().loadTexture(BitmapTextureAtlas);
		activity.getFontManager().loadFont(font);
		return font;
	}

	// remove sprite from scene
	public void removeSprite(final BaseGameActivity baseGameActivity,
                             final Scene scene, final Sprite sprite) {
		baseGameActivity.runOnUpdateThread(new Runnable() {
			public void run() {
				scene.detachChild(sprite);
			}
		});
	}

	public void removeSprite(final BaseGameActivity baseGameActivity,
                             final Scene scene, final Sprite sprite,
                             final boolean isUnregisterAreaTouch) {
		scene.unregisterTouchArea(sprite);
		removeSprite(baseGameActivity, scene, sprite);
	}

	public static void calculateScreenWidthAndHeight(BaseLiveWallpaperService activity) {
		Display display = ((WindowManager) activity
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		float screenWidth, screenHeight;
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		float screen_ratio = (float) (screenWidth / screenHeight);

		WaterDropMain.CAMERA_HEIGHT = 800;
		WaterDropMain.CAMERA_WIDTH = (int) (WaterDropMain.CAMERA_HEIGHT * screen_ratio);

	}

	
}
