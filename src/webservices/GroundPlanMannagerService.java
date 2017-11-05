package webservices;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.net.ssl.SSLEngineResult.Status;
import javax.persistence.PersistenceException;
import javax.servlet.ServletContext;
import javax.validation.ConstraintViolationException;
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

import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import model.Groundplanimage;
import repository.GroundPlanRepositories;



@Path( "/groundplan" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class GroundPlanMannagerService extends BaseService {
	
	final static String  FORMFIELD_DESCRIPTION = "description";
	final static String  FORMFIELD_FILE = "file";
	final static String  FORMFIELD_NAME = "name";

	final static String  GROUND_PLAN_IMAGE_FILE_PATH = "groundplans";
	
	@Inject
	GroundPlanRepositories repo;
	
	@GET
	@Path( "/all" )
	public Response getAll() {
		return Response.ok(repo.getAll()).build();
	}
	
	@GET
	@Path( "/get/{id}" )
	public Response get(@PathParam("id") int id) {
		return Response.ok(repo.get(id)).build();
	}
	
	@POST
	@Path( "/add" )
	@Consumes("multipart/form-data")
	//@Produces("multipart/form-data")
	public Response add(IMultipartBody multipartBody) {
		Groundplanimage newGroundPlan = new Groundplanimage();
		newGroundPlan.setName( getNameFormFieldValue(multipartBody) );
		newGroundPlan.setDescription( getDescriptionFormFieldValue(multipartBody) );
		String uploadedFileName = getFileNameFromFileFormFieldByFieldName( multipartBody, FORMFIELD_FILE );
		String[] uploadedFileNameParts = uploadedFileName.split("\\."); 
		String uploadedFileNameExtension = uploadedFileNameParts.length > 0? uploadedFileNameParts[uploadedFileNameParts.length-1]: "";
		newGroundPlan.setFilename( getGroundPlanFileName(newGroundPlan.getName(), uploadedFileNameExtension) ); //The Name given by the user shall be the filename used to save the file on the server
		InputStream fileStream = getFormDataFieldValues(multipartBody, FORMFIELD_FILE).get(0);
		saveFormDataStreamToFile( fileStream, newGroundPlan.getFilename(), GROUND_PLAN_IMAGE_FILE_PATH );
		/*try {
			BufferedImage img = createResizedCopy(ImageIO.read(fileStream), 50, 50, false);
			ImageIO.write(img, uploadedFileNameExtension, new File( getGroundPlanFileName(newGroundPlan.getName(), "thmb."+uploadedFileNameExtension)) );
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			saveFormDataStreamToFile( fileStream, getGroundPlanFileName(newGroundPlan.getName(), "thmb."+uploadedFileNameExtension));
		}*/
		
		try{
			repo.persist(newGroundPlan);
			repo.detach(newGroundPlan);
			return Response.ok(newGroundPlan).build();
		} catch (  PersistenceException e){
			Throwable cause = e.getCause();
		    while (cause != null) {
		        if (cause instanceof SQLIntegrityConstraintViolationException) {
		        	return Response.status(Response.Status.BAD_REQUEST).entity("GroundPlan with Name: '" + newGroundPlan.getName() + "' already exists").build();
		        }
		        cause = cause.getCause();
		    }			
		} catch ( Exception e ) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Unhandled exception. GroundPlan couldn't be added!").build();
		}
	
		return Response.serverError().entity("Unhandled exception. GroundPlan couldn't be added!").build();
	}

	@GET
	@Path("/image/{filename}")
	@Produces( "image/jpg" )
    public Response getImageByFileName(@PathParam("filename") String filename) {
		return super.getImageByFileName(filename, GROUND_PLAN_IMAGE_FILE_PATH);
    }	
	

	private String getGroundPlanFileName(String newGroundPlanName, String uploadedFileNameExtension) {
		String fileName = convertFreeTextToValidFileName(newGroundPlanName);
		return !uploadedFileNameExtension.isEmpty()? fileName + "." + uploadedFileNameExtension: fileName; 
  	}


	private String getFileExtensionOfFileFormField(IMultipartBody multipartBody) {
		String formElementName = getFormElementNameOfFormField(multipartBody, FORMFIELD_FILE);
		if( !formElementName.isEmpty() ){
			String[] nameParts = formElementName.split("."); 
			return nameParts.length > 0? nameParts[nameParts.length-1]: "";
		}
		return "";
	}


	private String convertFreeTextToValidFileName(String fileName) {
		fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_").trim().toLowerCase();
		return fileName.length() > 250? fileName.substring(0,250): fileName;
	}


	private String getNameFormFieldValue(IMultipartBody multipartBody) {
		return getFormDataValueFromFieldStream( getFormDataFieldValues(multipartBody, FORMFIELD_NAME).get(0) );
	}


	private String getDescriptionFormFieldValue(IMultipartBody multipartBody) {
		return getFormDataValueFromFieldStream( getFormDataFieldValues(multipartBody, FORMFIELD_DESCRIPTION).get(0) );
	}
	
	BufferedImage createResizedCopy(Image originalImage, 
            int scaledWidth, int scaledHeight, 
            boolean preserveAlpha)
    {
        System.out.println("resizing...");
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
        g.dispose();
        return scaledBI;
    }	
		
}
