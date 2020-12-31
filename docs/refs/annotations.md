# GraphQL Annotations
## @GraphQLClient
#### Example:
```java
@GraphQLClient(
    schema = "./path/to/file.gql",
    mapping = {
        @GraphQLClient.Scalar(from = "ID", to = String.class)
    },
    maxDepth = 3,
    nullChecking = true,
    implSuffix = "Graph",
    reactive = false,
    dtoPackage = "dto"
)
public interface MyClient {

}
```
#### Fields:
<table>
    <thead>
        <tr>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">
                <code>schema</code>
            </td>
            <td>
                Description
            </td>
            <td>
                A relative path from the root of your project to your 
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                true
            </td>
        </tr>
        <tr>
            <td rowspan="3">
                <code>mapping</code>
            </td>
            <td>
                Description
            </td>
            <td>
                Used for the class generation, adds a mapping from the GraphQL schema to the class used to (de/)serialize
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
        <tr>
            <td>
                default
            </td>
            <td>
                empty list
            </td>
        </tr>
        <tr>
            <td rowspan="3">
                <code>maxDepth</code>
            </td>
            <td>
                Description
            </td>
            <td>
                Used for the query generation, will only select children of object types up until this depth
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
        <tr>
            <td>
                default
            </td>
            <td>
                3
            </td>
        </tr>
        <tr>
            <td rowspan="3">
                <code>implSuffix</code>
            </td>
            <td>
                Description
            </td>
            <td>
                Used for the client implementation, for instance an interface named MyClient will generate an implementation 
                named <code>MyClientGraph</code>
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
        <tr>
            <td>
                default
            </td>
            <td>
                <code>"Graph"</code>
            </td>
        </tr>
        <tr>
            <td rowspan="3">
                <code>reactive</code>
            </td>
            <td>
                Description
            </td>
            <td>
               Used for the client implementation, if true the <code>Fetcher</code> used will be a <code>ReactiveFetcher</code> 
               that must return a <code>Publisher&lt;Response&lt;?&gt;&gt;</code>, vs a regular <code>Fetcher</code> that 
               would block and return a <code>Response&lt;?&gt;</code>. <b>Requires <code>io.projectreactor:reactor-core</code> 
               to be on the classpath.</b>
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
        <tr>
            <td>
                default
            </td>
            <td>
                <code>false</code>
            </td>
        </tr>
        <tr>
            <td rowspan="3">
                <code>dtoPackage</code>
            </td>
            <td>
                Description
            </td>
            <td>
               The sub-package the Java class DTOs get generated to, by default it creates a new package (named `dto`)
               in the same package as the interface annotated with `@GraphQlClient`
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
        <tr>
            <td>
                default
            </td>
            <td>
                <code>"dto"</code>
            </td>
        </tr>
    </tbody>
</table>
 
## @GraphQLQuery
```java
@GraphQLQuery(
    value = "field"
)
public Field getField();
```
#### Fields:
<table>
    <thead>
        <tr>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">
                <code>value</code>
            </td>
            <td>
                Description
            </td>
            <td>
               The query field from the GraphQL schema
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
    </tbody>
</table>

## @GraphQLMutation
```java
@GraphQLMutation(
    value = "field"
)
```
#### Fields:
<table>
    <thead>
        <tr>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">
                <code>value</code>
            </td>
            <td>
                Description
            </td>
            <td>
               The mutation field from the GraphQL schema
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
    </tbody>
</table>

## @GraphQLSubscription
```java
@GraphQLSubscription(
    value = "field"
)
```
!> requires `@GraphQLClient(reactive = true)`
#### Fields:
<table>
    <thead>
        <tr>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
            <th>
                Property
            </th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">
                <code>value</code>
            </td>
            <td>
                Description
            </td>
            <td>
               The subscription field from the GraphQL schema
            </td>
        </tr>
        <tr>
            <td>
                required
            </td>
            <td>
                false
            </td>
        </tr>
    </tbody>
</table>