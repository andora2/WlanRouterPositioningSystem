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

import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import model.Groundplanimage;
import repository.GroundPlanRepositories;



@Path( "/groundplan" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class GroundPlanMaintenanceWebService extends BaseWebServices {
	
	final static String  FORMFIELD_DESCRIPTION = "description";
	final static String  FORMFIELD_FILE = "file";
	final static String  FORMFIELD_NAME = "name";
	
	@Inject
	GroundPlanRepositories repo;
	
	@GET
	@Path( "/all" )
	public Response getSesnors() {
		return Response.ok(repo.getAllGroundPlans()).build();
	}
	
	
	@POST
	@Path( "/add" )
	@Consumes("multipart/form-data")
	@Produces("multipart/form-data")
	public Response add(IMultipartBody multipartBody) {
		Groundplanimage newGroundPlan = new Groundplanimage();
		newGroundPlan.setDescription( getDescriptionFormFieldValue(multipartBody) );
		String uploadedFileName = getFileNameFromFileFormFieldByFieldName( multipartBody, FORMFIELD_FILE );
		String[] uploadedFileNameParts = uploadedFileName.split("\\."); 
		String uploadedFileNameExtension = uploadedFileNameParts.length > 0? uploadedFileNameParts[uploadedFileNameParts.length-1]: "";
		newGroundPlan.setFilename( getGroundPlanFileName(multipartBody, uploadedFileNameExtension) ); //The Name given by the user shall be the filename used to save the file on the server
		saveFormDataStreamToFile( getFormDataFieldValues(multipartBody, FORMFIELD_FILE).get(0), 
								  newGroundPlan.getFilename());
		repo.persist(newGroundPlan);
		
		return Response.ok(newGroundPlan.getFilename()).build();
		//return Response.serverError().build();
	}


	private String getGroundPlanFileName(IMultipartBody multipartBody, String uploadedFileNameExtension) {
		String fileName = getNameFormFieldValue(multipartBody);
		fileName = convertFreeTextToValidFileName(fileName);
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
	
		
}
