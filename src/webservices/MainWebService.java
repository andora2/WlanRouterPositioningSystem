package webservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
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
public class MainWebService {
	
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
	
	@POST
	@Path( "/upload_image" )
	@Consumes("multipart/form-data")
	@Produces("multipart/form-data")
	public Response postFormData(IMultipartBody multipartBody) {
	  List <IAttachment> attachments = multipartBody.getAllAttachments();
	         String formElementValue = null; 
	         InputStream stream = null;
	         for (Iterator<IAttachment> it = attachments.iterator(); it.hasNext();) {
	              IAttachment attachment = it.next();
	              if (attachment == null) {
	                  continue;
	              }
	              DataHandler dataHandler = attachment.getDataHandler();
	              try {
					stream = dataHandler.getInputStream();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	              MultivaluedMap<String, String> map = attachment.getHeaders();
	              String fileName = null;
	              String formElementName = null;
	              String[] contentDisposition = map.getFirst("Content-Disposition").split(";");
	              for (String tempName : contentDisposition) {
	                  String[] names = tempName.split("=");
	                  formElementName = names[1].trim().replaceAll("\"", "");
	                  if ((tempName.trim().startsWith("filename"))) {
	                      fileName = formElementName;
	                  }
	              }
	              if (fileName == null) {
	                  StringBuffer sb = new StringBuffer();
	                  BufferedReader br = new BufferedReader(new InputStreamReader(stream));
	                  String line = null;
	                  try {
	                      while ((line = br.readLine()) != null) {
	                          sb.append(line);
	                      }
	                  } catch (IOException e) {
	                      e.printStackTrace();
	                  } finally {
	                      if (br != null) {
	                          try {
	                              br.close();
	                          } catch (IOException e) {
	                              e.printStackTrace();
	                          }
	                      }
	                  }
	                  formElementValue = sb.toString();
	                  System.out.println(formElementName + ":" + formElementValue);
	              } else {
	               //handle the file as you want
	               File tempFile = new File(fileName);
	              }
	         }
	         if (stream != null) {
	             try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	         }
	         return Response.ok("test").build();
	}
}
