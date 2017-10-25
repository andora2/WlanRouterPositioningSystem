DROP TABLE WLANHEATMAPMAN.PLANINGSESSION;

DROP TABLE WLANHEATMAPMAN.GROUNDPLANIMAGE;

DROP TABLE WLANHEATMAPMAN.SENSOR;

DROP TABLE WLANHEATMAPMAN.PINGSPEED;

DROP TABLE WLANHEATMAPMAN.RSSI;

DROP SCHEMA WLANHEATMAPMAN RESTRICT;

CREATE SCHEMA WLANHEATMAPMAN AUTHORIZATION ADRIAN;

CREATE TABLE WLANHEATMAPMAN.PLANINGSESSION (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 NO CYCLE CACHE 20),
		NAME VARCHAR(300),
		STARTTIME TIMESTAMP DEFAULT CURRENT TIMESTAMP,
		DESCRIPTION VARCHAR(255),
		GROUNDPLANID VARCHAR(255)
	)
	DATA CAPTURE NONE;

CREATE UNIQUE INDEX WLANHEATMAPMAN.SESSION_GROUNDPLAN
	ON WLANHEATMAPMAN.PLANINGSESSION
	(ID		ASC,
	 GROUNDPLANID		ASC) PCTFREE 10
ALLOW REVERSE SCANS;
	
CREATE INDEX WLANHEATMAPMAN.SESSION_NAME
	ON WLANHEATMAPMAN.PLANINGSESSION
	(NAME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;
	
CREATE TABLE WLANHEATMAPMAN.GROUNDPLANIMAGE (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 NO CYCLE CACHE 20),
		NAME VARCHAR(255) NOT NULL,
		FILENAME VARCHAR(255) NOT NULL,
		SAVEDTIME TIMESTAMP not null with DEFAULT CURRENT TIMESTAMP,
		DESCRIPTION VARCHAR(255)
	)
	DATA CAPTURE NONE;

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_GROUNDPLANIMAGE_NAME
	ON WLANHEATMAPMAN.GROUNDPLANIMAGE
	(NAME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;	

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_GROUNDPLANIMAGE_FILENAME
	ON WLANHEATMAPMAN.GROUNDPLANIMAGE
	(FILENAME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;	
	
CREATE TABLE WLANHEATMAPMAN.SENSOR (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 NO CYCLE CACHE 20),
		PLANINGSESSIONID INTEGER NOT NULL,
		NAME VARCHAR(300),
		HOSTNAME VARCHAR(250),
		IPADDRESS VARCHAR(15),
		MAPPOSX INTEGER,
		MAPPOSY INTEGER,
		MAPPOSZ INTEGER,
		GEOLON DOUBLE,
		GEOLAT DOUBLE
	)
	DATA CAPTURE NONE;
	
CREATE UNIQUE INDEX WLANHEATMAPMAN.SENSOR_SESSION_NAME
	ON WLANHEATMAPMAN.SENSOR
	(PLANINGSESSIONID		ASC,
	 NAME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;

CREATE UNIQUE INDEX WLANHEATMAPMAN.SENSOR_SESSION_HOSTNAME
	ON WLANHEATMAPMAN.SENSOR
	(PLANINGSESSIONID		ASC,
	 HOSTNAME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;

CREATE UNIQUE INDEX WLANHEATMAPMAN.SENSOR_SESSION_IPADDR
	ON WLANHEATMAPMAN.SENSOR
	(PLANINGSESSIONID		ASC,
	 IPADDRESS		ASC) PCTFREE 10
ALLOW REVERSE SCANS;



CREATE TABLE WLANHEATMAPMAN.PINGSPEED (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 NO CYCLE CACHE 20),
		SENSORID INTEGER NOT NULL,
		PLANINGSESSIONID INTEGER NOT NULL,
		SAVEDTIME TIMESTAMP DEFAULT CURRENT TIMESTAMP,
		RESPONSEMSTIME INTEGER NOT NULL,
		USEDPACKETBYTESIZE INTEGER NOT NULL
	)
	DATA CAPTURE NONE;

CREATE INDEX WLANHEATMAPMAN.SENSOR_PING
	ON WLANHEATMAPMAN.PINGSPEED
	(SENSORID		ASC) PCTFREE 10
ALLOW REVERSE SCANS;

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR_SESSION_PING
	ON WLANHEATMAPMAN.PINGSPEED
	(PLANINGSESSIONID		ASC,
	  SENSORID		ASC,
	  SAVEDTIME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;



CREATE TABLE WLANHEATMAPMAN.RSSI (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 INCREMENT BY 1 MINVALUE 0 MAXVALUE 2147483647 NO CYCLE CACHE 20),
		SENSORID INTEGER NOT NULL,
		PLANINGSESSIONID INTEGER NOT NULL,
		SAVEDTIME TIMESTAMP DEFAULT CURRENT TIMESTAMP,
		RSSI INTEGER NOT NULL
	)
	DATA CAPTURE NONE;

CREATE INDEX WLANHEATMAPMAN.SENSOR_RSSI
	ON WLANHEATMAPMAN.RSSI
	(SENSORID		ASC) PCTFREE 10
ALLOW REVERSE SCANS;

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR_SESSION_RSSI
	ON WLANHEATMAPMAN.RSSI
	(PLANINGSESSIONID		ASC,
	  SENSORID		ASC,
	  SAVEDTIME		ASC) PCTFREE 10
ALLOW REVERSE SCANS;

