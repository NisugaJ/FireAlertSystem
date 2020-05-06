/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dummy.sensor.rmi.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static dummy.sensor.rmi.server.SensorMonitorServer.JSON;
import dummy.sensor.rmi.server.SensorService;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.MediaPlayer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import util.Constants;

/**
 *
 * @author Dell
 */
public class SensorClient implements Runnable{
    
    private static SensorService sensorService;
    private static String sensorId;
    private static final int SENSOR_VALUE_RANGE = 7;
    
    public static void main(String[] args) {
            try {
//                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
//                System.out.println(registry);
//                sensorId = args[0];
                sensorId = args[0];
                sensorService = (SensorService)Naming.lookup("//localhost/SensorMonitorService");
                System.out.println("Connected to Server...");
                Thread updateThread = new Thread(new SensorClient());
                updateThread.start();
                sensorService.updateSensorCount('+');
                
        } catch (RemoteException e) {
            e.printStackTrace();
        }catch (NotBoundException ex) {
            Logger.getLogger(SensorClient.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SensorClient.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    public void run() {
        Random randomCO2 = new Random();
        Random randomSmoke = new Random();
        int rCO2, rSmoke;
        try {
            for(int i =0; i<5000; i++){
                 Thread.sleep(10000);
                 rCO2 = randomCO2.nextInt(SENSOR_VALUE_RANGE); 
                 rSmoke = randomSmoke.nextInt(SENSOR_VALUE_RANGE);
                 System.out.println("CO2 : " + rCO2+", Smoke: "+rSmoke);
                 
                 if( rCO2 > 5 || rSmoke > 5){
                     //Send Alert
                     if( sendAlert(rCO2, rSmoke) ){
                         System.out.println("Alert Sent..");
                     }else{
                         System.out.println("Alert Could not be Sent !!" );
                     }
                 }
                 
                 JSONObject obj = new JSONObject(sensorService.updateSensor(sensorId, rCO2, rSmoke ));
                 System.out.println("Updating...{"+sensorId+":"+i+"}");
                 System.out.println(obj);
                 if(!obj.isNull("deleteRequest") && obj.getBoolean("deleteRequest")){
                     if(deleteThisSensorFromDB()){
                        System.out.println("ClientSensor {"+sensorId+"} Deleted and Disconnected Successfully...");
                        sensorService.updateSensorCount('-');
                        System.exit(0);
                     }
                 }else if(!obj.isNull("error")){
                     System.out.println("Failed to update status..");
                 }
             }
        } catch (RemoteException e) {
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    
    private Boolean deleteThisSensorFromDB(){
      OkHttpClient client = new OkHttpClient(); 
      String reqBodyJson  = "{\"id\": \""+sensorId+"\"}";
      RequestBody body = RequestBody.create( reqBodyJson, JSON );          
      Request request = new Request.Builder()
          .url(Constants.REST_API_BASE_URL+"/sensors/delete")
          .delete(body)
          .build();
      try (Response response = client.newCall(request).execute()) {
          System.out.println(response.code());
          if(response.code() == 204 ){
            // 204 == NO-CONTENT == The Sensor Has been deleted
            return true;
          }
      }catch( Exception e ){
          e.printStackTrace();
      }
      return false;
    }
    
     private Boolean sendAlert(int co2, int smoke){
      OkHttpClient client = new OkHttpClient(); 
      String reqBodyJson  = "{\"id\": \""+sensorId+"\"}";
      JSONObject postAlertData = new JSONObject();
      postAlertData.put("sensorId", sensorId);
      postAlertData.put("co2Level", co2);
      postAlertData.put("smokeLevel", smoke);
      postAlertData.put("status", "UNFIXED");
      System.out.println(postAlertData.toString());
      RequestBody body = RequestBody.create( postAlertData.toString() , JSON );          
      Request request = new Request.Builder()
          .url(Constants.REST_API_BASE_URL+"/alerts/create")
          .post(body)
          .build();
      try (Response response = client.newCall(request).execute()) {
          System.out.println(response.code());
          if(response.code() == 200 ){// Alert has been sent
            return true;
          }
      }catch( Exception e ){
          e.printStackTrace();
      }
      return false;
    }
}

    

