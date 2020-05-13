#include <EEPROM.h>
#include <SoftwareSerial.h>
/*
 * Neha Mittal
 * Relay will be connected to each switch on the switch board
 * A phone will be used for User interface
 * Arduino is interfacing to the phone over bluetooth
 */

#ifdef EEPROM_ENABLED
uint8_t EEPROMaddress = 10;
#else
String save_config = "";
#endif

 enum ctrl_commands 
 {
  e_GETSTATE,
  e_SETSTATE,
  e_CTRLSWITCH
 };

 enum appliance_types
 {
  e_FAN,
  e_TUBELIGHT,
  e_BULB,
  e_PLUGPOINT
 };

 int BT_RXPIN = 2;
 int BT_TXPIN = 3;

 int board_state = 0;
 int num_relays = 4;
 int RELAYPIN = 4;

  SoftwareSerial btSerial(BT_RXPIN, BT_TXPIN);

 /*-------------------FUNCTIONS------------------------*/
 void write_config(const String &config_str);
 
 String read_config();

 void control_appliance(String &msg);
 /*----------------------------------------------------*/
 
void setup() {
  Serial.begin(9600);
  

  btSerial.begin(9600);
  Serial.println("Waking Up");
  
  //TODO: Read the number of relays from the config saved in memory
  for (int i = 0; i < num_relays; i++) {
    pinMode(RELAYPIN + i, OUTPUT);
  }
}

void loop() {
  String msg = "";
  int cmd = -1;
  if( !btSerial.available())
  {
    return;
  }
  cmd = btSerial.read();
  Serial.print("Rcvd Command: ");
  Serial.println (cmd);

  switch (cmd) {
    case e_GETSTATE:
    {
      Serial.println("e_GETSTATE");
      
      if( board_state == 0 ) {
        msg = read_config();
      }
      Serial.print("Read the board configuration ");
      Serial.print(msg);
      int len = msg.length();
      byte msg_ch[len+1] = {0};
      msg.getBytes(msg_ch, len+1);
      
      Serial.print("Sending bytes..");
      Serial.println(len);
      for(int i = 0; i < len; i++)
      {
        btSerial.write(msg_ch[i]);
//        Serial.print(msg_ch[i]);
      }
      break;
    }
    case e_SETSTATE:
    {
      byte data[200] = {0};
      byte val;
      int ptr = 0;
      int wait = 0;
      Serial.println("e_SETSTATE");
      
      while(btSerial.available() < 1 && (wait < 10)) {
        delay(1000);
//        Serial.println("Waiting for data");
        wait++;
      }
      
      while(btSerial.available() > 0) {
        val = btSerial.read();
        data[ptr++] = val;
//        print("Read: %c", val);
      }
      data[ptr] = '\0';
      String msg = String((char *)data);
//      Serial.println(msg);
      
      write_config(msg);
      break;
    }
    case e_CTRLSWITCH:
    {
      byte data[200] = {0};
      byte val;
      int ptr = 0;
      int wait = 0;
      Serial.print("e_CTRLSWITCH");
      while(btSerial.available() < 1 && (wait < 10)) {
        delay(1000);
//        Serial.println("Waiting for data");
        wait++;
      }
      while(btSerial.available() > 0) {
        val = btSerial.read();
        data[ptr++] = val;
      }
      data[ptr] = '\0';
      String msg = String((char *)data);

      Serial.println(msg);
      control_appliance(msg);
      break;
    }
  }

}

String read_config()
{
  //Read values form the EEPROM
  #ifdef EEPROM_ENABLED
  Serial.print("read_EEPROM at address");
  Serial.println(EEPROMaddress);
  int len = EEPROM.read(EEPROMaddress);
  char data[len+1] = {0};
  int i = 0;

  for(i = 0; i < len; i++)
  {
    data[i] = EEPROM.read(EEPROMaddress + 1 + i);
  }
  data[i] = '\0';
  
  return(String(data));
  #else
  return(save_config);
  #endif
}

void write_config(const String &config_str)
{
  byte len = config_str.length();
  
  #ifdef EEPROM_ENABLED
  EEPROM.write(EEPROMaddress, len);

  for(int i = 0; i < len; i++)
  {
    EEPROM.write(EEPROMaddress + 1 + i, config_str[i]);
  }
  #else
  save_config = config_str;
  #endif
  
}

void control_appliance(String &msg)
{
  char buff[20];
  int appliance_idx, state;
  
  msg.toCharArray(buff, sizeof(buff));

  char *p = buff;
  char *str;
  str = strtok_r(p, ":", &p);

  if(str != NULL)
  {
    appliance_idx = int(str[0]);
    Serial.println(str);
  }
    
  str = strtok_r(p, ":", &p);
Serial.println(str);
  if(p != NULL)
  {
    Serial.println(p);
    state = int(p);
  }

  Serial.print("Appliance Idx : ");
  Serial.println(appliance_idx);

  Serial.print("Applliance state : ");
  Serial.println(state);


  if(0 == state)
    digitalWrite(RELAYPIN + appliance_idx, LOW);
  else if(1 == state)
    digitalWrite(RELAYPIN + appliance_idx, HIGH);
}
