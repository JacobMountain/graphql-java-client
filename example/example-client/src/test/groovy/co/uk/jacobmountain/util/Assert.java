package co.uk.jacobmountain.util;


import co.uk.jacobmountain.resolvers.dto.MatchResult;
import co.uk.jacobmountain.resolvers.dto.Team;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Type;
import java.util.List;

public class Assert {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void assertEquals(MatchResult expected, co.uk.jacobmountain.dto.MatchResult actual) {
        MatchResult result = OBJECT_MAPPER.convertValue(actual, MatchResult.class);
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(List<MatchResult> expected, List<co.uk.jacobmountain.dto.MatchResult> actual) {
        List<MatchResult> result = OBJECT_MAPPER.convertValue(actual, new TypeReference<List<MatchResult>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        Assertions.assertEquals(expected, result);
    }

    public static void assertEquals(Team expected, co.uk.jacobmountain.dto.Team actual) {
        Team result = OBJECT_MAPPER.convertValue(actual, Team.class);
        Assertions.assertEquals(expected, result);
    }

}
