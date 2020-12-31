package com.jacobmountain.graphql.client.utils;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
public class OptionalUtils {

    @SafeVarargs
    public <T> Optional<T> first(Optional<T> first, Supplier<Optional<T>>... later) {
        if (first.isPresent()) {
            return first;
        }
        if (later.length == 0) {
            return Optional.empty();
        }
        Optional<T> head = later[0].get();
        return first(head, Arrays.copyOfRange(later, 1, later.length));
    }

}
