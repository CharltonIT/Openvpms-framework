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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Tests the {@link IMObjectBean} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class IMObjectBeanTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link IMObjectBean#isA} method.
     */
    @Test
    public void testIsA() {
        IMObjectBean bean = createBean("act.customerAccountPayment");
        String[] matches = {"act.customerAccountPaymentCash",
                            "act.customerAccountPayment"};
        assertTrue(bean.isA(matches));

        String[] nomatches = {"act.customerAccountPaymentCash",
                              "act.customerAccountPaymentCredit",
                              "act.customerAccountPaymentEFT"};
        assertFalse(bean.isA(nomatches));

        // test wildcards
        String[] wildmatch = {"act.customerEstimation*", "act.customerAccount*"};
        assertTrue(bean.isA(wildmatch));

        String[] wildnomatch = {"act.customerEstimation*",
                                "act.customerInvoice*"};
        assertFalse(bean.isA(wildnomatch));

    }

    /**
     * Tests the {@link IMObjectBean#hasNode(String)} method.
     */
    @Test public void testHasNode() {
        IMObjectBean bean = createBean("party.customerperson");
        assertTrue(bean.hasNode("firstName"));
        assertFalse(bean.hasNode("nonode"));
    }

    /**
     * Tests the {@link IMObjectBean#getDescriptor} method.
     */
    @Test public void testGetDescriptor() {
        IMObjectBean bean = createBean("party.customerperson");
        NodeDescriptor node = bean.getDescriptor("firstName");
        assertNotNull(node);
        assertEquals("firstName", node.getName());

        assertNull(bean.getDescriptor("nonode"));
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName()} method.
     */
    @Test public void testGetDisplayName() {
        IMObjectBean pet = createBean("party.animalpet");
        assertEquals("Patient(Pet)", pet.getDisplayName());

        // verify shortname is returned when no display name is present
        IMObjectBean customer = createBean("party.customerperson");
        assertEquals("Customer(Person)", customer.getDisplayName());
    }

    /**
     * Tests the {@link IMObjectBean#getDisplayName(String)} method.
     */
    @Test public void testNodeDisplayName() {
        IMObjectBean act = createBean("act.customerAccountPayment");
        assertEquals("Date", act.getDisplayName("startTime"));

        // verify that a node without a custom display name is an uncamel-cased
        // version of the node name
        IMObjectBean pet = createBean("party.animalpet");
        assertEquals("Date Of Birth", pet.getDisplayName("dateOfBirth"));
    }

    /**
     * Tests the {@link IMObjectBean#getArchetypeRange(String)} method.
     */
    @Test public void testGetArchetypeRange() {
        IMObjectBean bean = createBean("party.customerperson");

        // check a node with an archetype range assertion
        Set<String> shortNames = new HashSet<String>(Arrays.asList(bean.getArchetypeRange("contacts")));
        assertEquals(2, shortNames.size());
        assertTrue(shortNames.contains("contact.location"));
        assertTrue(shortNames.contains("contact.phoneNumber"));

        // check a node with no assertion
        assertEquals(0, bean.getArchetypeRange("name").length);
    }

    /**
     * Tests the {@link IMObjectBean#getValue(String)} and
     * {@link IMObjectBean#setValue(String, Object)} for a non-existent node.
     */
    @Test public void testGetSetInvalidNode() {
        IMObjectBean bean = createBean("party.customerperson");
        try {
            bean.getValue("badNode");
            assertTrue(false);
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }

        try {
            bean.setValue("badNode", "value");
            assertTrue(false);
        } catch (IMObjectBeanException expected) {
            assertEquals(NodeDescriptorNotFound, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link IMObjectBean#getValue(String)} method.
     */
    @Test public void testGetValue() {
        IMObjectBean bean = createBean("party.customerperson");
        assertEquals(bean.getValue("firstName"), null);
        bean.setValue("firstName", "Joe");
        assertEquals("Joe", bean.getValue("firstName"));
    }

    /**
     * Tests the {@link IMObjectBean#getBoolean(String)} method.
     */
    @Test public void testGetBoolean() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(false, bean.getBoolean("flag"));
        assertEquals(true, bean.getBoolean("flag", true));

        bean.setValue("flag", true);
        assertEquals(true, bean.getBoolean("flag"));
    }

    /**
     * Tests the {@link IMObjectBean#getInt} methods.
     */
    @Test public void testGetInt() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(0, bean.getInt("size"));
        assertEquals(-1, bean.getInt("size", -1));

        int size = 100;
        bean.setValue("size", size);
        assertEquals(size, bean.getInt("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getLong} methods.
     */
    @Test public void testGetLong() {
        IMObjectBean bean = createBean("act.types");
        assertEquals(0, bean.getLong("size"));
        assertEquals(-1, bean.getLong("size", -1));

        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(size, bean.getLong("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getString} methods.
     */
    @Test public void testGetString() {
        IMObjectBean bean = createBean("act.types");
        assertNull(bean.getValue("name"));
        assertEquals("foo", bean.getString("name", "foo"));

        bean.setValue("name", "bar");
        assertEquals("bar", bean.getValue("name"));

        // test conversion, long -> string
        long size = 10000000L;
        bean.setValue("size", size);
        assertEquals(Long.toString(size), bean.getString("size"));
    }

    /**
     * Tests the {@link IMObjectBean#getBigDecimal} methods.
     */
    @Test public void testGetBigDecimal() {
        IMObjectBean bean = createBean("act.types");

        assertNull(bean.getBigDecimal("amount"));
        assertEquals(bean.getBigDecimal("amount", BigDecimal.ZERO),
                     BigDecimal.ZERO);

        BigDecimal expected = new BigDecimal("1234.56");
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getBigDecimal("amount"));

        // quantity has a default value
        assertEquals(BigDecimal.ONE, bean.getBigDecimal("quantity"));
    }

    /**
     * Tests the {@link IMObjectBean#getMoney} methods.
     */
    @Test public void testMoney() {
        IMObjectBean bean = createBean("act.types");

        assertNull(bean.getMoney("amount"));
        assertEquals(bean.getMoney("amount", new Money(0)), new Money(0));

        Money expected = new Money("1234.56");
        bean.setValue("amount", expected);
        assertEquals(expected, bean.getMoney("amount"));
    }

    /**
     * Tests the {@link IMObjectBean#getDate} methods.
     */
    @Test public void testGetDate() {
        IMObjectBean bean = createBean("act.types");

        Date now = new Date();
        assertNull(bean.getDate("endTime"));
        assertEquals(bean.getDate("endTime", now), now);

        bean.setValue("endTime", now);
        assertEquals(now, bean.getDate("endTime"));
    }

    /**
     * Tests the {@link IMObjectBean#getValues(String)},
     * {@link IMObjectBean#addValue(String, IMObject)} and
     * {@link IMObjectBean#removeValue(String, IMObject)} methods.
     */
    @Test public void testCollection() {
        IMObjectBean bean = createBean("party.customerperson");
        List<IMObject> values = bean.getValues("contacts");
        assertNotNull(values);
        assertEquals(0, values.size());
        IMObjectBean locationBean = createBean("contact.location");
        IMObjectBean phoneBean = createBean("contact.phoneNumber");

        IMObject location = locationBean.getObject();
        IMObject phone = phoneBean.getObject();
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        checkEquals(bean.getValues("contacts"), location, phone);

        bean.removeValue("contacts", location);
        checkEquals(bean.getValues("contacts"), phone);

        bean.removeValue("contacts", phone);
        assertEquals(0, bean.getValues("contacts").size());

        // removal of non-existent object is a no-op
        bean.removeValue("contacts", phone);
    }

    /**
     * Tests the {@link IMObjectBean#getValues(String, Class)} method.
     */
    @Test public void testGetValuesTypeSafeCast() {
        IMObjectBean bean = createBean("party.customerperson");
        List<IMObject> values = bean.getValues("contacts");
        assertNotNull(values);
        assertEquals(0, values.size());
        IMObjectBean locationBean = createBean("contact.location");
        IMObjectBean phoneBean = createBean("contact.phoneNumber");

        Contact location = (Contact) locationBean.getObject();
        Contact phone = (Contact) phoneBean.getObject();
        assertNotNull(location);
        assertNotNull(phone);

        bean.addValue("contacts", location);
        bean.addValue("contacts", phone);
        List<Contact> contacts = bean.getValues("contacts", Contact.class);
        checkEquals(contacts, location, phone);

        try {
            bean.getValues("contacts", Act.class);
            fail("Expected IMObjectBeanException");
        } catch (IMObjectBeanException exception) {
            assertEquals(InvalidClassCast, exception.getErrorCode());
            assertEquals("Expected class of type " + Act.class.getName()
                    + " but got " + Contact.class.getName(),
                         exception.getMessage());
        }
    }

    /**
     * Tests the {@link IMObjectBean#getReference(String)} method.
     */
    @Test public void testGetReferenceNode() {
        IMObjectBean bean = createBean("actRelationship.simple");
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = (Act) service.create("act.simple");
        bean.setValue("source", act.getObjectReference());
        assertEquals(act.getObjectReference(), bean.getReference("source"));
    }

    /**
     * Tests the {@link IMObjectBean#getObject(String)} method.
     */
    @Test public void testGetObjectNode() {
        IMObjectBean bean = createBean("actRelationship.simple");
        assertNull(bean.getObject("source"));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Act act = (Act) service.create("act.simple");
        service.save(act);

        bean.setValue("source", act.getObjectReference());
        assertEquals(act, bean.getObject("source"));
    }

    /**
     * Tests the {@link IMObjectBean#save} method.
     */
    @Test public void testSave() {
        String name = "Bar,Foo";

        IMObjectBean bean = createBean("party.customerperson");
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        bean.setValue("title", "MR");

        // name is derived, so should be null when accessed via the object
        assertNull(bean.getObject().getName());

        // ... but non-null when accessed via its node
        assertEquals(name, bean.getValue("name"));

        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();

        bean.save();
        // verify that the name has been set on the object
        assertEquals(name, bean.getObject().getName());

        // verify that the object saved
        IMObject object = service.get(bean.getObject().getObjectReference());
        assertEquals(bean.getObject(), object);

        // verify that the name node was saved
        assertEquals(object.getName(), name);
    }

    /**
     * Verifies that two lists of objects match.
     *
     * @param actual the actual result
     * @param expected the expected result
     */
    private <T extends IMObject> void checkEquals(List<T> actual, T ... expected) {
        assertEquals(actual.size(), expected.length);
        for (IMObject e : expected) {
            boolean found = false;
            for (IMObject a : actual) {
                if (e.equals(a)) {
                    found = true;
                    break;
                }
            }
            assertTrue("IMObject not found: " + e, found);
        }
    }

}
