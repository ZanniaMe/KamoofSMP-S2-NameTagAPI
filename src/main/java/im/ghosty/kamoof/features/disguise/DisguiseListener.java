package im.ghosty.kamoof.features.disguise;

import im.ghosty.kamoof.KamoofPlugin;
import im.ghosty.kamoof.api.KamoofSMP;
import im.ghosty.kamoof.api.events.KamoofDisguiseEvent;
import im.ghosty.kamoof.features.Feature;
import im.ghosty.kamoof.features.drophead.SkullManager;
import im.ghosty.kamoof.utils.Message;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import im.ghosty.nickapi.NickAPI;
import gg.lode.nametagapi.NameTagAPI;
import gg.lode.nametagapi.INameTagAPI;

import java.util.Map;

/**
 * {@link Feature} pour gérer le déguisement du joueur et son utilisation.
 *
 * @since 1.0
 */
public final class DisguiseListener extends Feature {
	
	/**
	 * Pourquoi installer le KamoofSMP pour pas l'utiliser :clown:
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	/**
	 * Utilisation d'une tête
	 *
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		OfflinePlayer target = SkullManager.getOwner(event.getItem());
		if (target == null)
			return;
		
		switch (event.getAction()) {
			case LEFT_CLICK_BLOCK, LEFT_CLICK_AIR, PHYSICAL -> {
				return;
			}
			case RIGHT_CLICK_BLOCK -> {
				if (!KamoofPlugin.config().getBoolean("disguise.place-head"))
					event.setCancelled(true);
				return;
			}
		}
		
		String name = KamoofSMP.getInstance().getName(target);
		if (name == null)
			name = SkullManager.getName(event.getItem());
		
		KamoofDisguiseEvent disguiseEvent = new KamoofDisguiseEvent(player, name);
		Bukkit.getPluginManager().callEvent(disguiseEvent);
		if (disguiseEvent.isCancelled())
			return;
		
		if (player.getGameMode() != GameMode.CREATIVE)
			event.getItem().setAmount(event.getItem().getAmount() - 1);

		INameTagAPI api = NameTagAPI.getApi();

		if (KamoofPlugin.config().getBoolean("disguise.give-back") && api.hasNick(player)) {
			ItemStack item = SkullManager.getSkull(api.getNick(player));
			if (!player.getInventory().addItem(item).isEmpty())
				player.getWorld().dropItem(player.getLocation(), item);
		}
		
		DisguiseManager.disguise(player, name);
		Message.send(player, "messages.disguised", Map.of("player", KamoofSMP.getInstance().getName(player), "nick", name));
	}
	
	/**
	 * Perte du déguisement à la mort
	 *
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent event) {

		INameTagAPI api = NameTagAPI.getApi();
		if (api == null) return; // Name Tag not loaded

		Player player = event.getEntity();
		if (!api.hasNick(player))
			return;
		String disguise = api.getNick(player);
		KamoofSMP.getInstance().disguise(player, null);
		Message.send(player, "messages.lostdisguise", Map.of("player", KamoofSMP.getInstance().getName(player), "nick", disguise));
		
		DisguiseRestaurer.set(player.getUniqueId(), null);
	}
	
	/**
	 * Fix du "You logged in from another location"
	 *
	 * @since 1.3
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRequestJoin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
			return;
		Bukkit.getScheduler().runTask(KamoofPlugin.getInstance(), () -> {
			//Player player = NickAPI.getPlayerOfNickedName(event.getName());
			Player player = null;
			if (player == null)
				return;
			//NickAPI.setGameProfileName(player, NickAPI.getOriginalGameProfileName(player));
			//NickAPI.refreshPlayer(player);
			
			Bukkit.getScheduler().runTaskLater(KamoofPlugin.getInstance(), () -> {
				if (!player.isOnline())
					return;
				//NickAPI.setGameProfileName(player, event.getName());
				//NickAPI.refreshPlayer(player);
			}, 1L);
		});
	}
	
}
