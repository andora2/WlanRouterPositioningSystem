/*
 * Copyright (c) 2015, Majenko Technologies
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * * Neither the name of Majenko Technologies nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

#include <Wire.h>
#include <stdint.h>

//const char *ssid = "WifiApPosSysSrv";
//const char *password = "E892m761";
const char *g_hostname = "sensor3.wlanappossys";
const char *ssid = "WLAN-130A45";
const char *password = "6278996347999852";

//const char *ssid = "MasterOfDisaster";
//const char *password = "AVIYrJNBj5CXmP1Mt";
//const char *ssid = "Gast_bei_Uns";
//const char *password = "musafiri";


#define EOL        '\n'
#define EOCMD     ';'
#define CMD_NAME_DELIM  ':'
#define PARAM_DELIM   ','
#define DEFAULT_DELAY    50
#define MAX_LENGTH_SERIAL_LINE 125 //longest expected cmd DELTA_XY x 5 => 25chars x 5 = 125chars  

#define SET_HOST_NAME_CMD  "SET_HOST_NAME" // sets the wifi ssid
#define SET_WIFI_SSID_CMD  "SET_WIFISSID" // sets the wifi ssid
#define SET_WIFI_PWD_CMD  "SET_WIFIPWD" // sets the wifi pwd
#define WIFI_CONNECT_CMD "WIFI_CONNECT_CMD" // sets the wifi pwd
#define IS_CONNECTED "IS_CONNECTED" // sets the wifi pwd
#define GET_IP "GET_IP" // sets the wifi pwd

bool fullPrint = true;
bool fullStop = false;
char g_LATEST_CHAR_FROM_SERIAL = 0;

ESP8266WebServer server ( 80 );

const int led = 13;
#define MAX_RSSI_VALUES 40
int rssi[MAX_RSSI_VALUES];
int rssi2[MAX_RSSI_VALUES];
int rssi_idx = 0;
int avgRSSI = 0;
bool rssi_saved = false;


void handleRoot() {
	digitalWrite ( led, 1 );
	char temp[400];
	int sec = millis() / 1000;
	int min = sec / 60;
	int hr = min / 60;

	snprintf ( temp, 400,

"<html>\
  <head>\
    <meta http-equiv='refresh' content='0'/>\
    <title>ESP8266 Demo</title>\
    <style>\
      body { background-color: #cccccc; font-family: Arial, Helvetica, Sans-Serif; Color: #000088; }\
    </style>\
  </head>\
  <body>\
    <h1>Hello from ESP8266!</h1>\
    <p>Uptime: %02d:%02d:%02d</p>\
    <img src=\"/test.svg\" />\
  </body>\
</html>",

		hr, min % 60, sec % 60
	);
	server.send ( 200, "text/html", temp );
	digitalWrite ( led, 0 );
}

void handleNotFound() {
	digitalWrite ( led, 1 );
	String message = "File Not Found\n\n";
	message += "URI: ";
	message += server.uri();
	message += "\nMethod: ";
	message += ( server.method() == HTTP_GET ) ? "GET" : "POST";
	message += "\nArguments: ";
	message += server.args();
	message += "\n";

	for ( uint8_t i = 0; i < server.args(); i++ ) {
		message += " " + server.argName ( i ) + ": " + server.arg ( i ) + "\n";
	}

	server.send ( 404, "text/plain", message );
	digitalWrite ( led, 0 );
}

void serial_log_verbose_ln(String msg) {
  if (fullPrint) { 
    Serial.println ( "HTTP server started" ); 
  }
}

void serial_log_verbose(String msg) {
  if (fullPrint) { 
    Serial.println ( "HTTP server started" ); 
  }
}

void initWifi(const char* i_ssid, const char* i_pwd, const char* i_host){
  Serial.print ( "Disconnecting Wifi if connected." );
  if ( WiFi.status() == WL_CONNECTED ) {
    WiFi.disconnect();
  }
  while ( WiFi.status() == WL_CONNECTED ) {
    delay ( 500 );
    Serial.print ( "." );
  }

  WiFi.hostname(i_host);
  Serial.print ( "Sensor HOST set to: " );
  Serial.println ( i_host );

  Serial.println ( "" );
  Serial.print ( "Trying to connect to: " );
  Serial.println ( i_ssid );
  WiFi.begin ( i_ssid, i_pwd );
  Serial.println ( "" );

  // Wait for connection
  int count = 0;
  WiFi.mode(WIFI_STA);
  while ( WiFi.status() != WL_CONNECTED ) {
    delay ( 500 );
    Serial.print ( "." );
    count++;
    if(count%64 == 0){ //try to reconect afer 8 sec
      Serial.print ( "Maybe failed to connect!" );
      break;
/*      WiFi.begin ( i_ssid, i_pwd );
      Serial.println ( "" );
      Serial.print ( "Trying to reconnect connect to: " );
      Serial.println ( i_ssid );
      WiFi.begin ( i_ssid, i_pwd );
*/
    }
  }

  for (int rssi_idx = 0; rssi_idx < MAX_RSSI_VALUES; rssi_idx++) {
    rssi[rssi_idx] = (-1 * WiFi.RSSI());//(((-1 * WiFi.RSSI()) % 100)*3)%140;
    delay ( 50 );
  }

  Serial.println ( "" );
  Serial.print ( "Connected to " );
  Serial.println ( i_ssid );
  Serial.print ( "IP address: " );
  Serial.println ( WiFi.localIP() );

}

void restGetAvgRSSI(){
  char resAvgRSSI[5];
  sprintf(resAvgRSSI,"%03d",avgRSSI);
  server.send ( 200, "text/plain", resAvgRSSI);
}

void restGetCurrentRSSI(){
  char resRSSI[5];
  sprintf(resRSSI,"%03d",WiFi.RSSI());
  server.send ( 200, "text/plain", resRSSI);
}

void drawGraph() {
	String out = "";
	char temp[100];
	out += "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"400\" height=\"150\">\n";
 	out += "<rect width=\"400\" height=\"150\" fill=\"rgb(250, 230, 210)\" stroke-width=\"1\" stroke=\"rgb(0, 0, 0)\" />\n";
 	out += "<g stroke=\"black\">\n";
 	//int y = rand() % 130;
  int y = rssi[0];
  int yIdx = 0;
 	for (int x = 10 ; x < 390; x+= 10) {
 		//int y2 = rand() % 130;
    yIdx++;
    yIdx = yIdx<MAX_RSSI_VALUES? yIdx: MAX_RSSI_VALUES-1;
    int y2 = rssi[yIdx];

      Serial.print ( "y2=RSSI[");
      Serial.print ( yIdx );
      Serial.print ( "]: " );
      Serial.println ( y2 );
    
 		sprintf(temp, "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke-width=\"1\" />\n", x, y, x + 10, y2);
 		out += temp;
 		y = y2;
 	}
	out += "</g>\n</svg>\n";

	server.send ( 200, "image/svg+xml", out);
}


bool isEOLDelimiter(char i_nDelim){
  return i_nDelim == EOL;
}

bool isEOCmdDelimiter(char i_nDelim){
  return i_nDelim == EOCMD;
}

bool isEOCmdNameDelimiter(char i_nDelim){
  return i_nDelim == CMD_NAME_DELIM;
}

bool isParamDelimiter(char i_nDelim){
  return i_nDelim == PARAM_DELIM;
}

bool isEOParam(char i_nDelim){
  return isEOCmdDelimiter(i_nDelim) || isEOLine(i_nDelim) || isParamDelimiter(i_nDelim);
}

bool isEOCmdName(char i_nDelim){
  return isEOCmdDelimiter(i_nDelim) || isEOLine(i_nDelim) || isEOCmdNameDelimiter(i_nDelim);
}

bool isEOLine(char i_nDelim){
  return isEOLDelimiter(i_nDelim);
}

bool isEOCmd(char i_nDelim){
  return isEOCmdDelimiter(i_nDelim);
}

String readSerialCommandLine(){
  String strCmdLine = "";
  while (Serial.available() ){   //avoid buffer overflow
      strCmdLine = Serial.readStringUntil(EOL);
  } 
  
  strCmdLine.trim();
  if (strCmdLine.length() > 0){
    Serial.print(F("Received LINE input from serial: '")); Serial.print(strCmdLine); Serial.print("' -> ");Serial.println(strCmdLine.length()); 
  }

  return isEOCmdName(strCmdLine.charAt(strCmdLine.length()-1)) ? strCmdLine : "";
}

String readSerialCommandLine_bloed(){
  String strCmdLine = "";
  char inChar = 0;
  while (Serial.available() && //stop reading if there nothing left to read
       !isEOLine(inChar) &&     //detect End Of Line
       strCmdLine.length() <= MAX_LENGTH_SERIAL_LINE ){   //avoid buffer overflow
      inChar = (char)Serial.read();
      strCmdLine += inChar;
  } 
  
  strCmdLine.trim();
  if (strCmdLine.length() > 0){
    Serial.print(F("Received LINE input from serial: '")); Serial.print(strCmdLine); Serial.print("' -> ");Serial.println(strCmdLine.length()); 
  }

  return isEOCmdName(inChar) ? strCmdLine : "";
}

bool doesCmdNameContainDelimiters(String i_strCmdName){
  return (i_strCmdName.indexOf(EOL) > -1) ||
    (i_strCmdName.indexOf(EOCMD) > -1) ||
    (i_strCmdName.indexOf(PARAM_DELIM) > -1) ||
    (i_strCmdName.indexOf(CMD_NAME_DELIM) > -1);
}

int getFirstValidCmdNameDelimIdx(String& ir_strCmdLine){
  int idxCmdNameDelim = ir_strCmdLine.indexOf(CMD_NAME_DELIM);
  int idxEOCmd = ir_strCmdLine.indexOf(EOCMD);
  int idxEOL = ir_strCmdLine.indexOf(EOL);

  idxCmdNameDelim = idxCmdNameDelim  > -1 ? idxCmdNameDelim : 32767;//INT16_MAX;
  idxEOCmd = idxEOCmd  > -1 ? idxEOCmd : 32767;//INT16_MAX;
  idxEOL = idxEOL  > -1 ? idxEOL : 32767;//INT16_MAX;

  int idxRes = idxEOCmd < idxEOL? idxEOCmd: idxEOL;
  return idxCmdNameDelim < idxRes? idxCmdNameDelim: idxRes;
}

bool isNotCorruptedCmdName(String i_strCmdName){
  return !doesCmdNameContainDelimiters(i_strCmdName);
}

String pullCmdNameFromCmdLine(String& ir_strCmdLine){
  if (fullPrint) { Serial.print(F("ENTER pullCmdNameFromCmdLine( '")); Serial.print(ir_strCmdLine); Serial.println("' )"); }
  String strCmdName = ""; 
  int idxEOCmdName = getFirstValidCmdNameDelimIdx(ir_strCmdLine);
  if (fullPrint) { Serial.print(F("\tidxEOCmdName: ")); Serial.println(idxEOCmdName); }
  if (idxEOCmdName > -1                         //Delimiter for CmdName detected?
    && isNotCorruptedCmdName(ir_strCmdLine.substring(0, idxEOCmdName))  //could be that the serial read missed some bytes so we started in the middle of a cmd.
     ){ 
    strCmdName = ir_strCmdLine.substring(0, idxEOCmdName);
    if (fullPrint) { Serial.print(F("\tir_strCmdLine.substring(0, idxEOCmdName); => : '")); Serial.print(strCmdName); Serial.println("' )"); }
    ir_strCmdLine.remove(0, strCmdName.length()+1);
    if (fullPrint) { Serial.print(F("\tir_strCmdLine.remove(0, strCmdName.length()+1); => : '")); Serial.print(ir_strCmdLine); Serial.println("' )"); }
  }
  else {
    Serial.print(F("Failed to identify Cmd Name in Received LINE input from serial: '")); Serial.print(ir_strCmdLine); Serial.println("'");
  }
  return strCmdName;
}

bool isNotCorruptedParamValue(String i_strParamValue){
  return !doesCmdNameContainDelimiters(i_strParamValue);
}

int getFirstValidParamDelimIdx(String& ir_strCmdLine){
  int idxParamDelim = ir_strCmdLine.indexOf(PARAM_DELIM);
  int idxEOCmd = ir_strCmdLine.indexOf(EOCMD);
  int idxEOL = ir_strCmdLine.indexOf(EOL);

  idxParamDelim = idxParamDelim > -1 ? idxParamDelim : 32767;//INT16_MAX;
  idxEOCmd = idxEOCmd  > -1 ? idxEOCmd : 32767;//INT16_MAX;
  idxEOL = idxEOL  > -1 ? idxEOL : 32767;//INT16_MAX;

  int idxRes = idxEOCmd < idxEOL? idxEOCmd: idxEOL;
  return idxParamDelim < idxRes? idxParamDelim: idxRes;
}

String pullParamValueFromCmdLine(String& ir_strCmdLine){
  if (fullPrint) { Serial.print(F("ENTER pullParamValueFromCmdLine( '")); Serial.print(ir_strCmdLine); Serial.println("' )"); }
  String strParamValue = "";
  int idxParamDelim = getFirstValidParamDelimIdx(ir_strCmdLine);
  if (fullPrint) { Serial.print(F("\tidxParamDelim: ")); Serial.println(idxParamDelim); }
  if (idxParamDelim > -1                          //Delimiter for CmdName detected?
    && isNotCorruptedCmdName(ir_strCmdLine.substring(0, idxParamDelim))  //could be that the serial read missed some bytes so we started in the middle of a cmd.
    ){
    strParamValue = ir_strCmdLine.substring(0, idxParamDelim);
    if (fullPrint) { Serial.print(F("\tir_strCmdLine.substring(0, idxParamDelim); => : '")); Serial.print(strParamValue); Serial.println("' )"); }

    ir_strCmdLine.remove(0, strParamValue.length()+1);
    if (fullPrint) { Serial.print(F("\tir_strCmdLine.remove(0, strParamValue.length()+1); => : '")); Serial.print(ir_strCmdLine); Serial.println("' )"); }
  }
  return strParamValue;
}

void doConnectCmd(String& ir_strCmdLine){
  if (fullPrint) { Serial.print(F("ENTER doConnect( '")); Serial.print(ir_strCmdLine); Serial.println("' )"); }
  String ssid = pullParamValueFromCmdLine(ir_strCmdLine);
  String pwd = pullParamValueFromCmdLine(ir_strCmdLine);
  String hostname = pullParamValueFromCmdLine(ir_strCmdLine);
  if (ssid.length() > 0){
    initWifi(ssid.c_str(), pwd.c_str(),hostname.c_str());
  }
}

void doSetHostNameCmd(String& ir_strCmdLine){
  if (fullPrint) { Serial.print(F("ENTER setHostName( '")); Serial.print(ir_strCmdLine); Serial.println("' )"); }
  String hostname = pullParamValueFromCmdLine(ir_strCmdLine);
  if (hostname.length() > 0){
    WiFi.hostname(hostname.c_str());
  }
}

void doGetIPCmd(){
 if(WiFi.isConnected()){  
  Serial.print(WiFi.localIP());
 }
}

void doIsConnectedCmd(){
 Serial.print(WiFi.isConnected());
}

void execueCommands(String i_strCmdLine){
  if (fullPrint) { Serial.print(F("ENTER execueCommands( '")); Serial.print(i_strCmdLine); Serial.println("' )"); }

  String strCmd = pullCmdNameFromCmdLine(i_strCmdLine);

  while(strCmd.length() > 0) {
    strCmd.toUpperCase();
    Serial.print(F("Received command: '")); Serial.print(strCmd); Serial.println("'");

    if (strCmd.startsWith(WIFI_CONNECT_CMD)) {
      doConnectCmd(i_strCmdLine);
    } else if (strCmd.startsWith(GET_IP)) {
      doGetIPCmd();
    } else if (strCmd.startsWith(IS_CONNECTED)) {
      doIsConnectedCmd();
    } else if (strCmd.startsWith(SET_HOST_NAME_CMD)) {
      doSetHostNameCmd(i_strCmdLine);
    }

    strCmd = pullCmdNameFromCmdLine(i_strCmdLine);
  }
}

void executeSerialLine(){
  //if (fullPrint) { Serial.println(F("ENTER executeSerialLine()")); }
  String strInputLine = readSerialCommandLine();
  if (strInputLine.length() > 1){
    execueCommands(strInputLine);
  }
}


void scanAvailableWifis(){
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(100);

  Serial.println("scan start");
  // WiFi.scanNetworks will return the number of networks found
  int n = WiFi.scanNetworks();
  Serial.println("scan done");
  if (n == 0)
    Serial.println("no networks found");
  else
  {
    Serial.print(n);
    Serial.println(" networks found");
    for (int i = 0; i < n; ++i)
    {
      // Print SSID and RSSI for each network found
      Serial.print(i + 1);
      Serial.print(": ");
      Serial.print(WiFi.SSID(i));
      Serial.print(" (");
      Serial.print(WiFi.RSSI(i));
      Serial.print(")");
      Serial.println((WiFi.encryptionType(i) == ENC_TYPE_NONE)?" ":"*");
      delay(10);
    }
  }
  Serial.println("");
}
/*******************************************************************/
/*******************************************************************/
/*              MAIN                   */
/*******************************************************************/
void setup ( void ) {
  pinMode ( led, OUTPUT );
  digitalWrite ( led, 0 );
  Serial.begin ( 115200 );

  initWifi(ssid, password, g_hostname);

  if ( MDNS.begin ( "esp8266" ) ) {
    if (fullPrint) { Serial.println ( "MDNS responder started" ); }
  }

  server.on ( "/", handleRoot );
  server.on ( "/avg_rssi", restGetAvgRSSI );
  server.on ( "/rssi", restGetCurrentRSSI );
  server.on ( "/test.svg", drawGraph );
  server.on ( "/are_you_a_wifi_sensor", []() {
    server.send ( 200, "text/plain", "yes" );
  } );

  server.on ( "/inline", []() {
    server.send ( 200, "text/plain", "this works as well" );
  } );
  server.onNotFound ( handleNotFound );
  server.begin();
  if (fullPrint) { Serial.println ( "HTTP server started" );}
}

void loop ( void ) {
  server.handleClient();
  executeSerialLine();

  if( (millis() / 500) % 2 == 0 ){;
    if(!rssi_saved){
      avgRSSI = 0;
      rssi_saved = true;
      //rssi_idx++;
      //rssi_idx = rssi_idx < MAX_RSSI_VALUES? rssi_idx: 0;
      //rssi[rssi_idx] = (((-1 * WiFi.RSSI()) % 100)*3)%140;
      rssi2[0] = (-1 * WiFi.RSSI()); //(((-1 * WiFi.RSSI()) % 100)*3)%140;
      for (int i = 0 ; i < MAX_RSSI_VALUES-1; i++) {
        rssi2[i+1] = rssi[i];
      }
      for (int i = 0 ; i < MAX_RSSI_VALUES; i++) {
        rssi[i] = rssi2[i];
        avgRSSI += rssi[i];
      }
      avgRSSI = avgRSSI / MAX_RSSI_VALUES;
      
      Serial.print ( "RSSI[");
      //Serial.print ( rssi_idx );
      Serial.print ( 0 );
      Serial.print ( "]: " );
      Serial.println ( rssi[0] );
      Serial.print ( "AVG_RSSI: " );
      Serial.println ( avgRSSI );
      Serial.print ( "IP address: " );
      Serial.println ( WiFi.localIP() );
    }
  } else {
    rssi_saved=false;
  }
}
