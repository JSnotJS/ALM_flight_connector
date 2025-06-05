package com.yourorganizationname.connect.almconnector;

import com.google.gson.*;
import com.ibm.connect.sdk.api.Record;
import java.util.*;

public class AlmRecordMapper {

    private static final String ARRAY_STRING_SEP = " | ";

    public static Record mapRecord(String assetName, JsonObject jsonDataRow) {
        switch (assetName) {
            case "user":
            case "users":
                return mapUser(jsonDataRow);
            case "enrollments":
                return mapEnrollment(jsonDataRow);
            case "learning_objects":
                return mapLearningObject(jsonDataRow);
            case "skills":
                return mapSkill(jsonDataRow);
            case "user_skills":
                return mapUserSkill(jsonDataRow);
            default:
                throw new IllegalArgumentException("Unsupported asset path: " + assetName);
        }
    }


    // USERS
    private static Record mapUser(JsonObject jsonDataRow) {
        Record record = new Record();
        record.appendValue(getString(jsonDataRow, "id"));
        JsonObject attributesObj = jsonDataRow.getAsJsonObject("attributes");
        record.appendValue(getString(attributesObj, "name"));
        record.appendValue(getString(attributesObj, "email"));
        record.appendValue(getString(attributesObj, "lastLoginDate")); // timestamp
        record.appendValue(getString(attributesObj, "state"));
        // roles: array → string
        if (attributesObj.has("roles") && attributesObj.get("roles").isJsonArray()) {
            JsonArray roles = attributesObj.getAsJsonArray("roles");
            List<String> roleList = new ArrayList<>();
            for (JsonElement role : roles) {
                roleList.add(role.getAsString());
            }
            record.appendValue(String.join(ARRAY_STRING_SEP, roleList));
        } else {
            record.appendValue("");
        }
        return record;
    }
    

    // ENROLLMENTS
    private static Record mapEnrollment(JsonObject jsonDataRow) {
        //TODO
        Record record = new Record();
        record.appendValue(getString(jsonDataRow, "id"));
        return record;
    }
    

    // LEARNING OBJECTS
    private static Record mapLearningObject(JsonObject jsonDataRow) {
                //TODO

        Record record = new Record();
        record.appendValue(getString(jsonDataRow, "id"));
        record.appendValue(getString(jsonDataRow, "name"));
        record.appendValue(getString(jsonDataRow, "objectType"));
        record.appendValue(getString(jsonDataRow, "description"));
        return record;
    }
    

    // SKILLS
        private static Record mapSkill(JsonObject jsonDataRow) {
                    //TODO

        Record record = new Record();
        record.appendValue(getString(jsonDataRow, "id"));
        record.appendValue(getString(jsonDataRow, "name"));
        record.appendValue(getString(jsonDataRow, "description"));
        record.appendValue(getString(jsonDataRow, "state"));
        return record;
    }


    // USER SKILLS
    private static Record mapUserSkill(JsonObject jsonDataRow) {
                //TODO

        Record record = new Record();
        record.appendValue(getString(jsonDataRow, "id"));
        return record;
    }


    private static String getString(JsonObject obj, String key) {
                //TODO

    return obj.has(key) && !obj.get(key).isJsonNull()
        ? obj.get(key).getAsString()
        : "";
    }
}

