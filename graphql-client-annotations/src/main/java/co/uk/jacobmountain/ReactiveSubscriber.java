package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Response;
import org.reactivestreams.Publisher;

public interface ReactiveSubscriber<S, Error> {

    <A> Publisher<Response<S, Error>> subscribe(String subscription, A args);

}
