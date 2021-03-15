package com.jacobmountain.graphql.client.code.reactive;

import com.jacobmountain.graphql.client.ReactiveFetcher;
import com.jacobmountain.graphql.client.ReactiveSubscriber;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.code.Assembler;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;

public class ReactiveAssembler extends Assembler {
    public ReactiveAssembler(QueryGenerator queryGenerator,
                             Schema schema,
                             TypeMapper typeMapper) {
        super(
                new ReactiveQueryStage(queryGenerator, schema, typeMapper),
                new ReactiveReturnStage(schema, typeMapper),
                schema, typeMapper,
                ReactiveFetcher.class, ReactiveSubscriber.class
        );
    }
}
