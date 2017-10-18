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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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



@Path( "/base" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class BaseWebServices {
	
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
	                  if( names.length > 1){
		                  formElementName = names[1].trim().replaceAll("\"", "");
		                  if ((tempName.trim().startsWith("filename"))) {
		                      fileName = formElementName;
		                  }
	                  }
	              }
	              
	              if (fileName == null) {
	            	  formElementValue = getFormDataValueFromFieldStream(stream);
	        		  System.out.println(formElementName + ":" + formElementValue);
	              } else {
	               saveFormDataStreamToFile(stream, fileName);
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
	         return Response.ok().build();
	}

	protected void saveFormDataStreamToFile(InputStream stream, String fileName) {
		File tempFile = new File(fileName);
		   try {
			   Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		   } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		   }
		   System.out.println("Saved File to:" + tempFile.toPath());
	}

	protected String getFormDataValueFromFieldStream(InputStream stream) {
		String formElementValue;
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
		 return formElementValue;
	}	
	
	protected List<InputStream> getFormDataFieldValues(IMultipartBody multipartBody, String fieldName) {
		return multipartBody.getAllAttachments().stream()
				.filter(attachment -> isFormDataAttachmentOfFieldName(attachment, fieldName) )
				.map(attachment -> getAttachmentValueStream(attachment))
				.collect(Collectors.toList());
	}

	private InputStream getAttachmentValueStream(IAttachment attachment) {
		try {
			return attachment.getDataHandler().getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; 
		}
	}

	private boolean isFormDataAttachmentOfFieldName(IAttachment attachment, String fieldName) {
        MultivaluedMap<String, String> map = attachment.getHeaders();
        String[] contentDisposition = map.getFirst("Content-Disposition").split(";");
        for (String tempName : contentDisposition) {
            String[] names = tempName.split("=");
            if( names.length > 1){
                if ((tempName.trim().startsWith("name")) && names[1].substring(1, names[1].length()-1).matches(fieldName)) {
                    return true;
                }
            }
        }		
        return false;
	}

	protected String getFormElementNameOfFormField(IMultipartBody multipartBody, String fieldName) {
		IAttachment formElementAttachement = multipartBody.getAllAttachments().stream()
				.filter(attachment -> isFormDataAttachmentOfFieldName(attachment, fieldName) ).findAny().orElse(null);
		return formElementAttachement != null? getFormElemenNameByFomFieldName( formElementAttachement, fieldName ): "";
	}	

	private String getFormElemenNameByFomFieldName(IAttachment attachment, String fieldName) {
        MultivaluedMap<String, String> map = attachment.getHeaders();
        String formElementName = null;
        String[] contentDisposition = map.getFirst("Content-Disposition").split(";");
        for (String tempName : contentDisposition) {
            String[] names = tempName.split("=");
            if( names.length > 1){
                formElementName = names[1].trim().substring(1, names[1].length()-1);
                if ((tempName.trim().startsWith("name")) && formElementName.matches(fieldName)) {
                    return formElementName;
                }
            }
        }		
        return "";
	}

	protected String getFileNameFromFileFormFieldByFieldName(IMultipartBody multipartBody, String fieldName) {
		IAttachment formElementAttachement = multipartBody.getAllAttachments().stream()
				.filter(attachment -> isFormDataAttachmentOfFieldName(attachment, fieldName) ).findAny().orElse(null);
		return formElementAttachement != null? getFileNameFromFileFormFieldByFieldName( formElementAttachement, fieldName ): "";
	}	
	
	private String getFileNameFromFileFormFieldByFieldName(IAttachment attachment, String fieldName) {
        MultivaluedMap<String, String> map = attachment.getHeaders();
        String dispElemValue = null;
        String[] contentDisposition = map.getFirst("Content-Disposition").split(";");
        boolean isSearchdFieldName = false;
        for (String dispElem : contentDisposition) {
            String[] dispElemParts = dispElem.split("=");
            if( dispElemParts.length > 1){
                dispElemValue = dispElemParts[1].trim().substring(1, dispElemParts[1].length()-1);
                if ((dispElem.trim().startsWith("name")) && dispElemValue.matches(fieldName)) {
                	isSearchdFieldName = true;
                } else if( dispElem.trim().startsWith("filename") && isSearchdFieldName) {
                	return dispElemValue;
                }
            }
        }		
        return "";
	}
	
}
