package com.jacobmountain.resolvers;

import com.jacobmountain.resolvers.dto.Character;
import com.jacobmountain.resolvers.dto.*;
import com.jacobmountain.service.StarWarsService;
import io.leangen.graphql.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Query {

    private final StarWarsService starWarsService;

    @GraphQLQuery(name = "hero")
    public Character getHero(@GraphQLArgument(name = "hero") String id,
                             @GraphQLArgument(name = "episode") Episode episode) {
        if (StringUtils.isNoneEmpty(id)) {
            return starWarsService.getFriend(id);
        }
        return starWarsService.getHero(episode);
    }

    @GraphQLQuery(name = "reviews")
    public List<Review> getReviews(@GraphQLArgument(name = "episode") @GraphQLNonNull Episode episode) {
        return starWarsService.getReviews(episode);
    }

    @GraphQLQuery(name = "friendsConnection")
    public FriendsConnection getFriendsConnection(@GraphQLContext Character parent,
                                                  @GraphQLArgument(name = "first") Integer first,
                                                  @GraphQLArgument(name = "after") String after) {
        return new FriendsConnection();
    }

    @GraphQLQuery(name = "length")
    public Float getLength(@GraphQLContext Starship parent,
                           @GraphQLArgument(name = "unit") LengthUnit unit) {
        return parent.getLength();
    }

    @GraphQLQuery(name = "height")
    public Float getHeight(@GraphQLContext Human parent,
                           @GraphQLArgument(name = "unit") LengthUnit unit) {
        return parent.getHeight();
    }

    @GraphQLQuery(name = "friends")
    public List<Character> getFriends(@GraphQLContext Character parent) {
        List<Character> ret = new ArrayList<>();
        for (String id : parent.getFriends()) {
            ret.add(starWarsService.getFriend(id));
        }
        return ret;
    }

    @GraphQLQuery(name = "starships")
    public List<Starship> getStarships(@GraphQLContext Human parent) {
        return parent.getStarships().stream()
                .map(starWarsService::getShip)
                .collect(Collectors.toList());
    }

    @GraphQLMutation(name = "createReview")
    public Review createReview(@GraphQLArgument(name = "episode") Episode episode,
                               @GraphQLArgument(name = "review") Review input) {
        input.setEpisode(episode);
        return starWarsService.createReview(episode, input);
    }

    @GraphQLSubscription(name = "reviewAdded")
    public Flux<Review> watchReviews(@GraphQLArgument(name = "episode") Episode episode) {
        return starWarsService.watchReviews(episode);
    }

}
