package ch.luca.hydroslide.friends.commands;

import ch.luca.hydroslide.friends.manager.FriendPlayer;
import ch.luca.hydroslide.friends.Friends;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MsgSpyCommand extends Command {

    public MsgSpyCommand() {
        super("msgspy");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(Friends.getNoPlayer());
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer)sender;
        if(player.hasPermission("hydroslide.msgspy")) {
            if (args.length == 1) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                if(target == null) {
                    player.sendMessage(Friends.getPlayerNotOnline());
                    return;
                }

                FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(target.getName());
                if(friendPlayer == null) {
                    player.sendMessage(Friends.getPlayerNotOnline());
                    return;
                }

                Friends.getInstance().getFriendManager().setMSGSpy(target.getUniqueId(), !friendPlayer.isMsgSpy());
                if(friendPlayer.isMsgSpy()) {
                    friendPlayer.setMsgSpy(false);
                    target.sendMessage(Friends.getPrefix() + "MsgSpy wurde §cdeaktiviert§7.");
                    player.sendMessage(Friends.getPrefix() + "MsgSpy für §e" + target.getName() + " wurde §cdeaktiviert§7.");
                } else {
                    friendPlayer.setMsgSpy(true);
                    target.sendMessage(Friends.getPrefix() + "MsgSpy wurde §aaktiviert§7.");
                    player.sendMessage(Friends.getPrefix() + "MsgSpy für §e" + target.getName() + " wurde §aaktiviert§7.");
                }
            } else {
                FriendPlayer friendPlayer = Friends.getInstance().getPlayerFromCache(player.getName());
                if(friendPlayer == null) {
                    return;
                }
                Friends.getInstance().getFriendManager().setMSGSpy(player.getUniqueId(), !friendPlayer.isMsgSpy());
                if(friendPlayer.isMsgSpy()) {
                    friendPlayer.setMsgSpy(false);
                    player.sendMessage(Friends.getPrefix() + "MsgSpy wurde §cdeaktiviert§7.");
                } else {
                    friendPlayer.setMsgSpy(true);
                    player.sendMessage(Friends.getPrefix() + "MsgSpy wurde §aaktiviert§7.");
                }
            }
        } else {
            player.sendMessage(Friends.getPrefix() + "§cDieser Befehl wurde nicht gefunden. Nutze §e/help");
        }
    }
}
