CREATE SCHEMA WLANHEATMAPMAN;

--!DROP TABLE WLANHEATMAPMAN.Sensor;
CREATE TABLE WLANHEATMAPMAN.Sensor (
  macId VARCHAR(17) NOT NULL PRIMARY KEY,
  lastIpAddress VARCHAR(15) NULL,
  name VARCHAR(300) NULL,
  locationName VARCHAR(300) NULL,
  mapPosX INT NULL,
  mapPosY INT NULL,
  mapPosZ INT NULL,
  geoLon DOUBLE NULL,
  geoLat DOUBLE NULL
--  changeTime TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
--  registerTime TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR ON WLANHEATMAPMAN.Sensor (macId ASC);

--!DROP TABLE 'WLANHEATMAPMAN.RSSI';
CREATE TABLE WLANHEATMAPMAN.RSSI (
  sensorMacId VARCHAR(15) NOT NULL PRIMARY KEY,
  savedTime TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  rssi int NOT NULL
);

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR_RSSI ON WLANHEATMAPMAN.RSSI (sensorMacId ASC);
CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR_RSSI_TIME ON WLANHEATMAPMAN.RSSI (sensorMacId ASC, savedTime ASC);

--!DROP TABLE 'WLANHEATMAPMAN.PingSpeed';
CREATE TABLE WLANHEATMAPMAN.PingSpeed (
  sensorMacId VARCHAR(15) NOT NULL PRIMARY KEY,
  savedTime TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  responseMsTime int NOT NULL,
  usedPacketByteSize int NOT NULL
);

CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR_PING ON WLANHEATMAPMAN.PingSpeed (sensorMacId ASC);
CREATE UNIQUE INDEX WLANHEATMAPMAN.UNIQUE_SENSOR_PING_TIME ON WLANHEATMAPMAN.PingSpeed (sensorMacId ASC, savedTime ASC);







-------------------------------------------------------------- EXAMPLES -------------------------------------------
--!DROP TABLE AGCAB_PROD.Customer;
CREATE TABLE AGCAB_PROD.Customer (
  idCustomer INT NOT NULL PRIMARY KEY,
  name VARCHAR(45) NULL,
  familyName VARCHAR(45) NULL,
  tel VARCHAR(25) NULL,
  mobil VARCHAR(25) NULL,
  mail VARCHAR(255) NULL,
  CNP CHAR(13) NULL,
  createTime TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX AGCAB_PROD.UNIQUE_CUSTOMER_NAME ON AGCAB_PROD.Customer  (name ASC, familyName ASC);

--!Drop table AGCAB_PROD.Offer;
CREATE TABLE AGCAB_PROD.Offer (
  idOffer INT NOT NULL PRIMARY KEY,
  amount INT NOT NULL,
  details VARCHAR(4096),
  createDate DATE NOT NULL DEFAULT CURRENT_DATE,
  Customer_idCustomer INT NOT NULL,
  CONSTRAINT fk_Offer_Customer
    FOREIGN KEY (Customer_idCustomer)
    REFERENCES AGCAB_PROD.Customer (idCustomer)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX AGCAB_PROD.fk_Offer_Customer_idx ON AGCAB_PROD.Offer (Customer_idCustomer ASC);


CREATE TABLE AGCAB_PROD.WorkType (
  idWorkType INT NOT NULL PRIMARY KEY,
  name VARCHAR(255) NULL,
  details VARCHAR(4096) NULL,
  defaultPrice INT NULL );

--!DROP TABLE AGCAB_PROD.Termin;
CREATE TABLE AGCAB_PROD.Termin (
  plannedTime TIMESTAMP NOT NULL PRIMARY KEY,
  finishTime TIMESTAMP NULL,
  realTime TIMESTAMP NULL,
  payment INT NULL,
  cancelationType CHAR(12) NULL,
  Customer_idCustomer INT NOT NULL,
  Offer_idOffer INT NOT NULL,
  WorkType_idWorkType INT NOT NULL,
  CONSTRAINT fk_Termin_Customer
    FOREIGN KEY (Customer_idCustomer)
    REFERENCES AGCAB_PROD.Customer (idCustomer)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_Termin_Offer1
    FOREIGN KEY (Offer_idOffer)
    REFERENCES AGCAB_PROD.Offer (idOffer)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_Termin_WorkType1
    FOREIGN KEY (WorkType_idWorkType)
    REFERENCES AGCAB_PROD.WorkType (idWorkType)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE INDEX AGCAB_PROD.fk_Termin_Customer_idx ON AGCAB_PROD.Termin (Customer_idCustomer ASC);
CREATE INDEX AGCAB_PROD.fk_Termin_Offer1_idx ON AGCAB_PROD.Termin (Offer_idOffer ASC);
CREATE INDEX AGCAB_PROD.fk_Termin_WorkType1_idx ON AGCAB_PROD.Termin (WorkType_idWorkType ASC);
    