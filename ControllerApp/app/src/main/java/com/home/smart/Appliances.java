package com.home.smart;

public class Appliances {
    Types type;
    int status;
//    actions: only_on_off/rgb/dimmable/speed control/etc
//    data : could be device specific
    Appliances(Types type, int status) {
        this.type = type;
        this.status = status;
    }
    public enum Types {
        eFAN("Fan"),
        eTUBELIGHT("Tube Light"),
        eBULB("Bulb"),
        ePLUGPOINT("Plug Point"),
        eMAX("");

        private String text;

        Types(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        static public Types getType(int val) {
            if(val == eFAN.ordinal()) {
                return eFAN;
            } else if(val == eTUBELIGHT.ordinal()) {
                return eTUBELIGHT;
            } else if(val == eBULB.ordinal()) {
                return eBULB;
            } else if(val == ePLUGPOINT.ordinal()) {
                return ePLUGPOINT;
            }
            return eMAX;
        }
    }
    public enum Status {
        eON, eOFF
    }
}
