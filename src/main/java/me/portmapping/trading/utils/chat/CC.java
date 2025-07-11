package me.portmapping.trading.utils.chat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CC {

	public static final String SB_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------";
	public static final String MENU_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------";
	public static final String DARK_MENU_BAR = ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------";
	public static final String CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";
	public static final String TOP_SPLITTER = color("--------------------");
	public static final String BOTTOM_SPLITTER = color("--------------------");

	public static final String NO_KIT = CC.color("&4&lERROR&4! &cThat kit doesn't exist!");
	public static final String NO_ARENA = CC.color("&4&lERROR&4! &cThat arena doesn't exist!");

	@Setter
	private static boolean usingPlaceholderAPI = false;

	public static void sendMessage(Player player, String message) {
		message = parse(player, message);
		player.sendMessage(color(message));
	}

	public static void sendTitle(Player player, String header, String footer) {
		//Title title = new Title(parse(player, header), parse(player, footer), 10, 20, 10);
		player.sendTitle(parse(player, header), parse(player, footer), 10, 20, 10);
	}

	public static String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static String t(String string){
		return color(string);
	}

	public static List<String> t(List<String> lines){
		return color(lines);
	}

	public static List<String> color(List<String> lines) {
		List<String> toReturn = new ArrayList<>();
		for (String line : lines) {
			toReturn.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		return toReturn;
	}

	public static String parse(OfflinePlayer player, String string) {
		if (string == null) {
			return "null";
		}

		if (player == null) {
			return color(string);
		}

		if (usingPlaceholderAPI) {
			try {
				return color(string);
			} catch (NullPointerException ignored) {
				// PAPI randomly throws an NPE as the plugin is being disabled.
			}
		}

		return color(string);
	}



	public static String getFormattedLocation(Location location) {
		return color("&b" + location.getX() + "&7, &b" + location.getY() + "&7, &b" + location.getZ());
	}
}
