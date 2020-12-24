package co.uk.jacobmountain.service;

import co.uk.jacobmountain.resolvers.dto.Character;
import co.uk.jacobmountain.resolvers.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DefaultService implements StarWarsService {

    private static final Random RANDOM = new Random();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static ConcurrentMap<String, Human> humans;
    private static ConcurrentMap<String, Droid> droids;
    private static ConcurrentMap<String, Starship> starships;
    private static ConcurrentMap<String, Character> characters;

    static {
        try {
            starships = readToMap("ships.json", new TypeReference<List<Starship>>() {
            }, Starship::getId);
            humans = readToMap("humans.json", new TypeReference<List<Human>>() {
            }, Human::getId);
            droids = readToMap("droids.json", new TypeReference<List<Droid>>() {
            }, Droid::getId);
            characters = new ConcurrentHashMap<>();
            characters.putAll(humans);
            characters.putAll(droids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static <T> ConcurrentMap<String, T> readToMap(String file, TypeReference<List<T>> reference, Function<T, String> id) throws IOException {
        return read(file, reference)
                .stream()
                .collect(Collectors.toConcurrentMap(id, Function.identity()));
    }

    private static <T> T read(String file, TypeReference<T> ref) throws IOException {
        String contents = new BufferedReader(
                new InputStreamReader(DefaultService.class.getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return OBJECT_MAPPER.readValue(contents, ref);
    }

    public static Human randomHuman() {
        return random(new ArrayList<>(humans.values()));
    }

    private static <T> T random(List<T> list) {
        return list.get(RANDOM.nextInt(list.size() - 1));
    }

    public static Review randomReview(Episode episode) {
        Review review = new Review();
        review.setCommentary("");
        review.setStars(RANDOM.nextInt(5));
        review.setEpisode(episode);
        return review;
    }

    @Override
    public Character getHero(Episode episode) {
        return randomHuman();
    }

    @Override
    public List<Review> getReviews(Episode episode) {
        return null;
    }

    @Override
    public Review createReview(Episode episode, Review input) {
        return null;
    }

    @Override
    public Review reviewAdded(Episode episode) {
        return null;
    }

    @Override
    public Character getFriend(String id) {
        return characters.get(id);
    }

    @Override
    public Starship getShip(String id) {
        return starships.get(id);
    }
}
