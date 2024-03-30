package org.fuin.esc.jsonb;

import jakarta.validation.constraints.NotNull;
import org.eclipse.yasson.FieldAccessStrategy;
import org.fuin.esc.api.SerializedDataTypeRegistry;
import org.fuin.esc.api.SimpleSerializerDeserializerRegistry;
import org.fuin.objects4j.common.Contract;
import org.fuin.utils4j.TestOmitted;

import java.nio.charset.StandardCharsets;

/**
 * Helper methods for the test package.
 */
@TestOmitted("This is only a test class")
final class TestUtils {

    private TestUtils() {
    }

    /**
     * Creates a {@link JsonbDeSerializer} with standard settings.
     *
     * @return New instance.
     */
    public static JsonbDeSerializer createJsonbDeSerializer() {
        return JsonbDeSerializer.builder()
                .withSerializers(EscJsonbUtils.createEscJsonbSerializers())
                .withDeserializers(EscJsonbUtils.createEscJsonbDeserializers())
                .withPropertyVisibilityStrategy(new FieldAccessStrategy())
                .withEncoding(StandardCharsets.UTF_8)
                .build();
    }

    /**
     * Creates a registry that connects the type with the appropriate serializer and de-serializer.
     *
     * @param typeRegistry Type registry (Mapping from type name to class).
     * @param jsonbDeSer   JSON-B serializer/deserializer to use.
     */
    public static void initSerDeserializerRegistry(@NotNull SerializedDataTypeRegistry typeRegistry,
                                                   @NotNull JsonbDeSerializer jsonbDeSer) {

        Contract.requireArgNotNull("typeRegistry", typeRegistry);
        Contract.requireArgNotNull("jsonbDeSer", jsonbDeSer);

        SimpleSerializerDeserializerRegistry registry = new SimpleSerializerDeserializerRegistry();

        // Base types always needed
        registry.add(EscEvents.SER_TYPE, jsonbDeSer.getMimeType().getBaseType(), jsonbDeSer);
        registry.add(EscEvent.SER_TYPE, jsonbDeSer.getMimeType().getBaseType(), jsonbDeSer);
        registry.add(EscMeta.SER_TYPE, jsonbDeSer.getMimeType().getBaseType(), jsonbDeSer);

        // User defined types
        registry.add(MyMeta.SER_TYPE, jsonbDeSer.getMimeType().getBaseType(), jsonbDeSer);
        registry.add(MyEvent.SER_TYPE, jsonbDeSer.getMimeType().getBaseType(), jsonbDeSer);

        jsonbDeSer.init(typeRegistry, registry, registry);

    }

}
