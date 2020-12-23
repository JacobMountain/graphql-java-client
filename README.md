# graphql-client ![Build + Test](https://github.com/JacobMountain/graphql-client/workflows/Build%20+%20Test/badge.svg)
A Java GraphQL client annotation processor, generate a client class from a graphql schema file, and a Java interface

## Usage guide
#### 1. Generate a graphql schema file:
```GraphQL
schema {
  query: Query
  mutation: Mutation
}
# The query type, represents all of the entry points into our object graph
type Query {
  hero(episode: Episode): Character
  reviews(episode: Episode!): [Review]
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
  id: ID!
  # The name of the character
  name: String!
  # The friends of the character, or an empty list if they have none
  friends: [Character]
  # The friends of the character exposed as a connection with edges
  friendsConnection(first: Int, after: ID): FriendsConnection!
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
  id: ID!
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
  friendsConnection(first: Int, after: ID): FriendsConnection!
  # The movies this human appears in
  appearsIn: [Episode]!
  # A list of starships this person has piloted, or an empty list if none
  starships: [Starship]
}
# An autonomous mechanical character in the Star Wars universe
type Droid implements Character {
  # The ID of the droid
  id: ID!
  # What others call this droid
  name: String!
  # This droid's friends, or an empty list if they have none
  friends: [Character]
  # The friends of the droid exposed as a connection with edges
  friendsConnection(first: Int, after: ID): FriendsConnection!
  # The movies this droid appears in
  appearsIn: [Episode]!
  # This droid's primary function
  primaryFunction: String
}
# A connection object for a character's friends
type FriendsConnection {
  # The total number of friends
  totalCount: Int
  # The edges for each of the character's friends.
  edges: [FriendsEdge]
  # A list of the friends, as a convenience when edges are not needed.
  friends: [Character]
  # Information for paginating this connection
  pageInfo: PageInfo!
}
# An edge object for a character's friends
type FriendsEdge {
  # A cursor used for pagination
  cursor: ID!
  # The character represented by this friendship edge
  node: Character
}
# Information for paginating this connection
type PageInfo {
  startCursor: ID
  endCursor: ID
  hasNextPage: Boolean!
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
  # Favorite color, optional
  favorite_color: ColorInput
}
# The input object sent when passing in a color
input ColorInput {
  red: Int!
  green: Int!
  blue: Int!
}
type Starship {
  # The ID of the starship
  id: ID!
  # The name of the starship
  name: String!
  # Length of the starship, along the longest axis
  length(unit: LengthUnit = METER): Float
  coordinates: [[Float!]!]
}
union SearchResult = Human | Droid | Starship
```

#### 2. Create a java interface
```java
@GraphQLClient(
        schema = "Schema.gql",
        mapping = {
                @GraphQLClient.Scalar(from = "ID", to = String.class)
        },
        maxDepth = 5, // the max depth to use for graphql queries
        nullChecking = true // whether we should add client side null checks
)
public interface StarWarsClient {}
```
if this is the first time you've created the interface it may be a good idea to run build to generate all the DTO classes and to reduce IDE/compilation errors. 

#### 3. Add corresponding methods to the interface that match up with the schema
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
    Hero getHero(Episode episode);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

    @GraphQLQuery(value = "createReview", mutates = true)
    Review createReview(Episode episode, @GraphQLArgument("review") ReviewInput input);

}
```
#### 4. Implement the `Fetcher` interface
The Fetcher interface deals with turning queries/mutations into network requests (Usually HTTP, could be websockets, etc.). For a simple example on how to do this please look at [`ExampleFetcher.java`](https://github.com/JacobMountain/graphql-client/blob/develop/example/example-client/src/main/java/co/uk/jacobmountain/ExampleFetcher.java).
