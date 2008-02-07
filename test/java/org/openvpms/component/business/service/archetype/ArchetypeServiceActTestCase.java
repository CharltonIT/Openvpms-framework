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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.hibernate.im.entity.IMObjectDAOHibernate;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import static org.openvpms.component.business.dao.im.common.IMObjectDAOException.ErrorCode.FailedToDeleteCollectionOfObjects;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import static org.openvpms.component.business.service.archetype.ArchetypeServiceException.ErrorCode.FailedToDeleteObject;
import static org.openvpms.component.business.service.archetype.ArchetypeServiceException.ErrorCode.FailedToSaveCollectionOfObjects;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Test that ability to create and query on acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceActTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Test the creation of a simple act.
     */
    public void testSimpleActCreation() throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        service.save(person);
        Act act = createSimpleAct("study", "inprogress");
        Participation participation = createSimpleParticipation(
                "studyParticipation",
                person, act);
        act.addParticipation(participation);
        service.save(act);

        Act act1 = (Act) ArchetypeQueryHelper.getByUid(
                service, act.getArchetypeId(), act.getUid());
        assertEquals(act1, act);
    }

    /**
     * Test the search by acts function
     */
    @SuppressWarnings("unchecked")
    public void testGetActs() throws Exception {
        // create an act which participates in 5 acts
        Party person = createPerson("MR", "Jim", "Alateras");
        for (int index = 0; index < 5; index++) {
            Act act = createSimpleAct("study" + index, "inprogress");
            Participation participation = createSimpleParticipation(
                    "studyParticipation",
                    person, act);
            act.addParticipation(participation);
            service.save(act);
        }

        service.save(person);

        // now use the getActs request
        IPage<Act> acts = ArchetypeQueryHelper.getActs(
                service, person.getObjectReference(),
                "participation.simple", "act", "simple",
                null, null, null, null,
                null, false, 0, ArchetypeQuery.ALL_RESULTS);
        assertEquals(5, acts.getTotalResults());

        // now look at the paging aspects
        acts = ArchetypeQueryHelper.getActs(service,
                                            person.getObjectReference(),
                                            "participation.simple", "act",
                                            "simple", null, null, null, null,
                                            null, false, 0, 1);
        assertEquals(5, acts.getTotalResults());
        assertEquals(1, acts.getResults().size());
        assertFalse(StringUtils.isEmpty(acts.getResults().get(0).getName()));
    }

    /**
     * Retrieve acts using a start and end date.
     */
    public void testGetActsBetweenTimes() throws Exception {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 2 * 60 * 60 * 1000);
        ArchetypeQuery query = new ArchetypeQuery("act.simple", false, true)
                .add(new NodeConstraint("startTime", RelationalOp.BTW,
                                        startTime, endTime))
                .add(new NodeConstraint("name", "between"));
        int acount = service.get(query).getResults().size();
        service.save(createSimpleAct("between", "start"));
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);

        for (int index = 0; index < 5; index++) {
            service.save(createSimpleAct("between", "start"));
        }
        acount1 = service.get(query).getResults().size();
        assertEquals(acount + 6, acount1);
    }

    /**
     * Tests OVPMS-211.
     */
    public void testOVPMS211() throws Exception {
        Act estimationItem1
                = (Act) service.create("act.customerEstimationItem");
        ActBean estimationItem1Bean = new ActBean(estimationItem1);
        estimationItem1Bean.setValue("fixedPrice", "1.0");
        estimationItem1Bean.setValue("lowQty", "2.0");
        estimationItem1Bean.setValue("lowUnitPrice", "3.0");
        estimationItem1Bean.setValue("highQty", "4.0");
        estimationItem1Bean.setValue("highUnitPrice", "5.0");
        estimationItem1Bean.save();

        Act estimation = (Act) service.create("act.customerEstimation");
        ActBean estimationBean = new ActBean(estimation);
        estimationBean.setValue("status", "IN_PROGRESS");
        estimationBean.addRelationship("actRelationship.customerEstimationItem",
                                       estimationItem1);

        Act estimationItem2
                = (Act) service.create("act.customerEstimationItem");
        ActBean estimationItem2Bean = new ActBean(estimationItem2);
        estimationItem2Bean.setValue("fixedPrice", "2.0");
        estimationItem2Bean.setValue("lowQty", "3.0");
        estimationItem2Bean.setValue("lowUnitPrice", "4.0");
        estimationItem2Bean.setValue("highQty", "5.0");
        estimationItem2Bean.setValue("highUnitPrice", "6.0");
        estimationItem2Bean.save();

        estimationBean.addRelationship("actRelationship.customerEstimationItem",
                                       estimationItem2);
        estimationBean.save();

        // reload the estimation
        estimation = reload(estimation);
        estimationBean = new ActBean(estimation);

        // verify low & high totals have been calculated
        BigDecimal lowTotal = estimationBean.getBigDecimal("lowTotal");
        BigDecimal highTotal = estimationBean.getBigDecimal("highTotal");
        assertTrue(lowTotal.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(highTotal.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Tests OVPMS-228.
     */
    public void testOVPMS228() throws Exception {
        Act act = (Act) service.create("act.customerAccountPayment");
        assertNotNull(act);
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                act.getArchetypeId());
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("amount");
        assertNotNull(ndesc);
        ndesc.getValue(act);
        assertTrue(ndesc.getValue(act).getClass().getName(),
                   ndesc.getValue(act) instanceof BigDecimal);
    }

    /**
     * Saves a collection of acts.
     *
     * @throws Exception for any error
     */
    public void testSaveCollection() throws Exception {
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        List<IMObject> acts = Arrays.asList((IMObject) act1, act2, act3);
        checkSaveCollection(acts, 0);

        // verify the acts can be re-saved
        checkSaveCollection(acts, 1);

        // now change the first act, and attempt to re-save the collection.
        // This should fail as the collection doesn't have the latest version
        // of act1
        act1 = reload(act1);
        act1.setStatus("POSTED");
        service.save(act1);
        try {
            checkSaveCollection(acts, 2);
            fail("Expected save to fail");
        } catch (ArchetypeServiceException expected) {
            assertEquals(FailedToSaveCollectionOfObjects,
                         expected.getErrorCode());
        }
    }

    /**
     * Verifies that the {@link IArchetypeService#save(Collection<IMObject>)}
     * method can be used to save 2 or more acts that reference the same
     * ActRelationship.
     *
     * @throws Exception for any error
     */
    public void testOBF163() throws Exception {
        Act estimation = (Act) service.create("act.customerEstimation");
        estimation.setStatus("POSTED");
        ActRelationship relationship = (ActRelationship) service.create(
                "actRelationship.customerEstimationItem");
        Act item = (Act) service.create("act.customerEstimationItem");
        relationship.setSource(estimation.getObjectReference());
        relationship.setTarget(item.getObjectReference());
        estimation.addActRelationship(relationship);
        item.addActRelationship(relationship);

        List<IMObject> acts = Arrays.asList((IMObject) estimation, item);
        checkSaveCollection(acts, 0);

        // reload the estimation and item. Each will have a separate copy of
        // the same persistent act relationship
        estimation = reload(estimation);
        item = reload(item);
        assertNotNull(estimation);
        assertNotNull(item);

        acts = Arrays.asList((IMObject) estimation, item);

        // save the collection, and verify they have saved by checking the
        // versions.
        checkSaveCollection(acts, 1);

        // now remove the relationship, and add a new one
        estimation.removeActRelationship(relationship);
        item.removeActRelationship(relationship);

        ActRelationship relationship2 = (ActRelationship) service.create(
                "actRelationship.customerEstimationItem");
        relationship2.setSource(estimation.getObjectReference());
        relationship2.setTarget(item.getObjectReference());
        estimation.addActRelationship(relationship2);
        item.addActRelationship(relationship2);

        checkSaveCollection(acts, 2);
    }

    /**
     * Verifies that the {@link IArchetypeService#save(Collection<IMObject>)}
     * method and {@link IArchetypeService#save(IMObject) method can be used
     * to save the same object.
     *
     * @throws Exception for any error
     */
    public void testOBF170() {
        Party person = createPerson("MR", "Jim", "Alateras");
        service.save(person);

        Act act1 = createSimpleAct("act1", "IN_PROGRESS");

        Participation p1 = createSimpleParticipation("act1p1", person, act1);
        act1.addParticipation(p1);

        service.save(act1);
        act1.setStatus("POSTED");
        Collection<IMObject> objects = Arrays.asList((IMObject) act1);
        service.save(objects);

        act1.removeParticipation(p1);
        objects = Arrays.asList((IMObject) act1);
        service.save(objects);

        service.save(act1);
    }

    /**
     * Verifies an act can be removed.
     *
     * @throws Exception for any error
     */
    public void testSingleActRemove() throws Exception {
        Act act = createSimpleAct("act", "IN_PROGRESS");
        service.save(act);
        assertEquals(act, reload(act));

        service.remove(act);
        assertNull(reload(act));
    }

    /**
     * Creates a set of acts with non-parent/child relationships, and verifies
     * that deleting one act doesn't cascade to the rest.
     *
     * @throws Exception for any error
     */
    public void testPeerActRemoval() throws Exception {
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");
        ActBean bean1 = new ActBean(act1);
        ActBean bean2 = new ActBean(act2);

        // create a relationship from act1 -> act2
        ActRelationship r1 = bean1.addRelationship("actRelationship.simple",
                                                   act2);
        r1.setName("act1->act2");
        assertFalse(r1.isParentChildRelationship());

        // create a relationship from act2 -> act3
        ActRelationship r2 = bean2.addRelationship("actRelationship.simple",
                                                   act3);
        r2.setName("act2->act3");
        assertFalse(r2.isParentChildRelationship());

        service.save(act1);
        service.save(act2);
        service.save(act3);

        service.remove(act1);
        assertNull(reload(act1));
        assertNotNull(reload(act2));
        assertNotNull(reload(act3));

        service.remove(act3);
        assertNull(reload(act3));
        assertNotNull(reload(act2));
    }

    /**
     * Creates a parent/child act hierarchy, and verifies that:
     * <ul>
     * <li>deleting the children doesn't affect the remaining children or
     * parent; and</li>
     * <li>deleting the parent causes deletion of the children</li>
     * </ul>
     *
     * @throws Exception
     */
    public void testParentChildRemoval() throws Exception {
        Act estimation = (Act) service.create("act.customerEstimation");
        service.remove(estimation);
        estimation.setStatus("IN_PROGRESS");
        Act item1 = (Act) service.create("act.customerEstimationItem");
        Act item2 = (Act) service.create("act.customerEstimationItem");
        Act item3 = (Act) service.create("act.customerEstimationItem");
        ActBean bean = new ActBean(estimation);
        bean.addRelationship("actRelationship.customerEstimationItem", item1);
        bean.addRelationship("actRelationship.customerEstimationItem", item2);
        bean.addRelationship("actRelationship.customerEstimationItem", item3);
        service.save(item1);
        service.save(item2);
        service.save(item3);
        bean.save();

        // remove an item, and verify it has been removed and that the other
        // acts aren't removed
        service.remove(item1);
        assertNull(reload(item1));
        assertNotNull(reload(estimation));
        assertNotNull(reload(item2));
        assertNotNull(reload(item3));

        // now remove the estimation and verify the remaining items are removed
        service.remove(estimation);
        assertNull(reload(estimation));
        assertNull(reload(item2));
        assertNull(reload(item3));
    }

    /**
     * Verifies that a set of acts in a cyclic parent/child relationship can
     * be removed.
     *
     * @throws Exception for any error
     */
    public void testCyclicParentChildRemoval() throws Exception {
        // create 3 acts, with the following relationships:
        // act1 -> act2 -> act3 -> act1
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        ActBean bean1 = new ActBean(act1);
        ActRelationship r1 = bean1.addRelationship("actRelationship.simple",
                                                   act2);
        r1.setName("act1->act2");
        r1.setParentChildRelationship(true);

        ActBean bean2 = new ActBean(act2);
        ActRelationship r2 = bean2.addRelationship("actRelationship.simple",
                                                   act3);
        r2.setName("act2->act3");
        r2.setParentChildRelationship(true);

        ActBean bean3 = new ActBean(act3);
        ActRelationship r3 = bean3.addRelationship("actRelationship.simple",
                                                   act1);
        r3.setName("act3->act1");
        r3.setParentChildRelationship(true);

        bean1.save();
        bean2.save();
        bean3.save();

        // remove act2. The removal should cascade to include act3 and act1
        service.remove(act2);
        assertNull(reload(act1));
        assertNull(reload(act2));
        assertNull(reload(act3));
    }

    /**
     * Verifies that acts with peer and parent/child relationships are handled
     * correctly at deletion, i.e the deletion cascades to those target
     * acts in parent/child relationships, and not those in peer relationships.
     *
     * @throws Exception for any error
     */
    public void testPeerParentChildRemoval() throws Exception {
        // create 3 acts with the following relationships:
        // act1 -- (parent/child) --> act2
        //   |-------- (peer) ------> act3

        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act2", "IN_PROGRESS");

        ActBean bean1 = new ActBean(act1);
        ActRelationship r1 = bean1.addRelationship("actRelationship.simple",
                                                   act2);
        r1.setName("act1->act2");
        r1.setParentChildRelationship(true);

        ActRelationship r2 = bean1.addRelationship("actRelationship.simple",
                                                   act3);
        r2.setName("act1->act3");
        r2.setParentChildRelationship(false);

        service.save(act1);
        service.save(act2);
        service.save(act3);

        // remove act1, and verify that it and act2 are removed, and act3
        // remains.
        service.remove(act1);

        assertNull(reload(act1));
        assertNull(reload(act2));
        assertNotNull(reload(act3));
    }

    /**
     * Verifies that removal of acts with a parent/child relationship fails
     * when the parent act has changed subsequent to the version being deleted.
     *
     * @throws Exception for any error
     */
    public void testStaleParentChildRemoval() throws Exception {
        Act act1 = createSimpleAct("act1", "IN_PROGRESS");
        Act act2 = createSimpleAct("act2", "IN_PROGRESS");
        Act act3 = createSimpleAct("act3", "IN_PROGRESS");

        ActBean bean1 = new ActBean(act1);
        ActRelationship r1 = bean1.addRelationship("actRelationship.simple",
                                                   act2);
        r1.setName("act1->act2");
        r1.setParentChildRelationship(true);

        service.save(act1);
        service.save(act2);
        service.save(act3);

        Act stale = reload(act1);

        ActRelationship r2 = bean1.addRelationship("actRelationship.simple",
                                                   act3);
        r2.setName("act2->act3");
        r2.setParentChildRelationship(true);
        bean1.save();

        try {
            service.remove(stale);
            fail("Expected removal to fail");
        } catch (ArchetypeServiceException expected) {
            assertEquals(FailedToDeleteObject, expected.getErrorCode());
            IMObjectDAOException cause
                    = (IMObjectDAOException) expected.getCause();

            // verify the cause comes from the DAO collection deletion method
            assertEquals(FailedToDeleteCollectionOfObjects,
                         cause.getErrorCode());
        }
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
     * Saves a collection via the {@link IMObjectDAOHibernate#save(Collection)}
     * method and verifies they have saved with the correct version.
     *
     * @param objects the objects to save
     * @param version the expected version
     */
    private void checkSaveCollection(List<IMObject> objects, long version) {
        service.save(objects);
        for (IMObject object : objects) {
            assertEquals(version, object.getVersion());
            IMObject reloaded = reload(object);
            assertEquals(object, reloaded);
            assertEquals(version, reloaded.getVersion());
        }
    }

    /**
     * Helper to reload an object.
     *
     * @param object the object to reload
     * @return the reloaded object, or <tt>null</tt> if it can't be found
     * @throws ArchetypeServiceException for any error
     */
    @SuppressWarnings("unchecked")
    private <T extends IMObject> T reload(T object) {
        return (T) ArchetypeQueryHelper.getByObjectReference(
                service, object.getObjectReference());
    }

    /**
     * Create a simple act
     *
     * @param name   the name of the act
     * @param status the status of the act
     * @return Act
     */
    private Act createSimpleAct(String name, String status) {
        Act act = (Act) service.create("act.simple");

        act.setName(name);
        act.setStatus(status);
        act.setActivityStartTime(new Date());
        act.setActivityEndTime(
                new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000));

        return act;
    }

    /**
     * Create a simple participation
     *
     * @param name   the name of the participation
     * @param entity the entity in the participation
     * @param act    the act in the participation
     */
    private Participation createSimpleParticipation(String name, Entity entity,
                                                    Act act) {
        Participation participation = (Participation) service.create(
                "participation.simple");
        participation.setName(name);
        participation.setEntity(entity.getObjectReference());
        participation.setAct(act.getObjectReference());

        return participation;
    }

    /**
     * Create a person with the specified title, firstName and LastName
     *
     * @param title
     * @param firstName
     * @param lastName
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

}
