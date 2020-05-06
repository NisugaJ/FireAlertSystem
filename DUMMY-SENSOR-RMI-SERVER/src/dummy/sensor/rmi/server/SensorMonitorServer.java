/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dummy.sensor.rmi.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.JsonAdapter;
import dummy.sensor.rmi.client.SensorClient;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.swing.text.Document;
import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Constants;

/**
 *
 * @author Dell
 */
public class SensorMonitorServer extends UnicastRemoteObject implements SensorService{

    private int sensorCount = 0;
    
    public static final MediaType JSON  = MediaType.get("application/json; charset=utf-8");
    
    public SensorMonitorServer() throws RemoteException{
        super();
    }
    
    
    @Override
    public String updateSensor(String id, int co2, int smoke) throws RemoteException {
        try {
            String url = Constants.REST_API_BASE_URL + "/sensors/status_update/"+id;
            String jsonInputString = "{\"currentCO2Level\": "+co2+", \"currentSmokeLevel\": "+smoke+"}";
            String resp =  updateStatus(url,jsonInputString);
            String[] args = {"asd","asds"};

            return resp;
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return "";
    }

    @Override
    public synchronized int updateSensorCount(char type) throws RemoteException {
        if(type == '+'){
            sensorCount++;
        }else if(type == '-'){
            sensorCount--;
        }
        System.out.println("Sensor Count = "+sensorCount);
        return sensorCount;
    }
    
    public static void main(String[] args) {
          try {
//            if ( System.getSecurityManager() == null){
//                System.setSecurityManager(new RMISecurityManager());
//            }  

            SensorService sensorMonitorServer =  new SensorMonitorServer();
            
            Registry registry = LocateRegistry.createRegistry(1099);
//            registry.o
            System.out.println(registry);
            registry.rebind("SensorMonitorService",sensorMonitorServer);
            System.out.println("SensorMonitorService Server started...");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Private Methods */
    private String updateStatus(String url, String json) throws IOException {
        
        OkHttpClient client = new OkHttpClient();        
        RequestBody body = RequestBody.create( json, JSON );          
        Request request = new Request.Builder()
            .url(url)
            .put(body)
            .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.code() == 200){
                System.out.println("Updated a Sensor Status ......");
                return  response.body().string();
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
            return "{ \"error\" :\"Failed\" }";
    }
}
