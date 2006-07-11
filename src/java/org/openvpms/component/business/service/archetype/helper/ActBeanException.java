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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.apache.commons.resources.Messages;


/**
 * Exception class for exceptions raised by {@link ActBean}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActBeanException extends OpenVPMSException {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An enumeration of error codes.
     */
    public enum ErrorCode {
        ArchetypeNotFound
    }

    /**
     * The error code.
     */
    private final ErrorCode _errorCode;

    /**
     * The appropriate resource file is loaded cached into memory when this
     * class is loaded.
     */
    private static Messages MESSAGES
            = Messages.getMessages(
                    "org.openvpms.component.business.service.archetype.helper."
                    + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Constructs a new <code>ActBeanException</code>.
     *
     * @param errorCode the error code
     */
    public ActBeanException(ErrorCode errorCode, Object ... args) {
        super(ActBeanException.MESSAGES.getMessage(errorCode.toString(), args));
        _errorCode = errorCode;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public ActBeanException.ErrorCode getErrorCode() {
        return _errorCode;
    }

}
