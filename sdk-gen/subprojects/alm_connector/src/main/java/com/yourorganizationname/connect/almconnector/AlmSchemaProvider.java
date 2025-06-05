package com.yourorganizationname.connect.almconnector;

import java.util.List;

import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;

public class AlmSchemaProvider {

    public static List<CustomFlightAssetField> getFieldsFor(String assetName) {
        switch (assetName) {
            case "user":
            case "users":
                return getUsersFields();                
            case "enrollments":
                return getEnrollmentsFields();
            case "learning_objects":
                return getLearningObjectsFields();
            case "skills":
                return getSkillFields();
            case "user_skills":
                return getUserSkillsFields();
            default:
                throw new IllegalArgumentException("Unknown asset path: " + assetName);
        }
    }

    // USERS
    private static List<CustomFlightAssetField> getUsersFields() {
        return List.of(
            new CustomFlightAssetField().name("id").type("string").nullable(false),
            new CustomFlightAssetField().name("name").type("string").nullable(true),
            new CustomFlightAssetField().name("email").type("string").nullable(true),
            new CustomFlightAssetField().name("lastLoginDate").type("string").nullable(true),
            // new CustomFlightAssetField().name("lastLoginDate").type("timestamp").nullable(true),
            new CustomFlightAssetField().name("state").type("string").nullable(true),
            new CustomFlightAssetField().name("roles").type("string").nullable(true)
        );
    }

    // ENROLLMENTS
    private static List<CustomFlightAssetField> getEnrollmentsFields() {
                //TODO

        return null;
    }

    // LEARNING OBJECTS
    private static List<CustomFlightAssetField> getLearningObjectsFields() {
                //TODO

        return null;
    }

    // SKILLS
        private static List<CustomFlightAssetField> getSkillFields() {
                    //TODO

        return List.of(
            new CustomFlightAssetField().name("id").type("string").nullable(false),
            new CustomFlightAssetField().name("name").type("string").nullable(true)
        );
    }

    // USER SKILLS
    private static List<CustomFlightAssetField> getUserSkillsFields() {
                //TODO

        return null;
    }
    
}