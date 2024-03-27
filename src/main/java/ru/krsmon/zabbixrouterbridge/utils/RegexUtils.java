package ru.krsmon.zabbixrouterbridge.utils;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static ru.krsmon.zabbixrouterbridge.utils.GlobalConstrains.NEW_LINE;

@UtilityClass
public class RegexUtils {
    private static final Pattern IP_FIND = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3})");
    private static final Pattern MAC_FIND = Pattern.compile("((\\w{2}:){5}\\w{2})");
    private static final Pattern PACKET_LOSS = Pattern.compile("[\\s|=]?(\\d{1,3})%");
    private static final String PATTERN_REPLACE = "[^0-9]";

    public static Map<String, String> toArpMap(@NonNull String log) {
        var result = new HashMap<String, String>();

        for (var line : log.split(NEW_LINE)) {
            var ipMatcher = IP_FIND.matcher(line);
            var macMatcher = MAC_FIND.matcher(line);

            if (ipMatcher.find() && macMatcher.find())
                // Fix duplicate pair
                result.putIfAbsent(macMatcher.group(0).toUpperCase(), ipMatcher.group(0));
        }
        return result;
    }

    public static Map.Entry<Boolean, String> toPingResult(@NonNull String logs) {
        try {
            for (var line : logs.split(NEW_LINE)) {
                var matcher = PACKET_LOSS.matcher(line);
                if (matcher.find()) {
                    var loss = parseInt(matcher.group(0).replaceAll(PATTERN_REPLACE, ""));
                    return Map.entry(loss != 100, "packet loss %s%s".formatted(loss, "%"));
                }
            }
            return Map.entry(false, "fail: response not recognized");
        } catch (Exception exception) {
            return Map.entry(false, "fail: message '%s'".formatted(exception.getLocalizedMessage()));
        }
    }

}
