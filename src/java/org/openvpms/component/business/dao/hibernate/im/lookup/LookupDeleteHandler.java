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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.common.AbstractDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.lookup.Lookup;


/**
 * Implementation of {@link DeleteHandler} for {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupDeleteHandler extends AbstractDeleteHandler {

    /**
     * Creates a new <tt>LookupDeleteHandler<tt>.
     *
     * @param assembler the assembler
     */
    public LookupDeleteHandler(CompoundAssembler assembler) {
        super(assembler);
    }

    /**
     * Deletes an object.
     * <p/>
     * This implementation removes relationships associated with the lookup
     * prior to its deletion.
     *
     * @param object  the object to delete
     * @param session the session
     */
    @Override
    protected void delete(IMObjectDO object, Session session) {
        LookupDO lookup = (LookupDO) object;
        // remove relationships where the lookup is the source.
        LookupRelationshipDO[] relationships
                = lookup.getSourceLookupRelationships().toArray(
                new LookupRelationshipDO[0]);
        for (LookupRelationshipDO relationhip : relationships) {
            lookup.removeSourceLookupRelationship(relationhip);
            LookupDO target = (LookupDO) relationhip.getTarget();
            if (target != null) {
                target.removeTargetLookupRelationship(relationhip);
            }
        }

        // now remove relationships where the lookup is the target
        relationships = lookup.getTargetLookupRelationships().toArray(
                new LookupRelationshipDO[0]);
        for (LookupRelationshipDO relationship : relationships) {
            lookup.removeTargetLookupRelationship(relationship);
            LookupDO source = (LookupDO) relationship.getSource();
            if (source != null) {
                source.removeSourceLookupRelationship(relationship);
            }
        }
        session.delete(lookup);
    }

}