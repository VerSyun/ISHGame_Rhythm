package org.ishgame;

import org.ishgame.screen.RhythmScreen;

public class RhythmGame extends BaseGame {

    public void create() {
        super.create();
        setActiveScreen(new RhythmScreen());
    }
}
