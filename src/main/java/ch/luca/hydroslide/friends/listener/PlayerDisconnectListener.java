package ch.luca.hydroslide.friends.listener;

import ch.luca.hydroslide.friends.Friends;
import ch.luca.hydroslide.friends.commands.MsgCommand;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnectListener implements Listener {
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		
		if(Friends.getInstance().getUtil().getDelay().containsKey(p.getName().toLowerCase())) {
			Friends.getInstance().getUtil().getDelay().remove(p.getName().toLowerCase());
		}
		if(MsgCommand.getReply().containsKey(p)) {
			MsgCommand.getReply().remove(p);
		}
		if(Friends.getInstance().isPlayerCacheExist(p.getName())) {
			Friends.getInstance().getPlayerFromCache(p.getName()).leave();
		}
	}

}
