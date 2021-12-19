package org.opencv.intellij.plugin;

import com.intellij.CommonBundle;
import com.intellij.debugger.engine.FullValueEvaluatorProvider;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.debugger.ui.tree.ValueDescriptor;
import com.intellij.debugger.ui.tree.render.CompoundReferenceRenderer;
import com.intellij.debugger.ui.tree.render.CustomPopupFullValueEvaluator;
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import icons.MatIcons;
import org.intellij.images.editor.impl.ImageEditorManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

final class MatObjectRenderer extends CompoundReferenceRenderer implements FullValueEvaluatorProvider {
    private static final Logger LOG = Logger.getInstance(MatObjectRenderer.class);

    public static final String OPENCV_MAT_FQCN = "org.opencv.core.Mat";

    public MatObjectRenderer() {
        super("OpenCV Mat", null, null);
        setClassName(OPENCV_MAT_FQCN);
        setEnabled(true);
    }

    @Override
    public String calcLabel(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener listener) throws EvaluateException {
        try {
            MatDetails matDetails = new MatDetails((ObjectReference) descriptor.getValue(), evaluationContext);
            return "[" + matDetails.getWidth() + "*" + matDetails.getHeight() + ", " + matDetails.getType() + "]";
        } catch (EvaluateException e) {
            return super.calcLabel(descriptor, evaluationContext, listener);
        }
    }

    @Override
    public @Nullable Icon calcValueIcon(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener listener) {
        return MatIcons.OpenCv;
    }

    @Override
    public @Nullable XFullValueEvaluator getFullValueEvaluator(EvaluationContextImpl evaluationContext, ValueDescriptorImpl valueDescriptor) {
        return new IconPopupEvaluator(MatBundle.message("message.node.show.mat"), evaluationContext) {
            @Override
            protected Icon getData() {
                return getIcon(myEvaluationContext, valueDescriptor.getValue());
            }
        };
    }

    static JComponent createIconViewer(@Nullable Icon icon) {
        if (icon == null) return new JLabel(CommonBundle.message("label.no.data"), SwingConstants.CENTER);
        final int w = icon.getIconWidth();
        final int h = icon.getIconHeight();
        final BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        final Graphics2D g = image.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        return ImageEditorManagerImpl.createImageEditorUI(image);
    }

    @Nullable
    static ImageIcon getIcon(EvaluationContextImpl evaluationContext, Value value) {
        try {
            MatEvaluatorProvider evaluatorProvider = new MatEvaluatorProvider(value, evaluationContext);
            BufferedImage image = evaluatorProvider.getBitmap();
            return new ImageIcon(image);
        } catch (EvaluateException e) {
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "Error while evaluating expression: " + e.getMessage());
        } catch (MatEvaluatorProvider.NoAvailableMatVisualization e) {
            return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), e.getMessage());
        }
    }

    static abstract class IconPopupEvaluator extends CustomPopupFullValueEvaluator<Icon> {
        IconPopupEvaluator(@NotNull String linkText, @NotNull EvaluationContextImpl evaluationContext) {
            super(linkText, evaluationContext);
        }

        @Override
        protected JComponent createComponent(Icon data) {
            if (data instanceof ImageIcon) {
                if (((ImageIcon) data).getDescription() != null) {
                    return new JLabel(((ImageIcon) data).getDescription(), MatIcons.OpenCvVector, SwingConstants.CENTER);
                }
            }
            return createIconViewer(data);
        }
    }
}
