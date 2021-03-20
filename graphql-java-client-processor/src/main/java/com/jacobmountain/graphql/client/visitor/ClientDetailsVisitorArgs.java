package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.utils.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientDetailsVisitorArgs {

    private Schema schema;

    private TypeMapper typeMapper;

}
