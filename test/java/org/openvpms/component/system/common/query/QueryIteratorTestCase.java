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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.act.Act;

import java.util.Arrays;
import java.util.Date;


/**
 * Tests the {@link QueryIterator} classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-04 06:59:40Z $
 */
public class QueryIteratorTestCase extends AbstractQueryTest {

    /**
     * Tests the {@link IMObjectQueryIterator}.
     */
    public void testIMObjectQueryIterator() {
        ArchetypeQuery query = createQuery();

        Check<Act> check = new Check<Act>() {
            public void check(Act object) {
                // no-op
            }
        };
        query.setMaxResults(1);
        QueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        checkIterator(iterator, check);

        query.setFirstResult(0); // reset
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        iterator = new IMObjectQueryIterator<Act>(query);
        checkIterator(iterator, check);
    }

    /**
     * Tests the {@link NodeSetQueryIterator}.
     */
    public void testNodeSetQueryIterator() {
        ArchetypeQuery query = createQuery();
        Check<NodeSet> check = new Check<NodeSet>() {
            public void check(NodeSet set) {
                Date startTime = (Date) set.get("startTime");
                assertNotNull(startTime);
            }
        };
        QueryIterator<NodeSet> iterator
                = new NodeSetQueryIterator(query, Arrays.asList("startTime"));
        checkIterator(iterator, check);
    }

    /**
     * Tests the {@link ObjectSetQueryIterator}.
     */
    public void testObjectSetQueryIterator() {
        ArchetypeQuery query = createQuery();
        query.add(new NodeSelectConstraint("act.startTime"));

        Check<ObjectSet> check = new Check<ObjectSet>() {
            public void check(ObjectSet set) {
                Date startTime = (Date) set.get("act.startTime");
                assertNotNull(startTime);
            }
        };
        QueryIterator<ObjectSet> iterator = new ObjectSetQueryIterator(query);
        checkIterator(iterator, check);
    }


}