package ch.luca.hydroslide.friends.listener;

import ch.luca.hydroslide.friends.Friends;
import ch.luca.hydroslide.friends.manager.FriendPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {
	
	@EventHandler
	public void onSwitch(ServerSwitchEvent e) {
		ProxiedPlayer p = e.getPlayer();
		FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
		if(friendPlayer != null) {
			friendPlayer.switchServer(p.getServer().getInfo().getName());
		}
	}

}
