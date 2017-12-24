package ltguide.entityinfo;

import java.util.IllegalFormatException;

import org.bukkit.ChatColor;

public enum CommandMessage {
	ARGLENGTH(
			"&5Syntax: &f%s&n@ - search in the direction of cross-hair&nEntity ID - from F3 overlay&n# - last entity ID&nEntity Type - Skeleton/Spider/Cow/..."),
	PERMISSION("&cYou do not have permission."),
	CONSOLE("This functionality does not work from the console."),
	NOSUCHENTITY("&cThere is no such entity near you."),
	NOSUCHENTITIES("&cThere are no such entities near you."),
	NOENTITIES("&cThere are no entities in that direction."),
	RELOADABLE("&5Syntax: &f/%s reload"),
	RELOADDONE("&aReloaded configuration."),
	NOLASTENTITY("&cYou need to search for an Entity ID first."),
	INFO("%s &f(#%s) %s &f- &e%.0f:%.0f:%.0f &f- %s"),
	MOVEMENT("&amove %s"),
	NOMOVEMENT("&5no movement necessary");

	private String message;

	CommandMessage(String message) {
		setMessage(message);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return ChatColor.translateAlternateColorCodes('&', message);
//		message.replaceAll("(?i)&([0-F])", "\u00A7$1");
	}

	public String toString(Object... args) {
		try {
			return String.format(this.toString(), args);
		} catch (IllegalFormatException e) {
			return ChatColor.RED + "Error in " + this.name() + " translation! (" + e.getMessage() + ")";
		}
	}
}
