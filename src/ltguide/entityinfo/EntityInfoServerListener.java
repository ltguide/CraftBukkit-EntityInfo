package ltguide.entityinfo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

class EntityInfoServerListener implements Listener {

	private EntityInfo plugin;

	public EntityInfoServerListener(EntityInfo plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		// if (plugin.Permissions != null
		// &&
		// event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions"))
		// {
		// plugin.Permissions = null;
		plugin.checkPermissions = true;
		// }
	}

}
