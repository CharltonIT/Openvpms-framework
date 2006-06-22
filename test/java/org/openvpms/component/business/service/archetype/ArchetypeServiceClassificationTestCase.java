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
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// log4j
import org.apache.log4j.Logger;

/**
 * Test classification related functions through the 
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceClassificationTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceClassificationTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceClassificationTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceClassificationTestCase() {
    }

    /**
     * Test OVPMS-241 bug
     */
    public void testOVPMS241()
    throws Exception {
        // create and save a classification
        service.save(createContactPurpose("private"));
        
        Contact contact = createPhoneContact("03", "9763434");
        
        // add a purpose
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("contact.phoneNumber");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("purposes");
        
        List<IMObject> purposes = ArchetypeQueryHelper.get(service,
                new String[]{"classification.contactPurpose"}, true, 
                0, ArchetypeQuery.ALL_ROWS).getRows();
        int acount = purposes.size(); 
        
        ndesc.addChildToCollection(contact, purposes.get(0));
        assertTrue(contact.getClassifications().size() == 1);
        
        try {
            service.save(contact);
        } catch (ValidationException exception) {
            for (ValidationError error : exception.getErrors()) {
                logger.error("Validation Error Node:" + error.getNodeName() 
                        + " Message:" + error.getErrorMessage());
            }
            throw exception;
        }
        
        contact = (Contact)ArchetypeQueryHelper.getByUid(service, 
                contact.getArchetypeId(), contact.getUid());
        assertTrue(contact != null);
        assertTrue(contact.getClassifications().size() == 1);
        
        // remove a contact and save all
        contact.removeClassification(contact.getClassifications().iterator().next());
        service.save(contact);
        contact = (Contact)ArchetypeQueryHelper.getByUid(service,
                contact.getArchetypeId(), contact.getUid());
        assertTrue(contact != null);
        assertTrue(contact.getClassifications().size() == 0);
        purposes = ArchetypeQueryHelper.get(service,
                new String[]{"classification.contactPurpose"}, true, 
                0, ArchetypeQuery.ALL_ROWS).getRows();
        assertTrue(acount == purposes.size()); 
        
    }
    
    public void testClassificationEquals() {
        // create a classification and save
        String name = "randompurpose" + new Random().nextInt();
        Classification purpose = createContactPurpose(name);
        service.save(purpose);

        // reload the classification and verify equals == true
        String[] shortNames = {"classification.contactPurpose"};
        List<IMObject> class1 = ArchetypeQueryHelper.get(service, shortNames,
                                 true, 0, ArchetypeQuery.ALL_ROWS).getRows();
        IMObject purpose2 = get(name, class1);
        assertNotNull(purpose2);
        assertEquals(purpose, purpose2);
    }

    public void testOBF58() {
         // create a purpose & save
        String name = "randompurpose" + new Random().nextInt();
        Classification purpose = createContactPurpose(name);
        service.save(purpose);

        // reload the purpose
        String[] shortNames = {"classification.contactPurpose"};
        List<IMObject> purposes = ArchetypeQueryHelper.get(service, shortNames,
                                 true, 0, ArchetypeQuery.ALL_ROWS).getRows();
        purpose = (Classification) get(name, purposes);
        assertNotNull(purpose);

        Contact contact = createPhoneContact("03", "9763434");

        // add the purpose
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("contact.phoneNumber");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("purposes");
        ndesc.addChildToCollection(contact, purpose);

        // save the contact and reload
        service.save(contact);
        contact = (Contact) ArchetypeQueryHelper.getByObjectReference(service,
                contact.getObjectReference());
        assertTrue(contact != null);

        // get the purpose from the contact and verify it equals the original
        IMObject purpose2 = get(name, ndesc.getChildren(contact));
        assertNotNull(purpose2);
        assertEquals(purpose, purpose2);
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

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
    }
    
    /**
     * Create and return a phone number
     * 
     * @param areaCode
     *            the area code, numeric only
     * @param telephoneNumber
     *            a telephone number, numeric only
     * @return Contact                        
     */
    private Contact createPhoneContact(String areaCode, String telephoneNumber) {
        Contact contact = (Contact)service.create("contact.phoneNumber");
        contact.getDetails().setAttribute("areaCode", areaCode);
        contact.getDetails().setAttribute("telephoneNumber", telephoneNumber);
        contact.setActiveStartTime(new Date());
        
        return contact;
    }
    
    /**
     * Create a contact purpose
     * 
     * @param purpose
     *            the contact purpose
     * @return Classification
     */
    private Classification createContactPurpose(String purpose) {
        Classification classification = (Classification)service.create(
                "classification.contactPurpose");
        classification.setName(purpose);
        
        return classification;
    }
    
    /**
     * Returns an object by name.
     *
     * @param name the name
     * @param objects the objects
     * @return the matching object or null
     */
    protected IMObject get(String name, List<IMObject> objects) {
        for (IMObject object : objects) {
            if (object.getName().equals(name)) {
                return object;
            }
        }
        return null;
    }
}
