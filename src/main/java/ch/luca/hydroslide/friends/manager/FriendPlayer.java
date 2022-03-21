package ch.luca.hydroslide.friends.manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import ch.luca.hydroslide.friends.Friends;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class FriendPlayer {
	
	@Getter
	private ProxiedPlayer player;
	
	@Getter
	private CopyOnWriteArrayList<UUID> friends, friendsRequests;
	
	@Getter
	private String server;
	@Getter
	private boolean msg, requests, jump;

	@Getter
	@Setter
	private boolean msgSpy;

	@Getter
	@Setter
	private long lastOnline;
	
	public FriendPlayer(ProxiedPlayer player) {
		this.player = player;
		Friends.getInstance().getDatabase().executeQuery("SELECT * FROM Friends WHERE UUID='" + this.player.getUniqueId().toString() + "'", true, new Consumer<ResultSet>() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void accept(ResultSet rs) {
				if(rs == null) return;
				try {
					if(rs.next()) {
						friends = Friends.getInstance().getUtil().stringToList(rs.getString("Friends"));
						friendsRequests = Friends.getInstance().getUtil().stringToList(rs.getString("FriendsRequests"));
						
						if(friends != null && !friends.isEmpty()) {
							for(UUID uuid : friends) {
								ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
								if(proxiedPlayer == null) continue;
								proxiedPlayer.sendMessage(Friends.getPrefix() + "Dein Freund §e" + player.getName() + " §7ist nun §aonline§7.");
							}
						}
						
						server = Friends.getInstance().getDefaultServer();
						lastOnline = System.currentTimeMillis();
						msg = rs.getBoolean("MSG");
						requests = rs.getBoolean("Requests");
						jump = rs.getBoolean("Jump");
						
						String statement = "UPDATE Friends SET Name='" + player.getName() + "', Server='" + server + "', LastOnline="
								+ lastOnline + " WHERE UUID='" + player.getUniqueId().toString() + "'";
						Friends.getInstance().getDatabase().update(statement, false);
						
						if(friendsRequests != null && !friendsRequests.isEmpty()) {
							TextComponent tc1 = new TextComponent(Friends.getPrefix() + "§7Du hast ");
							
							TextComponent tc2 = new TextComponent();
							tc2.setText("§e" + friendsRequests.size() + " " + (friendsRequests.size() == 1 ? "Anfrage" : "Anfragen"));
							tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7öffne deine Anfragen.").create()));
							tc2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend requests"));
							
							TextComponent tc3 = new TextComponent(" §3offen.");
							
							player.sendMessage(new BaseComponent[] { tc1, tc2, tc3 });
						}
					} else {
						friends = new CopyOnWriteArrayList<UUID>();
						friendsRequests = new CopyOnWriteArrayList<UUID>();
						
						server = Friends.getInstance().getDefaultServer();
						lastOnline = System.currentTimeMillis();
						msg = true;
						requests = true;
						jump = true;
						
						String statement = "INSERT INTO Friends VALUES('" + player.getUniqueId().toString()
								+ "', '" + player.getName() + "', NULL, NULL, '" + server + "', " + lastOnline
								+ ", " + Boolean.valueOf(true) + ", " + Boolean.valueOf(true) + ", " + Boolean.valueOf(true) + ", '', '')";
						Friends.getInstance().getDatabase().update(statement, false);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		Friends.getInstance().getFriendManager().hasMSGSpy(player.getUniqueId(), msgSpyEnabled -> this.msgSpy = msgSpyEnabled);
	}
	@SuppressWarnings("deprecation")
	public void leave() {
		if(Friends.getInstance().isPlayerCacheExist(this.player.getName())) {
			Friends.getInstance().removePlayerCache(this.player.getName());
		}
		if(this.friends != null && !this.friends.isEmpty()) {
			for(UUID uuid : this.friends) {
				ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
				if(proxiedPlayer == null) continue;
				proxiedPlayer.sendMessage(Friends.getPrefix() + "Dein Freund §e" + this.player.getName() + " §7ist nun §coffline§7.");
			}
		}
		this.lastOnline = System.currentTimeMillis();
		String statement = "UPDATE Friends SET Server=NULL, LastOnline="
				+ this.lastOnline + " WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, true);
	}
	public void saveFriends(boolean async) {
		String friendString = Friends.getInstance().getUtil().listToString(this.friends);
		String statement = "UPDATE Friends SET Friends=" + friendString + " WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, async);
	}
	public void saveFriendRequests(boolean async) {
		String friendString = Friends.getInstance().getUtil().listToString(this.friendsRequests);
		String statement = "UPDATE Friends SET FriendsRequests=" + friendString + " WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, async);
	}
	public void switchServer(String server) {
		this.server = server;
		String statement = "UPDATE Friends SET Server='" + server + "' WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, true);
	}
	public void setJump(boolean value) {
		this.jump = value;
		String statement = "UPDATE Friends SET Jump=" + this.jump + " WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, true);
	}
	public void setMSG(boolean value) {
		this.msg = value;
		String statement = "UPDATE Friends SET MSG=" + this.msg + " WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, true);
	}
	public void setRequests(boolean value) {
		this.requests = value;
		String statement = "UPDATE Friends SET Requests=" + this.requests + " WHERE UUID='" + this.player.getUniqueId().toString() + "'";
		Friends.getInstance().getDatabase().update(statement, true);
	}
	public UUID getUUID() {
		return this.player.getUniqueId();
	}
	public String getName() {
		return this.player.getName();
	}
}
