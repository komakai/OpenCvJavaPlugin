package org.opencv.intellij.plugin;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MatDetails {

    public static final int CVTYPE_8U = 0;
    public static final int CVTYPE_8S = 1;
    public static final int CVTYPE_16U = 2;
    public static final int CVTYPE_16S = 3;
    public static final int CVTYPE_32S = 4;
    public static final int CVTYPE_32F = 5;
    public static final int CVTYPE_64F = 6;

    private final int dims;
    private final int depth;
    private final int channels;
    private final int width;
    private final int height;

    public MatDetails(@NotNull ObjectReference value, @NotNull EvaluationContext evaluationContext) throws EvaluateException {
        dims = callIntMethod(value, evaluationContext, "dims");
        depth = callIntMethod(value, evaluationContext, "depth");
        channels = callIntMethod(value, evaluationContext, "channels");
        if (dims == 2) {
            width = callIntMethod(value, evaluationContext, "width");
            height = callIntMethod(value, evaluationContext, "height");
        } else {
            width = -1;
            height = -1;
        }
    }

    @NotNull
    private Integer callIntMethod(@NotNull ObjectReference value, @NotNull EvaluationContext evaluationContext, @NotNull String methodName) throws EvaluateException {
        Method method = DebuggerUtils.findMethod(value.referenceType(), methodName, "()I");
        if (method != null) {
            Value outValue = evaluationContext.getDebugProcess().invokeMethod(evaluationContext, (ObjectReference)value, method, Collections.emptyList());
            if (outValue instanceof IntegerValue) {
                return ((IntegerValue)outValue).value();
            }
        }
        throw new EvaluateException("Method" + methodName + "not found");
    }

    public int getDims() {
        return dims;
    }

    public int getDepth() {
        return depth;
    }

    public int getChannels() {
        return channels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getType() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CV_");
        switch (depth) {
            case CVTYPE_8U:
                stringBuffer.append("8S");
                break;
            case CVTYPE_8S:
                stringBuffer.append("8U");
                break;
            case CVTYPE_16U:
                stringBuffer.append("16U");
                break;
            case CVTYPE_16S:
                stringBuffer.append("16S");
                break;
            case CVTYPE_32S:
                stringBuffer.append("32S");
                break;
            case CVTYPE_32F:
                stringBuffer.append("32F");
                break;
            case CVTYPE_64F:
                stringBuffer.append("64F");
                break;
            default:
                break;
        }
        if (channels > 1) {
            stringBuffer.append('C');
            stringBuffer.append(channels);
        }
        return stringBuffer.toString();
    }
}
