package Shalev_Aviv.TRAFFI.Service;

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
    
    public static Map<Integer, Integer[]> convertLightsToLanesMap(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode lightsNode = rootNode.get("Lights To Lanes");
        
        Map<Integer, Integer[]> lightsToLanesMap = new HashMap<>();
        
        // Convert from "Light X" format to integer keys
        lightsNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            Integer lightNum = Integer.parseInt(key);
            
            // Convert array node to int[]
            JsonNode arrayNode = entry.getValue();
            Integer[] lanes = new Integer[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                lanes[i] = arrayNode.get(i).asInt();
            }
            
            lightsToLanesMap.put(lightNum, lanes);
        });
        
        return lightsToLanesMap;
    }
    
    public static Map<Integer, Integer[]> convertlanesToLanesMap(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode dictNode = rootNode.get("Lanes To Lanes");
        
        Map<Integer, Integer[]> lanesToDestinationsMap = new HashMap<>();
        
        // Process each lane entry
        dictNode.fields().forEachRemaining(entry -> {
            Integer laneNum = Integer.parseInt(entry.getKey());
            JsonNode valueNode = entry.getValue();
            
            if (valueNode.isNull()) {
                lanesToDestinationsMap.put(laneNum, null);
            } else {
                // Convert array node to int[]
                Integer[] destinations = new Integer[valueNode.size()];
                for (Integer i = 0; i < valueNode.size(); i++) {
                    destinations[i] = valueNode.get(i).asInt();
                }
                lanesToDestinationsMap.put(laneNum, destinations);
            }
        });
        
        return lanesToDestinationsMap;
    }
}