package co.uk.jacobmountain.util;


import co.uk.jacobmountain.domain.Character;
import co.uk.jacobmountain.domain.Review;
import co.uk.jacobmountain.resolvers.dto.Human;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static void assertEquals(co.uk.jacobmountain.resolvers.dto.Character expected, Character actual) {
        Human result = MAPPER.to((co.uk.jacobmountain.domain.Human) actual);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(Human expected, Character actual) {
        Human result = MAPPER.to((co.uk.jacobmountain.domain.Human) actual);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(List<co.uk.jacobmountain.resolvers.dto.Review> expected, List<Review> actual) {
        List<co.uk.jacobmountain.resolvers.dto.Review> result = actual.stream()
                .map(MAPPER::toReview)
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, result);
    }

}
