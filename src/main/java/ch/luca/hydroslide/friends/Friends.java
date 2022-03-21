package ch.luca.hydroslide.friends;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import ch.luca.hydroslide.friends.commands.FriendCommand;
import ch.luca.hydroslide.friends.commands.MsgCommand;
import ch.luca.hydroslide.friends.commands.MsgSpyCommand;
import ch.luca.hydroslide.friends.commands.ReplyCommand;
import ch.luca.hydroslide.friends.database.Database;
import ch.luca.hydroslide.friends.listener.PlayerDisconnectListener;
import ch.luca.hydroslide.friends.listener.PluginMessageListener;
import ch.luca.hydroslide.friends.listener.PostLoginListener;
import ch.luca.hydroslide.friends.listener.ServerSwitchListener;
import ch.luca.hydroslide.friends.manager.FriendManager;
import ch.luca.hydroslide.friends.manager.FriendPlayer;
import ch.luca.hydroslide.friends.util.Util;
import org.apache.commons.dbcp2.BasicDataSource;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Friends extends Plugin {

	@Getter
	private static Friends instance;
	@Getter
	private static String prefix = "§bHydroSlide §8» §7";
	@Getter
	private static String msg = "§bMSG §a✧ §7";
	@Getter
	private static String prefixUse = "§bHydroSlide §8» §cBenutze: §e/";
	@Getter
	private static String noPlayer = "§bHydroSlide §8» §cBenutze diesen Befehl bitte im Spiel.";
	@Getter
	private static String playerNotOnline = "§bHydroSlide §8» §cDieser Spieler ist nicht online.";
	@Getter
	private static String noPermission = "§bHydroSlide §8» §cFür diesen Befehl fehlt dir die Berechtigung.";
	@Getter
	private static String playerNeverOnline = "§bHydroSlide §8» §cDieser Spieler wurde in der Datenbank nicht gefunden.";

	@Getter
	private BasicDataSource sqlPool;
	
	@Getter
	private FriendManager friendManager;
	@Getter
	private Database database;
	@Getter
	private Util util;
	
	@Getter
	private ConcurrentHashMap<String, FriendPlayer> playerCache = new ConcurrentHashMap<String, FriendPlayer>();
	
	@Getter
	private String defaultServer;
	
	@Override
	public void onEnable() {
		instance = this;
		
		try {
			if(!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}
			File configFile = new File(getDataFolder().getPath(), "config.yml");
			if(!configFile.exists()) {
				configFile.createNewFile();
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
				config.set("MySQL.Host", "host");
				config.set("MySQL.Database", "db");
				config.set("MySQL.Password", "pw");
				config.set("MySQL.User", "user");
				config.set("MySQL.Port", 3306);
				config.set("ConnectServer", "lobby");
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
			} else {
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
				
				try {
					sqlPool = new BasicDataSource();

					sqlPool.setDriverClassName("com.mysql.jdbc.Driver");
					sqlPool.setUrl("jdbc:mysql://" + config.getString("MySQL.Host") + ":" + config.getInt("MySQL.Port") + "/" + config.getString("MySQL.Database"));
					sqlPool.setUsername(config.getString("MySQL.User"));
					sqlPool.setPassword(config.getString("MySQL.Password"));

					sqlPool.setMaxIdle(30);
					sqlPool.setMinIdle(5);
					sqlPool.setDriverClassLoader(Friends.class.getClassLoader());
					
					defaultServer = config.getString("ConnectServer");

					System.out.println("[Friends] MySQL connected!");
					
					Connection connection = null;
					try {
						connection = sqlPool.getConnection();

						PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS Friends (UUID VARCHAR(100), Name VARCHAR(100), Friends LONGTEXT, FriendsRequests LONGTEXT, Server VARCHAR(100), LastOnline BIGINT, MSG TINYINT(1), Requests TINYINT(1), Jump TINYINT(1), SkinValue TEXT(10000), SkinSignature TEXT(10000), PRIMARY KEY (UUID))");
						preparedStatement.execute();
						preparedStatement.close();
					} catch(SQLException e) {
						e.printStackTrace();
					} finally {
						try {
							if(connection != null) {
								connection.close();
							}
						} catch(Exception exc) {
							exc.printStackTrace();
						}
					}

					try {
						connection = sqlPool.getConnection();

						PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS Friends_msgspy (UUID VARCHAR(36), PRIMARY KEY (UUID))");
						preparedStatement.execute();
						preparedStatement.close();
					} catch(SQLException e) {
						e.printStackTrace();
					} finally {
						try {
							if(connection != null) {
								connection.close();
							}
						} catch(Exception exc) {
							exc.printStackTrace();
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		friendManager = new FriendManager();
		database = new Database();
		util = new Util();
		
		PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
		
		pluginManager.registerCommand(this, new FriendCommand());
		pluginManager.registerCommand(this, new MsgCommand());
		pluginManager.registerCommand(this, new ReplyCommand());
		pluginManager.registerCommand(this, new MsgSpyCommand());

		pluginManager.registerListener(this, new PlayerDisconnectListener());
		pluginManager.registerListener(this, new PostLoginListener());
		pluginManager.registerListener(this, new ServerSwitchListener());
		pluginManager.registerListener(this, new PluginMessageListener());
	}
	@Override
	public void onDisable() {
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			FriendPlayer friendPlayer = getPlayerFromCache(p.getName());
			if(friendPlayer != null) {
				friendPlayer.leave();
			}
		}
		try {
			sqlPool.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public boolean isPlayerCacheExist(String name) {
		return playerCache.containsKey(name.toLowerCase());
	}
	public FriendPlayer getPlayerFromCache(String name) {
		if((!playerCache.containsKey(name.toLowerCase())) && (ProxyServer.getInstance().getPlayer(name) == null) ) {
			return null;
		}
		return playerCache.computeIfAbsent(name.toLowerCase(), new Function<String, FriendPlayer>() {

			@Override
			public FriendPlayer apply(String name) {
				return new FriendPlayer(ProxyServer.getInstance().getPlayer(name));
			}
		});
	}
	public void removePlayerCache(String name) {
		playerCache.remove(name.toLowerCase());
	}
}
