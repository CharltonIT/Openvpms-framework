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


package org.openvpms.component.business.domain.im.common;

// java core
import java.io.Serializable;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * This class holds a reference to another {@link IMObject}. To create a
 * valid reference you must supply an archetypeId and the linkId. 
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class IMObjectReference implements Serializable, Cloneable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * A archetype identity.
     */
    private ArchetypeId archetypeId;
    
    /**
     * The unique identity of the object.
     */
    private String linkId;
    
    /**
     * Default constructor. 
     */
    public IMObjectReference() {
        // do nothing
    }
    
    /**
     * Costruct an object object reference using the specified 
     * {@link IMObject}
     * 
     * @param object
     *            the im object
     * @throws IMObjectException
     *            if an object reference cannot be constructed.                                 
     */
    public IMObjectReference(IMObject object) {
        if (object == null) {
            throw new IMObjectException(
                    IMObjectException.ErrorCode.FailedToCreateObjectReference);
        }

        this.archetypeId = object.getArchetypeId();
        this.linkId = object.getLinkId();
    }
    
    /**
     * Construct an object reference using the specified arhcetype id and 
     * uid
     * 
     * @param archetypeId 
     *            the archetype id of the object
     * @param linkId
     *            the link of the object
     * @throws IMObjectException                                          
     */
    public IMObjectReference(ArchetypeId archetypeId, String linkId) {
        if (archetypeId == null) {
            throw new IMObjectException(
                    IMObjectException.ErrorCode.FailedToCreateObjectReference);
        }
        
        this.archetypeId = archetypeId;
        this.linkId = linkId;
    }

    /**
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }
    
    /**
     * Return the archetypeId as a string
     * 
     * @return String
     */
    public String getArchetypeIdAsString() {
        return archetypeId.getQualifiedName();
    }

    /**
     * @return Returns the linkId.
     */
    public String getLinkId() {
        return linkId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        IMObjectReference rhs = (IMObjectReference)obj;
        return new EqualsBuilder()
            .append(linkId, rhs.linkId)
            .append(archetypeId, rhs.archetypeId)
            .isEquals();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(archetypeId)   
            .append(linkId)
            .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("linkId", linkId)
            .append("archetypeId", archetypeId)
            .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        IMObjectReference copy = (IMObjectReference)super.clone();
        
        copy.archetypeId = this.archetypeId;
        copy.linkId = linkId;
        
        return copy;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * @param linkId The linkId to set.
     */
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

}
