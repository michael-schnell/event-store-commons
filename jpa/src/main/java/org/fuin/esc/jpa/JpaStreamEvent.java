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
package org.fuin.esc.jpa;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.fuin.objects4j.common.Contract;

/**
 * Connects the stream with the event entries.
 */
@MappedSuperclass
public abstract class JpaStreamEvent {

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "EVENTS_ID", nullable = false, updatable = false)
    private JpaEvent event;

    /**
     * Protected default constructor only required for JPA.
     */
    protected JpaStreamEvent() {
        super();
    }

    /**
     * Constructs a stream event.
     * 
     * @param event
     *            Event to be connected with this event stream.
     */
    public JpaStreamEvent(@NotNull final JpaEvent event) {
        super();
        Contract.requireArgNotNull("eventEntry", event);
        this.event = event;
    }

    /**
     * Returns the actual event.
     * 
     * @return Event connected with this stream
     */
    @NotNull
    public final JpaEvent getEvent() {
        return event;
    }

}
