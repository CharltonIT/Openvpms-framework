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

package org.openvpms.component.business.service.archetype;

// spring-context
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.search.IPage;
import org.openvpms.component.system.common.search.SortCriteria;
import org.openvpms.component.system.common.search.SortCriteria.SortDirection;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// log4j
import org.apache.log4j.Logger;

/**
 * Test that sorting part of the api works on the IArchetypeService
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceSortTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceSortTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceSortTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceSortTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml" 
                };
    }

    /**
     * Test sort on a non-sortable property
     */
    public void testSortOnNonExistentProperty()
    throws Exception {
        try {
            service.get("entity", "act", null, null, false, false, 0, 1, 
                    new SortCriteria("baby", SortDirection.Ascending));
            fail("This request should have thrown an exception");
        } catch (ArchetypeServiceException exception) {
            if (exception.getErrorCode() != ArchetypeServiceException.ErrorCode.InvalidSortProperty) {
                fail (exception.getErrorCode() + " is not a valid exception");
            }
        }
    }
    
    /**
     * Test sort on name in ascending order
     */
    public void testSortOnNameInAscendingOrder() 
    throws Exception {
        IPage<IMObject> objects = service.get("entity", "act", null, null, 
                false, false, 0, IPage.ALL_ROWS, 
                new SortCriteria("name", SortDirection.Ascending));
        IMObject lhs = null;
        IMObject rhs = null;
        for (IMObject object : objects.getRows()) {
            if (lhs == null) {
                lhs = object;
                continue;
            }
            rhs = object;
            if (lhs.getName().compareTo(rhs.getName()) == 1) {
                fail("The objects are not in ascending order lhs="
                        + lhs.getName() + " rhs=" + rhs.getName());
            }
            lhs = rhs;
            if (logger.isDebugEnabled()) {
                logger.debug("Name :" + object.getName());
            }
        }
    }
    
    /**
     * Test sort on name in ascending order
     */
    public void testSortOnNameInDescendingOrder() 
    throws Exception {
        IPage<IMObject> objects = service.get("entity", "act", null, null, 
                false, false, 0, IPage.ALL_ROWS, 
                new SortCriteria("name", SortDirection.Descending));
        IMObject lhs = null;
        IMObject rhs = null;
        for (IMObject object : objects.getRows()) {
            if (lhs == null) {
                lhs = object;
                continue;
            }
            rhs = object;
            if (lhs.getName().compareTo(rhs.getName()) == -1) {
                fail("The objects are not in descending order lhs="
                        + lhs.getName() + " rhs=" + rhs.getName());
            }
            
            lhs = rhs;
            if (logger.isDebugEnabled()) {
                logger.debug("Name :" + object.getName());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
    }
    
    
}