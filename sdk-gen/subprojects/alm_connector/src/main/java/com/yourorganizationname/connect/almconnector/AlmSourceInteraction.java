/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.almconnector;

import java.lang.invoke.LambdaConversionException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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

    List<CustomFlightAssetDescriptor> descriptors = List.of();

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
        // // Zwracamy ticket z polem "path" (endpoint API) i ewentualnymi interaction_properties
        // // Ticket to binarna reprezentacja JSON z tymi danymi
        
        // JsonObject ticketJson = new JsonObject();
        // ticketJson.addProperty("path", getAsset().getPath());  // np. "/users"
        // ticketJson.addProperty("id", getAsset().getId());  // np. "/users"
        
        // // interactionProperties mogą zawierać np. {id} parametry wymagane w path
        // JsonObject interactionProps = new JsonObject();
        // if (getAsset().getInteractionProperties() != null) {
        //     for (Entry<String, Object> entry : getAsset().getInteractionProperties().entrySet()) {
        //         interactionProps.addProperty(entry.getKey(), entry.getValue().toString());
        //     }
        // }
        // ticketJson.add("interaction_properties", interactionProps);

        // Ticket ticket = new Ticket(ticketJson.toString().getBytes(StandardCharsets.UTF_8));
        // return List.of(ticket);

        Ticket ticket = new Ticket(String.format("{\"request_id\": \"%s\"}", UUID.randomUUID()).getBytes());
        return List.of(ticket);
    }


    @Override
    public List<CustomFlightAssetField> getFields() {
        String name = getAsset().getName();
        return AlmSchemaProvider.getFieldsFor(name); 
    }
    

    private void initializeRecordIterator() throws Exception {
        // Pobieramy ticket (z flight info), parsujemy go i wykonujemy request HTTP
        // Ticket ticket = getTickets().get(0);
        // String ticketJsonStr = new String(ticket.getBytes(), StandardCharsets.UTF_8);
        // JsonObject ticketJson = JsonParser.parseString(ticketJsonStr).getAsJsonObject();

        // String endpointPath = ticketJson.get("path").getAsString();
        // String resourceName = ticketJson.get("id").getAsString();
        // JsonObject interactionProps = ticketJson.getAsJsonObject("interaction_properties");
        
        // Zamieniamy placeholdery np. {id} w endpointPath na wartości z interactionProps
        // for (Map.Entry<String, com.google.gson.JsonElement> entry : interactionProps.entrySet()) {
        //     endpointPath = endpointPath.replace("{" + entry.getKey() + "}", entry.getValue().getAsString());
        // }

        String endpointPath = substituteUriParams(getAsset().getPath());
        String resourceName = getAsset().getId();
        
        AlmConnector connector = getConnector();
        AlmRequestHandler requestHandler = new AlmRequestHandler(connector.getConnectionProperties());
        JsonObject responseBody = requestHandler.sendAuthorizedGET(
            API + endpointPath,
            connector.getConnectionProperties().getProperty("access_token")
        );

        List<Record> records = new ArrayList<>();
        if (responseBody.get("data") == null) {
            records.add(AlmRecordMapper.mapRecord(resourceName, responseBody));
        } else {
            for (com.google.gson.JsonElement element : responseBody.getAsJsonArray("data")) {
                JsonObject obj = element.getAsJsonObject();
                records.add(AlmRecordMapper.mapRecord(resourceName, obj));
            }
        }
        recordIterator = records.iterator();

        
    }

    private String substituteUriParams(String endpointPath) {
        if (getAsset().getInteractionProperties() != null) {
            for (Entry<String, Object> entry : getAsset().getInteractionProperties().entrySet()) {
                endpointPath = endpointPath.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }
        return endpointPath;
    }

}
