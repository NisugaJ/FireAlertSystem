package fireservice;

import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.omg.CORBA.portable.UnknownException;

import models.FireSensor;
import utils.MongoDatabaseConn;

import com.google.gson.*;
import com.mongodb.MongoSocketException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.combine;

/**
 * All the functions are related to `sensors` Collection
 * 
 * @author IT18117110( Jayawardana N.S | nisuga.rockwell@gmail.com )
 *
 */
@Path("/sensors")
public class SensorService {
	MongoDatabase db;
	MongoCollection<Document> sensorCollection;

	/*
	 * When a request comes having /sensors, a new SensorService Object is
	 * constructed with an available Singleton DB Connection.
	 */
	public SensorService() {
		try {

			// Getting an availabe DB Connection Object
			db = MongoDatabaseConn.getDbConn().getDatabase();
			sensorCollection = db.getCollection("sensors");
			System.out.println("Connection to Remote MongoDB Cluster is OK.....");
		} catch (MongoSocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * To get all available sensors in the collection
	 * 
	 * @return Response
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensor() {
		try {
			FindIterable<Document> allSensors = sensorCollection.find();
			ArrayList<FireSensor> sensors = new ArrayList<FireSensor>();
			for (Document document : allSensors) {
				FireSensor foundSensor = new FireSensor();
				foundSensor.setFireSensorId(((ObjectId) document.getObjectId("_id")).toHexString());
				foundSensor.setDescription(document.getString("description"));
				foundSensor.setActive(document.getBoolean("active"));
				foundSensor.setCurrentCO2Level(document.getInteger("currentCO2Level"));
				foundSensor.setCurrentSmokeLevel(document.getInteger("currentSmokeLevel"));
				foundSensor.setDeleteReqeust(document.getBoolean("deleteRequest"));
				foundSensor.setFloorId(document.getInteger("floorId"));
				foundSensor.setFloorId(document.getInteger("roomId"));
				sensors.add(foundSensor);
			}
			return Response.status(Status.OK).entity("{\"data\":" + new Gson().toJson(sensors) + "}")
					.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).entity(
					"{\"message\":\"Couldn't load sensors from DB \", " + " \"error\": " + e.getCause() + "," + "}")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	/**
	 * 
	 * To get a sensors in DB
	 * 
	 * @param String id
	 * @return Response
	 */
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensor(@PathParam("id") String id) {
		Document doc = null;
		try {
			doc = sensorCollection.find(eq("_id", new ObjectId(id))).first();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\" " + e.getMessage() + " \" }")
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (doc != null) {
			FireSensor foundSensor = new FireSensor();
			foundSensor.setFireSensorId(new ObjectId(id).toHexString());
			foundSensor.setDescription(doc.getString("description"));
			foundSensor.setActive(doc.getBoolean("active"));
			foundSensor.setCurrentCO2Level(doc.getInteger("currentCO2Level"));
			foundSensor.setCurrentSmokeLevel(doc.getInteger("currentSmokeLevel"));
			foundSensor.setDeleteReqeust(doc.getBoolean("deleteRequest"));
			foundSensor.setFloorId(doc.getInteger("floorId"));
			foundSensor.setRoomId(doc.getInteger("roomId"));

			return Response.status(Status.OK).entity("{\"data\":" + new Gson().toJson(foundSensor) + "}")
					.type(MediaType.APPLICATION_JSON).build();
		} else
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Sensor Not Found \" }")
					.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * 
	 * To add a new Sensor to the Collection
	 * 
	 * @return Response
	 */
	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSensor(JsonObject postData) {
		// System.out.println( postData);
		Document newFireSensorDoc = new Document();
		newFireSensorDoc.append("description", postData.getString("description"));
		newFireSensorDoc.append("active", true);
		newFireSensorDoc.append("currentSmokeLevel", 0);
		newFireSensorDoc.append("currentCO2Level", 0);
		newFireSensorDoc.append("floorId", Integer.valueOf(postData.getString("floorId") ));
		newFireSensorDoc.append("roomId", Integer.valueOf( postData.getString("roomId") ));
		newFireSensorDoc.append("deleteRequest", false);

		try {
			sensorCollection.insertOne(newFireSensorDoc);
			FireSensor newSensor = new FireSensor();
			newSensor.setFireSensorId(((ObjectId) newFireSensorDoc.get("_id")).toHexString());
			newSensor.setDescription(newFireSensorDoc.getString("description"));
			newSensor.setFloorId(newFireSensorDoc.getInteger("floorId"));
			newSensor.setRoomId(newFireSensorDoc.getInteger("roomId"));
			return Response.status(Status.OK).entity("{\"data\":" + new Gson().toJson(newSensor) + "}")
					.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND)
					.entity("{\"message\":\"Couldn't insert new Sensor :" + e.getMessage() + "\" }")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@Path("/delete")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteSensor(JsonObject postData) {

		DeleteResult result = sensorCollection.deleteOne(eq("_id", new ObjectId(postData.getString("id"))));
		String msg = "Deleted Sensors(" + postData + ") successfully !!";
		System.out.println(result);
		if (result.getDeletedCount() == 1)
			return Response.status(Status.NO_CONTENT).entity("{\"message\":" + msg + " \" }")
					.type(MediaType.APPLICATION_JSON).build();
		else
			return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Sensor not found\"}")
					.type(MediaType.APPLICATION_JSON).build();
	}

	@Path("/delete_request")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response setSensorDeleteRequest(JsonObject postData) {

		UpdateResult updateResult;
		try {
			System.out.println(postData);
			updateResult = sensorCollection.updateOne(eq("_id", new ObjectId(postData.getString("id"))),
					combine(set("deleteRequest", true)));

			if (updateResult.getMatchedCount() == 1)
				return Response.status(Status.OK).entity("{\"message\":\"Updated Sensor\" }")
						.type(MediaType.APPLICATION_JSON).build();
			else
				return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Sensor not found\"}")
						.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"message\":\"Server Error\"}")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@Path("/status_update/{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSensorStatus(JsonObject putData, @PathParam("id") String id) {

		UpdateResult updateResult;
		try {
			updateResult = sensorCollection.updateOne(eq("_id", new ObjectId(id)),
					combine(set("currentSmokeLevel", putData.getInt("currentSmokeLevel")),
							set("currentCO2Level", putData.getInt("currentCO2Level"))));

			if (updateResult.getMatchedCount() == 1)
				return Response.status(Status.OK)
						.entity("{\"deleteRequest\":"
								+ sensorCollection.find(eq("_id", new ObjectId(id))).first().getBoolean("deleteRequest")
								+ "}")
						.type(MediaType.APPLICATION_JSON).build();
			else
				return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Sensor not found\"}")
						.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"message\":\"Server Error\"}")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@Path("/update/{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSensor(JsonObject putData, @PathParam("id") String id) {

		UpdateResult updateResult;
		try {
			updateResult = sensorCollection.updateOne(eq("_id", new ObjectId(id)),
					combine(
//							set("currentSmokeLevel", putData.getInt("currentSmokeLevel")),
//							set("currentCO2Level", putData.getInt("currentCO2Level")),
							set("description", putData.getString("description")),
							set("floorId",Integer.valueOf( putData.getString("floorId")) ),
							set("roomId",Integer.valueOf( putData.getString("roomId")) )
					));

			if (updateResult.getModifiedCount() == 1)
				return Response.status(Status.OK).entity("{\"message\":\"Sensor Updated\" }")
						.type(MediaType.APPLICATION_JSON).build();
			else
				return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Sensor not found\"}")
						.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"message\":\"Server Error\"}")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}
}