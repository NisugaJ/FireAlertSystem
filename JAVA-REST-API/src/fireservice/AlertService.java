package fireservice;

import static com.mongodb.client.model.Filters.eq;

import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.json.JsonObject;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.bson.types.ObjectId;

import models.Alert;
import utils.MongoDatabaseConn;

import com.google.gson.*;
import com.mongodb.MongoSocketException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

@Path("/alerts")
public class AlertService {
	MongoDatabase db;
	MongoCollection<Document> alertCollection;

	public AlertService() {
		try {
			db = MongoDatabaseConn.getDbConn().getDatabase();
			alertCollection = db.getCollection("alerts");
			System.out.println("Connection to Remote MongoDB Cluster is OK.....");
		} catch (MongoSocketException e) {
			e.printStackTrace();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlerts() {
		try {
			FindIterable<Document> allAlerts = alertCollection.find();
			ArrayList<Alert> alerts = new ArrayList<Alert>();
			for (Document document : allAlerts) {
				Alert foundAlert = new Alert();
				foundAlert.setAlertId(((ObjectId) document.getObjectId("_id")).toHexString());
				foundAlert.setSensorId(document.getString("sensorId"));
				foundAlert.setTime(document.getDate("time").toString());
				foundAlert.setCo2Level(document.getInteger("co2Level"));
				foundAlert.setSmokeLevel(document.getInteger("smokeLevel"));
				foundAlert.setStatus(document.getString("status"));
				alerts.add(foundAlert);
			}
			return Response.status(Status.OK).entity("{\"data\":" + new Gson().toJson(alerts) + "}")
					.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.NOT_FOUND).entity(
					"{\"message\":\"Couldn't load Alerts from DB \", " + " \"error\": " + e.getCause() + "," + "}")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlert(@PathParam("id") String id) {
		Document doc = null;
		try {
			doc = alertCollection.find(eq("_id", new ObjectId(id))).first();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\" " + e.getMessage() + " \" }")
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (doc != null) {
			Alert foundAlert = new Alert();
			foundAlert.setAlertId(((ObjectId) doc.getObjectId("_id")).toHexString());
			foundAlert.setSensorId(doc.getString("sensorId"));
			foundAlert.setTime(doc.getDate("time").toString());
			foundAlert.setCo2Level(doc.getInteger("co2Level"));
			foundAlert.setSmokeLevel(doc.getInteger("smokeLevel"));
			foundAlert.setStatus(doc.getString("status"));

			return Response.status(Status.OK).entity("{\"data\":" + new Gson().toJson(foundAlert) + "}")
					.type(MediaType.APPLICATION_JSON).build();
		} else
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Alert Not Found \" }")
					.type(MediaType.APPLICATION_JSON).build();
	}

	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAlert(JsonObject postData) {
		Document newAlertDoc = new Document();
		newAlertDoc.append("sensorId", postData.getString("sensorId"));
		newAlertDoc.append("time", new Date());
		newAlertDoc.append("co2Level", postData.getInt("co2Level"));
		newAlertDoc.append("smokeLevel", postData.getInt("smokeLevel"));
		newAlertDoc.append("status", postData.getString("status"));

		try {
			alertCollection.insertOne(newAlertDoc);
			Alert newAlert = new Alert();
			newAlert.setAlertId(((ObjectId) newAlertDoc.get("_id")).toHexString());
			newAlert.setSensorId(newAlertDoc.getString("sensorId"));
			newAlert.setTime(newAlertDoc.getDate("time").toString());
			newAlert.setCo2Level(newAlertDoc.getInteger("co2Level"));
			newAlert.setSmokeLevel(newAlertDoc.getInteger("smokeLevel"));
			newAlert.setStatus(newAlertDoc.getString("status"));
			
			if(sendAlertEmail(newAlert))
				return Response.status(Status.OK).entity("{\"data\":" + new Gson().toJson(newAlert) + "}")
					.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
		
		}
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity("{\"message\":\"Couldn't insert new Alert!!\" }")
				.type(MediaType.APPLICATION_JSON).build();
	}

	@Path("/delete/{id}")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAlert(@PathParam("id") String id) {
		System.out.println(id);
		DeleteResult result = alertCollection.deleteOne(eq("_id", new ObjectId(id)));
		String msg = "Deleted Alert(" + id + ") successfully !!";
		System.out.println(result);
		if (result.getDeletedCount() == 1)
			return Response.status(Status.NO_CONTENT).entity("{\"message\":" + msg + " \" }")
					.type(MediaType.APPLICATION_JSON).build();
		else
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Alert not found\"}")
					.type(MediaType.APPLICATION_JSON).build();
	}

	@Path("/deleteBySensor")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAlertsOfSensor(JsonObject postData) {
		DeleteResult result = alertCollection.deleteMany(eq("sensorId", postData.getString("id")));
		String msg = "Deleted Alerts of Sensor: (" + postData.getString("id") + ") successfully !!";
		System.out.println(postData.getString("id").toString());
		System.out.println(result);
		if (result.getDeletedCount() > 0)
			return Response.status(Status.NO_CONTENT).entity("{\"message\":" + msg + " \" }")
					.type(MediaType.APPLICATION_JSON).build();
		else
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\"No Alerts found\"}")
					.type(MediaType.APPLICATION_JSON).build();
	}

	@Path("/status_update/{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAlertStatus(JsonObject putData, @PathParam("id") String id) {
		UpdateResult updateResult;
		try {
			updateResult = alertCollection.updateOne(eq("_id", new ObjectId(id)),
					combine(set("status", putData.getString("status"))));
			if (updateResult.getModifiedCount() == 1)
				return Response.status(Status.OK).entity("{\"message\":\"Alert Updated\" }")
						.type(MediaType.APPLICATION_JSON).build();
			else
				return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Alert not found\"}")
						.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"message\":\"Server Error\"}")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	/**
	 * Sends an Email Alert
	 * 
	 * @param alert
	 * @return
	 */
	// This will be called when a new Alert has being saved to DB.
	private Boolean sendAlertEmail(Alert alert) {
		
		final String fromEmail = "nisujdev@gmail.com"; //requires valid gmail id
		final String password = "r0c4we11@dev"; // correct password for gmail id
		final String toEmail = "nisuga.rockwell@gmail.com"; // can be any email id 
		
		System.out.println("SSLEmail Start");
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
		props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
		props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
		props.put("mail.smtp.port", "465"); //SMTP Port
		props.put("mail.smtps.timeout", "60000"); 
		props.put("mail.smtps.connectiontimeout", "60000"); 
		
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		};
		
		Session session = Session.getDefaultInstance(props, auth);
		
			// Send the actual HTML message, as big as you like
			String bodyHtml = "<div><h2>New Alert!!!</h2><br/>"
					+ "<h4>Time :"+alert.getTime()+"</h4><br/>"
					+ "<h4>Sensor: "+alert.getSensorId()+"</h4><br/>"
					+ "<h4>CO2 Level:"+alert.getCo2Level()+"</h4><br/>"
					+ "<h4>Smoke Level: "+alert.getSmokeLevel()+"</h4><br/>"
					+ "<h4>Status :"+alert.getStatus()+"</h4><br/>"
					+ "</div>";
		try{
			sendEmailTempalate(session, toEmail,"URGENT: New Alert!!", bodyHtml);

			System.out.println("Sent Email message successfully....");
			return true;
		} catch (Exception mex) {
			mex.printStackTrace();
		}
		return false;
}


	private  void sendEmailTempalate(Session session, String toEmail, String subject, String body) {
		try {
			MimeMessage msg = new MimeMessage(session);
			// set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress("admin@firealerts.com", "Admin-FireMonitoring"));

			msg.setReplyTo(InternetAddress.parse("admin@firealerts.com", false));

			msg.setSubject(subject, "UTF-8");

			msg.setContent(body, "text/html; charset=utf-8");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
			System.out.println("Message is ready");
			Transport.send(msg);

			System.out.println("Email Sent Successfully!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}