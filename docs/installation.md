# Installation

!> graphql-client is currently in development and as such only SNAPSHOT builds are available

!> graphql-client is currently not published to Maven Central, so (for now) you'll have to add our GitHub package repository
### Gradle
Add our GitHub package repository
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/JacobMountain/graphql-client/")
    }
}
```
Add the dependencies
```groovy
dependencies {
    compile group: "com.jacobmountain", name: "graphql-client", version: "1.0.0-SNAPSHOT"
    annotationProcessor group: "com.jacobmountain", name: "graphql-client-processor", version: "1.0.0-SNAPSHOT"
}
```

### Maven
?> TODO add maven installation process

### Development Environment
If you delegate building your project to your IDE, you may have to set up your IDE to process annotations too. In IntelliJ,
this can be done by going to `Preferences > Build, Execution, Deployment > Compiler > Annotation Processors` then checking
`Enable annotation processors`