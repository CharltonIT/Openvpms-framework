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

package org.openvpms.component.system.common.query;

// java-core
import java.util.List;


/**
 * This interface is used to support pagination of large result sets.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public interface IPage<T> {
    /**
     * @return Returns the rows.
     */
    public List<T> getRows();

    /**
     * @return Returns the totalNumOfRows.
     */
    public int getTotalNumOfRows();

    /**
     * Return the first row requested
     * 
     * @return int
     */
    public int getFirstRow();
    
    /**
     * Return the number of rows requested
     * 
     * @return int
     */
    public int getNumOfRows();
}