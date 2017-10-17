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

import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;



@Path( "main" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class SensorMaintenanceWebService {
	
	@GET
	@Path( "/add_sensor/{id}" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response move02(@PathParam("id") String sensorId) {
		return Response.ok().build();
		//return Response.serverError().build();
	}
	
		
	@GET
	@Path( "/sensors" )
	@Produces( MediaType.APPLICATION_JSON )
	public Response getSesnors() {
		return Response.ok().build();
	}

}
