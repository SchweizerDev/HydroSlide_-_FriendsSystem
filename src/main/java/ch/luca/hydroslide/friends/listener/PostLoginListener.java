package ch.luca.hydroslide.friends.listener;

import ch.luca.hydroslide.friends.Friends;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {
	
	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		ProxiedPlayer p = e.getPlayer();
		Friends.getInstance().getPlayerFromCache(p.getName());
	}

}
