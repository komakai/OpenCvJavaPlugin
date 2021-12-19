package org.opencv.intellij.plugin;

import com.google.common.collect.ImmutableList;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.sun.jdi.*;

import java.util.Collections;
import java.util.List;

public class MatUtil {

    public static String getDescription(EvaluationContext evaluationContext, DebugProcessImpl debugProcess, ObjectReference matRef) throws EvaluateException {
        Method toStringMethod = DebuggerUtils.findMethod(matRef.referenceType(), "toString", "()Ljava/lang/String;");
        if (toStringMethod == null) {
            return null;
        }
        StringReference stringRef = (StringReference) debugProcess.invokeMethod(evaluationContext, matRef, toStringMethod, Collections.emptyList());
        return stringRef.toString();
    }

    public static ArrayReference getData(EvaluationContext evaluationContext, DebugProcess debugProcess, VirtualMachineProxyImpl vm, ObjectReference matRef, MatDetails matDetails, String typeMnemonic, String typePrimitive) throws EvaluateException {
        Method getMethod = DebuggerUtils.findMethod(matRef.referenceType(), "get", "(II[" + typeMnemonic + ")I");
        if (getMethod == null) {
            return null;
        }

        List<ReferenceType> classes = vm.classesByName(typePrimitive + "[]");
        if (classes.size() != 1 || !(classes.get(0) instanceof ArrayType)) {
            return null;
        }
        ArrayType arrayType = (ArrayType)classes.get(0);

        ArrayReference arrayRef = arrayType.newInstance(matDetails.getWidth() * matDetails.getHeight() * matDetails.getChannels());

        Value dstRow0 = DebuggerUtilsEx.createValue(vm, "int", 0);
        Value dstCol0 = DebuggerUtilsEx.createValue(vm, "int", 0);

        debugProcess.invokeMethod(evaluationContext, matRef, getMethod, ImmutableList.of(dstRow0, dstCol0, arrayRef));
        return arrayRef;
    }

}
