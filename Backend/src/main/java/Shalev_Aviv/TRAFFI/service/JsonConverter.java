package Shalev_Aviv.TRAFFI.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonConverter {

    public static int[][] convertJsonToMatrix(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode graphNode = rootNode.get("Lights To Lights");
        
        return objectMapper.readValue(graphNode.toString(), int[][].class);
    }
    
    public static Map<Integer, int[]> convertLightsToLanesMap(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode lightsNode = rootNode.get("Lights To Lanes");
        
        Map<Integer, int[]> lightsToLanesMap = new HashMap<>();
        
        // Convert from "Light X" format to integer keys
        lightsNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            int lightNum = Integer.parseInt(key.split(" ")[1]);
            
            // Convert array node to int[]
            JsonNode arrayNode = entry.getValue();
            int[] lanes = new int[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                lanes[i] = arrayNode.get(i).asInt();
            }
            
            lightsToLanesMap.put(lightNum, lanes);
        });
        
        return lightsToLanesMap;
    }
    
    public static Map<Integer, int[]> convertlanesToLanesMap(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode dictNode = rootNode.get("Lanes To Lanes");
        
        Map<Integer, int[]> lanesToDestinationsMap = new HashMap<>();
        
        // Process each lane entry
        dictNode.fields().forEachRemaining(entry -> {
            int laneNum = Integer.parseInt(entry.getKey());
            JsonNode valueNode = entry.getValue();
            
            if (valueNode.isNull()) {
                lanesToDestinationsMap.put(laneNum, null);
            } else {
                // Convert array node to int[]
                int[] destinations = new int[valueNode.size()];
                for (int i = 0; i < valueNode.size(); i++) {
                    destinations[i] = valueNode.get(i).asInt();
                }
                lanesToDestinationsMap.put(laneNum, destinations);
            }
        });
        
        return lanesToDestinationsMap;
    }
}