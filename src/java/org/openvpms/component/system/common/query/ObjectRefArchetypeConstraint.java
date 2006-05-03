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


package org.openvpms.component.system.common.query;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * An object reference constraint is a linkId constraint on a specific 
 * archetypeId
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ObjectRefArchetypeConstraint extends ArchetypeIdConstraint {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The linkId
     */
    private String linkId;
    
    /**
     * Construct a constraint usin the specified reference
     * @param reference
     *            the object reference
     */
    public ObjectRefArchetypeConstraint(IMObjectReference reference) {
        super(reference.getArchetypeId(), false);
        this.linkId = reference.getLinkId();
    }

    /**
     * @return Returns the linkId.
     */
    public String getLinkId() {
        return linkId;
    }
}