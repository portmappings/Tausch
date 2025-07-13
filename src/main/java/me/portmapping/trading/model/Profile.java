package me.portmapping.trading.model;

import lombok.Data;
import org.bson.Document;

import java.util.UUID;

@Data
public class Profile {
    private final UUID uuid;
    private boolean ignoreTrades;

    public Document toBson() {
        return new Document()
                .append("uuid", uuid.toString())
                .append("ignoreTrades", ignoreTrades);
    }

    public static Profile fromBson(Document doc) {
        if (doc == null) return null;

        UUID uuid = UUID.fromString(doc.getString("uuid"));
        Profile profile = new Profile(uuid);
        profile.setIgnoreTrades(doc.getBoolean("ignoreTrades", false));
        return profile;
    }
}
