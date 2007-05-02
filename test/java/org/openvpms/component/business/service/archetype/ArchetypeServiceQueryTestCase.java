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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeProperty;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Test that ability to create and query on acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceQueryTestCase extends
                                           AbstractDependencyInjectionSpringContextTests {
    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;


    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceQueryTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceQueryTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
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

        this.service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Test the query by code in the lookup entity. This will support
     * OVPMS-35
     */
    public void testOVPMS35()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Belarus"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test query by code with wildcard
     */
    public void testGetByCodeWithWildcard()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Bel*"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test query by code with wild in short name
     */
    public void testGetCodeWithWildCardShortName()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Bel*"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test query by code with wild in short name and an order clause
     */
    public void testGetCodeWithWildCardShortNameAndOrdered()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false, true)
                .add(new NodeConstraint("name", RelationalOp.EQ, "Bel*"))
                .add(new NodeSortConstraint("name", true));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test OVPMS245
     */
    public void testOVPMS245()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint("product.product", false,
                                        true))
                .add(new CollectionNodeConstraint("classifications", true)
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin)
                        .add(new OrConstraint()
                        .add(new ArchetypeNodeConstraint(
                                ArchetypeProperty.ConceptName,
                                RelationalOp.IsNULL))
                        .add(new AndConstraint()
                        .add(new ArchetypeNodeConstraint(
                                ArchetypeProperty.ConceptName, RelationalOp.EQ,
                                "species"))
                        .add(new NodeConstraint("name", RelationalOp.EQ,
                                                "Canine"))
                        .add(new NodeSortConstraint("name", true)))));

        IPage<IMObject> page = service.get(query);
        assertNotNull(page);
    }

    /**
     * Tests the NodeSet get method. This verifies that subcollections are
     * loaded correctly, avoiding LazyInitializationException.
     */
    public void testGetNodeSet() {
        // set up a party with a single contact and contact purpose
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "0123456789");
        Lookup purpose = (Lookup) service.create("lookup.contactPurpose");
        purpose.setCode("Home");
        service.save(purpose);

        contact.addClassification(purpose);

        Party person = (Party) service.create("person.person");
        person.getDetails().put("lastName", "Anderson");
        person.getDetails().put("firstName", "Tim");
        person.getDetails().put("title", "MR");
        person.addContact(contact);
        service.save(person);

        // query the firstName, lastName and contacts nodes of the person
        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());
        List<String> names = Arrays.asList("firstName", "lastName", "contacts");
        IPage<NodeSet> page = service.getNodes(query, names);
        assertNotNull(page);

        // verify that the page only has a single element, and that the node
        // set has the expected nodes
        assertEquals(1, page.getResults().size());
        NodeSet nodes = page.getResults().get(0);
        assertEquals(3, nodes.getNames().size());
        assertTrue(nodes.getNames().contains("firstName"));
        assertTrue(nodes.getNames().contains("lastName"));
        assertTrue(nodes.getNames().contains("contacts"));

        // verify the values of the simple nodes
        assertEquals(person.getObjectReference(), nodes.getObjectReference());
        assertEquals("Tim", nodes.get("firstName"));
        assertEquals("Anderson", nodes.get("lastName"));

        // verify the values of the contact node. If the classification hasn't
        // been loaded, a LazyInitializationException will be raised by
        // hibernate
        Collection<Contact> contacts
                = (Collection<Contact>) nodes.get("contacts");
        assertEquals(1, contacts.size());
        contact = contacts.toArray(new Contact[0])[0];
        assertEquals("03", contact.getDetails().get("areaCode"));
        assertEquals("0123456789",
                     contact.getDetails().get("telephoneNumber"));
        assertEquals(1, contact.getClassificationsAsArray().length);
        purpose = contact.getClassificationsAsArray()[0];
        assertEquals("Home", purpose.getName());
    }

    /**
     * Tests the partial get method. This verifies that specified subcollections
     * are loaded correctly, avoiding LazyInitializationException.
     */
    public void testGetPartialObject() {
        // set up a party with a single contact and contact purpose
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "0123456789");
        Lookup purpose = (Lookup) service.create("lookup.contactPurpose");
        purpose.setCode("HOME");
        service.save(purpose);

        contact.addClassification(purpose);

        Party person = (Party) service.create("person.person");
        person.getDetails().put("lastName", "Anderson");
        person.getDetails().put("firstName", "Tim");
        person.getDetails().put("title", "MR");
        person.addContact(contact);
        service.save(person);

        // query the firstName, lastName and contacts nodes of the person
        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());
        List<String> names = Arrays.asList("firstName", "lastName", "title",
                                           "contacts");
        IPage<IMObject> page = service.get(query, names);
        assertNotNull(page);

        // verify that the page only has a single element, and that the
        // contacts node has been loaded.
        assertEquals(1, page.getResults().size());
        Party person2 = (Party) page.getResults().get(0);
        Set<Contact> contacts = person2.getContacts();
        assertEquals(1, contacts.size());

        // verify the values of the simple nodes. Note that although details
        // is a collection, it is treated as a simple node by hibernate as it
        // maps to a single column. We specify it to load anyway
        assertEquals(person.getObjectReference(), person2.getObjectReference());
        assertEquals(3, person2.getDetails().size());
        assertEquals("Tim", person.getDetails().get("firstName"));
        assertEquals("Anderson", person.getDetails().get("lastName"));
        assertEquals("MR", person2.getDetails().get("title"));

        // verify the values of the contact node. If the classification hasn't
        // been loaded, a LazyInitializationException will be raised by
        // hibernate
        Contact contact2 = contacts.toArray(new Contact[0])[0];
        assertEquals("03", contact2.getDetails().get("areaCode"));
        assertEquals("0123456789",
                     contact2.getDetails().get("telephoneNumber"));
        assertEquals(1, contact2.getClassificationsAsArray().length);
        Lookup purpose2 = contact2.getClassificationsAsArray()[0];
        assertEquals("HOME", purpose2.getCode());
    }

    /**
     * Verifies that additional constraints can be use with
     * {@link ObjectRefConstraint}.
     */
    public void testOBF155() {
        Party person = (Party) service.create("person.person");
        person.getDetails().put("lastName", "Anderson");
        person.getDetails().put("firstName", "Tim");
        person.getDetails().put("title", "MR");
        service.save(person);

        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());

        // verify that the page only has a single element
        IPage<IMObject> page = service.get(query);
        assertEquals(1, page.getResults().size());

        // constrain the query, and verify the page is empty
        query.add(new NodeConstraint("name", "Mr Foo"));
        page = service.get(query);
        assertEquals(0, page.getResults().size());
    }

}
