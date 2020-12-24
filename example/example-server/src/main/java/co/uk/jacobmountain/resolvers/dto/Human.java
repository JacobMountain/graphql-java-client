package co.uk.jacobmountain.resolvers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Human implements Character, SearchResult {

    private String id;

    private String name;

    private String homePlanet;

    private Float height;

    private Float mass;

    private List<String> friends;

    private List<Episode> appearsIn;

    private List<String> starships;

}
