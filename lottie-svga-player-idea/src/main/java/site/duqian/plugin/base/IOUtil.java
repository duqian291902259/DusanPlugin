package site.duqian.plugin.base;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IOUtil {

    @Nullable
    public static String getFileContent(String fileName) {
        return getFileContent(fileName, true);
    }

    @Nullable
    public static String fileToString(String fileName) {
        return getFileContent(fileName, false);
    }

    @Nullable
    public static String getFileContent(String fileName, boolean isFromRes) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            StringBuilder buffer = new StringBuilder();
            if (isFromRes) {
                inputStream = getResourceAsStream(fileName);
            } else {
                inputStream = new FileInputStream(fileName);
            }
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append('\n');
                }
                return buffer.toString();
            }
        } catch (Exception e) {
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

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存内容到指定目录
     */
    public static void saveFile(String directory, String fileName, String content) {
        String path = String.format("%s/%s", directory, fileName);
        File dirFile = new File(directory);
        File file = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        file.delete();
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

    public static final String TAG = "IOUtils";

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
    public static long copy(InputStream input, OutputStream output) throws IOException {
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

    @NotNull
    public static String getFileHeader(String path) {
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

    public static String processFileSizeText(String filePath) {
        long length = new File(filePath).length();
        if (length < 1024) {
            return String.format("%sB", length);
        } else if (length < 1048576) {
            return String.format("%sK", Math.round(length * 1.0 / 1024 * 10) / 10.0);
        } else {
            return String.format("%sM", Math.round(length * 1.0 / 1048576 * 100) / 100.0);
        }
    }

    public static String bytesToHexString(byte[] src) {
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

    public static String fileToBase64(String filePath) {
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

    public static String resourceToBase64(String resPath) {
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
