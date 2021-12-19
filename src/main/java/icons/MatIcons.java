package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class MatIcons {
    private static @NotNull Icon load(@NotNull String path) {
        return IconManager.getInstance().getIcon(path, MatIcons.class);
    }

    public static final @NotNull Icon ToggleTransparencyChessboard = load("/icons/ToggleTransparencyChessboard.svg");
    public static final @NotNull Icon OpenCv = load("/icons/opencv.png");
    public static final @NotNull Icon OpenCvVector = load("/icons/opencvVector.svg");
}
