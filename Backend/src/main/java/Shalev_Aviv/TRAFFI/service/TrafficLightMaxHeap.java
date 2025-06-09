package Shalev_Aviv.TRAFFI.Service;
import Shalev_Aviv.TRAFFI.TrafficLight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TrafficLightMaxHeap {
    private final List<TrafficLight> heap = new ArrayList<>();
    private final Map<TrafficLight, Integer> indexMap = new HashMap<>();
    private final Comparator<TrafficLight> comparator = new TrafficLightComparator();

    public TrafficLightMaxHeap(TrafficLight[] array) {
        for (TrafficLight t : array) {
            heap.add(t);
            indexMap.put(t, heap.size() - 1);
        }
        buildHeap();
    }

    private void buildHeap() {
        for (int i = heap.size() / 2 - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    public TrafficLight peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public TrafficLight extractMax() {
        if (heap.isEmpty()) return null;
        TrafficLight max = heap.get(0);
        swap(0, heap.size() - 1);
        heap.remove(heap.size() - 1);
        indexMap.remove(max);
        heapifyDown(0);
        return max;
    }

    public void updateWeight(TrafficLight t) {
        Integer idx = indexMap.get(t);
        System.out.println("Updating weight for: " + t + " at index: " + idx);
        if (idx == null) {
            System.out.println("TrafficLight not found in heap!");
            return;
        }
        heapifyUp(idx);
        heapifyDown(idx);
        System.out.println("Heap order after update:");
        for (TrafficLight tl : heap) {
            System.out.println(tl + " weights: " + tl.getEmergencyWeight() + ", " + tl.getRegularWeight());
        }
    }

    private void heapifyUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare(heap.get(i), heap.get(parent)) > 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    private void heapifyDown(int i) {
        int size = heap.size();
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, largest = i;
            if (left < size && comparator.compare(heap.get(left), heap.get(largest)) > 0) largest = left;
            if (right < size && comparator.compare(heap.get(right), heap.get(largest)) > 0) largest = right;
            if (largest != i) {
                swap(i, largest);
                i = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        TrafficLight ti = heap.get(i), tj = heap.get(j);
        heap.set(i, tj);
        heap.set(j, ti);
        indexMap.put(ti, j);
        indexMap.put(tj, i);
    }

    public int getIndex(TrafficLight t) {
        return indexMap.getOrDefault(t, -1);
    }
}