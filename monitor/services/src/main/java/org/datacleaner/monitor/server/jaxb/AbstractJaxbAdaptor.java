/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.datacleaner.util.JaxbValidationEventHandler;

/**
 * Utility abstract class for making it easier to implement a writer object for
 * the JAXB model.
 */
public abstract class AbstractJaxbAdaptor<E> {

    private final JAXBContext _jaxbContext;

    protected AbstractJaxbAdaptor(final Class<E> clazz) {
        try {
            _jaxbContext = JAXBContext.newInstance(clazz.getPackage().getName(), clazz.getClassLoader());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected DatatypeFactory getDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected XMLGregorianCalendar createDate(final Date date) {
        if (date == null) {
            return null;
        }
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return getDatatypeFactory().newXMLGregorianCalendar(cal);
    }

    protected Date createDate(final XMLGregorianCalendar gregorianCalendar) {
        if (gregorianCalendar == null) {
            return null;
        }
        return gregorianCalendar.toGregorianCalendar().getTime();
    }

    @SuppressWarnings("unchecked")
    protected E unmarshal(final InputStream in) {
        final Unmarshaller unmarshaller = createUnmarshaller();
        try {
            Object result = unmarshaller.unmarshal(in);
            if (result instanceof JAXBElement<?>) {
                result = ((JAXBElement<?>) result).getValue();
            }
            return (E) result;
        } catch (final JAXBException e) {
            throw new JaxbException(e);
        }
    }

    protected Unmarshaller createUnmarshaller() {
        try {
            final Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new JaxbValidationEventHandler());
            return unmarshaller;
        } catch (final JAXBException e) {
            throw new JaxbException(e);
        }
    }

    protected Marshaller createMarshaller() {
        try {
            final Marshaller marshaller = _jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setEventHandler(new JaxbValidationEventHandler());
            return marshaller;
        } catch (final JAXBException e) {
            throw new JaxbException(e);
        }
    }

    protected void marshal(final JAXBElement<E> obj, final OutputStream outputStream) {
        final Marshaller marshaller = createMarshaller();
        try {
            marshaller.marshal(obj, outputStream);
        } catch (final JAXBException e) {
            throw new JaxbException(e);
        }
    }

    protected void marshal(final E obj, final OutputStream outputStream) {
        final Marshaller marshaller = createMarshaller();
        try {
            marshaller.marshal(obj, outputStream);
        } catch (final JAXBException e) {
            throw new JaxbException(e);
        }
    }

    protected JAXBContext getJaxbContext() {
        return _jaxbContext;
    }
}
