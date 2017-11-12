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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
import model.Sensor;
import repository.PlaningSessionRepositories;
import repository.SensorRepositories;



@Path( "sensor" )
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class SensorMannagementService {
	
	@Inject
	ArduinoSensor arduinoSensor;

	@Inject
	SensorRepositories sensorRepo;

	@Inject
	PlaningSessionRepositories planingSessionRepo;
	
	@GET
	@Path( "/sensors" )
	public Response getSesnors() {
		return Response.ok().build();
	}
	
	@GET
	@Path( "/add/{sessionid}/{ssid}/{pwd}/{name}/{lon}/{lat}" )
	public Response configure(@PathParam("sessionid") int i_sessionId,
							  @PathParam("ssid") String i_ssid, 
							  @PathParam("pwd") String i_pwd, 
							  @PathParam("name") String i_name,
							  @PathParam("lon") double i_lon,
							  @PathParam("lat") double i_lat) {
		Response res = Response.serverError().entity("Failed to add new Sensor for given parameters!").build();
		String resIP = arduinoSensor.connectToWifi(i_ssid, i_pwd, i_name);
		try {
			//boolean reachble = Inet4Address.getByName(resIP).isReachable(2000);
			if(!resIP.isEmpty() /*&& Inet4Address.getByName(resIP).isReachable(2000)*/){
				Sensor newSensor = new Sensor();
				newSensor.setPlaningsession( planingSessionRepo.get(i_sessionId) );
				newSensor.setName( i_name );
				newSensor.setIpaddress(resIP);
				newSensor.setGeolat(i_lat);
				newSensor.setGeolon(i_lon);
				sensorRepo.persist(newSensor);
				sensorRepo.detach(newSensor);
				res = Response.ok(newSensor).build();	
			}
		} catch (Exception/*IOException*/ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res = Response.serverError().entity("Received Sensor IP('" + resIP + "') is not reachable!").build();
		}
		return res;
	}
	
	@GET
	@Path( "/connect_to_wifi/{ssid}/{pwd}" )
	public Response connectToWifi(@PathParam("ssid") String i_ssid, @PathParam("pwd") String i_pwd ) {
		String resIP = arduinoSensor.connectToWifi(i_ssid, i_pwd,"");
		return !resIP.isEmpty()? Response.ok().build():
			        Response.serverError().entity("Sensor failed to connect to Wifi!").build();
	}
		
	@GET
	@Path( "/serial_ports" )
	public Response getSeriaPorts() {
		return Response.ok(arduinoSensor.getSeriaPorts()).build();
	}


	@GET
	@Path( "/ssid_list" )
	public Response getAvailableSSIDList(){
		ArrayList<String>ssids=new ArrayList<String>();
		ProcessBuilder builder = new ProcessBuilder(
		        "cmd.exe", "/c", "netsh wlan show networks");
		builder.redirectErrorStream(true);
		Process p;
		try {
			p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line="";
			while (line!=null) {
			    line = r.readLine();
			    if (line!=null && line.startsWith("SSID") ){
			    	String[] ssidLine = line.split(":");
		            if(ssidLine.length == 2 && !ssidLine[1].trim().isEmpty())
		            {
		                ssids.add(ssidLine[1].trim());
		            }
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i=1;i<ssids.size();i++)
		{
		    System.out.println("SSID name == "+ssids.get(i) );
		}
		return Response.ok(ssids).build();
		
	}
}
