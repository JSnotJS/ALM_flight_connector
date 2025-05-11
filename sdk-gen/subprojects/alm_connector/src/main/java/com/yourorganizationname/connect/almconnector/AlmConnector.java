/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.almconnector;

import java.util.List;

import org.apache.arrow.flight.Ticket;

import com.ibm.connect.sdk.api.RowBasedConnector;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionConfiguration;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionResponse;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetsCriteria;

@SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
public class AlmConnector
        extends RowBasedConnector<AlmSourceInteraction, AlmTargetInteraction>
{
    /**
     * Creates a row-based connector.
     *
     * @param properties
     *            connection properties
     */
    public AlmConnector(ConnectionProperties properties)
    {
        super(properties);
    }

    @Override
    public void close() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public List<CustomFlightAssetDescriptor> discoverAssets(CustomFlightAssetsCriteria criteria) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AlmSourceInteraction getSourceInteraction(CustomFlightAssetDescriptor asset, Ticket ticket)
    {
        // TODO include your ticket info
        return new AlmSourceInteraction(this, asset);
    }

    @Override
    public AlmTargetInteraction getTargetInteraction(CustomFlightAssetDescriptor asset)
    {
        return new AlmTargetInteraction(this, asset);
    }

    @Override
    public ConnectionActionResponse performAction(String action, ConnectionActionConfiguration conf)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void commit()
    {
        // TODO Auto-generated method stub

    }

}
