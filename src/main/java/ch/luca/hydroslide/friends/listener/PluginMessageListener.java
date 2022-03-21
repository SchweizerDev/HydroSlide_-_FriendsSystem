package ch.luca.hydroslide.friends.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import ch.luca.hydroslide.friends.Friends;
import ch.luca.hydroslide.friends.manager.FriendPlayer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());

		if(!e.getTag().equals("BungeeCord")) return;

		String subChannel = in.readUTF();
		if(!subChannel.equals("Friends")) return;

		String category = in.readUTF();
		switch(category) {
		case "Settings": {
			String typ = in.readUTF();
			if(typ.equalsIgnoreCase("Jump")) {
				String playerName = in.readUTF();
				ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
				if(p == null) return;

				boolean value = in.readBoolean();

				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer != null) {
					friendPlayer.setJump(value);
				}
			} else if(typ.equalsIgnoreCase("MSG")) {
				String playerName = in.readUTF();
				ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
				if(p == null) return;

				boolean value = in.readBoolean();

				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer != null) {
					friendPlayer.setMSG(value);
				}
			} else if(typ.equalsIgnoreCase("Requests")) {
				String playerName = in.readUTF();
				ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
				if(p == null) return;

				boolean value = in.readBoolean();

				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer != null) {
					friendPlayer.setRequests(value);
				}
			}
			return;
		}
		case "Jump": {
			String playerName = in.readUTF();
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
			if(p == null) return;

			String otherName = in.readUTF();
			ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(otherName);
			if(proxiedPlayer == null) {
				p.sendMessage(Friends.getPlayerNotOnline());
				return;
			}
			FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
			if(friendProxiedPlayer == null) {
				p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
				return;
			}
			if(!friendProxiedPlayer.getFriends().contains(p.getUniqueId())) {
				p.sendMessage(Friends.getPrefix() + "§cMit diesem Spieler bist du nicht befreundet.");
				return;
			}
			if(!friendProxiedPlayer.isJump()) {
				p.sendMessage(Friends.getPrefix() + "Dein Freund hat Nachspringen §cdeaktivert§7.");
				return;
			}
			p.connect(proxiedPlayer.getServer().getInfo());
			return;
		}
		case "RemoveFriend": {
			String playerName = in.readUTF();
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
			if(p == null) return;

			FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
			if(friendPlayer == null) return;

			String otherName = in.readUTF();

			ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(otherName);
			if(proxiedPlayer != null) {
				FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
				if(friendProxiedPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(!friendPlayer.getFriends().contains(proxiedPlayer.getUniqueId())) {
					p.sendMessage(Friends.getPrefix() + "§cMit diesem Spieler bist du nicht befreundet.");
					return;
				}
				friendPlayer.getFriends().remove(proxiedPlayer.getUniqueId());
				friendPlayer.saveFriends(true);
				friendProxiedPlayer.getFriends().remove(p.getUniqueId());
				friendProxiedPlayer.saveFriends(true);
				p.sendMessage(Friends.getPrefix() + "Die Freundschaft mit §e" + proxiedPlayer.getName() + " §7wurde aufgelöst.");
				proxiedPlayer.sendMessage(Friends.getPrefix() + "Die Freundschaft mit §e" + p.getName() + " §7wurde aufgelöst.");
				return;
			}
			Friends.getInstance().getFriendManager().getFriendData(otherName, true, new Consumer<ResultSet>() {

				@Override
				public void accept(ResultSet rs) {
					if(rs == null) {
						p.sendMessage(Friends.getPrefix() + "§cMit diesem Spieler bist du nicht befreundet.");
						return;
					}
					try {
						UUID uuid = UUID.fromString(rs.getString("UUID"));
						if(!friendPlayer.getFriends().contains(uuid)) {
							p.sendMessage(Friends.getPrefix() + "§cMit diesem Spieler bist du nicht befreundet.");
							return;
						}
						friendPlayer.getFriends().remove(uuid);
						friendPlayer.saveFriends(false);
						p.sendMessage(Friends.getPrefix() + "Die Freundschaft mit §e" + rs.getString("Name") + " §7wurde aufgelöst.");

						CopyOnWriteArrayList<UUID> friends = Friends.getInstance().getUtil().stringToList(rs.getString("Friends"));
						friends.remove(p.getUniqueId());
						Friends.getInstance().getFriendManager().saveFriends(uuid.toString(), false, friends);
						return;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}
		case "Accept": {
			String playerName = in.readUTF();
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
			if(p == null) return;

			FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
			if(friendPlayer == null) return;

			String otherName = in.readUTF();
			ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(otherName);
			if(proxiedPlayer != null) {
				FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
				if(friendProxiedPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(!friendPlayer.getFriendsRequests().contains(proxiedPlayer.getUniqueId())) {
					p.sendMessage(Friends.getPrefix() + "§cMit diesem Spieler bist du nicht befreundet.");
					return;
				}
				friendPlayer.getFriendsRequests().remove(proxiedPlayer.getUniqueId());
				friendPlayer.getFriends().add(proxiedPlayer.getUniqueId());
				friendPlayer.saveFriends(true);
				friendPlayer.saveFriendRequests(true);
				friendProxiedPlayer.getFriends().add(p.getUniqueId());
				friendProxiedPlayer.saveFriends(true);
				p.sendMessage(Friends.getPrefix() + "Du bist nun mit §e" + proxiedPlayer.getName() + " §7befreundet.");
				proxiedPlayer.sendMessage(Friends.getPrefix() + "Du bist nun mit §e" + p.getName() + " §7befreundet.");
				return;
			}
			Friends.getInstance().getFriendManager().getFriendData(otherName, true, new Consumer<ResultSet>() {

				@Override
				public void accept(ResultSet rs) {
					if(rs == null) {
						p.sendMessage(Friends.getPrefix() + "§cDu hast keine Anfrage von diesem Spieler.");
						return;
					}
					try {
						UUID uuid = UUID.fromString(rs.getString("UUID"));
						if(!friendPlayer.getFriendsRequests().contains(uuid)) {
							p.sendMessage(Friends.getPrefix() + "§cDu hast keine Anfrage von diesem Spieler.");
							return;
						}
						friendPlayer.getFriendsRequests().remove(uuid);
						friendPlayer.getFriends().add(uuid);
						friendPlayer.saveFriends(false);
						friendPlayer.saveFriendRequests(false);
						p.sendMessage(Friends.getPrefix() + "Du bist nun mit §e" + rs.getString("Name") + " §7befreundet.");

						CopyOnWriteArrayList<UUID> friends = Friends.getInstance().getUtil().stringToList(rs.getString("Friends"));
						friends.add(p.getUniqueId());
						Friends.getInstance().getFriendManager().saveFriends(uuid.toString(), false, friends);
						return;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}
		case "Deny": {
			String playerName = in.readUTF();
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
			if(p == null) return;

			FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
			if(friendPlayer == null) return;

			String otherName = in.readUTF();

			ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(otherName);
			if(proxiedPlayer != null) {
				if(!friendPlayer.getFriendsRequests().contains(proxiedPlayer.getUniqueId())) {
					p.sendMessage(Friends.getPrefix() + "§cDu hast keine Anfrage von diesem Spieler.");
					return;
				}
				friendPlayer.getFriendsRequests().remove(proxiedPlayer.getUniqueId());
				friendPlayer.saveFriendRequests(true);
				p.sendMessage(Friends.getPrefix() + "Du hast die Anfrage von §e" + proxiedPlayer.getName() + " §7abgelehnt.");
				proxiedPlayer.sendMessage(Friends.getPrefix() + "Der Spieler §e" + p.getName() + " §7hat deine Anfrage abgelehnt.");
				return;
			}
			Friends.getInstance().getFriendManager().getFriendData(otherName, true, new Consumer<ResultSet>() {

				@Override
				public void accept(ResultSet rs) {
					if(rs == null) {
						p.sendMessage(Friends.getPrefix() + "§cDu hast keine Anfrage von diesem Spieler.");
						return;
					}
					try {
						UUID uuid = UUID.fromString(rs.getString("UUID"));
						if(!friendPlayer.getFriendsRequests().contains(uuid)) {
							p.sendMessage(Friends.getPrefix() + "§cDu hast keine Anfrage von diesem Spieler.");
							return;
						}
						friendPlayer.getFriendsRequests().remove(uuid);
						friendPlayer.saveFriendRequests(false);
						p.sendMessage(Friends.getPrefix() + "Du hast die Anfrage von §e" + rs.getString("Name") + " §7abgelehnt.");
						return;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}
		case "AddFriend": {
			String playerName = in.readUTF();
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playerName);
			if(p == null) return;
			FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
			if(friendPlayer == null) {
				p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
				return;
			}
			String otherName = in.readUTF();
			if(otherName.equalsIgnoreCase(p.getName())) {
				p.sendMessage(Friends.getPrefix() + "§cDu kannst dich nicht selber als Freund hinzufügen.");
				return;
			}
			ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(otherName);
			if(proxiedPlayer != null) {
				FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
				if(friendProxiedPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(!friendProxiedPlayer.isRequests()) {
					p.sendMessage(Friends.getPrefix() + "Dieser Spieler erlaubt keine Anfragen.");
					return;
				}
				if(friendProxiedPlayer.getFriends().contains(p.getUniqueId())) {
					p.sendMessage(Friends.getPrefix() + "Du bist bereits mit diesem Spieler befreundet.");
					return;
				}
				if(friendProxiedPlayer.getFriendsRequests().contains(p.getUniqueId())) {
					p.sendMessage(Friends.getPrefix() + "Du hast bereits eine Anfrage gesendet.");
					return;
				}
				if(friendPlayer.getFriendsRequests().contains(proxiedPlayer.getUniqueId())) {
					p.sendMessage(Friends.getPrefix() + "Dieser Spieler hat dir bereits eine Anfrage gesendet.");
					return;
				}
				friendProxiedPlayer.getFriendsRequests().add(p.getUniqueId());
				friendProxiedPlayer.saveFriendRequests(true);
				p.sendMessage(Friends.getPrefix() + "Du hast §e" + proxiedPlayer.getName() + " §7eine Freundschaftsanfrage gesendet.");
				proxiedPlayer.sendMessage(Friends.getPrefix() + "Der Spieler §e" + p.getName() + " §7hat dir eine Freundschaftsanfrage gesendet.");
				TextComponent tc0 = new TextComponent(Friends.getPrefix());
				TextComponent tc1 = new TextComponent(" §8| ");
				TextComponent tc2 = new TextComponent();
				tc2.setText("§aAnnehmen");
				tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aAnnehmen").create()));
				tc2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + p.getName()));
				TextComponent tc3 = new TextComponent();
				tc3.setText("§cAblehnen");
				tc3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cAblehnen").create()));
				tc3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + p.getName()));
				proxiedPlayer.sendMessage(new BaseComponent[] { tc0, tc2, tc1, tc3 });
				return;
			}
			Friends.getInstance().getFriendManager().getFriendData(otherName, true, new Consumer<ResultSet>() {

				@Override
				public void accept(ResultSet rs) {
					if(rs == null) {
						p.sendMessage(Friends.getPlayerNeverOnline());
						return;
					}
					try {
						if(!rs.getBoolean("Requests")) {
							p.sendMessage(Friends.getPrefix() + "Dieser Spieler erlaubt keine Anfragen.");
							return;
						}
						UUID uuid = UUID.fromString(rs.getString("UUID"));
						if(friendPlayer.getFriends().contains(uuid)) {
							p.sendMessage(Friends.getPrefix() + "Du bist bereits mit diesem Spieler befreundet.");
							return;
						}
						if(friendPlayer.getFriendsRequests().contains(uuid)) {
							p.sendMessage(Friends.getPrefix() + "Dieser Spieler hat dir bereits eine Anfrage gesendet.");
							return;
						}
						CopyOnWriteArrayList<UUID> friendsRequests = Friends.getInstance().getUtil().stringToList(rs.getString("FriendsRequests"));
						if(friendsRequests.contains(p.getUniqueId())) {
							p.sendMessage(Friends.getPrefix() + "Du hast bereits eine Anfrage gesendet.");
							return;
						}
						friendsRequests.add(p.getUniqueId());
						Friends.getInstance().getFriendManager().saveFriendsRequests(uuid.toString(), false, friendsRequests);
						p.sendMessage(Friends.getPrefix() + "Du hast §e" + rs.getString("Name") + " §7eine Freundschaftsanfrage gesendet.");
						return;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}
		}
	}
}
