# graphql-client ![Build + Test](https://github.com/JacobMountain/graphql-client/workflows/Build%20+%20Test/badge.svg) [![Documentation](https://img.shields.io/badge/read%20the-docs-blue)](https://jacobmountain.github.io/graphql-client/#/) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jacobmountain/graphql-java-client)

A Java GraphQL client annotation processor, generate a client class from a graphql schema file, and a Java interface.

## Usage guide
### 1. Generate a graphql schema file:
```GraphQL
schema {
    query: Query
}
# The query type, represents all of the entry points into our object graph
type Query {
    hero(episode: Episode): Character
}
# The episodes in the Star Wars trilogy
enum Episode {
    # Star Wars Episode IV: A New Hope, released in 1977.
    NEWHOPE
    # Star Wars Episode V: The Empire Strikes Back, released in 1980.
    EMPIRE
    # Star Wars Episode VI: Return of the Jedi, released in 1983.
    JEDI
}
# A character from the Star Wars universe
interface Character {
    # The ID of the character
    id: ID!
    # The name of the character
    name: String!
    # The friends of the character, or an empty list if they have none
    friends: [Character]
}
# Units of height
enum LengthUnit {
    # The standard unit around the world
    METER
    # Primarily used in the United States
    FOOT
}
# A humanoid creature from the Star Wars universe
type Human implements Character {
    # The ID of the human
    id: ID!
    # What this human calls themselves
    name: String!
    # The home planet of the human, or null if unknown
    homePlanet: String
    # Height in the preferred unit, default is meters
    height(unit: LengthUnit = METER): Float
    # The friends of the character, or an empty list if they have none
    friends: [Character]
}
# An autonomous mechanical character in the Star Wars universe
type Droid implements Character {
    # The ID of the droid
    id: ID!
    # What others call this droid
    name: String!
    # This droid's friends, or an empty list if they have none
    friends: [Character]
    # This droid's primary function
    primaryFunction: String
}
```

### 2. Create a java interface
```java
@GraphQLClient(
    schema = "Schema.gql",
    mapping = {
            @GraphQLClient.Scalar(from = "ID", to = String.class)
    },
    maxDepth = 5, // the max depth to use for graphql queries
    nullChecking = true, // whether we should add client side null checks,
    reactive = false
)
public interface StarWarsClient {}
```
if this is the first time you've created the interface it may be a good idea to run build to generate all the DTO classes 
and to reduce IDE/compilation errors. 

### 3. Add corresponding methods to the interface that match up with the schema
```java
@GraphQLClient(
    schema = "Schema.gql",
    mapping = {
            @GraphQLClient.Scalar(from = "ID", to = String.class)
    },
    maxDepth = 5, // the max depth to use for graphql queries
    nullChecking = true // whether we should add client side null checks
)
public interface StarWarsClient {

    @GraphQLQuery("hero")
    Character getHero(Episode episode);

}
```
### 4. Implement the `Fetcher` interface
The Fetcher interface deals with turning queries/mutations into network requests (Usually HTTP, could be websockets, etc.). 
For a simple example on how to do this please look at [`RestTemplateFetcher.java`](https://github.com/JacobMountain/graphql-client/blob/develop/example/example-client/src/main/java/co/uk/jacobmountain/fetchers/RestTemplateFetcher.java) 
or [`WebClientFetcher.java`](https://github.com/JacobMountain/graphql-client/blob/develop/example/example-client/src/main/java/co/uk/jacobmountain/fetchers/WebClientFetcher.java).

### 5. Create an instance of you newly generated interface
Run `build` and then create an instance of the newly generated implementation.
```java
StarWarsClient client = new StarWarsClientGraph(new RestTemplateFetcher("http://your.domain.com"));
```
where `HttpFetcher` is an implementation of the `Fetcher` interface. The default suffix for the implementation is `Graph`, 
and is overridable with the `implSuffix` parameter of the `@GraphQLClient` annotation.

### 6. Profit?
```java
Character hero = heroclient.getHero(Episode.NEWHOPE);
log.info("My favourite Star Wars character is: {}!", hero.getName());
```
