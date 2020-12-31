# Installation

!> graphql-java-client is pre version 1, and as such breaking changes may occur between releases

### Gradle
```groovy
dependencies {
    compile group: "com.jacobmountain", name: "graphql-java-client", version: "0.1.0" 
    annotationProcessor group: "com.jacobmountain", name: "graphql-java-client-processor", version: "0.1.0"
}
```

### Maven
```xml
...
<dependencies>
    <dependency>
        <groupId>com.jacobmountain</groupId>
        <artifactId>graphql-java-client</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
...
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source> <!-- depending on your project -->
                <target>1.8</target> <!-- depending on your project -->
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.jacobmountain</groupId>
                        <artifactId>graphql-java-client-processor</artifactId>
                        <version>0.1.0</version>
                    </path>
                    <!-- other annotation processors -->
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Development Environment
If you delegate building your project to your IDE, you may have to set up your IDE to process annotations too. In IntelliJ,
this can be done by going to `Preferences > Build, Execution, Deployment > Compiler > Annotation Processors` then checking
`Enable annotation processors`