package im.ghosty.kamoof.commands;

import im.ghosty.kamoof.KamoofPlugin;
import im.ghosty.kamoof.api.KamoofSMP;
import im.ghosty.kamoof.api.events.KamoofUndisguiseEvent;
import im.ghosty.kamoof.features.disguise.DisguiseManager;
import im.ghosty.kamoof.features.drophead.SkullManager;
import im.ghosty.kamoof.utils.Lang;
import im.ghosty.kamoof.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import im.ghosty.nickapi.NickAPI;
import gg.lode.nametagapi.NameTagAPI;
import gg.lode.nametagapi.INameTagAPI;

import java.util.List;
import java.util.Map;

/**
 * La commande <code>/undisguise</code>, permettant de retirer son propre déguisement.
 * @since 1.0
 */
public final class UndisguiseCMD implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			Lang.send(sender, "PLAYER_ONLY");
			return true;
		}


		INameTagAPI api = NameTagAPI.getApi();
		
		if (api.hasNick(player)) {
			String disguise = api.getNick(player);
			
			Bukkit.getPluginManager().callEvent(new KamoofUndisguiseEvent(player, disguise));
			DisguiseManager.undisguise(player);
			
			if (KamoofPlugin.config().getBoolean("disguise.give-back")) {
				ItemStack item = SkullManager.getSkull(disguise);
				if (!player.getInventory().addItem(item).isEmpty())
					player.getWorld().dropItem(player.getLocation(), item);
			}
			Message.send(player, "messages.undisguise", Map.of("player", KamoofSMP.getInstance().getName(player), "nick", disguise));
		} else {
			Message.send(player, "messages.nodisguise", Map.of("player", KamoofSMP.getInstance().getName(player)));
		}
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return List.of();
	}
	
}
