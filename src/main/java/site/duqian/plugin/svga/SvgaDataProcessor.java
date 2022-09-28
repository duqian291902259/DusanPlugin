package site.duqian.plugin.svga;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.duqian.plugin.base.IOUtil;

import java.awt.*;

/**
 * Description:解析并替换svga播放内容
 * <p>
 * Created by 杜乾 on 2022/9/26 - 09:04.
 * E-mail: duqian2010@gmail.com
 */
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
        if (file == null || !file.exists()) {
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
        String htmlContent = IOUtil.getFileContent("svga/htm/player.html");
        if (htmlContent == null) {
            return null;
        }
        htmlContent = htmlContent.replace(CSS_SCRIPT_STUFF, processCss());
        htmlContent = htmlContent.replace(JS_SCRIPT_STUFF, buildJsContent());
        htmlContent = htmlContent.replace(FILE_SIZE_STUFF, IOUtil.processFileSizeText(path));
        Color borderColor = JBColor.border();
        htmlContent = htmlContent.replace(BORDER_COLOR_STUFF, String.format("rgb(%d,%d,%d)",
                borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue()));
        Color themeBgColor = JBColor.background();
        htmlContent = htmlContent.replace(BACKGROUND_COLOR_STUFF, String.format("rgb(%d,%d,%d)",
                themeBgColor.getRed(), themeBgColor.getGreen(), themeBgColor.getBlue()));
        //Color fontColor = JBColor.foreground();
        Color fontColor = Color.decode("#ffffff");
        htmlContent = htmlContent.replace(FONT_COLOR_STUFF, String.format("rgb(%d,%d,%d)",
                fontColor.getRed(), fontColor.getGreen(), fontColor.getBlue()));
        htmlContent = htmlContent.replace(FONT_FAMILY_STUFF, UIUtil.getLabelFont().getFamily());
        htmlContent = htmlContent.replace(BACKGROUND_IMAGE_STUFF, String.format("data:image/svg+xml;base64,%s",
                IOUtil.resourceToBase64("svga/img/backgroundImage.svg")));
        htmlContent = htmlContent.replace(SVGA_DATA_STUFF, String.format("data:svga/%s;base64,%s",
                getSvgaVersion(path), IOUtil.fileToBase64(path)));
        return htmlContent;
    }

    @NotNull
    private static String buildJsContent() {
        return processJs("svga/js/svga.min.js") + '\n' +
                processJs("svga/js/jszip.min.js") + '\n' +
                processJs("svga/js/main.js");
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
        String jsContent = IOUtil.getFileContent("svga/htm/player.css");
        if (jsContent != null) {
            return String.format("<style>%s</style>", jsContent);
        }
        return "";
    }

    @NotNull
    private static String getSvgaVersion(String path) {
        return "504B0304".equals(IOUtil.getFileHeader(path)) ? SVGA_V1 : SVGA_V2;
    }
}
