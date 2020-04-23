package com.led_on_off.led;

public class Appliances {
    int type;
    int status;
//    actions: only_on_off/rgb/dimmable/speed control/etc
//    data : could be device specific
    public enum Types{
        eFAN("Fan"),
        eTUBELIGHT("Tube Light"),
        eBULB("Bulb"),
        ePLUGPOINT("Plug Point");

        private String text;

        Types(String text) {
            this.text = text;
        }

    public String getText() {
        return this.text;
    }
}
    public enum Status {
        eON, eOFF
    }
}
