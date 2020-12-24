package co.uk.jacobmountain.util;

import co.uk.jacobmountain.dto.Character;
import co.uk.jacobmountain.dto.Starship;
import co.uk.jacobmountain.resolvers.dto.Human;
import co.uk.jacobmountain.resolvers.dto.Review;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper
public interface AssertionMapper {

    Human to(co.uk.jacobmountain.dto.Human input);

    Review toReview(co.uk.jacobmountain.dto.Review review);

    default List<String> mapFriends(List<Character> value) {
        return value.stream()
                .filter(Objects::nonNull)
                .map(Character::getId)
                .collect(Collectors.toList());
    }

    default List<String> map(List<Starship> value) {
        return value.stream()
                .map(Starship::getId)
                .collect(Collectors.toList());
    }

}