package com.jacobmountain.graphql.client;

import com.jacobmountain.graphql.client.dto.Response;

import java.util.function.Consumer;

public interface Subscriber<S, Error> {

    <A> void subscribe(String subscription, A args, Consumer<Response<S, Error>> callback);

}
