package org.lozin.tools.string;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleFix {
	
	private static final Pattern COLOR_PATTERN = Pattern.compile("(§)([0-9a-fA-FkKmMnNoOrR])");
	private static final EnumMap<ColorCode, String> ANSI_COLORS = new EnumMap<>(ColorCode.class);
	
	static {
		ANSI_COLORS.put(ColorCode.BLACK, "\u001B[30m");
		ANSI_COLORS.put(ColorCode.DARK_BLUE, "\u001B[34m");
		ANSI_COLORS.put(ColorCode.DARK_GREEN, "\u001B[32m");
		ANSI_COLORS.put(ColorCode.DARK_CYAN, "\u001B[36m");
		ANSI_COLORS.put(ColorCode.DARK_RED, "\u001B[31m");
		ANSI_COLORS.put(ColorCode.PURPLE, "\u001B[35m");
		ANSI_COLORS.put(ColorCode.GOLD, "\u001B[33m");
		ANSI_COLORS.put(ColorCode.GRAY, "\u001B[37m");
		ANSI_COLORS.put(ColorCode.DARK_GRAY, "\u001B[90m");
		ANSI_COLORS.put(ColorCode.BLUE, "\u001B[94m");
		ANSI_COLORS.put(ColorCode.GREEN, "\u001B[92m");
		ANSI_COLORS.put(ColorCode.LIGHT_BLUE, "\u001B[96m");
		ANSI_COLORS.put(ColorCode.RED, "\u001B[91m");
		ANSI_COLORS.put(ColorCode.PINK, "\u001B[93m");
		ANSI_COLORS.put(ColorCode.YELLOW, "\u001B[93m");
		ANSI_COLORS.put(ColorCode.WHITE, "\u001B[97m");
		ANSI_COLORS.put(ColorCode.MAGIC, "\u001B[1m");  // 粗体
		ANSI_COLORS.put(ColorCode.STRIKETHROUGH, "\u001B[9m"); // 删除线
		ANSI_COLORS.put(ColorCode.UNDERLINE, "\u001B[4m");  // 下划线
		ANSI_COLORS.put(ColorCode.ITALIC, "\u001B[3m");    // 斜体
		ANSI_COLORS.put(ColorCode.RESET, "\u001B[0m");     // 重置
	}
	
	@Getter
	public enum ColorCode {
		BLACK('0'), DARK_BLUE('1'), DARK_GREEN('2'), DARK_CYAN('3'),
		DARK_RED('4'), PURPLE('5'), GOLD('6'), GRAY('7'),
		DARK_GRAY('8'), BLUE('9'), GREEN('a'), LIGHT_BLUE('b'),
		RED('c'), PINK('d'), YELLOW('e'), WHITE('f'),
		MAGIC('k'), STRIKETHROUGH('m'), UNDERLINE('n'), ITALIC('o'), RESET('r');
		private final char code;
		ColorCode(char code) {
			this.code = code;
		}
	}
	public static void log(Plugin plugin, String message) {
		Logger logger = plugin.getLogger();
		StringBuffer sb = new StringBuffer();
		sb.delete(0, sb.length());
		message = optimize(message);
		if (!message.contains("§")) return;
		Matcher matcher = COLOR_PATTERN.matcher(message);
		while (matcher.find()) {
			char colorCode = matcher.group(2).toLowerCase().charAt(0);
			String ansiCode = ANSI_COLORS.getOrDefault(
					ColorCode.values()[Character.digit(colorCode, 16)],
					ANSI_COLORS.get(ColorCode.RESET)
			);
			matcher.appendReplacement(sb, Matcher.quoteReplacement(ansiCode));
		}
		matcher.appendTail(sb);
		logger.info(sb + ANSI_COLORS.get(ColorCode.RESET));
	}
	public static String optimize(String message) {
		if (message == null) return null;
		if (message.contains("&")) {
			Pattern pattern = Pattern.compile("(&)([0-9a-z])");
			Matcher matcher = pattern.matcher(message);
			while (matcher.find()){
				message = message.replace(matcher.group(), "§" + matcher.group(2));
			}
		}
		return message;
	}
}