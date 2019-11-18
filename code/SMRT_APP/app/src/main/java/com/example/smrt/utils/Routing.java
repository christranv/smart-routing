package com.example.smrt.utils;

import org.json.JSONArray;

public interface Routing {
    void drawRoute(JSONArray pArr) throws Exception;
    void addRoute(JSONArray pArr, boolean isSub) throws Exception;
}
