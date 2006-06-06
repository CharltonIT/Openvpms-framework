/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */



package org.openvpms.component.system.common.jxpath;

// java core
import java.text.SimpleDateFormat;
import java.util.Date;

// commons-beanutils
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;


/**
 * <code>Converter</code> implementation that converts an incoming
 * String into a {@link java.util.Date} object, optionally using a
 * default value or throwing a {@link ConversionException} if a conversion
 * error occurs
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class UtilDateConverter implements Converter {

    /**
     * The default value specified to our Constructor, if any.
     */
    private Object defaultValue = null;

    /**
     * Should we return the default value on conversion errors?
     */
    private boolean useDefault = true;
    
    /**
     * THe date formatter.
     */
    private SimpleDateFormat formatter;

    /**
     * Create a {@link Converter} that will throw a {@link ConversionException}
     * if a conversion error occurs.
     */
    public UtilDateConverter() {
        this.defaultValue = null;
        this.useDefault = false;
        this.formatter = new SimpleDateFormat("dd/MM/yyyy");
    }

    /**
     * Create a {@link Converter} that will return the specified default value
     * if a conversion error occurs.
     *
     * @param defaultValue 
     *            the default value to be returned
     */
    public UtilDateConverter(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.useDefault = true;
    }

    /**
     * Convert the specified input object into an output object of the
     * specified type.
     *
     * @param type  
     *            the type to which this value should be converted
     * @param value 
     *            the input value to be converted
     * @throws ConversionException 
     *            if conversion cannot be performed successfully
     */
    public Object convert(Class type, Object value) {
        if (value == null) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException("No value specified");
            }
        }

        try {
            if (value instanceof String) {
                return (Date)formatter.parse((String)value);
            }
            
            if (value instanceof java.sql.Date) {
                return (Date)value;
            }
        } catch (Exception exception) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException("Cannot convert " + value + 
                        " to type java.util.Date. Must use [" + formatter.toPattern() +
                        "] for this locale.", exception);
            }
        }
        
        // if we get here then throw an exception
        throw new ConversionException("Cannot convert " + value + 
                " to type java.util.Date.");
    }
}