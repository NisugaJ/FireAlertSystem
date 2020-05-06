package fireservice;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

import models.ClientProcess;
import models.FireSensor;
import utils.MongoDatabaseConn;

import com.google.gson.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

@Path("/init")
public class InitService {

	private static final String RMI_PATH = "C:\\MyProjects\\FireMonitoringSystem\\DUMMY-SENSOR-RMI-SERVER\\production\\";
	private static Process rmiServerProcess;
	private static ArrayList<ClientProcess> clientProcesses;
	Boolean serverStarted;

	/**
	 * 
	 */
	public InitService() {
		serverStarted = false;
	}

	@Path("/start_rmi_server")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startRMI_Server(JsonObject postData) {
		if (postData.getBoolean("rmi_server_start")) {
			try {
//				ServerThread serverThread = new ServerThread("SERVER");
//				serverThread.run();

				ProcessBuilder pb = new ProcessBuilder("java", "-jar", RMI_PATH + "DUMMY-SENSOR-RMI-SERVER-APP.jar");
				rmiServerProcess = pb.start();

				BufferedReader in = new BufferedReader(new InputStreamReader(rmiServerProcess.getInputStream()));
				BufferedReader err = new BufferedReader(new InputStreamReader(rmiServerProcess.getErrorStream()));

				new Thread() {
					public void run() {
						try {
							String error;
							while ((error = err.readLine()) != null)
								System.out.println(error);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();

				synchronized (serverStarted) {
					new Thread() {
						public void run() {
							try {
								String msg;
								while ((msg = in.readLine()) != null) {
									serverStarted = true;
									System.out.println(msg);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				}

				System.out.println(in.ready());
				Boolean status = rmiServerProcess.waitFor(5, TimeUnit.SECONDS);
				System.out.println(status);
				System.out.println("SERVER STARTED->" + serverStarted);
				if (serverStarted)
					return Response.status(Status.OK).entity("{\"message\":\"Started\"}")
							.type(MediaType.APPLICATION_JSON).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Invalid. not found\"}")
				.type(MediaType.APPLICATION_JSON).build();
	}

	@Path("/start_rmi_sensors")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startCurrentSensors(JsonObject postData) {
		System.out.println(postData);
		if (postData.getBoolean("rmi_sensors_start")) {
			try {
				MongoDatabase db = MongoDatabaseConn.getDbConn().getDatabase();
				FindIterable<Document> allSensors = db.getCollection("sensors").find();
				ArrayList<ClientProcess> clientStartProcesses = new ArrayList<ClientProcess>();
				for (Document sensor : allSensors) {
					if (sensor.getBoolean("active")) {
						String sensorId = ((ObjectId) sensor.getObjectId("_id")).toHexString().toString();

						ProcessBuilder pb = new ProcessBuilder("java", "-jar",
								RMI_PATH + "DUMMY-SENSOR-RMI-CLIENT-APP.jar", sensorId);
						Process p = pb.start();
						BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
						BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

						new Thread() {
							public void run() {
								try {
									String error;
									while ((error = err.readLine()) != null)
										System.out.println(error);

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();

						new Thread() {
							public void run() {
								try {
									String msg;
									while ((msg = in.readLine()) != null) {
										System.out.println(msg);
									}

									System.out.println("RMI Client Sensor:" + sensorId + " Started... ");

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();
						clientStartProcesses.add(new ClientProcess(p, true));

					}

				}
				clientProcesses = clientStartProcesses;
				System.out.println(clientProcesses);
				return Response.status(Status.OK).entity("{\"message\":\"Started " + "Clients" + "\"}")
						.type(MediaType.APPLICATION_JSON).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Response.status(Status.NOT_FOUND).entity("{\"message\":\"Failed\"}").type(MediaType.APPLICATION_JSON)
				.build();
	}

	@Path("/shutdown_rmi_server")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response shutDownRmiServer(JsonObject postData) {
		if (postData.getBoolean("rmi_server_shutdown")) {
			System.out.println(rmiServerProcess);
			try {

				if (rmiServerProcess != null) {
					rmiServerProcess.destroyForcibly();
					System.out.println("RMI Server is Down now... ");
					rmiServerProcess = null;
					return Response.status(Status.OK).entity("{\"message\":\"Shutdown OK\"}")
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Response.status(Status.NOT_FOUND).entity("{\"error\":\"Failed\"}").type(MediaType.APPLICATION_JSON)
				.build();
	}

	@Path("/shutdown_rmi_sensor_clients")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response shutDownRmiClients(JsonObject postData) {
		System.out.println(postData);
		if (postData.getBoolean("rmi_sensors_shutdown")) {
			try {
				System.out.println(clientProcesses);
				if (clientProcesses.size() != 0) {
					for (ClientProcess clientProcess : clientProcesses) {
						clientProcess.getProcess().destroyForcibly();
					}
					System.out.println("RMI Clients are Down now... ");
					
					return Response.status(Status.OK).entity("{\"message\":\"ClientS Shutdown OK\"}")
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Response.status(Status.NOT_FOUND).entity("{\"error\":\"Failed. Server Error\"}").type(MediaType.APPLICATION_JSON)
				.build();
	}

}