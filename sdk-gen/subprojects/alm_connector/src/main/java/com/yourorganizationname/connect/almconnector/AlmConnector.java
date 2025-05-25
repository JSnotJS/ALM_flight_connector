/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.almconnector;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;


import org.apache.arrow.flight.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.SQLException;

import com.ibm.connect.sdk.api.RowBasedConnector;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionConfiguration;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionResponse;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetsCriteria;
import com.ibm.wdp.connect.common.sdk.api.models.DiscoveredAssetInteractionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.DiscoveredAssetType;

@SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
public class AlmConnector extends RowBasedConnector<AlmSourceInteraction, AlmTargetInteraction>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlmConnector.class);

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
    public void connect() throws IOException, InterruptedException {
        // PIPELINE AUTORYZUJĄCY CONNECTOR, KROKI OPISANE NA:
        // https://experienceleague.adobe.com/en/docs/learning-manager/using/integration/developer-manual
        final String TOKEN_DATA_PATH = "/oauth/token/check";
        final String code = getConnectionProperties().getProperty("code");
        String accessToken = getConnectionProperties().getProperty("access_token");
        String refreshToken = getConnectionProperties().getProperty("refresh_token");

        AlmRequestHandler requestHandler = new AlmRequestHandler(getConnectionProperties());

        try {
            if (accessToken != null) {
                JsonObject jsonObj = requestHandler.sendAuthorizedGET(TOKEN_DATA_PATH, accessToken);
                if (jsonObj.get("expires_in").getAsInt() > 0) {
                    accessToken = jsonObj.get("access_token").getAsString();
                    getConnectionProperties().setProperty("access_token", accessToken);
                    // valid token => nie odpalamy pipelina z generowaniem teokenu
                    return; 
                }
            }
            // REFRESH_TOKEN
            if (refreshToken == null) {
                JsonObject jsonObject = requestHandler.fetchRefreshToken(code);
                refreshToken = jsonObject.get("refresh_token").getAsString();
                getConnectionProperties().setProperty("refresh_token", refreshToken);
            }
            // ACCESS_TOKEN
            JsonObject jsonObject2 = requestHandler.fetchAccessToken(refreshToken);
            accessToken = jsonObject2.get("access_token").getAsString();
            getConnectionProperties().setProperty("access_token", accessToken); 

        } catch (Exception e) {
            throw new RuntimeException("Error during Adobe auth: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CustomFlightAssetDescriptor> discoverAssets(CustomFlightAssetsCriteria criteria) throws Exception
    {
        List<CustomFlightAssetDescriptor> assets = new ArrayList<>();

        // USERS
        assets.add(createAsset("user.current", "/user", "Current user")); // do pozyskania userid
        assets.add(createAsset("users", "/users", "All users"));
        // assets.add(createAsset("users.by_id", "/users/{id}", "User by ID", Map.of("id", "<required>")));

        // ENROLLMENTS
        assets.add(createAsset("user.enrollments", "/users/{id}/enrollments", "All enrollments for user", Map.of("id", "<required>")));
        // assets.add(createAsset("user.enrollment_by_id", "/users/{id}/enrollments/{enrollmentId}", "Enrollment by ID", Map.of("id", "<required>", "enrollmentId", "<required>")));

        // LEARNING OBJECTS
        assets.add(createAsset("learning.objects", "/learningObjects", "All learning objects"));
        // assets.add(createAsset("learning.object_by_id", "/learningObjects/{id}", "Learning object by ID", Map.of("id", "<required>")));

        // SKILLS
        assets.add(createAsset("skills", "/skills", "All skills"));
        // assets.add(createAsset("skills.by_id", "/skills/{id}", "Skill by ID", Map.of("id", "<required>")));

        // USER SKILLS
        assets.add(createAsset("user.skills", "/users/{userId}/userSkills", "All user skills", Map.of("userId", "<required>")));
        // assets.add(createAsset("user.skill_by_id", "/users/{userId}/userSkills/{id}", "User skill by ID", Map.of("userId", "<required>", "id", "<required>")));

        return assets;

        // final Properties filters = ModelMapper.toProperties(criteria.getFilters());
        // final String schemaNamePattern = filters.getProperty("schema_name_pattern");
        // final String path = normalizePath(criteria.getPath());
        // final String[] pathElements = splitPath(path);
        // final List<CustomFlightAssetDescriptor> assets;

        // if (pathElements.length == 0) {
        //     assets = schemaNamePattern == null ? listSchemas(criteria) : listTables(criteria, null);
        // } else if (pathElements.length == 1) {
        //     assets = listTables(criteria, pathElements[0]);
        // } else if (pathElements.length == 2) {
        //     // GET /user(s)  ::users   <- do ustalenia, który lepszy (z czy bez s)
        //     // GET /skills   ::skills
        //     // GET /enrollments  ::learning object
        //     // GET /learningObjects   ::
        //     // GET /users/{userId}/userSkills
        //     // endpointy wybrane na podstawie grafikki z discorda
        //     //  ustalić czy poza przeklejeniem patha z ""criteria", trzzeba coś jeszcze tam wrzucać przy przetworzeniu (chuj wie jak to działa, tak se napisałem)
        //     final String schemaName = pathElements[0];
        //     final String tableName = pathElements[1];
        //     final String includePrimaryKey = filters.getProperty("primary_key", "false");
        //     if (Boolean.valueOf(includePrimaryKey)) {
        //         assets = listPrimaryKeys(schemaName, tableName);
        //     } else {
        //         final DiscoveredAssetInteractionProperties interactionProperties = new DiscoveredAssetInteractionProperties();
        //         interactionProperties.put("schema_name", schemaName);
        //         interactionProperties.put("table_name", tableName);
        //         final CustomFlightAssetDescriptor asset = new CustomFlightAssetDescriptor().name(tableName).path(path)
        //                 .assetType(tableAssetType()).interactionProperties(interactionProperties);
        //         if (Boolean.TRUE.equals(criteria.isExtendedMetadata())) {
        //             asset.setExtendedMetadata(listExtendedMetadata(schemaName, tableName));
        //         }
        //         assets = new ArrayList<>();
        //         assets.add(asset);
        //     }
        // } else {
        //     throw new IllegalArgumentException("Invalid path");
        // }
        // return assets;
    }

    private CustomFlightAssetDescriptor createAsset(String id, String path, String description) {
    return createAsset(id, path, description, null);
}

    private CustomFlightAssetDescriptor createAsset(String id, String path, String description, Map<String, String> interactionProperties) {
        CustomFlightAssetDescriptor asset = new CustomFlightAssetDescriptor();
        asset.setId(id);
        asset.setName(id);
        asset.setPath(path);
        asset.setDescription(description);

        DiscoveredAssetType type = new DiscoveredAssetType()
            .type("table")
            .dataset(true)
            .datasetContainer(false);

        asset.setAssetType(type);

        if (interactionProperties != null && !interactionProperties.isEmpty()) {
            DiscoveredAssetInteractionProperties props = new DiscoveredAssetInteractionProperties();
            interactionProperties.forEach(props::put);
            asset.setInteractionProperties(props);
        }

        return asset;
    }

    // TODO: dorobić
    private List<CustomFlightAssetDescriptor> listSchemas(CustomFlightAssetsCriteria criteria) throws SQLException {
        final List<CustomFlightAssetDescriptor> descriptors = new ArrayList<>();
        throw new IllegalArgumentException("listSchemas not yet implemented");
        // return descriptors;
    }

    // TODO: dorobić
    private List<CustomFlightAssetDescriptor> listTables(CustomFlightAssetsCriteria criteria, String schemaName) throws SQLException {
        final List<CustomFlightAssetDescriptor> descriptors = new ArrayList<>();
        throw new IllegalArgumentException("listTables not yet implemented");
        // return descriptors;
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
