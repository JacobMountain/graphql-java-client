package co.uk.jacobmountain.util;


import co.uk.jacobmountain.dto.Character;
import co.uk.jacobmountain.dto.Review;
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
        Human result = MAPPER.to((co.uk.jacobmountain.dto.Human) actual);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(Human expected, Character actual) {
        Human result = MAPPER.to((co.uk.jacobmountain.dto.Human) actual);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(List<co.uk.jacobmountain.resolvers.dto.Review> expected, List<Review> actual) {
        List<co.uk.jacobmountain.resolvers.dto.Review> result = actual.stream()
                .map(MAPPER::toReview)
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, result);
    }

//    public static void assertEquals(MatchResult expected, co.uk.jacobmountain.dto.MatchResult actual) {
//        MatchResult result = OBJECT_MAPPER.convertValue(actual, MatchResult.class);
//        Assertions.assertEquals(expected, result);
//    }
//
//    public static void assertEquals(List<MatchResult> expected, List<co.uk.jacobmountain.dto.MatchResult> actual) {
//        List<MatchResult> result = OBJECT_MAPPER.convertValue(actual, new TypeReference<List<MatchResult>>() {
//            @Override
//            public Type getType() {
//                return super.getType();
//            }
//        });
//        Assertions.assertEquals(expected, result);
//    }
//
//    public static void assertEquals(Team expected, co.uk.jacobmountain.dto.Team actual) {
//        Team result = OBJECT_MAPPER.convertValue(actual, Team.class);
//        Assertions.assertEquals(expected, result);
//    }

}
