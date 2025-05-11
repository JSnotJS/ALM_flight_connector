/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.almconnector;

import java.util.Arrays;

import com.ibm.connect.sdk.api.ConnectorFactory;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightDatasourceTypes;

@SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
public class AlmConnectorFactory implements ConnectorFactory
{
    private static final AlmConnectorFactory INSTANCE = new AlmConnectorFactory();

    /**
     * A connector factory instance.
     *
     * @return a connector factory instance
     */
    public static AlmConnectorFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates a connector for the given data source type.
     *
     * @param datasourceTypeName
     *            the name of the data source type
     * @param properties
     *            connection properties
     * @return a connector for the given data source type
     */
    @Override
    public AlmConnector createConnector(String datasourceTypeName, ConnectionProperties properties)
    {
        if ("alm_connector".equals(datasourceTypeName)) {
            return new AlmConnector(properties);
        }
        throw new UnsupportedOperationException(datasourceTypeName + " is not supported!");
    }

    @Override
    public CustomFlightDatasourceTypes getDatasourceTypes()
    {
        return new CustomFlightDatasourceTypes().datasourceTypes(Arrays.asList(new AlmDatasourceType()));
    }
}
