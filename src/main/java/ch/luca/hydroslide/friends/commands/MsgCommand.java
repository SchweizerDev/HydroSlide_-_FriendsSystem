package ch.luca.hydroslide.friends.commands;

import java.util.concurrent.ConcurrentHashMap;

import ch.luca.hydroslide.friends.manager.FriendPlayer;
import ch.luca.hydroslide.friends.Friends;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MsgCommand extends Command {
	
	@Getter
	private static ConcurrentHashMap<ProxiedPlayer, String> reply = new ConcurrentHashMap<ProxiedPlayer, String>();
	
	public MsgCommand() {
		super("msg");
	}
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(Friends.getNoPlayer());
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if(args.length == 0) {
			p.sendMessage(Friends.getPrefixUse() + "msg <Spieler> <Nachricht>");
			return;
		}
		FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
		if(friendPlayer == null) {
			p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
			return;
		}
		if(p.getName().equalsIgnoreCase(args[0])) {
			p.sendMessage(Friends.getPrefix() + "§cDu kannst nicht mit dir selber schreiben.");
			return;
		}
		ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[0]);
		if(proxiedPlayer == null) {
			p.sendMessage(Friends.getPlayerNotOnline());
			return;
		}
		FriendPlayer friendProxiedPlayer = Friends.getInstance().getPlayerFromCache(proxiedPlayer.getName());
		if(friendProxiedPlayer == null) {
			p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
			return;
		}
		if(!friendProxiedPlayer.isMsg()) {
			p.sendMessage(Friends.getPrefix() + "§cDieser Spieler hat Direktnachrichten deaktivert.");
			return;
		}
		String msg = "";
		for (int i = 1; i <= args.length - 1; i++) {
			msg = msg + args[i] + " ";
		}
		reply.put(p, proxiedPlayer.getName());
		reply.put(proxiedPlayer, p.getName());
		p.sendMessage(Friends.getMsg() + "§e" + p.getDisplayName() + " §8» §a" + proxiedPlayer.getDisplayName() + " §8× §7" + msg);
		proxiedPlayer.sendMessage(Friends.getMsg() + "§e" + p.getDisplayName() + " §8» §a" + proxiedPlayer.getDisplayName() + " §8× §7" + msg);
		String finalMsg = msg;
		Friends.getInstance().getPlayerCache().values().stream().filter(FriendPlayer::isMsgSpy).forEach(f -> {
			f.getPlayer().sendMessage(Friends.getPrefix() + "§a" + p.getDisplayName() + " §8» §a" + proxiedPlayer.getDisplayName() + " §8➤ §7" + finalMsg);
		});
		return;
	}
}
