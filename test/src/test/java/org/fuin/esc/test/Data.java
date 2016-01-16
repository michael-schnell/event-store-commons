/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. <http://www.fuin.org/>
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
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.esc.test;

import static org.fuin.utils4j.JaxbUtils.marshal;
import static org.fuin.utils4j.JaxbUtils.unmarshal;

import java.io.Serializable;
import java.io.StringReader;

import javax.activation.MimeTypeParseException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.fuin.esc.spi.EnhancedMimeType;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.Immutable;
import org.fuin.objects4j.vo.ValueObject;

/**
 * Represents a block of data in a serialized form.
 */
@Immutable
@XmlRootElement(name = "data")
public final class Data implements ValueObject, Serializable {

    private static final long serialVersionUID = 1000L;

    /** Unique type of the data. */
    @NotNull
    @XmlAttribute(name = "type")
    private String type;

    /** Internet Media Type that classifies the raw event data. */
    @NotNull
    @XmlAttribute(name = "mime-type")
    private String mimeType;

    /** Raw event data in format defined by the mime type and encoding. */
    @NotNull
    @XmlValue
    @XmlJavaTypeAdapter(CDataXmlAdapter.class)
    private String content;

    /**
     * Protected constructor for deserialization.
     */
    protected Data() {
        super();
    }

    /**
     * Creates a data object.
     * 
     * @param type
     *            Unique identifier for the type of data.
     * @param mimeType
     *            Internet Media Type with encoding and version that classifies the data.
     * @param content
     *            Content.
     */
    public Data(@NotNull final String type, @NotNull final EnhancedMimeType mimeType,
            @NotNull final String content) {
        super();

        Contract.requireArgNotNull("type", type);
        Contract.requireArgNotNull("mimeType", mimeType);
        Contract.requireArgNotNull("content", content);

        this.type = type;
        this.mimeType = mimeType.toString();
        this.content = content;

    }

    /**
     * Returns the unique identifier for the type of data.
     * 
     * @return Unique and never changing type name.
     */
    @NotNull
    public final String getType() {
        return type;
    }

    /**
     * Returns the Internet Media Type that classifies the data.
     * 
     * @return Mime type.
     */
    @NotNull
    public final EnhancedMimeType getMimeType() {
        return EnhancedMimeType.create(mimeType);
    }

    /**
     * Returns the raw data block.
     * 
     * @return Raw data.
     */
    @NotNull
    public final String getContent() {
        return content;
    }

    /**
     * Returns the information if the content is XML.
     * 
     * @return TRUE if the mime type is 'application/xml' else FALSE.
     */
    public final boolean isXml() {
        return getMimeType().getBaseType().equals("application/xml");
    }

    /**
     * Returns the information if the content is JSON.
     * 
     * @return TRUE if the mime type is 'application/json' else FALSE.
     */
    public final boolean isJson() {
        return getMimeType().getBaseType().equals("application/json");
    }

    /**
     * Returns the information if the content is TEXT.
     * 
     * @return TRUE if the mime type is 'text/plain' else FALSE.
     */
    public final boolean isText() {
        return getMimeType().getBaseType().equals("text/plain");
    }

    /**
     * Unmarshals the content into an object. Content is required to be "application/xml", "application/json"
     * or "text/plain".
     * 
     * @param classesToBeBound
     *            In case the XML JAXB unmarshalling is used, you have to pass the classes for the content
     *            here.
     * 
     * @return Object created from content.
     * 
     * @param <T>
     *            Type expected to be returned.
     */
    @SuppressWarnings("unchecked")
    public final <T> T unmarshalContent(final Class<?>... classesToBeBound) {

        if (!(isJson() || isXml() || isText())) {
            throw new IllegalStateException("Can only unmarshal JSON, XML or TEXT content, not: " + mimeType);
        }

        // We can only handle JSON...
        if (isJson()) {
            return (T) Json.createReader(new StringReader(content)).readObject();
        }
        // ...or XML
        if (isXml()) {
            return unmarshal(content, classesToBeBound);
        }
        // ...or TEXT
        return (T) content;
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this).append("type", type).append("mimeType", mimeType)
                .append("content", content).toString();
    }

    /**
     * Creates a new instance from a given object.
     * 
     * @param type
     *            Name of the type.
     * @param obj
     *            Object to convert.
     * 
     * @return Either JSON or XML UTF-8 encoded content without a version.
     */
    public static Data valueOf(final String type, final Object obj) {
        return new Data(type, mimeType(obj), content(obj));
    }

    private static EnhancedMimeType mimeType(final Object obj) {
        try {
            // We can only handle JSON...
            if (obj instanceof JsonObject) {
                return new EnhancedMimeType("application/json; encoding=utf-8");
            }
            // ...or XML
            return new EnhancedMimeType("application/xml; encoding=utf-8");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String content(final Object obj) {
        // We can only handle JSON...
        if (obj instanceof JsonObject) {
            return obj.toString();
        }
        // ...or XML
        return marshal(obj, obj.getClass());
    }

}
