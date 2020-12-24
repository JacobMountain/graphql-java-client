package co.uk.jacobmountain.service;

import co.uk.jacobmountain.resolvers.dto.Character;
import co.uk.jacobmountain.resolvers.dto.*;

import java.util.List;

public interface StarWarsService {

    Character getHero(Episode episode);

    List<Review> getReviews(Episode episode);

    Review createReview(Episode episode, ReviewInput input);

    Review reviewAdded(Episode episode);

    Character getFriend(String id);

    Starship getShip(String id);

}
