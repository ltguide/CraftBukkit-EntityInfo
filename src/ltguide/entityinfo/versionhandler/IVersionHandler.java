package ltguide.entityinfo.versionhandler;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ltguide.entityinfo.CommandException;

public interface IVersionHandler {

	public void entitySearch(Player player) throws CommandException;

	public void entityInfo(Player player, LivingEntity entity);

}
