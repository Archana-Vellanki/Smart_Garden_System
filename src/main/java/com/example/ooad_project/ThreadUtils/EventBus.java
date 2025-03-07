package com.example.ooad_project.ThreadUtils;

import java.util.*;
import java.util.function.Consumer;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private static final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    public static void subscribe(String eventType, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void publish(String eventType, Object event) {
        List<Consumer<Object>> eventListeners = listeners.getOrDefault(eventType, Collections.emptyList());
        eventListeners.forEach(listener -> listener.accept(event));
    }
}

