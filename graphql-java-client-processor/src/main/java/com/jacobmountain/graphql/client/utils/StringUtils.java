package com.jacobmountain.graphql.client.utils;

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

    public String pascalCase(String... parts) {
        return Stream.of(parts)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }

    public String enquote(String value) {
        return "\"" + value + "\"";
    }

    public boolean isEmpty(String str) {
        return str == null || str.trim().equals("");
    }

    public boolean hasLength(String str) {
        return !isEmpty(str);
    }

    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // Step-wise comparison
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
