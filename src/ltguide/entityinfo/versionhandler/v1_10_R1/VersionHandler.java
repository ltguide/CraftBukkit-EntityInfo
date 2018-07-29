package ltguide.entityinfo.versionhandler.v1_10_R1;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftMonster;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import ltguide.entityinfo.CommandException;
import ltguide.entityinfo.CommandMessage;
import ltguide.entityinfo.EntityInfo;
import ltguide.entityinfo.versionhandler.IVersionHandler;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.WorldServer;

public class VersionHandler implements IVersionHandler {

	public void entitySearch(Player player) throws CommandException {
		WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

		Location playerLocation = player.getLocation();
		Vector delta = playerLocation.getDirection().multiply(new Vector(4, 4, 4));

		Location leftCorner = new Location(player.getWorld(), (int) playerLocation.getX(),
				(int) playerLocation.getY(), (int) playerLocation.getZ());
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
		// case DOWN:
		// break;
		// case EAST_NORTH_EAST:
		// break;
		// case EAST_SOUTH_EAST:
		// break;
		// case NORTH_NORTH_EAST:
		// break;
		// case NORTH_NORTH_WEST:
		// break;
		// case SELF:
		// break;
		// case SOUTH_SOUTH_EAST:
		// break;
		// case SOUTH_SOUTH_WEST:
		// break;
		// case UP:
		// break;
		// case WEST_NORTH_WEST:
		// break;
		// case WEST_SOUTH_WEST:
		// break;
		default:
			break;
		}

		// World world = player.getWorld();
		// world.getBlockAt(leftCorner).setType(Material.NETHERRACK);
		// world.getBlockAt(rightCorner).setType(Material.GLOWSTONE);

		List<Entity> entities = new ArrayList<Entity>();
		AxisAlignedBB bBox = new AxisAlignedBB(leftCorner.getX(), leftCorner.getY(), leftCorner.getZ(),
				rightCorner.getX(), rightCorner.getY(), rightCorner.getZ());

		int count = 0;
		int distance = 4;
		while (distance < EntityInfo.maxDistance) {
			for (Object object : worldServer.getEntities(entityPlayer, bBox)) {
				if (object instanceof EntityLiving) {
					Entity entity = ((net.minecraft.server.v1_10_R1.Entity) object).getBukkitEntity();
					if (!(entity instanceof LivingEntity))
						continue;
					LivingEntity lEntity = (LivingEntity) entity;
					if (entities.contains(lEntity))
						continue;
					entities.add(lEntity);

					entityInfo(player, lEntity);
					if (count++ == 0)
						EntityInfo.INSTANCE.setLast(player, lEntity);
				}
				if (count == EntityInfo.maxResults)
					return;
			}

			bBox.c(delta.getX(), delta.getY(), delta.getZ());
			distance += 4;

			// world.getBlockAt(leftCorner.add(delta.toLocation(world))).setType(Material.NETHERRACK);
			// world.getBlockAt(rightCorner.add(delta.toLocation(world))).setType(Material.GLOWSTONE);
		}

		if (count == 0)
			throw new CommandException(CommandMessage.NOENTITIES);
	}

	public void entityInfo(Player player, LivingEntity entity) {
		if (!(entity instanceof CraftLivingEntity))
			return;

		Location entityLocation = entity.getLocation();
		EntityInfo.INSTANCE.sendMsg(player,
				CommandMessage.INFO.toString(getEntityName(entity), entity.getEntityId(),
						getEntityHealth(entity), entityLocation.getX(), entityLocation.getY(),
						entityLocation.getZ(), getMovements(player.getLocation(), entityLocation)));
	}

	private String getEntityName(Entity entity) {
		String name = entity.getCustomName();

		if (name == null)
			name = entity.getName();

		String color;
		if (entity instanceof CraftAnimals)
			color = EntityInfo.INSTANCE.getColor("animal");
		else if (entity instanceof CraftMonster)
			color = EntityInfo.INSTANCE.getColor("monster");
		else if (entity instanceof CraftPlayer) {
			name = ((CraftPlayer) entity).getName();
			color = EntityInfo.INSTANCE.getColor("player");
		} else
			color = EntityInfo.INSTANCE.getColor("animal");

		return color + name;
	}

	private String getEntityHealth(LivingEntity entity) {
		int health = (int) ((entity.getHealth() / getMaxHealth(entity)) * 100);

		ChatColor color;
		if (health < 34)
			color = ChatColor.RED;
		else if (health < 67)
			color = ChatColor.GOLD;
		else
			color = ChatColor.DARK_GREEN;

		return color.toString() + health + "%";
	}

	private double getMaxHealth(LivingEntity entity) {
		return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		// if (entity instanceof CraftMonster)
		// return 20;
		// if (entity instanceof CraftPlayer)
		// return 20;
		// if (entity instanceof CraftChicken)
		// return 4;
		// if (entity instanceof CraftWolf)
		// return 8;
		// return 10;
	}

	private double getDegrees(double yaw) {
		yaw = (yaw - 90) % 360;
		if (yaw < 0)
			yaw += 360;

		return yaw;
	}

	private BlockFace getCardinal(Location location) {
		return getCardinal(getDegrees(location.getYaw()));
	}

	private BlockFace getCardinal(double degrees) {
		if (degrees >= 315 || degrees < 45)
			return BlockFace.NORTH;
		if (degrees < 135)
			return BlockFace.EAST;
		if (degrees < 225)
			return BlockFace.SOUTH;
		return BlockFace.WEST;
	}

	private BlockFace getDirection(Location location) {
		return getDirection(getDegrees(location.getYaw()));
	}

	private BlockFace getDirection(double degrees) {
		/*
		 * 0 - 22.4 north 22.5 - 67.4 67.5 - 112.4 east 112.5 - 157.4 157.5 -
		 * 202.4 south 202.5 - 247.4 247.5 - 292.4 west 292.5 - 337.4 337.5 -
		 * 359.9 north
		 */

		if (degrees >= 337.5 || degrees < 22.5)
			return BlockFace.NORTH;
		if (degrees >= 22.5 && degrees < 67.5)
			return BlockFace.NORTH_EAST;
		if (degrees >= 67.5 && degrees < 112.5)
			return BlockFace.EAST;
		if (degrees >= 112.5 && degrees < 157.5)
			return BlockFace.SOUTH_EAST;
		if (degrees >= 157.5 && degrees < 202.5)
			return BlockFace.SOUTH;
		if (degrees >= 202.5 && degrees < 247.5)
			return BlockFace.SOUTH_WEST;
		if (degrees >= 247.5 && degrees < 292.5)
			return BlockFace.WEST;
		return BlockFace.NORTH_WEST;
	}

	private String getMovements(Location playerLocation, Location entityLocation) {
		entityLocation.subtract(playerLocation);
		BlockFace cardinal = getCardinal(playerLocation);

		List<String> movements = new ArrayList<String>();
		addMovement(movements, cardinal, 'x', (int) entityLocation.getX());
		addMovement(movements, cardinal, 'z', (int) entityLocation.getZ());
		addMovement(movements, cardinal, 'y', (int) entityLocation.getY());

		if (movements.size() == 0)
			return CommandMessage.NOMOVEMENT.toString();

		return CommandMessage.MOVEMENT.toString(joinAsString(movements, ", "));
	}

	private void addMovement(List<String> movements, BlockFace cardinal, char axis, int coordinate) {
		if (coordinate == 0)
			return;
		movements.add(Math.abs(coordinate) + " " + getOrientation(cardinal, axis, coordinate));
	}

	private String getOrientation(BlockFace cardinal, char axis, int coordinate) {
		switch (axis) {
		case 'x':
			switch (cardinal) {
			case NORTH:
				if (coordinate < 0)
					return "forward";
				return "backward";
			case SOUTH:
				if (coordinate < 0)
					return "backward";
				return "forward";
			case EAST:
				if (coordinate < 0)
					return "left";
				return "right";
			case WEST:
				if (coordinate < 0)
					return "right";
				return "left";
			}
		case 'y':
			if (coordinate < 0)
				return "down";
			return "up";
		case 'z':
			switch (cardinal) {
			case NORTH:
				if (coordinate < 0)
					return "right";
				return "left";
			case SOUTH:
				if (coordinate < 0)
					return "left";
				return "right";
			case EAST:
				if (coordinate < 0)
					return "forward";
				return "backward";
			case WEST:
				if (coordinate < 0)
					return "backward";
				return "forward";
			}
		}

		return null;
	}

	private String joinAsString(List<String> list, String separator) {
		if (list.size() == 0)
			return "";

		StringBuilder sb = new StringBuilder(list.get(0));
		for (int i = 1; i < list.size(); i++)
			sb.append(separator + list.get(i));

		return sb.toString();
	}

}
