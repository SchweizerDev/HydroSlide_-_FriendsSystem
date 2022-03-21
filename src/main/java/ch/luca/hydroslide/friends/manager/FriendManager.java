package ch.luca.hydroslide.friends.manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import ch.luca.hydroslide.friends.Friends;

public class FriendManager {

	
	public void getFriends(String uuid, boolean async, Consumer<CopyOnWriteArrayList<UUID>> consumer) {
		Friends.getInstance().getDatabase().executeQuery("SELECT * FROM Friends WHERE UUID='" + uuid + "'", async, new Consumer<ResultSet>() {
			
			@Override
			public void accept(ResultSet rs) {
				if(rs == null) return;
				try {
					if(rs.next()) {
						String friend = rs.getString("Friends");
						consumer.accept(Friends.getInstance().getUtil().stringToList(friend));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void getFriendRequests(String uuid, boolean async, Consumer<CopyOnWriteArrayList<UUID>> consumer) {
		Friends.getInstance().getDatabase().executeQuery("SELECT * FROM Friends WHERE UUID='" + uuid + "'", async, new Consumer<ResultSet>() {
			
			@Override
			public void accept(ResultSet rs) {
				if(rs == null) return;
				try {
					if(rs.next()) {
						String friend = rs.getString("FriendsRequests");
						consumer.accept(Friends.getInstance().getUtil().stringToList(friend));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void saveFriends(String uuid, boolean async, CopyOnWriteArrayList<UUID> friends) {
		String friendString = Friends.getInstance().getUtil().listToString(friends);
		String statement = "UPDATE Friends SET Friends=" + friendString + " WHERE UUID='" + uuid + "'";
		Friends.getInstance().getDatabase().update(statement, async);
	}
	public void saveFriendsRequests(String uuid, boolean async, CopyOnWriteArrayList<UUID> friendsRequests) {
		String friendString = Friends.getInstance().getUtil().listToString(friendsRequests);
		String statement = "UPDATE Friends SET FriendsRequests=" + friendString + " WHERE UUID='" + uuid + "'";
		Friends.getInstance().getDatabase().update(statement, async);
	}
	public void getFriendData(String name, boolean async, Consumer<ResultSet> consumer) {
		Friends.getInstance().getDatabase().executeQuery("SELECT * FROM Friends WHERE Name='" + name + "'", async, new Consumer<ResultSet>() {

			@Override
			public void accept(ResultSet t) {
				if(t != null) {
					try {
						if(t.next()) {
							consumer.accept(t);
							return;
						} 
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				consumer.accept(null);
			}
		});
	}
	public void getFriendData(UUID uuid, boolean async, Consumer<ResultSet> consumer) {
		Friends.getInstance().getDatabase().executeQuery("SELECT * FROM Friends WHERE UUID='" + uuid.toString() + "'", async, new Consumer<ResultSet>() {

			@Override
			public void accept(ResultSet t) {
				if(t != null) {
					try {
						if(t.next()) {
							consumer.accept(t);
							return;
						} 
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				consumer.accept(null);
			}
		});
	}

	public void hasMSGSpy(UUID uuid, Consumer<Boolean> consumer) {
		Friends.getInstance().getDatabase().executeQuery("SELECT * FROM Friends_msgspy WHERE UUID='" + uuid.toString() + "'", true, t -> {
			if(t != null) {
				try {
					if(t.next()) {
						consumer.accept(true);
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			consumer.accept(false);
		});
	}

	public void setMSGSpy(UUID uuid, boolean enabled) {
		if(enabled) {
			Friends.getInstance().getDatabase().update("INSERT IGNORE INTO Friends_msgspy (UUID) VALUES('" + uuid.toString() + "')", true);
			return;
		}
		Friends.getInstance().getDatabase().update("DELETE FROM Friends_msgspy WHERE UUID = '" + uuid.toString() + "'", true);
	}
}
