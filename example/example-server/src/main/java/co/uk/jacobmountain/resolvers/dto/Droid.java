package co.uk.jacobmountain.resolvers.dto;

import lombok.Data;

import java.util.List;

@Data
public class Droid implements Character, SearchResult {

    private String id;

    private String name;

    private List<String> friends;

    private List<Episode> appearsIn;

    private String primaryFunction;

}
