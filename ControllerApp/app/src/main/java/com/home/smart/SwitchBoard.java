package com.home.smart;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SwitchBoard {
    private String TAG = getClass().toString();
    private String boardName;

    private List<Appliances> switches = new ArrayList<Appliances>(4);
    private List<Integer> switch_status;
    private configStatus status; //Configured or not
    private int num_appliances;

    public SwitchBoard() {
        this.status = configStatus.e_NOT_CONFIGURED;
    }

    SwitchBoard(String board_config)
    {
        if(board_config.length() <= 2) {
            status = configStatus.e_NOT_CONFIGURED;
        }
        else {
            //parse the config
            parseBoardConfig(board_config);
            status = configStatus.e_CONFIGURED;
        }
    }

    private void parseBoardConfig(String board_config) {
        Log.d(TAG, "parseBoardConfig: " + board_config);
        String segments[] = board_config.split("|");

        for(int i = 1; i < segments.length; i++) {
            String data[] = segments[i].split(":");
            int val = Integer.valueOf(data[0]);
            switches.add(new Appliances(Appliances.Types.values()[val], Integer.valueOf(data[1])));
        }
        boardName = segments[0];
    }

    public String formBoardConfig() {
        List<String> joined_app = new ArrayList<String>();
        for (Appliances conn: switches) {
            joined_app.add(String.join(":", String.valueOf(conn.type.ordinal()), String.valueOf(conn.status)));
        }
        String config = String.join("|", this.boardName, String.join("|", joined_app));
        Log.d(TAG, "getBoardConfig: "+ config);
        return config;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String name) {
        this.boardName = name;
    }

    public List<Appliances> getSwitches() {
        return switches;
    }

    public void setSwitches(int index, Appliances.Types type, int status) {
        if(switches.size() < index+1) {
            switches.add(index, new Appliances(type, status));
        } else {
            switches.set(index, new Appliances(type, status));
        }
    }

    public int getStatus() {
        return status.getValue();
    }

    private enum configStatus{
        e_NOT_CONFIGURED(0),
        e_CONFIGURED(1);

        private int value;

        configStatus(final  int newVal) {
            value = newVal;
        }

        public int getValue() {
            return value;
        }
    }

}
