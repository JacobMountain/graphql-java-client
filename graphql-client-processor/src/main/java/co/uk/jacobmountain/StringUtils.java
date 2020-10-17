package co.uk.jacobmountain;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {

    static String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
    static String decapitalize(String word) {
        return word.substring(0, 1).toLowerCase() + word.substring(1);
    }

    static String camelCase(String... parts) {
        return Stream.concat(
                Stream.of(parts[0])
                        .map(StringUtils::decapitalize),
                Stream.of(parts)
                        .skip(1)
                        .map(StringUtils::capitalize)
        ).collect(Collectors.joining());
    }

    static String enquote(String value) {
        return "\"" + value + "\"";
    }

    static boolean isEmpty(String str) {
        return str == null || str.trim().equals("");
    }

}
