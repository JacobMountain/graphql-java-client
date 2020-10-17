package co.uk.jacobmountain;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.squareup.javapoet.TypeName.INT;

public class TypeMapper {

    private static final Map<String, TypeName> SCALARS = new HashMap<>();

    private final Map<String, TypeName> scalars;

    private final String packageName;

    static {
        // TODO use ints for non-nullable fields
        SCALARS.put("Int", INT.box());
        SCALARS.put("int", INT);
        SCALARS.put("ID", INT.box());
        SCALARS.put("String", ClassName.get(String.class));
    }

    public TypeMapper(String packageName, GraphQLClient.Scalar... scalars) {
        Map<String, TypeName> scalarMap = Stream.of(scalars)
                .collect(Collectors.toMap(GraphQLClient.Scalar::from, GraphQLClientProcessor::getTypeName));
        scalarMap.putAll(SCALARS);
        this.scalars = scalarMap;
        this.packageName = packageName;
    }

    // from graphql type to java poet type
    public TypeName getType(Type<?> type) {
        if (type instanceof ListType) {
            String unwrapped = unwrap(type);
            return ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    getType(unwrapped)
            );
        } else if (type instanceof NonNullType) {
            String unwrapped = unwrap(((NonNullType) type).getType());
            return getType(unwrapped);
        } else {
            String unwrapped = unwrap(type);
            return getType(unwrapped);
        }
    }

    public TypeName defaultPackage(TypeName typeName) {
        if (typeName instanceof ClassName) {
            ClassName className = (ClassName) typeName;
            if (StringUtils.isEmpty(className.packageName())) {
                return ClassName.get(packageName, className.simpleName());
            }
        }
        return typeName;
    }

    public TypeName getType(String name) {
        return scalars.getOrDefault(
                name,
                ClassName.get(packageName, name)
        );
    }

    public static String unwrap(Type<?> type) {
        if (type instanceof ListType) {
            return unwrap(((ListType) type).getType());
        } else if (type instanceof NonNullType) {
            return unwrap(((NonNullType) type).getType());
        } else {
            return ((graphql.language.TypeName) type).getName();
        }
    }

}
