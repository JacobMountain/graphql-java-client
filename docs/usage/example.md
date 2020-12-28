
### 1. Generate a GraphQL schema file
For more information on how to do this, [see here](https://graphql.org/learn/schema/). This is the schema we will use:
```graphql
schema {
    query: Query
    mutation: Mutation
    subscription: Subscription
}
# The query type, represents all of the entry points into our object graph
type Query {
    hero(episode: Episode, hero: String): Character
}
# The mutation type, represents all updates we can make to our data
type Mutation {
    createReview(episode: Episode, review: ReviewInput!): Review
}
# The subscription type, represents all subscriptions we can make to our data
type Subscription {
    reviewAdded(episode: Episode): Review
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
    id: String!
    # The name of the character
    name: String!
    # The friends of the character, or an empty list if they have none
    friends: [Character]
    # The friends of the character exposed as a connection with edges
    friendsConnection(first: Int, after: String): FriendsConnection!
    # The movies this character appears in
    appearsIn: [Episode]!
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
    id: String!
    # What this human calls themselves
    name: String!
    # The home planet of the human, or null if unknown
    homePlanet: String
    # Height in the preferred unit, default is meters
    height(unit: LengthUnit = METER): Float
    # Mass in kilograms, or null if unknown
    mass: Float
    # This human's friends, or an empty list if they have none
    friends: [Character]
    # The friends of the human exposed as a connection with edges
    friendsConnection(first: Int, after: String): FriendsConnection!
    # The movies this human appears in
    appearsIn: [Episode]!
    # A list of starships this person has piloted, or an empty list if none
    starships: [Starship]
}
# An autonomous mechanical character in the Star Wars universe
type Droid implements Character {
    # The ID of the droid
    id: String!
    # What others call this droid
    name: String!
    # This droid's friends, or an empty list if they have none
    friends: [Character]
    # The movies this droid appears in
    appearsIn: [Episode]!
    # This droid's primary function
    primaryFunction: String
}
# Represents a review for a movie
type Review {
    # The movie
    episode: Episode
    # The number of stars this review gave, 1-5
    stars: Int!
    # Comment about the movie
    commentary: String
}
# The input object sent when someone is creating a new review
input ReviewInput {
    # 0-5 stars
    stars: Int!
    # Comment about the movie, optional
    commentary: String
}
type Starship {
    # The ID of the starship
    id: String!
    # The name of the starship
    name: String!
    # Length of the starship, along the longest axis
    length(unit: LengthUnit = METER): Float
    coordinates: [[Float!]!]
}
```
Once you have a file containing your 
schema, place the file in an easy location (such as the root of the project). You could place it in the `resources` folder, 
but this is not necessary as it is read at compile time and not needed once built. In this example we will name our schema 
file `schema.gql` and we'll place it in the root of our project:
```
/project
 |--/build
 |--/src
 |   |--/main
 |   |--/test
 |--build.gradle
 |--schema.gql <---
```

### 2. Create an interface
Inside our main package we will create an interface that will act as the link between our java code, and the GraphQL queries.
```java
@GraphQLClient(
        schema = "schema.gql"
)
public interface StarWarsClient {

}
```
Once you've created an empty interface it may be worth building, this will cause the annotation processor to attempt to 
read the schema and generate the necessary Java classes. 
### 3. Add methods
Now we can start mapping our queries and mutations to Java methods.
```java
@GraphQLClient(
        schema = "schema.gql"
)
public interface StarWarsClient {

    @GraphQLQuery("hero")
    Character getHero(Episode episode);

    /* 
        the @GraphQLArgument is optional (it will use the variable name for the GraphQL variable), it is a
        useful annotation for decoupling between the schema and the interface
    */

    @GraphQLQuery("hero")
    Character getHero(@GraphQLArgument("hero") String id); 

}
```
This will generate a class named `StarWarsClientGraph` that implements `StarWarsClient`. The implementation will take a 
`Fetcher` as an argument to its constructor. The implementation will contain the two methods from the interface, each will 
assemble its arguments into a generated Java class, then pass a GraphQL query and the args to the fetcher. The query generated
for the first method will look like this:
```graphql
query Hero($episode: Episode) { 
    hero(episode: $episode) { 
        id 
        name 
        friends { 
            id 
            name 
            ... on Human { 
                homePlanet 
                height 
                mass 
                __typename 
            } 
            ... on Droid { 
                primaryFunction 
                __typename 
            } 
            __typename 
        } 
        appearsIn
        ... on Human { 
            homePlanet 
            height 
            mass
            appearsIn 
            starships { 
                id 
                name 
                length 
                coordinates 
                __typename 
            } 
            __typename 
        } 
        ... on Droid { 
            appearsIn 
            primaryFunction 
            __typename 
        } 
        __typename 
    } 
}
```

?> The actual query will be minified (without tabs or new-lines), and may differ slightly between versions