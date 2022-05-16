package org.ishgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class BaseScreen implements Screen, InputProcessor {

    protected Stage mainStage;
    protected Stage uiStage;
    protected Table uiTable;

    public BaseScreen() {
        mainStage = new Stage();
        uiStage = new Stage();

        uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);

        initialize();
    }

    public abstract void initialize();

    public abstract void update(float deltaTime);

    // Игровой цикл:
    // (1) входной процесс
    // (2) логика апдейтов
    // (3) рендер графики
    public void render(float deltaTime) {
        // ограничение времени, которое может пройти при перетаскивании окна
        deltaTime = Math.min(deltaTime, 1 / 30f);

        // методы act
        uiStage.act(deltaTime);
        mainStage.act(deltaTime);

        // определяется пользователем
        update(deltaTime);

        // очистка экрана
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // отрисовка графики
        mainStage.draw();
        uiStage.draw();
    }

    // методы, требуемые интерфейсом Screen
    public void resize(int width, int height) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
    }

    public void show() {
        InputMultiplexer inputProcessor = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputProcessor.addProcessor(this);
        inputProcessor.addProcessor(uiStage);
        inputProcessor.addProcessor(mainStage);
    }

    public void hide() {
        InputMultiplexer inputProcessor = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputProcessor.removeProcessor(this);
        inputProcessor.removeProcessor(uiStage);
        inputProcessor.removeProcessor(mainStage);
    }


    //Полезно для проверки touchDown-ивентов (когда кнопкой или касанием юзер нажимает на объект класса Actor)
    public boolean isTouchDownEvent(Event event) {
        return (event instanceof InputEvent) && ((InputEvent) event).getType().equals(Type.touchDown);
    }

    // методы, требуемые интерфейсом InputProcessor
    public boolean keyDown(int keycode) {
        return false;
    }

    public boolean keyUp(int keycode) {
        return false;
    }

    public boolean keyTyped(char character) {
        return false;
    }

    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
