package ltguide.entityinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.WorldServer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftAnimals;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMonster;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class EntityInfo extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft");
	private Configuration config;
	private Map<String, Integer> entityLast;
	
	public PermissionHandler Permissions;
	public boolean checkPermissions;
	
	public void onDisable() {}
	
	public void onEnable() {
		getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, new EntityInfoServerListener(this), Priority.Monitor, this);
		reload(null);
		sendLog("v" + getDescription().getVersion() + " enabled");
	}
	
	private void sendMsg(CommandSender target, String msg) {
		sendMsg(target, msg, false);
	}
	
	private void sendMsg(CommandSender target, String msg, Boolean log) {
		String[] messages = msg.split("&n");
		for (String message : messages) {
			if (target instanceof Player) {
				((Player) target).sendMessage(message);
				if (log) sendLog("->" + ((Player) target).getName() + " " + message);
			}
			else sendLog("->*CONSOLE " + message);
		}
	}
	
	private void sendLog(String msg) {
		log.info("[" + getDescription().getName() + "] " + ChatColor.stripColor(msg));
	}
	
	private void reload(CommandSender sender) {
		entityLast = new HashMap<String, Integer>();
		
		Permissions = null;
		checkPermissions = true;
		
		if (config == null) config = getConfiguration();
		else config.load();
		
		boolean saveConfig = false;
		for (CommandMessage message : CommandMessage.values()) {
			String key = "messages." + message.name().toLowerCase();
			String value = config.getString(key);
			if (value == null) {
				config.setProperty(key, message.getMessage());
				saveConfig = true;
			}
			else message.setMessage(value);
		}
		if (saveConfig && !config.save()) sendLog("error saving config file");
		
		if (sender != null) sendMsg(sender, CommandMessage.RELOADDONE.toString(), true);
	}
	
	public Boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player)) return true;
		
		Player player = (Player) sender;
		if (Permissions == null && checkPermissions) {
			checkPermissions = false;
			Plugin plugin = getServer().getPluginManager().getPlugin("Permissions");
			if (plugin != null && plugin.isEnabled()) Permissions = ((Permissions) plugin).getHandler();
			
			if (Permissions == null) sendLog("no Permissions-like plugin found, so using Bukkit Permissions");
		}
		
		if (Permissions != null) return Permissions.has(player, node);
		return player.hasPermission(node);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (!hasPermission(sender, "entityinfo.use")) throw new CommandException(CommandMessage.PERMISSION);
			
			if (args.length == 0) {
				if (hasPermission(sender, "entityinfo.reload")) sendMsg(sender, CommandMessage.RELOADABLE.toString(label));
				throw new CommandException(CommandMessage.ARGLENGTH, command.getUsage().replace("<command>", label));
			}
			
			if (args[0].equalsIgnoreCase("reload")) {
				if (!hasPermission(sender, "giveto.reload")) throw new CommandException(CommandMessage.PERMISSION);
				reload(sender);
				return true;
			}
			
			if (!(sender instanceof Player)) throw new CommandException(CommandMessage.CONSOLE);
			
			Player player = (Player) sender;
			
			if (args[0].matches("[1-9](?:[0-9]+)?")) entityById(player, Integer.parseInt(args[0]));
			else if (args[0].equals("#")) entityById(player);
			else if (args[0].equals("@")) entitySearch(player);
		}
		catch (CommandException e) {
			sendMsg(sender, e.getMessage());
		}
		
		return true;
	}
	
	private void entityById(Player player) throws CommandException {
		if (!entityLast.containsKey(player.getName())) throw new CommandException(CommandMessage.NOLASTENTITY);
		
		entityById(player, entityLast.get(player.getName()));
	}
	
	private void entityById(Player player, int entityId) throws CommandException {
		entityLast.put(player.getName(), entityId);
		List<Entity> entities = player.getNearbyEntities(64, 128, 64);
		for (Entity entity : entities) {
			if (entity.getEntityId() == entityId) {
				entityInfo(player, entity);
				return;
			}
		}
		
		throw new CommandException(CommandMessage.NOSUCHENTITY);
	}
	
	@SuppressWarnings("unchecked")
	private void entitySearch(Player player) throws CommandException {
		WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		
		Location playerLocation = player.getLocation();
		Vector delta = playerLocation.getDirection().multiply(new Vector(4, 4, 4));;
		
		Location leftCorner = new Location(player.getWorld(), (int) playerLocation.getX(), (int) playerLocation.getY(), (int) playerLocation.getZ());
		Location rightCorner = leftCorner.clone();
		
		switch (getDirection(playerLocation)) {
			case NORTH:
				leftCorner.add(-4, -2, -4);
				rightCorner.add(1, 2, 4);
				break;
			case NORTH_EAST:
				leftCorner.add(-5, -2, -5);
				rightCorner.add(1, 2, 1);
				break;
			case EAST:
				leftCorner.add(-4, -2, -4);
				rightCorner.add(4, 2, 1);
				break;
			case SOUTH_EAST:
				leftCorner.add(-1, -2, -5);
				rightCorner.add(5, 2, 1);
				break;
			case SOUTH:
				leftCorner.add(-1, -2, -4);
				rightCorner.add(4, 2, 4);
				break;
			case SOUTH_WEST:
				leftCorner.add(-1, -2, -1);
				rightCorner.add(5, 2, 5);
				break;
			case WEST:
				leftCorner.add(-4, -2, -1);
				rightCorner.add(4, 2, 4);
				break;
			case NORTH_WEST:
				leftCorner.add(-5, -2, -1);
				rightCorner.add(1, 2, 5);
				break;
		}
		
		//World world = player.getWorld();
		//world.getBlockAt(leftCorner).setType(Material.NETHERRACK);
		//world.getBlockAt(rightCorner).setType(Material.GLOWSTONE);
		
		AxisAlignedBB bBox = AxisAlignedBB.a(leftCorner.getX(), leftCorner.getY(), leftCorner.getZ(), rightCorner.getX(), rightCorner.getY(), rightCorner.getZ());
		Set<net.minecraft.server.Entity> entities = new HashSet<net.minecraft.server.Entity>();
		int distance = 4;
		while (entities.size() < 5 && distance < 64) {
			entities.addAll(worldServer.b(entityPlayer, bBox));
			
			bBox.d(delta.getX(), delta.getY(), delta.getZ());
			distance += 4;
			
			//world.getBlockAt(leftCorner.add(delta.toLocation(world))).setType(Material.NETHERRACK);
			//world.getBlockAt(rightCorner.add(delta.toLocation(world))).setType(Material.GLOWSTONE);
		}
		
		if (entities.size() == 0) throw new CommandException(CommandMessage.NOENTITIES);
		
		for (net.minecraft.server.Entity entity : entities)
			entityInfo(player, entity.getBukkitEntity());
		
		entityLast.put(player.getName(), entities.iterator().next().id);
	}
	
	private void entityInfo(Player player, Entity entity) {
		if (!(entity instanceof CraftLivingEntity)) return;
		
		Location entityLocation = entity.getLocation();
		String clazz = entity.getClass().getSimpleName().replace("Craft", "");
		ChatColor color = ChatColor.GOLD;
		
		if (entity instanceof CraftAnimals) color = ChatColor.DARK_GREEN;
		else if (entity instanceof CraftMonster) color = ChatColor.DARK_RED;
		else if (entity instanceof CraftPlayer) {
			clazz = ((CraftPlayer) entity).getName();
			color = ChatColor.BLUE;
		}
		
		sendMsg(player, CommandMessage.INFO.toString(color + clazz, entity.getEntityId(), entityLocation.getX(), entityLocation.getY(), entityLocation.getZ(), getMovements(player.getLocation(), entityLocation)));
	}
	
	private double getDegrees(double yaw) {
		yaw = (yaw - 90) % 360;
		if (yaw < 0) yaw += 360;
		
		return yaw;
	}
	
	private BlockFace getCardinal(Location location) {
		return getCardinal(getDegrees(location.getYaw()));
	}
	
	private BlockFace getCardinal(double degrees) {
		if (degrees >= 315 || degrees < 45) return BlockFace.NORTH;
		if (degrees < 135) return BlockFace.EAST;
		if (degrees < 225) return BlockFace.SOUTH;
		return BlockFace.WEST;
	}
	
	private BlockFace getDirection(Location location) {
		return getDirection(getDegrees(location.getYaw()));
	}
	
	private BlockFace getDirection(double degrees) {
		/*
			0 - 22.4      north
			22.5 - 67.4
			67.5 - 112.4  east
			112.5 - 157.4
			157.5 - 202.4 south
			202.5 - 247.4
			247.5 - 292.4 west
			292.5 - 337.4
			337.5 - 359.9 north
		*/

		if (degrees >= 337.5 || degrees < 22.5) return BlockFace.NORTH;
		if (degrees >= 22.5 && degrees < 67.5) return BlockFace.NORTH_EAST;
		if (degrees >= 67.5 && degrees < 112.5) return BlockFace.EAST;
		if (degrees >= 112.5 && degrees < 157.5) return BlockFace.SOUTH_EAST;
		if (degrees >= 157.5 && degrees < 202.5) return BlockFace.SOUTH;
		if (degrees >= 202.5 && degrees < 247.5) return BlockFace.SOUTH_WEST;
		if (degrees >= 247.5 && degrees < 292.5) return BlockFace.WEST;
		return BlockFace.NORTH_WEST;
	}
	
	private String getMovements(Location playerLocation, Location entityLocation) {
		entityLocation.subtract(playerLocation);
		BlockFace cardinal = getCardinal(playerLocation);
		
		List<String> movements = new ArrayList<String>();
		addMovement(movements, cardinal, 'x', (int) entityLocation.getX());
		addMovement(movements, cardinal, 'z', (int) entityLocation.getZ());
		addMovement(movements, cardinal, 'y', (int) entityLocation.getY());
		
		if (movements.size() == 0) return CommandMessage.NOMOVEMENT.toString();
		
		return CommandMessage.MOVEMENT.toString(joinAsString(movements, ", "));
	}
	
	private void addMovement(List<String> movements, BlockFace cardinal, char axis, int coordinate) {
		if (coordinate == 0) return;
		movements.add(Math.abs(coordinate) + " " + getOrientation(cardinal, axis, coordinate));
	}
	
	private String getOrientation(BlockFace cardinal, char axis, int coordinate) {
		switch (axis) {
			case 'x':
				switch (cardinal) {
					case NORTH:
						if (coordinate < 0) return "forward";
						return "backward";
					case SOUTH:
						if (coordinate < 0) return "backward";
						return "forward";
					case EAST:
						if (coordinate < 0) return "left";
						return "right";
					case WEST:
						if (coordinate < 0) return "right";
						return "left";
				}
			case 'y':
				if (coordinate < 0) return "down";
				return "up";
			case 'z':
				switch (cardinal) {
					case NORTH:
						if (coordinate < 0) return "right";
						return "left";
					case SOUTH:
						if (coordinate < 0) return "left";
						return "right";
					case EAST:
						if (coordinate < 0) return "forward";
						return "backward";
					case WEST:
						if (coordinate < 0) return "backward";
						return "forward";
				}
		}
		
		return null;
	}
	
	private String joinAsString(List<String> list, String separator) {
		if (list.size() == 0) return "";
		
		StringBuilder sb = new StringBuilder(list.get(0));
		for (int i = 1; i < list.size(); i++)
			sb.append(separator + list.get(i));
		
		return sb.toString();
	}
}
