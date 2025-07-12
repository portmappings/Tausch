package me.portmapping.trading.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import me.portmapping.trading.Tausch;

@Getter
public class MongoHandler {

	private final MongoClient mongoClient;


	public MongoHandler(Tausch plugin) {
		mongoClient = MongoClients.create(plugin.getSettingsConfig().getConfig().getString("MONGO.URI"));

		String databaseName = plugin.getSettingsConfig().getConfig().getString("MONGO.DATABASE");

	}

}
