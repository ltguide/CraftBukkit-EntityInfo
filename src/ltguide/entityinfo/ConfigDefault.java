package ltguide.entityinfo;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

class ConfigDefault {

	private FileConfiguration config;
	boolean saveConfig = false;

	ConfigDefault(FileConfiguration config) {
		this.config = config;
	}

	public int getInt(String node, int value) {
		int i = config.getInt(node, -9999);
		if (i != -9999)
			return i;

		return setNode(node, value);
	}

	public String getColor(String node, String value) {
		return ChatColor.translateAlternateColorCodes('&', getString(node, value));
	}

	public String getString(String node, String value) {
		String s = config.getString(node, "b@dStR1Ng");
		if (!s.equals("b@dStR1Ng"))
			return s;

		return setNode(node, value);
	}

	private <T> T setNode(String node, T value) {
		saveConfig = true;
		config.set(node, value);
		return value;
	}

	public boolean save() {
		if (saveConfig)
			EntityInfo.INSTANCE.saveConfig();
		return true;
		// return saveConfig ? EntityInfo.INSTANCE.saveConfig() : true;
	}
}
