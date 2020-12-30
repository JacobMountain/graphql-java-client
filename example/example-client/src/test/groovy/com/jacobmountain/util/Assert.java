package com.jacobmountain.util;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacobmountain.dto.Character;
import com.jacobmountain.dto.Review;
import com.jacobmountain.resolvers.dto.Human;
import org.junit.jupiter.api.Assertions;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

public class Assert {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final AssertionMapper MAPPER = Mappers.getMapper(AssertionMapper.class);

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void assertEquals(Character expected, Character actual) {
        Human result = MAPPER.to((com.jacobmountain.dto.Human) actual);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(Human expected, Character actual) {
        Human result = MAPPER.to((com.jacobmountain.dto.Human) actual);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(List<Review> expected, List<Review> actual) {
        List<Review> result = actual.stream()
                .map(MAPPER::toReview)
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, result);
    }

}
