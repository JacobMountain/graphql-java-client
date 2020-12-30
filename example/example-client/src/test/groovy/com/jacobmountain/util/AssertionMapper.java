package com.jacobmountain.util;

import com.jacobmountain.dto.Character;
import com.jacobmountain.dto.Starship;
import com.jacobmountain.resolvers.dto.Human;
import com.jacobmountain.resolvers.dto.Review;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper
public interface AssertionMapper {

    Human to(com.jacobmountain.dto.Human input);

    Review toReview(com.jacobmountain.dto.Review review);

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
