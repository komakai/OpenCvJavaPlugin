package org.opencv.intellij.plugin;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.sun.jdi.*;
import org.jetbrains.annotations.NotNull;
import org.opencv.intellij.plugin.visualizations.BitmapView;
import org.opencv.intellij.plugin.visualizations.HeatMapView;
import org.opencv.intellij.plugin.visualizations.MatrixView;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Evaluator in the name BitmapEvaluatorProvider implies the use of the debugger evaluation mechanism to query the app for the desired
 * values.
 */
public final class MatEvaluatorProvider {

    @NotNull private final EvaluationContextImpl evaluationContext;

    @NotNull private final ObjectReference matRef;

    public MatEvaluatorProvider(@NotNull Value mat, @NotNull EvaluationContextImpl evaluationContext) {
        this.evaluationContext = evaluationContext;

        String fqcn = mat.type().name();
        if (!MatObjectRenderer.OPENCV_MAT_FQCN.equals(fqcn)) {
            throw new RuntimeException("Invalid parameter passed into method");
        } else {
            matRef = (ObjectReference)mat;
        }
    }

    public String getDescription() {
        return "Mat";
    }

    public BufferedImage getBitmap() throws EvaluateException {
        DebugProcessImpl debugProcess = evaluationContext.getDebugProcess();
        VirtualMachineProxyImpl vm = debugProcess.getVirtualMachineProxy();

        MatDetails matDetails = new MatDetails(matRef, evaluationContext);

        int dims = matDetails.getDims();
        int width = matDetails.getWidth();
        int height = matDetails.getHeight();
        int depth = matDetails.getDepth();
        int channels = matDetails.getChannels();

        if (dims == 2 && matDetails.getWidth() != -1 && matDetails.getHeight() != -1) {
            if (channels == 1 && width <= 10 && height <= 10) {
                return getMatrixView(evaluationContext, debugProcess, vm, matRef, matDetails);
            } else if (depth == MatDetails.CVTYPE_8U && (channels == 1 || channels == 3 || channels == 4)) {
                return BitmapView.getBitmapView(evaluationContext, debugProcess, vm, matDetails, matRef);
            } else if (channels == 1) {
                return HeatMapView.getHeatMapView(evaluationContext, debugProcess, vm, matDetails, matRef);
            }
        }
        throw new NoAvailableMatVisualization(MatUtil.getDescription(evaluationContext, debugProcess, matRef));
    }

    private BufferedImage getMatrixView(EvaluationContextImpl evaluationContext, DebugProcessImpl debugProcess, VirtualMachineProxyImpl vm, ObjectReference matRef, MatDetails matDetails) throws EvaluateException {
        ArrayReference arrayReference = null;
        switch (matDetails.getDepth()) {
            case MatDetails.CVTYPE_8U:
            case MatDetails.CVTYPE_8S:
                arrayReference = MatUtil.getData(evaluationContext, debugProcess, vm, matRef, matDetails, "B", "byte");
                break;
            case MatDetails.CVTYPE_16U:
            case MatDetails.CVTYPE_16S:
                arrayReference = MatUtil.getData(evaluationContext, debugProcess, vm, matRef, matDetails, "S", "short");
                break;
            case MatDetails.CVTYPE_32S:
                arrayReference = MatUtil.getData(evaluationContext, debugProcess, vm, matRef, matDetails, "I", "int");
                break;
            case MatDetails.CVTYPE_32F:
                arrayReference = MatUtil.getData(evaluationContext, debugProcess, vm, matRef, matDetails, "F", "float");
                break;
            case MatDetails.CVTYPE_64F:
                arrayReference =MatUtil. getData(evaluationContext, debugProcess, vm, matRef, matDetails, "D", "double");
                break;
        }
        if (arrayReference == null) {
            return null;
        }
        List<Value> values = arrayReference.getValues();
        double[] elements = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Value elemValue = values.get(i);
            if (elemValue instanceof PrimitiveValue) {
                elements[i] = ((PrimitiveValue)elemValue).doubleValue();
            }
        }
        return MatrixView.drawView(elements, matDetails);
    }

    public static class NoAvailableMatVisualization extends RuntimeException {
        public NoAvailableMatVisualization(String message) {
            super(message);
        }
    }
}
