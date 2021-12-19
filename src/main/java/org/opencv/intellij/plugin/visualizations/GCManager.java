package org.opencv.intellij.plugin.visualizations;

import com.sun.jdi.ObjectReference;

import java.util.ArrayList;
import java.util.List;

public class GCManager implements AutoCloseable {

    private final List<ObjectReference> protectedObjects = new ArrayList<>();

    public void protectObjectReference(ObjectReference objectReference) {
        objectReference.disableCollection();
        protectedObjects.add(objectReference);
    }

    @Override
    public void close() throws Exception {
        for (ObjectReference objectReference: protectedObjects) {
            objectReference.enableCollection();
        }
    }
}
