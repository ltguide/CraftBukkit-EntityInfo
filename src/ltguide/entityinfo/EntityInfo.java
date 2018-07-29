package ltguide.entityinfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ltguide.entityinfo.versionhandler.IVersionHandler;

public class EntityInfo extends JavaPlugin {

	private final Logger log = this.getLogger();
	private FileConfiguration config;

	private Map<String, Integer> entityLast = new HashMap<String, Integer>();
	private Map<String, String> entityColor = new HashMap<String, String>();
	public static int maxResults;
	public static int maxDistance;

	// public PermissionHandler Permissions;
	public boolean checkPermissions;
	public static EntityInfo INSTANCE;

	public IVersionHandler vh;

	@Override
	public void onDisable() {
		INSTANCE = null;
	}

	@Override
	public void onEnable() {
		String packageName = this.getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);
		try {
			final Class<?> clazz = Class
					.forName("ltguide.entityinfo.versionhandler." + version + ".VersionHandler");
			if (IVersionHandler.class.isAssignableFrom(clazz))
				vh = (IVersionHandler) clazz.getConstructor().newInstance();
		} catch (final Exception e) {
			e.printStackTrace();
			this.getLogger().severe("Could not find support for this CraftBukkit version.");
			this.getLogger().info(
					"Check for an issue relating to this version at https://github.com/ltguide/CraftBukkit-EntityInfo and post an issue if one does not already exist please.");
			this.setEnabled(false);
			return;
		}
		this.getLogger().info("Loading support for " + version + ".");
	
		INSTANCE = this;

		// getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE,
		// new EntityInfoServerListener(this), Priority.Monitor, this);
		reload();
		// sendLog("v" + getDescription().getVersion() + " enabled");
	}

	// public void sendMsg(CommandSender target, CommandMessage msg) {
	// sendMsg(target, msg, false);
	// }

	public void sendMsg(CommandSender target, String msg) {
		sendMsg(target, msg, false);
	}

	public void sendMsg(CommandSender target, CommandMessage msg, boolean log) {
		sendMsg(target, msg, log);
	}

	public void sendMsg(CommandSender target, String msg, Boolean log) {
		String[] messages = msg.split("&n");
		for (String message : messages) {
			if (target instanceof Player) {
				((Player) target).sendMessage(message);
				if (log)
					sendLog("->" + ((Player) target).getName() + " " + message);
			} else
				sendLog("->*CONSOLE " + message);
		}
	}

	public String getColor(String type) {
		return this.entityColor.get(type);
	}

	public void setLast(Player player, LivingEntity entity) {
		entityLast.put(player.getName(), entity.getEntityId());
	}

	private void sendLog(String msg) {
		log.info("[" + getDescription().getName() + "] " + ChatColor.stripColor(msg));
	}

	private void reload() {
		entityLast.clear();

		// Permissions = null;
		checkPermissions = true;

		if (config == null)
			config = this.getConfig();
		else
			this.reloadConfig();

		ConfigDefault configDefault = new ConfigDefault(config);

		maxResults = configDefault.getInt("maxresults", 5);
		maxDistance = configDefault.getInt("maxdistance", 64);

		entityColor.clear();
		entityColor.put("animal", configDefault.getColor("colors.animal", "&2"));
		entityColor.put("monster", configDefault.getColor("colors.monster", "&4"));
		entityColor.put("player", configDefault.getColor("colors.player", "&9"));
		entityColor.put("other", configDefault.getColor("colors.other", "&6"));

		for (CommandMessage message : CommandMessage.values())
			message.setMessage(configDefault.getString("messages." + message.name().toLowerCase(),
					message.getMessage()));

		if (!configDefault.save())
			sendLog("error saving config file");
	}

	public Boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player))
			return true;

		Player player = (Player) sender;
		// if (Permissions == null && checkPermissions) {
		// checkPermissions = false;
		// Plugin plugin =
		// getServer().getPluginManager().getPlugin("Permissions");
		// if (plugin != null && plugin.isEnabled())
		// Permissions = ((Permissions) plugin).getHandler();
		//
		// if (Permissions == null)
		// sendLog("no Permissions-like plugin found, so using Bukkit
		// Permissions");
		// }
		//
		// if (Permissions != null)
		// return Permissions.has(player, node);
		return player.hasPermission(node);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (!hasPermission(sender, "entityinfo.use"))
				throw new CommandException(CommandMessage.PERMISSION);

			if (args.length == 0) {
				if (hasPermission(sender, "entityinfo.reload"))
					sendMsg(sender, CommandMessage.RELOADABLE.toString(label));
				throw new CommandException(CommandMessage.ARGLENGTH,
						command.getUsage().replace("<command>", label));
			}

			if (args[0].equalsIgnoreCase("reload")) {
				if (!hasPermission(sender, "giveto.reload"))
					throw new CommandException(CommandMessage.PERMISSION);
				reload();
				sendMsg(sender, CommandMessage.RELOADDONE, true);
				return true;
			}

			if (!(sender instanceof Player))
				throw new CommandException(CommandMessage.CONSOLE);

			Player player = (Player) sender;

			if (args[0].matches("[1-9](?:[0-9]+)?"))
				entityById(player, Integer.parseInt(args[0]));
			else if (args[0].equals("#"))
				entityById(player);
			else if (args[0].equals("@"))
				entitySearch(player);
			else if (args[0].matches("(?i:[a-z]+)"))
				entityByType(player, args[0]);
		} catch (CommandException e) {
			sendMsg(sender, e.getMessage());
		}

		return true;
	}

	private void entityById(Player player) throws CommandException {
		if (!entityLast.containsKey(player.getName()))
			throw new CommandException(CommandMessage.NOLASTENTITY);

		entityById(player, entityLast.get(player.getName()));
	}

	private void entityById(Player player, int entityId) throws CommandException {
		entityLast.put(player.getName(), entityId);

		List<Entity> entities = player.getNearbyEntities(maxDistance, 128, maxDistance);
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity && entity.getEntityId() == entityId) {
				vh.entityInfo(player, (LivingEntity) entity);
				return;
			}
		}

		throw new CommandException(CommandMessage.NOSUCHENTITY);
	}

	private void entityByType(Player player, String entityType) throws CommandException {
		Pattern pattern = Pattern.compile("Craft" + entityType + ".*", Pattern.CASE_INSENSITIVE);

		int count = 0;
		List<Entity> entities = player.getNearbyEntities(maxDistance, 128, maxDistance);
		for (Entity entity : entities) {
			if (!(entity instanceof LivingEntity))
				continue;
			if (pattern.matcher(entity.getClass().getSimpleName()).matches()) {
				LivingEntity lEntity = (LivingEntity) entity;
				vh.entityInfo(player, lEntity);
				if (count == 0)
					entityLast.put(player.getName(), lEntity.getEntityId());
				if (++count == maxResults)
					break;
			}
		}

		if (count == 0)
			throw new CommandException(CommandMessage.NOSUCHENTITIES);
	}

	private void entitySearch(Player player) throws CommandException {
		vh.entitySearch(player);
	}
}
