package org.ishgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Создается, когда игра запущена;
 * управляет экранами, которые создаются в течение игры.
 */
public abstract class BaseGame extends Game {
    /**
     * Сохраняет ссылку на игру; используется, когда вызван метод setActiveScreen.
     */
    private static BaseGame game;

    public static LabelStyle labelStyle;
    public static TextButtonStyle textButtonStyle;

    /**
     * Вызывается при инициализации игры; сохраняет глобальную ссылку на игровой объект.
     */
    public BaseGame() {
        game = this;
    }

    /**
     * Вызывается при инициализации игры,
     * после этого Gdx.input и другие игровые объекты инициализируются.
     */
    public void create() {

        // prepare for multiple classes/stages/actors to receive discrete input
        Gdx.input.setInputProcessor(new InputMultiplexer());

        // parameters for generating a custom bitmap font
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Bahnschrift.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameters.size = 32;
        fontParameters.color = Color.WHITE;
        fontParameters.borderWidth = 2;
        fontParameters.borderColor = Color.BLACK;
        fontParameters.borderStraight = true;
        fontParameters.minFilter = TextureFilter.Linear;
        fontParameters.magFilter = TextureFilter.Linear;

        BitmapFont customFont = fontGenerator.generateFont(fontParameters);

        labelStyle = new LabelStyle();
        labelStyle.font = customFont;

        textButtonStyle = new TextButtonStyle();

        Texture buttonTex = new Texture(Gdx.files.internal("button.png"));
        NinePatch buttonPatch = new NinePatch(buttonTex, 24, 24, 24, 24);
        textButtonStyle.up = new NinePatchDrawable(buttonPatch);
        textButtonStyle.font = customFont;
        textButtonStyle.fontColor = Color.GRAY;
    }

    /**
     * Используется для смены экранов, когда игра запущена
     * Метод сделан статичным для упрощения использования.
     */
    public static void setActiveScreen(BaseScreen s) {
        game.setScreen(s);
    }
}
