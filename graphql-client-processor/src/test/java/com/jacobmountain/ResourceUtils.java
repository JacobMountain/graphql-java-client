package com.jacobmountain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ResourceUtils {

    public static String readResource(String file) {
        return new BufferedReader(
                new InputStreamReader(ResourceUtils.class.getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

}
