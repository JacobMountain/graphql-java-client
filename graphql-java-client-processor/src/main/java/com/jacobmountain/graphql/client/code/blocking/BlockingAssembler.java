package com.jacobmountain.graphql.client.code.blocking;

import com.jacobmountain.graphql.client.Fetcher;
import com.jacobmountain.graphql.client.Subscriber;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.code.Assembler;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;

public class BlockingAssembler extends Assembler {
    public BlockingAssembler(QueryGenerator queryGenerator,
                             Schema schema,
                             TypeMapper typeMapper) {
        super(
                new BlockingQueryStage(queryGenerator, schema, typeMapper),
                new OptionalReturnStage(schema, typeMapper),
                schema, typeMapper,
                Fetcher.class, Subscriber.class
        );
    }
}
