package me.portmapping.trading;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.portmapping.trading.database.MongoHandler;
import me.portmapping.trading.listeners.PlayerListener;
import me.portmapping.trading.manager.CommandManager;
import me.portmapping.trading.manager.ProfileManager;
import me.portmapping.trading.manager.TradeManager;
import me.portmapping.trading.model.Profile;
import me.portmapping.trading.utils.Threads;
import me.portmapping.trading.utils.chat.Language;
import me.portmapping.trading.utils.config.FileConfig;
import me.portmapping.trading.utils.menu.ButtonListener;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;


@Getter
public final class Tausch extends JavaPlugin {
    @Getter @Setter(AccessLevel.PRIVATE) private static Tausch instance;

    private FileConfig settingsConfig;
    private FileConfig messagesConfig;
    private FileConfig menusConfig;

    private MongoHandler mongoHandler;
    private CommandManager commandManager;
    private TradeManager tradeManager;
    private ProfileManager profileManager;

    @Override
    public void onEnable() {
        setInstance(this);
        Threads.init();
        this.settingsConfig = new FileConfig(this, "settings.yml");
        this.messagesConfig = new FileConfig(this, "messages.yml");
        this.menusConfig = new FileConfig(this, "menus.yml");
        Language.loadMessages();

        this.mongoHandler = new MongoHandler(this);
        this.commandManager = new CommandManager(this);
        this.tradeManager = new TradeManager(this);
        this.profileManager = new ProfileManager(this);


        this.getServer().getPluginManager().registerEvents(new ButtonListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

    }

    @Override
    public void onDisable() {
        try {
            for (Profile profile : this.getProfileManager().getAllData()) {
                Threads.executeData(() -> this.getProfileManager().saveData(profile));
            }

            if (this.mongoHandler != null) {
                this.mongoHandler.getMongoClient().close();
            }
            Threads.close();
        } catch (NullPointerException ignored) {}
    }
}
