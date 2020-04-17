package com.led_on_off.led;

import java.util.List;

public class SwitchBoard {
    private String boardName;
    private List<Appliances> switches;

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String name) {
        this.boardName = name;
    }
}
