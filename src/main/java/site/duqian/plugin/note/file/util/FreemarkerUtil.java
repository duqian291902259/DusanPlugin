package site.duqian.plugin.note.file.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * freemarker Utils
 */
public class FreemarkerUtil {
    private static Configuration configuration;
    private FreemarkerUtil(){}

    static {
        configuration = new Configuration(Configuration.getVersion());
        ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(FreemarkerUtil.class,"template");
        configuration.setTemplateLoader(classTemplateLoader);
        configuration.setDefaultEncoding("utf-8");

    }

    /**
     * Render md
     */
    public static void genFile(String ftlUri, Map<String, Object> data, String filePath) throws Exception {
        Template template = configuration.getTemplate(ftlUri);
        FileWriter writer = new FileWriter(new File(filePath));
        template.process(data, writer);
        writer.close();
    }
}
