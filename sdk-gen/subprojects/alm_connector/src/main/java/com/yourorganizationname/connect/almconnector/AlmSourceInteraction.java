/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.almconnector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.arrow.flight.Ticket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;

@SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
public class AlmSourceInteraction extends RowBasedSourceInteraction<AlmConnector>
{
    private final String API = "/primeapi/v2";

    private Iterator<Record> recordIterator;

    protected AlmSourceInteraction(AlmConnector connector, CustomFlightAssetDescriptor asset)
    {
        super();
        setConnector(connector);
        setAsset(asset);
    }

    @Override
    public Record getRecord() {
        // Jeśli iterator jeszcze nie istnieje, to inicjujemy go z pobranych danych
        if (recordIterator == null) {
            try {
                initializeRecordIterator();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize record iterator: " + e.getMessage(), e);
            }
        }

        if (recordIterator.hasNext()) {
            return recordIterator.next();
        }
        return null; // koniec danych
    }

    @Override
    public void close() throws Exception
    {
        super.close();
    }

    @Override
    public List<Ticket> getTickets() throws Exception {
        // Zwracamy ticket z polem "path" (endpoint API) i ewentualnymi interaction_properties
        // Ticket to binarna reprezentacja JSON z tymi danymi
        
        JsonObject ticketJson = new JsonObject();
        ticketJson.addProperty("path", getAsset().getPath());  // np. "/users"
        
        // interactionProperties mogą zawierać np. {id} parametry wymagane w path
        JsonObject interactionProps = new JsonObject();
        if (getAsset().getInteractionProperties() != null) {
            for (Entry<String, Object> entry : getAsset().getInteractionProperties().entrySet()) {
                interactionProps.addProperty(entry.getKey(), entry.getValue().toString());
            }
        }
        ticketJson.add("interaction_properties", interactionProps);

        Ticket ticket = new Ticket(ticketJson.toString().getBytes(StandardCharsets.UTF_8));
        return List.of(ticket);
    }

    @Override
    public List<CustomFlightAssetField> getFields() {
        return List.of(
            new CustomFlightAssetField().name("id").type("string"),
            new CustomFlightAssetField().name("name").type("string"),
            new CustomFlightAssetField().name("email").type("string"),
            new CustomFlightAssetField().name("lastLoginDate").type("string"),
            new CustomFlightAssetField().name("state").type("string"),
            new CustomFlightAssetField().name("roles").type("string")
        );
    }

    private void initializeRecordIterator() throws Exception {
        // Pobieramy ticket (z flight info), parsujemy go i wykonujemy request HTTP
        Ticket ticket = getTickets().get(0);
        String ticketJsonStr = new String(ticket.getBytes(), StandardCharsets.UTF_8);
        JsonObject ticketJson = JsonParser.parseString(ticketJsonStr).getAsJsonObject();

        String endpointPath = ticketJson.get("path").getAsString();
        JsonObject interactionProps = ticketJson.getAsJsonObject("interaction_properties");

        // Zamieniamy placeholdery np. {id} w endpointPath na wartości z interactionProps
        for (Map.Entry<String, com.google.gson.JsonElement> entry : interactionProps.entrySet()) {
            endpointPath = endpointPath.replace("{" + entry.getKey() + "}", entry.getValue().getAsString());
        }
        
        AlmConnector connector = getConnector();
        AlmRequestHandler requestHandler = new AlmRequestHandler(connector.getConnectionProperties());
        JsonObject responseBody = requestHandler.sendAuthorizedGET(
            API + endpointPath,
            connector.getConnectionProperties().getProperty("access_token")
        );

        List<Record> records = new ArrayList<>();
        // Zakładamy, że dane są w polu "data" jako tablica JSON
        for (com.google.gson.JsonElement element : responseBody.getAsJsonArray("data")) {
            JsonObject obj = element.getAsJsonObject();
            Record record = new Record();

            record.appendValue(obj.get("id").getAsString());                   // id
            record.appendValue(obj.get("name").getAsString());                 // name
            record.appendValue(obj.get("email").getAsString());                // email
            record.appendValue(obj.get("lastLoginDate").getAsString());        // lastLoginDate
            record.appendValue(obj.get("state").getAsString());                // state

            if (obj.has("roles") && obj.get("roles").isJsonArray()) {
                JsonArray rolesArray = obj.getAsJsonArray("roles");
                StringBuilder rolesStr = new StringBuilder();
                for (JsonElement role : rolesArray) {
                    if (rolesStr.length() > 0) rolesStr.append(", ");
                    rolesStr.append(role.getAsString());
                }
                record.appendValue(rolesStr.toString());                              // roles
            } else {
                record.appendValue("");                                                // roles = empty string
            }

            records.add(record);
        }

        recordIterator = records.iterator();

        
    }

}
