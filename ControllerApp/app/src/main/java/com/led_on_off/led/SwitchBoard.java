package com.led_on_off.led;

import java.util.List;

public class SwitchBoard {
    private String boardName;

    private List<Appliances> switches;
    private configStatus status; //Configured or not

    public SwitchBoard() {
        this.status = configStatus.e_NOT_CONFIGURED;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String name) {
        this.boardName = name;
    }

    private enum configStatus{
        e_NOT_CONFIGURED,
        e_CONFIGURED;
    }
}
