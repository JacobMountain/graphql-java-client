package co.uk.jacobmountain.utils;

import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class StringUtils {

    public String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public String decapitalize(String word) {
        return word.substring(0, 1).toLowerCase() + word.substring(1);
    }

    public String camelCase(String... parts) {
        return Stream.concat(
                Stream.of(parts[0])
                        .map(StringUtils::decapitalize),
                Stream.of(parts)
                        .skip(1)
                        .map(StringUtils::capitalize)
        ).collect(Collectors.joining());
    }

    public String enquote(String value) {
        return "\"" + value + "\"";
    }

    public boolean isEmpty(String str) {
        return str == null || str.trim().equals("");
    }

}
