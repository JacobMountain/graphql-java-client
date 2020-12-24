package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("Droid")
public class Droid implements Character, SearchResult {

    private String id;

    private String name;

    private List<String> friends;

    private List<Episode> appearsIn;

    private String primaryFunction;

}
