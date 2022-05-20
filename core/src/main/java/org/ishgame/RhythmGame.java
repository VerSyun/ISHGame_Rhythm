package org.ishgame;

import org.ishgame.screen.RhythmScreen;

public class RhythmGame extends BaseGame { //класс, который создает игру

    public void create() {
        super.create();
        setActiveScreen(new RhythmScreen());
    }
}
