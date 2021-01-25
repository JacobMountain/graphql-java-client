package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.annotations.GraphQLFragment;
import com.jacobmountain.graphql.client.annotations.GraphQLMutation;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;
import com.jacobmountain.graphql.client.annotations.GraphQLSubscription;
import com.jacobmountain.graphql.client.modules.ClientDetails;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor8;
import java.lang.annotation.Annotation;
import java.util.Arrays;

public class ClientDetailsVisitor extends ElementKindVisitor8<ClientDetails, Void> {

    @Override
    public ClientDetails visitType(TypeElement type, Void unused) {
        return ClientDetails.builder()
                .requiresSubscriber(requiresSubscriber(type))
                .requiresFetcher(requiresFetcher(type))
                .fragments(Arrays.asList(type.getAnnotationsByType(GraphQLFragment.class)))
                .build();
    }

    private boolean requiresSubscriber(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .anyMatch(it -> hasAnnotation(it, GraphQLSubscription.class));
    }

    private boolean requiresFetcher(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .anyMatch(it -> hasAnnotation(it, GraphQLQuery.class) || hasAnnotation(it, GraphQLMutation.class));
    }

    private boolean hasAnnotation(Element el, Class<? extends Annotation> annotation) {
        return el.getAnnotation(annotation) != null;
    }

}
