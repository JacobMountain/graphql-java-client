package co.uk.jacobmountain;

import co.uk.jacobmountain.resolvers.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaConfig {

    @Autowired
    private Query query;

//    @Bean
//    public GraphQLSchema schema() {
//        return SchemaParser.newParser()
//                .file("Schema.graphqls")
//                .resolvers(query)
//                .build()
//                .makeExecutableSchema();
//
//    }

}
