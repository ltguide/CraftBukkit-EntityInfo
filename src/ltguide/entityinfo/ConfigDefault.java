package ltguide.entityinfo;

import org.bukkit.util.config.Configuration;

class ConfigDefault {
	private Configuration config;
	boolean saveConfig = false;
	
	ConfigDefault(Configuration config) {
		this.config = config;
	}
	
	public int getInt(String node, int value) {
		int i = config.getInt(node, -9999);
		if (i != -9999) return i;
		
		return setNode(node, value);
	}
	
	public String getColor(String node, String value) {
		return getString(node, value).replaceAll("(?i)&([0-F])", "\u00A7$1");
	}
	
	public String getString(String node, String value) {
		String s = config.getString(node, "b@dStR1Ng");
		if (!s.equals("b@dStR1Ng")) return s;
		
		return setNode(node, value);
	}
	
	private <T> T setNode(String node, T value) {
		saveConfig = true;
		config.setProperty(node, value);
		return value;
	}
	
	public boolean save() {
		return saveConfig ? config.save() : true;
	}
}
