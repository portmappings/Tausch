package me.portmapping.trading.utils.config;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConfigCursor {

	private final FileConfig fileConfig;

	private String path;

	public ConfigCursor(FileConfig fileConfig, String path) {
		this.fileConfig = fileConfig;
		this.path = path;
	}

	public FileConfig getFileConfig() {
		return this.fileConfig;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean exists() {
		return exists(null);
	}

	public boolean exists(String path) {
		return this.fileConfig.getConfig().contains(this.path + ((path == null) ? "" : ("." + path)));
	}

	public Set<String> getKeys() {
		return getKeys(null);
	}

	public Set<String> getKeys(String path) {
		return this.fileConfig.getConfig()
				.getConfigurationSection(this.path + ((path == null) ? "" : ("." + path))).getKeys(false);
	}

	public boolean getBoolean(String path) {
		return this.fileConfig.getConfig()
				.getBoolean(((this.path == null) ? "" : (this.path + ".")) + path);
	}

	public boolean getBoolean(String path, boolean defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue);
			this.fileConfig.save();
			return defaultValue;
		}
		return this.fileConfig.getConfig().getBoolean(fullPath);
	}

	public int getInt(String path) {
		return this.fileConfig.getConfig()
				.getInt(((this.path == null) ? "" : (this.path + ".")) + path);
	}

	public int getInt(String path, int defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue);
			this.fileConfig.save();
			return defaultValue;
		}
		return this.fileConfig.getConfig().getInt(fullPath);
	}

	public double getDouble(String path) {
		return this.fileConfig.getConfig()
				.getDouble(((this.path == null) ? "" : (this.path + ".")) + path);
	}

	public double getDouble(String path, double defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue);
			this.fileConfig.save();
			return defaultValue;
		}
		return this.fileConfig.getConfig().getDouble(fullPath);
	}

	public long getLong(String path) {
		return this.fileConfig.getConfig()
				.getLong(((this.path == null) ? "" : (this.path + ".")) + path);
	}

	public long getLong(String path, long defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue);
			this.fileConfig.save();
			return defaultValue;
		}
		return this.fileConfig.getConfig().getLong(fullPath);
	}

	public String getString(String path) {
		return this.fileConfig.getConfig()
				.getString(((this.path == null) ? "" : (this.path + ".")) + path);
	}

	public String getString(String path, String defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue);
			this.fileConfig.save();
			return defaultValue;
		}
		return this.fileConfig.getConfig().getString(fullPath);
	}

	public List<String> getStringList(String path) {
		return this.fileConfig.getConfig()
				.getStringList(((this.path == null) ? "" : (this.path + ".")) + path);
	}

	public List<String> getStringList(String path, List<String> defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue);
			this.fileConfig.save();
			return defaultValue;
		}
		return this.fileConfig.getConfig().getStringList(fullPath);
	}

	public UUID getUuid(String path) {
		String value = this.fileConfig.getConfig().getString(this.path + "." + path);
		if (value == null) return null;
		return UUID.fromString(value);
	}

	public UUID getUuid(String path, UUID defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue.toString());
			this.fileConfig.save();
			return defaultValue;
		}
		String value = this.fileConfig.getConfig().getString(fullPath);
		return UUID.fromString(value);
	}

	public World getWorld(String path) {
		String worldName = this.fileConfig.getConfig().getString(this.path + "." + path);
		if (worldName == null) return null;
		return Bukkit.getWorld(worldName);
	}

	public World getWorld(String path, World defaultValue) {
		String fullPath = this.path + ((path == null) ? "" : ("." + path));
		if (!this.fileConfig.getConfig().contains(fullPath)) {
			this.fileConfig.getConfig().set(fullPath, defaultValue.getName());
			this.fileConfig.save();
			return defaultValue;
		}
		String worldName = this.fileConfig.getConfig().getString(fullPath);
		return Bukkit.getWorld(worldName);
	}

	public void set(Object value) {
		set(null, value);
	}

	public void set(String path, Object value) {
		this.fileConfig.getConfig().set(this.path + ((path == null) ? "" : ("." + path)), value);
	}

	public void save() {
		this.fileConfig.save();
	}
}
