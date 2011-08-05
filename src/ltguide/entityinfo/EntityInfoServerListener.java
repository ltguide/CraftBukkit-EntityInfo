package ltguide.entityinfo;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;

class EntityInfoServerListener extends ServerListener {
	private EntityInfo plugin;
	
	public EntityInfoServerListener(EntityInfo plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if (plugin.Permissions != null && event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")) {
			plugin.Permissions = null;
			plugin.checkPermissions = true;
		}
	}
	
}
