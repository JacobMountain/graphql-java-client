# GraphQL Annotations
## Fetcher
The `Fetcher` is the class responsible for taking the GraphQL requests (queries and mutations) and turning them into web 
requests (usually HTTP or WebSocket). 
### Typename
```java
public interface Fetcher<Q, M, Error> {}
```

|Type Variable | Description |
|---|---|
|Q     | The type generated of the generated Query class        |
|M     | The type generated of the generated Mutation class     |
|Error | A custom error class, usually contains a `String message` field |

### Methods
#### .query(query, args)
> Called when the client attempts to make a query
```java
<A> Response<Q, Error> query(String query, A args);
```


|Variable |Type|nullable| Description |
|---|---|---|---|
|query|`String`|`false`|The GraphQL query|
|args|`A`|`true`|An object containing any args/variables used in the request|

#### .mutate(query, args)
> Called when the client attempts to make a mutation
```
<A> Response<M, Error> mutate(String query, A args);
```

|Variable |Type|nullable| Description |
|---|---|---|---|
|query|`String`|`false`|The GraphQL query|
|args|`A`|`true`|An object containing any args/variables used in the request|


## ReactiveFetcher
The `ReactiveFetcher` (like the `Fetcher`) is also class responsible for taking the GraphQL requests (queries and mutations) 
and turning them into web requests (usually HTTP or WebSocket).
### Typename
```java
public interface ReactiveFetcher<Q, M, Error> {}
```

|Type Variable | Description |
|---|---|
|Q     | The type generated of the generated Query class        |
|M     | The type generated of the generated Mutation class     |
|Error | A custom error class, usually contains a `String message` field |

### Methods
#### .query(query, args)
> Called when the client attempts to make a query
```java
<A> Publisher<Response<Q, Error>> query(String query, A args);
```

|Variable |Type|nullable| Description |
|---|---|---|---|
|query|`String`|`false`|The GraphQL query|
|args|`A`|`true`|An object containing any args/variables used in the request|

#### .mutate(query, args)
> Called when the client attempts to make a mutation
```java
<A> Publisher<Response<M, Error>> mutate(String mutation, A args);
```

|Variable |Type|nullable| Description |
|---|---|---|---|
|query|`String`|`false`|The GraphQL query|
|args|`A`|`true`|An object containing any args/variables used in the request|

## ReactiveSubscriber
The `ReactiveSubscriber` (like the `ReactiveFetcher`) is also class responsible for taking the GraphQL requests (this time
only subscriptions) and turning them into web requests (usually WebSocket).
### Typename
```java
public interface ReactiveSubscriber<S, Error> {}
```

|Type Variable | Description |
|---|---|
|S     | The type generated of the generated Subscription class          |
|Error | A custom error class, usually contains a `String message` field |

### Methods
#### .subscribe(query, args)
> Called when the client attempts to make a query
```java
<A> Publisher<Response<S, Error>> subscribe(String query, A args);
```

|Variable |Type|nullable| Description |
|---|---|---|---|
|query|`String`|`false`|The GraphQL subscription query|
|args|`A`|`true`|An object containing any args/variables used in the request|
