/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.esc.esjc;

import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;

import org.fuin.esc.api.CommonEvent;
import org.fuin.esc.api.TypeName;
import org.fuin.esc.spi.Base64Data;
import org.fuin.esc.spi.Converter;
import org.fuin.esc.spi.EnhancedMimeType;
import org.fuin.esc.spi.EscMeta;
import org.fuin.esc.spi.EscSpiUtils;
import org.fuin.esc.spi.SerializedDataType;
import org.fuin.esc.spi.Serializer;
import org.fuin.esc.spi.SerializerRegistry;
import org.fuin.objects4j.common.Contract;

import com.github.msemys.esjc.EventData;

/**
 * Converts a {@link CommonEvent} into {@link EventData}.
 */
public final class CommonEvent2EventDataConverter implements Converter<CommonEvent, EventData> {

    private static final EnhancedMimeType XML_UTF8 = EnhancedMimeType.create("application", "xml",
            Charset.forName("utf-8"));

    private static final EnhancedMimeType JSON_UTF8 = EnhancedMimeType.create("application", "json",
            Charset.forName("utf-8"));;

    private final SerializerRegistry serRegistry;

    private final EnhancedMimeType targetContentType;

    /**
     * Constructor with all mandatory data.
     * 
     * @param serRegistry
     *            Registry used to locate serializers.
     * @param targetContentType
     *            Target content type (Allows only 'application/xml' or 'application/json' with 'utf-8'
     *            encoding).
     */
    public CommonEvent2EventDataConverter(@NotNull final SerializerRegistry serRegistry,
            final EnhancedMimeType targetContentType) {
        super();
        Contract.requireArgNotNull("serRegistry", serRegistry);
        Contract.requireArgNotNull("targetContentType", targetContentType);
        if (!(targetContentType.matchEncoding(JSON_UTF8) || targetContentType.matchEncoding(XML_UTF8))) {
            throw new IllegalArgumentException(
                    "Only 'application/xml' or 'application/json' with 'utf-8' encoding is allowed, but was: "
                            + targetContentType);
        }
        this.serRegistry = serRegistry;
        this.targetContentType = targetContentType;
    }

    /**
     * Converts a common event into event data.
     * 
     * @param commonEvent
     *            Event to convert.
     * 
     * @return Converted event as event data.
     */
    @Override
    public final EventData convert(final CommonEvent commonEvent) {

        // User's data
        final SerializedDataType serUserDataType = new SerializedDataType(commonEvent.getDataType()
                .asBaseType());
        final Serializer userDataSerializer = serRegistry.getSerializer(serUserDataType);
        final byte[] serUserData = userDataSerializer.marshal(commonEvent.getData());
        final byte[] serData;
        if (userDataSerializer.getMimeType().matchEncoding(targetContentType)) {
            serData = serUserData;
        } else {
            final Serializer dataSerializer = getSerializer(Base64Data.TYPE);
            serData = dataSerializer.marshal(new Base64Data(serUserData));
        }

        // EscMeta
        final EscMeta escMeta = EscSpiUtils.createEscMeta(serRegistry, targetContentType, commonEvent);
        final Serializer escMetaSerializer = getSerializer(EscMeta.TYPE);
        final byte[] escSerMeta = escMetaSerializer.marshal(escMeta);

        // Create event data
        final EventData.Builder builder = EventData.newBuilder().eventId(commonEvent.getId().asBaseType())
                .type(commonEvent.getDataType().asBaseType());
        if (targetContentType.isJson()) {
            builder.jsonData(serData);
            builder.jsonMetadata(escSerMeta);
        } else {
            builder.data(serData);
            builder.metadata(escSerMeta);
        }
        return builder.build();

    }

    private Serializer getSerializer(final TypeName typeName) {
        final SerializedDataType serDataType = new SerializedDataType(typeName.asBaseType());
        final Serializer serializer = serRegistry.getSerializer(serDataType);
        if (!serializer.getMimeType().matchEncoding(targetContentType)) {
            throw new IllegalArgumentException("Target content type is '" + targetContentType
                    + "', but serializer returned for " + serDataType + "' was: " + serializer.getMimeType());
        }
        return serializer;
    }

    @Override
    public final Class<CommonEvent> getSourceType() {
        return CommonEvent.class;
    }

    @Override
    public final Class<EventData> getTargetType() {
        return EventData.class;
    }

}
