package co.uk.jacobmountain;

import co.uk.jacobmountain.modules.ClientDetails;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor8;

public class ClientDetailsVisitor extends ElementKindVisitor8<ClientDetails, Void> {

    @Override
    public ClientDetails visitType(TypeElement type, Void unused) {
        return ClientDetails.builder()
                .requiresSubscriber(requiresSubscriber(type))
                .requiresFetcher(requiresFetcher(type))
                .build();
    }

    private boolean requiresSubscriber(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .anyMatch(it -> it.getAnnotation(GraphQLSubscription.class) != null);
    }

    private boolean requiresFetcher(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .anyMatch(it -> it.getAnnotation(GraphQLQuery.class) != null);
    }

}
