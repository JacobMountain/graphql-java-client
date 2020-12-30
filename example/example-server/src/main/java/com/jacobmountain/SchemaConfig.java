package com.jacobmountain;

import com.jacobmountain.resolvers.Query;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SchemaConfig {

    private final Query query;

    @Bean
    public GraphQLSchema graphQLSchema() {
        return new GraphQLSchemaGenerator()
                .withBasePackages("com.jacobmountain")
                .withOperationsFromSingleton(query, Query.class)
                .generate();
    }

}
