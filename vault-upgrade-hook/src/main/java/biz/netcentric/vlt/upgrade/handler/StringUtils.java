package biz.netcentric.vlt.upgrade.handler;

import java.nio.CharBuffer;

public class StringUtils {

    public static String join(char separator, String... parts) {
        return join(CharBuffer.wrap(new char[] { separator }), parts);
    }

    public static String join(CharSequence separator, String... parts) {
        if (parts == null) {
            parts = new String[] { null };
        }
        int length = 0;

        final int separatorLength = separator.length();
        for (String part : parts) {
            length += (part == null ? 4 : part.length()) + separatorLength;
        }

        final StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < parts.length; i++) {
            result.append(parts[i]);
            if (i + 1 < parts.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }
}
