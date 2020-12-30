package com.jacobmountain;

import com.jacobmountain.dto.Response;
import org.reactivestreams.Publisher;

public interface ReactiveSubscriber<S, Error> {

    <A> Publisher<Response<S, Error>> subscribe(String subscription, A args);

}
