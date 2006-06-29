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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Tests the {@link TypeHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TypeHelperTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service service
     */
    private ArchetypeService _service;

    /**
     * Tests the {@link TypeHelper#isA(IMObject, String)} method.
     */
    public void testIsASingle() {
        IMObject object = _service.create("animal.pet");
        assertNotNull(object);
        assertTrue(TypeHelper.isA(object, "animal.pet"));
        assertFalse(TypeHelper.isA(object, "horse.pet"));

        // test wildcards
        assertTrue(TypeHelper.isA(object, "animal.*"));
        assertTrue(TypeHelper.isA(object, "animal.p*"));
        assertFalse(TypeHelper.isA(object, "horse.p*"));
    }

    /**
     * Tests the {@link TypeHelper#isA(IMObject, String...)} method.
     */
    public void testIsAMultiple() {
        IMObject object = _service.create("act.customerAccountPayment");
        assertNotNull(object);
        String[] matches = {"act.customerAccountPaymentCash",
                            "act.customerAccountPayment"};
        assertTrue(TypeHelper.isA(object, matches));

        String[] nomatches = {"act.customerAccountPaymentCash",
                              "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(TypeHelper.isA(object, nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(TypeHelper.isA(object, wildmatch));

        String[] wildnomatch = {"act.customerEstimation*",
                                "act.customerInvoice*"};
        assertFalse(TypeHelper.isA(object, wildnomatch));
    }

    /**
     * Tests the {@link TypeHelper#isA(IMObjectReference, String)} method.
     */
    public void testIsARef() {
        IMObject object = _service.create("party.customerperson");
        assertNotNull(object);
        IMObjectReference ref = object.getObjectReference();

        assertTrue(TypeHelper.isA(ref, "party.customerperson"));
        assertFalse(TypeHelper.isA(ref, "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.isA(ref, "party.*"));
        assertTrue(TypeHelper.isA(ref, "party.customer*"));
        assertFalse(TypeHelper.isA(ref, "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#matches(ArchetypeId, String)} method.
     */
    public void testMatchesId() {
        IMObject object = _service.create("party.customerperson");
        assertNotNull(object);
        ArchetypeId id = object.getArchetypeId();

        assertTrue(TypeHelper.matches(id, "party.customerperson"));
        assertFalse(TypeHelper.matches(id, "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.matches(id, "party.*"));
        assertTrue(TypeHelper.matches(id, "party.customer*"));
        assertFalse(TypeHelper.matches(id, "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#matches(String, String)} method.
     */
    public void testMatches() {
        assertTrue(TypeHelper.matches("party.customerperson",
                                      "party.customerperson"));
        assertFalse(TypeHelper.matches("party.customerperson",
                                       "party.patientpet"));

        // test wildcards
        assertTrue(TypeHelper.matches("party.customerperson", "party.*"));
        assertTrue(TypeHelper.matches("party.customerperson",
                                      "party.customer*"));
        assertFalse(TypeHelper.matches("party.customerperson",
                                       "party.patient*"));
    }

    /**
     * Tests the {@link TypeHelper#matches(String[], String)} method.
     */
    public void testShortNamesMatches() {
        String[] parties = {"party.customerperson", "party.patientpet"};
        assertTrue(TypeHelper.matches(parties, "party.*"));
        assertTrue(TypeHelper.matches(parties, "party*"));
        assertFalse(TypeHelper.matches(parties, "*.customerperson"));
    }

    /**
     * (non-Javadoc)
     *
     * @see AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        _service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }
}
