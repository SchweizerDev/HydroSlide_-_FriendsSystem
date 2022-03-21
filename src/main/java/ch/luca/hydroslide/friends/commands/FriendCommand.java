package ch.luca.hydroslide.friends.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import ch.luca.hydroslide.friends.manager.FriendPlayer;
import ch.luca.hydroslide.friends.Friends;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import com.google.common.collect.ImmutableSet;

public class FriendCommand extends Command implements TabExecutor {

	public FriendCommand() {
		super("friend");
	}
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(Friends.getNoPlayer());
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if(!Friends.getInstance().getUtil().checkDelay(p.getName())) {
			p.sendMessage(Friends.getPrefix() + "§cBitte warte kurz...");
			return;
		}
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("removeall")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				p.sendMessage(Friends.getPrefix() + "§cAlle Freunde werden gelöscht...");
				ProxyServer.getInstance().getScheduler().runAsync(Friends.getInstance(), () -> {
					CopyOnWriteArrayList<UUID> friends = friendPlayer.getFriends();
					for(UUID uuid : friends) {
						ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
						if(proxiedPlayer != null) {
							if(Friends.getInstance().isPlayerCacheExist(proxiedPlayer.getName())) {
								FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
								if(friendProxiedPlayer.getFriends().contains(p.getUniqueId())) {
									friendProxiedPlayer.getFriends().remove(p.getUniqueId());
									friendProxiedPlayer.saveFriends(false);
									continue;
								}
							}
						}
						Friends.getInstance().getFriendManager().getFriends(uuid.toString(), false, new Consumer<CopyOnWriteArrayList<UUID>>() {

							@Override
							public void accept(CopyOnWriteArrayList<UUID> friends) {
								if(friends.contains(p.getUniqueId())) {
									friends.remove(p.getUniqueId());
									Friends.getInstance().getFriendManager().saveFriends(uuid.toString(), false, friends);
								}
							}
						});
					}
					int size = friends.size();
					friendPlayer.getFriends().clear();
					friendPlayer.saveFriends(false);
					p.sendMessage(Friends.getPrefix() + "Es wurden §e" + size + " §7Freunde gelöscht.");
				});
				return;
			} else if(args[0].equalsIgnoreCase("list")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(friendPlayer.getFriends().isEmpty()) {
					p.sendMessage(Friends.getPrefix() + "Du hast keine Freunde :(.");
					return;
				}
				ProxyServer.getInstance().getScheduler().runAsync(Friends.getInstance(), () -> {
					p.sendMessage(Friends.getPrefix() + "Deine Freunde:");
					for(UUID uuid : friendPlayer.getFriends()) {
						ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
						if(proxiedPlayer != null) {
							p.sendMessage(Friends.getPrefix() + "§8» §e" + proxiedPlayer.getName());
							continue;
						}
						Friends.getInstance().getFriendManager().getFriendData(uuid, false, new Consumer<ResultSet>() {

							@Override
							public void accept(ResultSet rs) {
								if(rs == null) return;
								try {
									if(rs.next()) {
										p.sendMessage(Friends.getPrefix() + "§8» §e" + rs.getString("Name"));
										return;
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
				return;
			} else if(args[0].equalsIgnoreCase("requests")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(friendPlayer.getFriendsRequests().isEmpty()) {
					p.sendMessage(Friends.getPrefix() + "§cDu hast keine Freundschaftsanfragen.");
					return;
				}
				TextComponent tc0 = new TextComponent("§8» ");
				TextComponent tc1 = new TextComponent(" §8| ");
				TextComponent tc2 = new TextComponent(" §8» ");
				ProxyServer.getInstance().getScheduler().runAsync(Friends.getInstance(), () -> {
					p.sendMessage(Friends.getPrefix() + "Deine Anfragen:");
					for(UUID uuid : friendPlayer.getFriendsRequests()) {
						ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
						if(proxiedPlayer != null) {
							TextComponent tc3 = new TextComponent();
							tc3.setText("§aAnnehmen");
							tc3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aAnnehmen").create()));
							tc3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + proxiedPlayer.getName()));
							TextComponent tc4 = new TextComponent();
							tc4.setText("§cAblehnen");
							tc4.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cAblehnen").create()));
							tc4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + proxiedPlayer.getName()));
							TextComponent tc5 = new TextComponent("§e" + proxiedPlayer.getName());
							
							p.sendMessage(new BaseComponent[] { tc0, tc5, tc2, tc3, tc1, tc4 });
							continue;
						}
						Friends.getInstance().getFriendManager().getFriendData(uuid, false, new Consumer<ResultSet>() {

							@Override
							public void accept(ResultSet rs) {
								if(rs == null) return;
								try {
									if(rs.next()) {
										String name = rs.getString("Name");
										TextComponent tc3 = new TextComponent();
										tc3.setText("§aAnnehmen");
										tc3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aAnnehmen").create()));
										tc3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + name));
										TextComponent tc4 = new TextComponent();
										tc4.setText("§cAblehnen");
										tc4.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cAblehnen").create()));
										tc4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + name));
										TextComponent tc5 = new TextComponent("§e" + name);
										
										p.sendMessage(new BaseComponent[] { tc0, tc5, tc2, tc3, tc1, tc4 });
										return;
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
				return;
			} else if(args[0].equalsIgnoreCase("add")) {
				p.sendMessage(Friends.getPrefixUse() + "friend add <Spieler>");
				return;
			} else if(args[0].equalsIgnoreCase("accept")) {
				p.sendMessage(Friends.getPrefixUse() + "friend accept <Spieler>");
				return;
			} else if(args[0].equalsIgnoreCase("deny")) {
				p.sendMessage(Friends.getPrefixUse() + "friend deny <Spieler>");
				return;
			} else if(args[0].equalsIgnoreCase("remove")) {
				p.sendMessage(Friends.getPrefixUse() + "friend remove <Spieler>");
				return;
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("add")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(args[1].equalsIgnoreCase(p.getName())) {
					p.sendMessage(Friends.getPrefix() + "§cDu kannst dich nicht selber als Freund hinzufügen.");
					return;
				}
				ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[1]);
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
				Friends.getInstance().getFriendManager().getFriendData(args[1], true, new Consumer<ResultSet>() {

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
			} else if(args[0].equalsIgnoreCase("accept")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(args[1].equalsIgnoreCase(p.getName())) {
					p.sendMessage(Friends.getPrefix() + "§cDu kannst dich nicht selber als Freund hinzufügen.");
					return;
				}
				if(args[1].equalsIgnoreCase("all")) {
					if(friendPlayer.getFriendsRequests().isEmpty()) {
						p.sendMessage(Friends.getPrefix() + "§cEs wurden keine Anfragen gefunden.");
						return;
					}
					ProxyServer.getInstance().getScheduler().runAsync(Friends.getInstance(), () -> {
						for(UUID uuid : friendPlayer.getFriendsRequests()) {
							ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
							if(proxiedPlayer != null) {
								FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
								if(friendProxiedPlayer == null) {
									p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
									continue;
								}
								friendPlayer.getFriendsRequests().remove(proxiedPlayer.getUniqueId());
								friendPlayer.getFriends().add(proxiedPlayer.getUniqueId());
								friendProxiedPlayer.getFriends().add(p.getUniqueId());
								friendProxiedPlayer.saveFriends(false);
								p.sendMessage(Friends.getPrefix() + "Du bist nun mit §e" + proxiedPlayer.getName() + " §7befreundet.");
								proxiedPlayer.sendMessage(Friends.getPrefix() + "Du bist nun mit §e" + p.getName() + " §7befreundet.");
								if(friendPlayer.getFriends().size() >= 36) {
									p.sendMessage(Friends.getPrefix() + "§cDie maximale Anzahl Freunde wurde erreicht.");
									return;
								}
								continue;
							}
							Friends.getInstance().getFriendManager().getFriendData(uuid, false, new Consumer<ResultSet>() {

								@Override
								public void accept(ResultSet rs) {
									if(rs == null) {
										return;
									}
									try {
										friendPlayer.getFriendsRequests().remove(uuid);
										friendPlayer.getFriends().add(uuid);
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
							if(friendPlayer.getFriends().size() >= 36) {
								p.sendMessage(Friends.getPrefix() + "§cDie maximale Anzahl Freunde wurde erreicht.");
								return;
							}
						}
						friendPlayer.saveFriendRequests(false);
						friendPlayer.saveFriends(false);
					});
					return;
				}
				ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[1]);
				if(proxiedPlayer != null) {
					FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
					if(friendProxiedPlayer == null) {
						p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
						return;
					}
					if(!friendPlayer.getFriendsRequests().contains(proxiedPlayer.getUniqueId())) {
						p.sendMessage(Friends.getPrefix() + "§cDu hast keine Anfrage von diesem Spieler.");
						return;
					}
					if(friendPlayer.getFriends().size() >= 36) {
						p.sendMessage(Friends.getPrefix() + "§cDie maximale Anzahl Freunde wurde erreicht.");
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
				Friends.getInstance().getFriendManager().getFriendData(args[1], true, new Consumer<ResultSet>() {

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
			} else if(args[0].equalsIgnoreCase("deny")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(args[1].equalsIgnoreCase(p.getName())) {
					p.sendMessage(Friends.getPrefix() + "§cDu kannst dich nicht selber als Freund hinzufügen.");
					return;
				}
				if(args[1].equalsIgnoreCase("all")) {
					if(friendPlayer.getFriendsRequests().isEmpty()) {
						p.sendMessage(Friends.getPrefix() + "§cEs wurden keine Anfragen gefunden.");
						return;
					}
					ProxyServer.getInstance().getScheduler().runAsync(Friends.getInstance(), () -> {
						for(UUID uuid : friendPlayer.getFriendsRequests()) {
							ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
							if(proxiedPlayer != null) {
								p.sendMessage(Friends.getPrefix() + "Du hast die Anfrage von §e" + proxiedPlayer.getName() + " §7abgelehnt.");
								proxiedPlayer.sendMessage(Friends.getPrefix() + "Der Spieler §e" + p.getName() + " §7hat deine Anfrage abgelehnt.");
								continue;
							}
							Friends.getInstance().getFriendManager().getFriendData(uuid, false, new Consumer<ResultSet>() {

								@Override
								public void accept(ResultSet rs) {
									if(rs == null) {
										return;
									}
									try {
										p.sendMessage(Friends.getPrefix() + "Du hast die Anfrage von §e" + rs.getString("Name") + " §7abgelehnt.");
										return;
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							});
						}
						friendPlayer.getFriendsRequests().clear();
						friendPlayer.saveFriendRequests(false);
					});
					return;
				}
				ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[1]);
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
				Friends.getInstance().getFriendManager().getFriendData(args[1], true, new Consumer<ResultSet>() {

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
			} else if(args[0].equalsIgnoreCase("remove")) {
				FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
				if(friendPlayer == null) {
					p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
					return;
				}
				if(args[1].equalsIgnoreCase(p.getName())) {
					p.sendMessage(Friends.getPrefix() + "§cDu kannst dich nicht selber als Freund entfernen.");
					return;
				}
				ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[1]);
				if(proxiedPlayer != null) {
					FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
					if(friendProxiedPlayer == null) {
						p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
						return;
					}
					if(!friendPlayer.getFriends().contains(proxiedPlayer.getUniqueId())) {
						p.sendMessage(Friends.getPrefix() + "Mit diesem Spieler bist du nicht befreundet.");
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
				Friends.getInstance().getFriendManager().getFriendData(args[1], true, new Consumer<ResultSet>() {

					@Override
					public void accept(ResultSet rs) {
						if(rs == null) {
							p.sendMessage(Friends.getPrefix() + "Mit diesem Spieler bist du nicht befreundet.");
							return;
						}
						try {
							UUID uuid = UUID.fromString(rs.getString("UUID"));
							if(!friendPlayer.getFriends().contains(uuid)) {
								p.sendMessage(Friends.getPrefix() + "Mit diesem Spieler bist du nicht befreundet.");
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
		}
		p.sendMessage(Friends.getPrefixUse() + "friend list §8- §7Zeigt deine Freunde §8(§7Nur Online§8)");
		p.sendMessage(Friends.getPrefixUse() + "friend add <Spieler> §8- §7Schicke dem Spieler eine Anfrage");
		p.sendMessage(Friends.getPrefixUse() + "friend remove <Spieler> §8- §7Entferne einen Freund");
		p.sendMessage(Friends.getPrefixUse() + "friend accept <Spieler|all> §8- §7Nehme die Anfrage/n an");
		p.sendMessage(Friends.getPrefixUse() + "friend deny <Spieler|all> §8- §7Lehne die Anfrage/n ab");
		p.sendMessage(Friends.getPrefixUse() + "friend removeall §8- §7Entferne alle deine Freunde");
		return;
	}
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(args.length > 2 || args.length < 2) {
			return ImmutableSet.of();
		}

		Set<String> matches = new HashSet<String>();
		if(args.length == 2) {
			if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("accept")
					|| args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("remove")) {
				String search = args[1].toLowerCase();
				for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
					if(player.getName().toLowerCase().startsWith( search ) ) {
						matches.add( player.getName() );
					}
				}
				if("all".startsWith(search)) matches.add( "all" );
				if("current".startsWith(search)) matches.add( "current" );
			}
		}
		return matches;
	}
}
