package me.portmapping.trading.manager;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.Profile;
import me.portmapping.trading.utils.Threads;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProfileManager {

    private final Tausch instance;

    private Map<UUID, Profile> profileMap;
    public ProfileManager(Tausch instance){
        this.instance = instance;
        this.profileMap = new HashMap<>();
    }

    public void createProfile(Player player) {
        Profile data = new Profile(player.getUniqueId());
        this.profileMap.put(data.getUuid(), data);
        Threads.executeData(() -> loadData(data));

    }

    public Profile getProfile(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            if (!this.profileMap.containsKey(uuid)) {
                createProfile(Objects.requireNonNull(Bukkit.getPlayer(uuid)));
            }
        }
        return this.profileMap.get(uuid);
    }


    public void loadData(Profile data) {
        Document doc = instance.getMongoHandler().getProfiles()
                .find(Filters.eq("uuid", data.getUuid().toString()))
                .first();

        if (doc == null) {
            data.setIgnoreTrades(false);
            Threads.executeData(() -> saveData(data));
            return;
        }

        Profile loaded = Profile.fromBson(doc);
        data.setIgnoreTrades(loaded.isIgnoreTrades());
    }

    public void saveData(Profile data) {
        if (data == null) return;

        instance.getMongoHandler().getProfiles().replaceOne(
                Filters.eq("uuid", data.getUuid().toString()),
                data.toBson(),
                new ReplaceOptions().upsert(true)
        );
    }
}
