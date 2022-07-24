package site.duqian.plugin.note.file.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * freemarker工具
 */
public class FreemarkerUtil {
    private static Configuration configuration;
    private FreemarkerUtil(){}

    static {
        // 1.设置配置类
        configuration = new Configuration(Configuration.getVersion());
        //2. 设置模板所在的目录
        ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(FreemarkerUtil.class,"template");
        configuration.setTemplateLoader(classTemplateLoader);
        //3.设置字符集
        configuration.setDefaultEncoding("utf-8");

    }

    /**
     * 模板渲染生成目标文件
     *
     * @param ftlUri   模板路径
     * @param data     数据
     * @param filePath 文件地址
     * @throws IOException
     */
    public static void genFile(String ftlUri, Map<String, Object> data, String filePath) throws Exception {
        //4.加载模板
        Template template = configuration.getTemplate(ftlUri);
        //6.创建Writer对象
        FileWriter writer = new FileWriter(new File(filePath));
        //7.输出数据模型到文件中
        template.process(data, writer);
        //8.关闭Writer对象
        writer.close();
    }
}
