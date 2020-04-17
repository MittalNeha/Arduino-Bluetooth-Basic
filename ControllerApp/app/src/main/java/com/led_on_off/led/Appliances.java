package com.led_on_off.led;

public class Appliances {
    int type;
    int status;
//    actions: only_on_off/rgb/dimmable/speed control/etc
//    data : could be device specific
    public enum Types{
        eFAN,
    eTUBELIGHT,
    eBULB,
    ePLUGPOINT
}
    public enum Status {
        eON, eOFF
    }
}
