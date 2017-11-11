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
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.ibm.websphere.jaxrs20.multipart.IAttachment;
import com.ibm.websphere.jaxrs20.multipart.IMultipartBody;

import model.Groundplanimage;
import model.Planingsession;
import repository.GroundPlanRepositories;
import repository.PlaningSessionRepositories;



@Path( "/planing_session" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class PlaningSessionWebService extends BaseService {
	
	@Inject
	PlaningSessionRepositories sessionRepo;

	@Inject
	GroundPlanRepositories groundPlanRepo;
	
	@GET
	@Path( "/" )
	public Response getAll() {
		return Response.ok(sessionRepo.getAll()).build();
	}
	
	@GET
	@Path( "/latest" )
	public Response getLatest() {
		return Response.ok(sessionRepo.getLatest()).build();
	}

	@GET
	@Path( "/sensors/{planing_session_id}" )
	public Response get(@PathParam("planing_session_id") int id) {
		return Response.ok(sessionRepo.get(id).getSensors()).build();
	}
	
	@GET
	@Path( "/add/{name}/{groundplanid}/{description}" )
	public Response add(@PathParam("name") String name, @PathParam("groundplanid") int groundPlanId, @PathParam("description") String description ) {
		Groundplanimage groundPlan = groundPlanRepo.get(groundPlanId);
		Planingsession newSession = new Planingsession();
		newSession.setName(name);
		newSession.setStarttime(new Timestamp((new Date()).getTime()));
		newSession.setGroundplanimage(groundPlan);
		newSession.setDescription(description);
		return persistNewSession(newSession);
	}

	
	private Response persistNewSession(Planingsession newSession) {
		try{
			sessionRepo.persist(newSession);
			
			return Response.ok(newSession).build();
		} catch (  PersistenceException e){
			Throwable cause = e.getCause();
		    while (cause != null) {
		        if (cause instanceof SQLIntegrityConstraintViolationException) {
		        	return Response.status(Response.Status.BAD_REQUEST).entity("Session with Name: '" + newSession.getName() + "' already exists").build();
		        }
		        cause = cause.getCause();
		    }			
		} catch ( Exception e ) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Unhandled exception. Session couldn't be added!").build();
		}
	
		return Response.serverError().entity("Unhandled exception. Session couldn't be added!").build();
	}
		
}
