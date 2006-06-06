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

// java-core
import java.math.BigDecimal;
import java.util.Date;

//spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//openvpms-framework
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectRefArchetypeConstraint;

// log4j
import org.apache.log4j.Logger;

/**
 * Test that ability to create and query on Documentss.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceFinancialActTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceFinancialActTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceFinancialActTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceFinancialActTestCase() {
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
     * Test the creation of a FinancialAct using the {@link NodeDescriptors}
     */
    public void testFinancialActCreationThruNodeDescriptors()
    throws Exception {
        FinancialAct act = (FinancialAct)service.create("financial.act");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("financial.act");
        NodeDescriptor ndesc = null;
        
        // set the name node
        ndesc = adesc.getNodeDescriptor("name");
        assertTrue(ndesc != null);
        ndesc.setValue(act, "name.doc");
        assertTrue(act.getName().equals("name.doc"));
        
        // set the quantity node
        ndesc = adesc.getNodeDescriptor("quantity");
        assertTrue(ndesc != null);
        ndesc.setValue(act, new BigDecimal(1));
        assertTrue(act.getQuantity().equals(new BigDecimal(1)));
        
        // set the fixed amount node
        ndesc = adesc.getNodeDescriptor("fixedAmount");
        assertTrue(ndesc != null);
        ndesc.setValue(act, new Money(1));
        assertTrue(act.getFixedAmount().equals(new Money(1)));
        
        // set the unit amount node
        ndesc = adesc.getNodeDescriptor("unitAmount");
        assertTrue(ndesc != null);
        ndesc.setValue(act, new Money(2));
        assertTrue(act.getUnitAmount().equals(new Money(2)));
        
        // set the tax amount node
        ndesc = adesc.getNodeDescriptor("taxAmount");
        assertTrue(ndesc != null);
        ndesc.setValue(act, new Money(3));
        assertTrue(act.getTaxAmount().equals(new Money(3)));
        
        // set the total node
        ndesc = adesc.getNodeDescriptor("total");
        assertTrue(ndesc != null);
        ndesc.setValue(act, new Money(4));
        assertTrue(act.getTotal().equals(new Money(4)));
        
        // set the credit node
        ndesc = adesc.getNodeDescriptor("credit");
        assertTrue(ndesc != null);
        ndesc.setValue(act, true);
        assertTrue(act.isCredit() == true);
        
        // set the credit node
        ndesc = adesc.getNodeDescriptor("printed");
        assertTrue(ndesc != null);
        ndesc.setValue(act, false);
        assertTrue(act.isPrinted() == false);

        // set the start time node
        ndesc = adesc.getNodeDescriptor("startTime");
        assertTrue(ndesc != null);
        ndesc.setValue(act, "20/12/1963");
        assertTrue(ndesc.getValue(act) instanceof Date);

        // save the document
        service.save(act);
    }
    
    /**
     * Test the creation of a simple financial act
     */
    public void testSimpleFinancialActCreation()
    throws Exception { 
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1), 
                new Money(2), new Money(3), new Money(4), true, false);
        service.save(act);
    }
    
    /**
     * Test the creation of multiple document and the retrievakl of some 
     * documents
     */
    public void testMultipleFinancialActCreation()
    throws Exception {
        for (int index = 0; index < 10; index++) {
            FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1), 
                    new Money(2), new Money(3), new Money(4), true, false);
            service.save(act);
        }
    }
    
    /** 
     * Test creation and retrieval of a document act
     */
    public void testFinancialActRetrieval()
    throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1), 
                new Money(2), new Money(3), new Money(4), true, false);
        service.save(act);
        
        // retrieve it
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefArchetypeConstraint(
                act.getObjectReference())));
        assertTrue(page.getTotalNumOfRows() == 1);
        
        FinancialAct newAct = (FinancialAct)page.getRows().iterator().next();
        assertTrue(newAct.getObjectReference().equals(act.getObjectReference()));
    }
    
    /** 
     * Test the modification of a document act
     */
    public void testFinancialActModification()
    throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1), 
                new Money(2), new Money(3), new Money(4), true, false);
        service.save(act);
        
        // retrieve it
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefArchetypeConstraint(
                act.getObjectReference())));
        assertTrue(page.getTotalNumOfRows() == 1);
        
        FinancialAct newAct = (FinancialAct)page.getRows().iterator().next();
        newAct.setCredit(false);
        newAct.setQuantity(new BigDecimal(123));
        service.save(act);
    }
    
    /**
     * Test the deletion of a document act
     */
    public void testFinancialActDeletion()
    throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1), 
                new Money(2), new Money(3), new Money(4), true, false);
        service.save(act);
        
        // retrieve it
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefArchetypeConstraint(
                act.getObjectReference())));
        assertTrue(page.getTotalNumOfRows() == 1);
    
        // delete it
        FinancialAct newAct = (FinancialAct)page.getRows().iterator().next();
        service.remove(newAct);
        
        // try and retrieve it again
        page = service.get(new ArchetypeQuery(new ObjectRefArchetypeConstraint(
                act.getObjectReference())));
        assertTrue(page.getTotalNumOfRows() == 0);
    }
    
    /**
     * Test that we can use a date node from an existing node to set the date 
     * node of another node.
     */
    public void testOBF45()
    throws Exception {
        // create an act
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1), 
                new Money(2), new Money(3), new Money(4), true, false);
        act.setActivityStartTime(new Date());
        service.save(act);
        
        // retrieve the act
        IPage<IMObject> page = service.get(new ArchetypeQuery(new ObjectRefArchetypeConstraint(
                act.getObjectReference())));
        assertTrue(page.getTotalNumOfRows() == 1);
        act = (FinancialAct)page.getRows().iterator().next();
        
        // use the descriptors to set the value of endTime
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("financial.act");
        NodeDescriptor etndesc = adesc.getNodeDescriptor("endTime");
        NodeDescriptor stndesc = adesc.getNodeDescriptor("startTime");
        etndesc.setValue(act, stndesc.getValue(act));
        assertTrue(act.getActivityStartTime().equals(act.getActivityEndTime()));
    }
    
    /**
     * Create a financial act given the specified info
     * 
     * @param quantity
     *            the quantity for this act
     * @param fixedAmount
     *            the fixed amount
     * @param unitAmount
     *            the unit amount
     * @param taxAmount
     *            the tax amount
     * @param total
     *            the total amount
     * @param credit
     *            whether it is a credit amount    
     * @param printed
     *            whether it has been printed                                                                      
     * @return FinancialAct            
     */
    public FinancialAct createFinancialAct(BigDecimal quantity, Money fixedAmount, 
            Money unitAmount, Money taxAmount, Money total, boolean credit, boolean printed) {
        FinancialAct act = (FinancialAct)service.create("financial.act");
        act.setName("financialAct1");
        act.setDescription("This is the first financial act");
        act.setQuantity(quantity);
        act.setFixedAmount(fixedAmount);
        act.setUnitAmount(unitAmount);
        act.setTaxAmount(taxAmount);
        act.setTotal(total);
        act.setCredit(credit);
        act.setPrinted(printed);
        
        return act;
    }
}