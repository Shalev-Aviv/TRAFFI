package Shalev_Aviv.TRAFFI.Service;
import Shalev_Aviv.TRAFFI.TrafficLight;

import java.util.Comparator;

public class TrafficLightComparator implements Comparator<TrafficLight> {
    @Override
    public int compare(TrafficLight t1, TrafficLight t2) {
        int cmp = Integer.compare(t2.getEmergencyWeight(), t1.getEmergencyWeight());
        if (cmp == 0) {
            cmp = Integer.compare(t2.getRegularWeight(), t1.getRegularWeight());
        }
        return cmp;
    }
}