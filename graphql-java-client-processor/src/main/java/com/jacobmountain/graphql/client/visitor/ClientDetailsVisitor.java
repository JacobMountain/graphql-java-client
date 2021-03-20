package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.annotations.GraphQLMutation;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;
import com.jacobmountain.graphql.client.annotations.GraphQLSubscription;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor8;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class ClientDetailsVisitor extends ElementKindVisitor8<ClientDetails, ClientDetailsVisitorArgs> {

    @Override
    public ClientDetails visitType(TypeElement type, ClientDetailsVisitorArgs args) {
        return ClientDetails.builder()
                .name(type.getSimpleName().toString())
                .clientInterface(ClassName.get(type))
                .requiresSubscriber(requiresSubscriber(type))
                .requiresFetcher(requiresFetcher(type))
                .methods(visitMethods(args, type))
                .build();
    }

    private List<MethodDetails> visitMethods(ClientDetailsVisitorArgs args, TypeElement type) {
        final MethodDetailsVisitor methodVisitor = new MethodDetailsVisitor(args.getSchema());
        return type.getEnclosedElements()
                .stream()
                .map(method -> methodVisitor.visit(method, args.getTypeMapper()))
                .collect(Collectors.toList());
    }

    private boolean requiresSubscriber(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .anyMatch(method -> hasAnnotation(method, GraphQLSubscription.class));
    }

    private boolean requiresFetcher(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .anyMatch(method -> hasAnnotation(method, GraphQLQuery.class) || hasAnnotation(method, GraphQLMutation.class));
    }

    private boolean hasAnnotation(Element el, Class<? extends Annotation> annotation) {
        return el.getAnnotation(annotation) != null;
    }

}
