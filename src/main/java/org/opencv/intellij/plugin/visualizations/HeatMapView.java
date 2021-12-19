package org.opencv.intellij.plugin.visualizations;

import com.google.common.collect.ImmutableList;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.sun.jdi.*;
import org.opencv.intellij.plugin.MatDetails;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeatMapView {
    private static Method getStaticMethod(ClassType clsType, String methodName) {
        List<Method> methods = clsType.methodsByName(methodName);
        if (methods.size() != 1) {
            return null;
        }
        return methods.get(0);
    }
    private static Method getConstructorForClass(ClassType clsType, String signature) {
        List<Method> methods = clsType.methodsByName("<init>", signature);
        if (methods.size() != 1) {
            return null;
        }
        return methods.get(0);
    }

    public static BufferedImage getHeatMapView(EvaluationContext evaluationContext, DebugProcessImpl debugProcess, VirtualMachineProxyImpl vm, MatDetails matDetails, ObjectReference matRef) throws EvaluateException {
        try (GCManager gcManager = new GCManager()) {
            int keyHeight = 10;
            ClassType matClassType = (ClassType) matRef.referenceType();
            Method constructor = getConstructorForClass(matClassType, "()V");
            if (constructor == null) {
                return null;
            }
            ObjectReference normalizedMat = debugProcess.newInstance(evaluationContext, matClassType, constructor, Collections.emptyList());
            gcManager.protectObjectReference(normalizedMat);
            ClassType coreRef = (ClassType) debugProcess.findClass(evaluationContext, "org.opencv.core.Core", evaluationContext.getClassLoader());

            Method normalize = DebuggerUtils.findMethod(coreRef, "normalize", "(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;DDII)V");
            if (normalize == null) {
                return null;
            }
            Value alphaValue = DebuggerUtilsEx.createValue(vm, "double", 0.0);
            Value betaValue = DebuggerUtilsEx.createValue(vm, "double", 255.0);
            Value colsValue = DebuggerUtilsEx.createValue(vm, "int", matDetails.getWidth());
            // public static final int NORM_MINMAX = 32
            Value normTypeValue = DebuggerUtilsEx.createValue(vm, "int", 32);
            Value typeValue = DebuggerUtilsEx.createValue(vm, "int", MatDetails.CVTYPE_8U);
            debugProcess.invokeMethod(evaluationContext, coreRef, normalize, ImmutableList.of(matRef, normalizedMat, alphaValue, betaValue, normTypeValue, typeValue));
            Method constructor2 = getConstructorForClass(matClassType, "(III)V");
            if (constructor2 == null) {
                return null;
            }
            Value adjustedRowsValue = DebuggerUtilsEx.createValue(vm, "int", matDetails.getHeight() + keyHeight);
            ObjectReference normalizedKey = debugProcess.newInstance(evaluationContext, matClassType, constructor2, ImmutableList.of(adjustedRowsValue, colsValue, typeValue));
            gcManager.protectObjectReference(normalizedKey);
            Method put = DebuggerUtils.findMethod(matRef.referenceType(), "put", "(II[B)I");
            if (put == null) {
                return null;
            }
            List<ReferenceType> classes = vm.classesByName("byte[]");
            if (classes.size() != 1 || !(classes.get(0) instanceof ArrayType)) {
                return null;
            }
            ArrayType byteArrayType = (ArrayType) classes.get(0);
            ArrayReference byteArray = byteArrayType.newInstance(keyHeight * matDetails.getWidth());
            List<Value> values = new ArrayList<>();
            Value byteValue = null;
            byte bVal = 0;
            for (int x = 0; x < matDetails.getWidth(); x++) {
                if (byteValue == null || bVal != (byte) ((x * 256L) / matDetails.getWidth())) {
                    bVal = (byte) ((x * 256L) / matDetails.getWidth());
                    byteValue = DebuggerUtilsEx.createValue(vm, "byte", bVal);
                }
                values.add(byteValue);
            }
            for (int y = 1; y < 10; y++) {
                for (int x = 0; x < matDetails.getWidth(); x++) {
                    values.add(values.get(x));
                }
            }
            byteArray.setValues(values);
            Value zeroValue = DebuggerUtilsEx.createValue(vm, "int", 0);
            debugProcess.invokeMethod(evaluationContext, normalizedKey, put, ImmutableList.of(zeroValue, zeroValue, byteArray));
            Method submatMethod = DebuggerUtils.findMethod(normalizedKey.referenceType(), "submat", "(IIII)Lorg/opencv/core/Mat;");
            if (submatMethod == null) {
                return null;
            }
            Value rowStartValue = DebuggerUtilsEx.createValue(vm, "int", keyHeight);
            Value rowEndValue = DebuggerUtilsEx.createValue(vm, "int", keyHeight + matDetails.getHeight());
            ObjectReference submat = (ObjectReference) debugProcess.invokeMethod(evaluationContext, normalizedKey, submatMethod, ImmutableList.of(rowStartValue, rowEndValue, zeroValue, colsValue));
            gcManager.protectObjectReference(submat);
            Method copyTo = DebuggerUtils.findMethod(normalizedMat.referenceType(), "copyTo", "(Lorg/opencv/core/Mat;)V");
            if (copyTo == null) {
                return null;
            }
            debugProcess.invokeMethod(evaluationContext, normalizedMat, copyTo, ImmutableList.of(submat));
            ObjectReference colorMapMat = debugProcess.newInstance(evaluationContext, matClassType, constructor, Collections.emptyList());
            gcManager.protectObjectReference(colorMapMat);
            ClassType imgProcRef = (ClassType) debugProcess.findClass(evaluationContext, "org.opencv.imgproc.Imgproc", evaluationContext.getClassLoader());
            Method applyColorMap = DebuggerUtils.findMethod(imgProcRef, "applyColorMap", "(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;I)V");
            if (applyColorMap == null) {
                return null;
            }
            // public static final int COLORMAP_JET = 2
            Value colorMapJet = DebuggerUtilsEx.createValue(vm, "int", 2);
            debugProcess.invokeMethod(evaluationContext, imgProcRef, applyColorMap, ImmutableList.of(normalizedKey, colorMapMat, colorMapJet));
            MatDetails colorMapMatDetails = new MatDetails(colorMapMat, evaluationContext);
            return BitmapView.getBitmapView(evaluationContext, debugProcess, vm, colorMapMatDetails, colorMapMat);
        } catch (Exception e) {
            return null;
        }
    }
}
