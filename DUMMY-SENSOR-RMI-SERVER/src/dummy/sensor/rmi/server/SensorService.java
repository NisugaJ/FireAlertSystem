/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dummy.sensor.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Dell
 */
public interface SensorService extends  Remote{
    public String updateSensor(String id, int co2, int smoke) throws RemoteException;
    public int updateSensorCount(char type) throws RemoteException;
    
}
