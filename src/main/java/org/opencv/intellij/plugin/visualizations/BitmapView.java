package org.opencv.intellij.plugin.visualizations;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ByteValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.opencv.intellij.plugin.MatDetails;
import org.opencv.intellij.plugin.MatUtil;

import java.awt.image.BufferedImage;
import java.util.List;

public class BitmapView {

    public static BufferedImage getBitmapView(EvaluationContext evaluationContext, DebugProcessImpl debugProcess, VirtualMachineProxyImpl vm, MatDetails matDetails, ObjectReference matRef) throws EvaluateException {
        BitmapDecoder.BitmapExtractor bitmapExtractor = null;
        if (matDetails.getDepth() == MatDetails.CVTYPE_8U) {
            if (matDetails.getChannels() == 1) {
                bitmapExtractor = new BitmapDecoder.GRAYSCALE_BitmapExtractor();
            } else if (matDetails.getChannels() == 3) {
                bitmapExtractor = new BitmapDecoder.RGB_BitmapExtractor();
            } else if (matDetails.getChannels() == 4) {
                bitmapExtractor = new BitmapDecoder.ARGB_BitmapExtractor();
            } else {
                return null;
            }
        } else {
            return null;
        }

        ArrayReference byteArray = MatUtil.getData(evaluationContext, debugProcess, vm, matRef, matDetails, "B", "byte");
        List<Value> pixelValues = byteArray.getValues();
        byte[] argb = new byte[pixelValues.size()];
        for (int i = 0; i < pixelValues.size(); i++) {
            Value pixelValue = pixelValues.get(i);
            if (pixelValue instanceof ByteValue) {
                argb[i] = ((ByteValue)pixelValue).byteValue();
            }
        }
        return bitmapExtractor.getImage(matDetails.getWidth(), matDetails.getHeight(), argb);
    }
}
