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
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;



@Path( "main" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class TBD_MainWebService {
	
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

//	@GET
//	@Path( "/connect_to_serial_port/{system_port_name}" )
//	@Produces( MediaType.APPLICATION_JSON )
//	public Response connectToSeriaPort(@PathParam("system_port_name") String i_strSystemPortName) {
//		this.strSelectedComPort = i_strSystemPortName;
//		
//		return Response.ok(SerialPort.getCommPort(i_strSystemPortName)).build();
//	}
	

	
	
	@GET
	@Path("/image/{filename}")
	@Produces( "image/jpg" )
    public Response getImageByFileName(@PathParam("filename") String filename) {
		File repositoryFile = new File(filename);
        try {
        	final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge((int) TimeUnit.MINUTES.toSeconds(1)); 
            return Response.ok(new FileInputStream(repositoryFile)).cacheControl(cacheControl).build();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return Response.ok("").build();
    }	

	@GET
	@Path("/image_names")
    public Response getImageNames() {
        File tempFile = new File("test");
        String s = tempFile.getAbsolutePath();
        try {
			String s1 = tempFile.getCanonicalPath();
			String sx = s1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String s2 = tempFile.getParent();
        String s3 = tempFile.getPath();
        return Response.ok(s).build();
    }	
	
	
	@GET
	@Path("/rssi/avg/{sessionid}/{sensorid}")
    public Response getRssiAvg(@PathParam("sessionid") int sessionid, @PathParam("sensorid") int sensorid ) {
		HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("https://192.168.0.122/rssi");

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream stream = entity.getContent()) {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(stream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }		
        return Response.ok("").build();
	}
}
