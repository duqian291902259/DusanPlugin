package site.duqian.plugin.svga.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class SvgaDataProcessor {

    private static final String SVGA_V1 = "1.0";
    private static final String SVGA_V2 = "2.0";
    private static final String CSS_SCRIPT_STUFF = "{CSS_STUFF}";
    private static final String JS_SCRIPT_STUFF = "{JS_SCRIPT_STUFF}";
    private static final String SVGA_DATA_STUFF = "{SVGA_DATA_STUFF}";
    private static final String FONT_FAMILY_STUFF = "{FONT_FAMILY_STUFF}";
    private static final String FONT_COLOR_STUFF = "#{FONT_COLOR_STUFF}";
    private static final String BORDER_COLOR_STUFF = "#{BORDER_COLOR_STUFF}";
    private static final String BACKGROUND_COLOR_STUFF = "#{BACKGROUND_COLOR_STUFF}";
    private static final String BACKGROUND_IMAGE_STUFF = "{BACKGROUND_IMAGE_STUFF}";
    private static final String FILE_SIZE_STUFF = "{FILE_SIZE_STUFF}";

    @NotNull
    public static String processSvgaData(VirtualFile file) {
        if (file == null) {
            return "";
        }
        if (!file.exists()) {
            return "";
        }
        String htmlContent = processHtml(file.getPath());
        if (htmlContent == null) {
            return "";
        }
        return htmlContent;
    }

    @Nullable
    private static String processHtml(String path) {
        String htmlContent = IOUtil.getFileContent("htm/player.htm");
        if (htmlContent == null) {
            return null;
        }
        htmlContent = htmlContent.replace(CSS_SCRIPT_STUFF, processCss());
        htmlContent = htmlContent.replace(JS_SCRIPT_STUFF, buildJsContent());
        htmlContent = htmlContent.replace(FILE_SIZE_STUFF, processFileSizeText(path));
        Color borderColor = JBColor.border();
        htmlContent = htmlContent.replace(BORDER_COLOR_STUFF, String.format("rgb(%d,%d,%d)",
                borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue()));
        Color themeBgColor = JBColor.background();
        htmlContent = htmlContent.replace(BACKGROUND_COLOR_STUFF, String.format("rgb(%d,%d,%d)",
                themeBgColor.getRed(), themeBgColor.getGreen(), themeBgColor.getBlue()));
        Color fontColor = JBColor.foreground();
        htmlContent = htmlContent.replace(FONT_COLOR_STUFF, String.format("rgb(%d,%d,%d)",
                fontColor.getRed(), fontColor.getGreen(), fontColor.getBlue()));
        htmlContent = htmlContent.replace(FONT_FAMILY_STUFF, UIUtil.getLabelFont().getFamily());
        htmlContent = htmlContent.replace(BACKGROUND_IMAGE_STUFF, String.format("data:image/svg+xml;base64,%s",
                resourceToBase64("img/backgroundImage.svg")));
        htmlContent = htmlContent.replace(SVGA_DATA_STUFF, String.format("data:svga/%s;base64,%s",
                getSvgaVersion(path), fileToBase64(path)));
        return htmlContent;
    }

    @NotNull
    private static String buildJsContent() {
        return processJs("js/svga.min.js") + '\n' +
                processJs("js/jszip.min.js") + '\n' +
                processJs("js/main.js");
    }

    @NotNull
    private static String processJs(String path) {
        String jsContent = IOUtil.getFileContent(path);
        if (jsContent != null) {
            return String.format("<script type=\"text/javascript\">%s</script>", jsContent);
        }
        return "";
    }

    @NotNull
    private static String processCss() {
        String jsContent = IOUtil.getFileContent("htm/player.css");
        if (jsContent != null) {
            return String.format("<style>%s</style>", jsContent);
        }
        return "";
    }

    @NotNull
    private static String getSvgaVersion(String path) {
        return "504B0304".equals(getFileHeader(path)) ? SVGA_V1 : SVGA_V2;
    }

    @NotNull
    private static String getFileHeader(String path) {
        FileInputStream is = null;
        String value = "";
        try {
            is = new FileInputStream(path);
            byte[] b = new byte[4];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    private static String processFileSizeText(String filePath) {
        long length = new File(filePath).length();
        if (length < 1024) {
            return String.format("%sB", length);
        } else if (length < 1048576) {
            return String.format("%sK", Math.round(length * 1.0 / 1024 * 10) / 10.0);
        } else {
            return String.format("%sM", Math.round(length * 1.0 / 1048576 * 100) / 100.0);
        }
    }

    @NotNull
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return builder.toString();
        }
        String hv;
        for (byte b : src) {
            hv = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    @Nullable
    private static String fileToBase64(String filePath) {
        String base64 = null;
        InputStream in = null;
        try {
            File file = new File(filePath);
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            if (in.read(bytes) != -1) {
                base64 = Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    @Nullable
    private static String resourceToBase64(String resPath) {
        String base64 = null;
        InputStream in = IOUtil.getResourceAsStream(resPath);
        if (in == null) {
            return null;
        }
        try {
            byte[] bytes = new byte[in.available()];
            if (in.read(bytes) != -1) {
                base64 = Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }
}
