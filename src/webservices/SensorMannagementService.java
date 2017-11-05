package webservices;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.fazecast.jSerialComm.SerialPort;
import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import arduino.Arduino;



@Path( "sensor" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class SensorMannagementService {
	
	@Inject
	ArduinoSensor arduinoSensor;
	
	@GET
	@Path( "/sensors" )
	public Response getSesnors() {
		return Response.ok().build();
	}
	
	@GET
	@Path( "/config/{ssid}/{pwd}/{name}" )
	public Response configure(@PathParam("ssid") String i_ssid, @PathParam("pwd") String i_pwd, @PathParam("name") String i_name ) {
		boolean res = arduinoSensor.connectToWifi(i_ssid, i_pwd);
		return res? Response.ok().build():
			        Response.serverError().entity("Sensor failed to connect to Wifi!").build();
	}
	
	@GET
	@Path( "/connect_to_wifi/{ssid}/{pwd}" )
	public Response connectToWifi(@PathParam("ssid") String i_ssid, @PathParam("pwd") String i_pwd ) {
		boolean res = arduinoSensor.connectToWifi(i_ssid, i_pwd);
		return res? Response.ok().build():
			        Response.serverError().entity("Sensor failed to connect to Wifi!").build();
	}
		
	@GET
	@Path( "/serial_ports" )
	public Response getSeriaPorts() {
		return Response.ok(arduinoSensor.getSeriaPorts()).build();
	}


}
