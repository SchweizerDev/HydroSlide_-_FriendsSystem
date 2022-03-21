package ch.luca.hydroslide.friends.util;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;

public class Util {

	@Getter
	private ConcurrentHashMap<String, Long> delay = new ConcurrentHashMap<String, Long>();
	public CopyOnWriteArrayList<UUID> stringToList(String s) {
		CopyOnWriteArrayList<UUID> list = new CopyOnWriteArrayList<UUID>();
		if(s != null) {
			if(s.contains(",")) {
				String[] split = s.split(",");
				for(int i = 0; i < split.length; i++) {
					list.add(UUID.fromString(split[i]));
				}
			} else {
				if(!s.equalsIgnoreCase("") && !s.equalsIgnoreCase("null")) {
					list.add(UUID.fromString(s));
				}
			}
		}
		return list;
	}
	public String listToString(CopyOnWriteArrayList<UUID> list) {
		String s = "";
		if(list.size() == 0) {
			return "NULL";
		}
		for(UUID uuid : list) {
			if(s.equalsIgnoreCase("")) {
				s = uuid.toString();
			} else {
				s = s + "," + uuid.toString();
			}
		}
		return "'" + s + "'";
	}
	public boolean checkDelay(String name) {
		if(!delay.containsKey(name.toLowerCase())) {
			delay.put(name.toLowerCase(), System.currentTimeMillis() + 250);
			return true;
		}
		long l = delay.get(name.toLowerCase());
		if(System.currentTimeMillis() > l) {
			delay.remove(name.toLowerCase());
			return true;
		}
		return false;
	}
}
