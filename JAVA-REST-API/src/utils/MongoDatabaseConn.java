/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author IT18117110( Jayawardana N.S | nisuga.rockwell@gmail.com )
 */
public class MongoDatabaseConn {

	private volatile static MongoDatabaseConn databaseConn;
	private MongoDatabase database;

	public MongoDatabaseConn() {
	}

	public static MongoDatabaseConn getDbConn() {

		try {
			synchronized (MongoDatabaseConn.class) {
				if (databaseConn == null) {
					MongoClientURI uri = new MongoClientURI(
							"mongodb://admin:30GS6VH87id4wxIA@sa-alarmsystemdbcluster-shard-00-00-gopcl.mongodb.net:27017,sa-alarmsystemdbcluster-shard-00-01-gopcl.mongodb.net:27017,sa-alarmsystemdbcluster-shard-00-02-gopcl.mongodb.net:27017/fire_db?ssl=true&replicaSet=SA-AlarmSystemDBCluster-shard-0&authSource=admin&retryWrites=true&w=majority");
					MongoClient mongoClient = new MongoClient(uri);
					databaseConn = new MongoDatabaseConn();
					databaseConn.database = mongoClient.getDatabase("fire_db");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getCause());
		}
		return databaseConn;
	}

	public MongoDatabase getDatabase() {
		return database;
	}
	
	
}
