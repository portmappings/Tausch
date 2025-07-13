package me.portmapping.trading.database;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import me.portmapping.trading.Tausch;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.UUID;

@Getter
public class MongoHandler {

	private final MongoClient mongoClient;
	private final MongoCollection<Document> tradeHistory;
	private final MongoCollection<Document> profiles;

	public MongoHandler(Tausch plugin) {
		mongoClient = MongoClients.create(plugin.getSettingsConfig().getConfig().getString("MONGO.URI"));

		String databaseName = plugin.getSettingsConfig().getConfig().getString("MONGO.DATABASE");
		this.tradeHistory = mongoClient.getDatabase(databaseName).getCollection("tradeHistory");
		this.profiles = mongoClient.getDatabase(databaseName).getCollection("profiles");
	}

}
