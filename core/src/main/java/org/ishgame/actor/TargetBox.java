package org.ishgame.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import org.ishgame.BaseActor;
import org.ishgame.BaseGame;

public class TargetBox extends BaseActor { //класс, отвечающий за логику кнопки

    public TargetBox(float x, float y, Stage stage, String letter, Color color) {

        super(x, y, stage);

        loadTexture("box.png");
        setSize(64, 64);

        // добавляет центрированную метку с буквой заданного цвета
        Label letterLabel = new Label(letter, BaseGame.labelStyle);
        letterLabel.setSize(64, 64);
        letterLabel.setAlignment(Align.center);
        letterLabel.setColor(color);
        addActor(letterLabel);
    }

    public void pulse() { //анимация пульсации

        Action pulse = Actions.sequence(
                Actions.scaleTo(1.2f, 1.2f, 0.05f),
                Actions.scaleTo(1.0f, 1.0f, 0.05f));

        addAction(pulse);
    }
}
