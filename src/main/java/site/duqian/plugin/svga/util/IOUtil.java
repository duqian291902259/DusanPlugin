package site.duqian.plugin.svga.util;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOUtil {

    @Nullable
    public static String getFileContent(String fileName) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            StringBuilder buffer = new StringBuilder();
            inputStream = getResourceAsStream(fileName);
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append('\n');
                }
                return buffer.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(reader);
            close(inputStream);
        }
        return null;
    }

    @Nullable
    public static InputStream getResourceAsStream(String fileName) {
        return IOUtil.class.getClassLoader().getResourceAsStream(fileName);
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
