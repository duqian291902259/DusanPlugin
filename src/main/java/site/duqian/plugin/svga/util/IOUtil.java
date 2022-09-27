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

    /**
     * 保存内容到"ccvoice/file/xxxx"配置文件
     */
    public static void saveFile(String directory, String fileName, String content) {
        String path = String.format("%s/%s", directory, fileName);
        File dirFile = new File(directory);
        File file = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("FileUtil save file \"" + path + "\" exception " + e);
                return;
            }
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            System.out.println("FileUtil write content to file " + path + " exception " + e);
        } finally {
            close(fw);
            close(bw);
        }
    }

    private static final String TAG = "IOUtils";

    public static void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public static void closeStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }

    public static long copy(File in, OutputStream out) throws IOException {
        return copy(new FileInputStream(in), out);
    }

    public static long copy(InputStream in, File out) throws IOException {
        return copy(in, new FileOutputStream(out));
    }

    /**
     * Pipe an InputStream to the given OutputStream <p /> Taken from Apache Commons IOUtils.
     */
    private static long copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[1024 * 4];
            long count = 0;
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
            output.flush();
            return count;
        } finally {
            closeStream(input);
            closeStream(output);
        }
    }

    public static InputStream byte2InputStream(byte[] b) {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(b);
        return byteInputStream;
    }
}
