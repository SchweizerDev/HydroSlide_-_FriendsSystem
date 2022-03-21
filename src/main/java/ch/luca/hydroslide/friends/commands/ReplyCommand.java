package ch.luca.hydroslide.friends.commands;

import ch.luca.hydroslide.friends.Friends;
import ch.luca.hydroslide.friends.manager.FriendPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReplyCommand extends Command {
	
	public ReplyCommand() {
		super("r");
	}
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(Friends.getNoPlayer());
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if(!MsgCommand.getReply().containsKey(p)) {
			p.sendMessage(Friends.getPrefix() + "Dir hat niemand geschrieben.");
			return;
		}
		FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(p.getName());
		if(friendPlayer == null) {
			p.sendMessage(Friends.getPrefix() + "§cEs ist ein Fehler aufgetreten.");
			return;
		}
		ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(MsgCommand.getReply().get(p));
		if(proxiedPlayer == null) {
			MsgCommand.getReply().remove(p);
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
		for (int i = 0; i <= args.length - 1; i++) {
			msg = msg + args[i] + " ";
		}
		MsgCommand.getReply().put(p, proxiedPlayer.getName());
		MsgCommand.getReply().put(proxiedPlayer, p.getName());
		p.sendMessage(Friends.getMsg() + "§e" + p.getDisplayName() + " §8» §a" + proxiedPlayer.getDisplayName() + " §8× §7" + msg);
		proxiedPlayer.sendMessage(Friends.getMsg() + "§e" + p.getDisplayName() + " §8» §a" + proxiedPlayer.getDisplayName() + " §8× §7" + msg);
		String finalMsg = msg;
		Friends.getInstance().getPlayerCache().values().stream().filter(FriendPlayer::isMsgSpy).forEach(f -> {
			f.getPlayer().sendMessage(Friends.getPrefix() + "§a" + p.getDisplayName() + " §8» §a" + proxiedPlayer.getDisplayName() + " §8➤ §7" + finalMsg);
		});
		return;
	}
}
