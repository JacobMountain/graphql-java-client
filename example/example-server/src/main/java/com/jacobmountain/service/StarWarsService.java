package com.jacobmountain.service;

import com.jacobmountain.resolvers.dto.Character;
import com.jacobmountain.resolvers.dto.Episode;
import com.jacobmountain.resolvers.dto.Review;
import com.jacobmountain.resolvers.dto.Starship;
import reactor.core.publisher.Flux;

import java.util.List;

public interface StarWarsService {

    Character getHero(Episode episode);

    List<Review> getReviews(Episode episode);

    Review createReview(Episode episode, Review input);

    Review reviewAdded(Episode episode);

    Character getFriend(String id);

    Starship getShip(String id);

    Flux<Review> watchReviews(Episode episode);

    Review createRandomReview(int id, Episode episode);

}
