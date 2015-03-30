package com.dnk.fallingmyo;

import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JeffreyHao-Chan on 2015-03-29.
 */
public class FirebaseCommunicator {

    public static HashMap<String, Object> getData(DataSnapshot snapshot) {
        return (HashMap<String, Object>) snapshot.getValue();
    }

    public static HashMap<String, Object> getDataSubset(HashMap<String, Object> data, String key) {
        return (HashMap<String, Object>) data.get(key);
    }

    public static ArrayList<Object> getObjectList(DataSnapshot snapshot) {
        return new ArrayList<>(getData(snapshot).values());
    }

    public static HashMap<String, Object> getObjectFromList(int index, List<Object> list) {
        return (HashMap<String, Object>) list.get(index);
    }
}
