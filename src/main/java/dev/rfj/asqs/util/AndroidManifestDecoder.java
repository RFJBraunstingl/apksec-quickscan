package dev.rfj.asqs.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AndroidManifestDecoder {

    public static final int BYTE_BUFFER_SIZE = 8192;

    public static String decodeManifestFromApk(File apkFile) throws IOException {
        byte[] axml = extractFileFromApk(apkFile, "AndroidManifest.xml");
        if (axml == null) throw new FileNotFoundException("AndroidManifest.xml not found in APK");
        return decodeAxml(axml);
    }

    private static byte[] extractFileFromApk(File apkFile, String path) throws IOException {
        try (ZipFile zip = new ZipFile(apkFile)) {
            ZipEntry entry = zip.getEntry(path);
            if (entry == null) return null;
            try (InputStream in = zip.getInputStream(entry)) {
                return readAllBytes(in);
            }
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[BYTE_BUFFER_SIZE];
        int r;
        while ((r = in.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toByteArray();
    }

    public static String decodeAxml(byte[] data) throws IOException {
        ByteBufferReader br = new ByteBufferReader(data);

        // AXML file begins with an XML chunk header
        int type = br.readU16();
        br.readU16(); // headerSize
        int chunkSize = br.readU32();

        if (type != 0x0003) {
            throw new IOException("Not an AXML file: type=0x" + Integer.toHexString(type));
        }

        StringPool strings = null;
        int[] resourceIds = null;

        StringBuilder out = new StringBuilder();
        Deque<String> tagStack = new ArrayDeque<>();

        int end = chunkSize;
        while (br.position() < end) {
            int chunkType = br.readU16();
            int headerSize = br.readU16();
            int size = br.readU32();
            int chunkStart = br.position() - 8;

            switch (chunkType) {
                case 0x0001: // STRING_POOL
                    strings = StringPool.parse(br, chunkStart, size);
                    break;

                case 0x0180: // RESOURCE_IDS
                    resourceIds = parseResourceIds(br, size);
                    break;

                case 0x0102: // START_TAG
                    if (strings == null) throw new IOException("String pool not parsed yet.");

                    // parse start tag
                    br.readU32(); // lineNumber
                    br.readU32(); // comment
                    int nsIdx = br.readU32();
                    int nameIdx = br.readU32();
                    br.readU32(); // flags
                    int attrCount = br.readU16();
                    br.readU16(); // classAttr
                    br.readU16(); // idAttr
                    br.readU16(); // styleAttr
                    br.readU16(); // unused

                    String ns = nsIdx == -1 ? null : strings.get(nsIdx);
                    String name = strings.get(nameIdx);
                    String indent = indent(tagStack.size());

                    out.append(indent).append("<").append(name);

                    // attributes
                    for (int i = 0; i < attrCount; i++) {
                        int attrNs = br.readU32();
                        int attrName = br.readU32();
                        int rawValue = br.readU32();
                        int typedSize = br.readU16();
                        br.readU8(); // zero
                        int dataType = br.readU8();
                        int dataValue = br.readU32();

                        String aName = strings.get(attrName);
                        String aValue;

                        if (rawValue != -1) {
                            aValue = strings.get(rawValue);
                        } else {
                            aValue = decodeTypedValue(dataType, dataValue, strings);
                        }

                        out.append(" ").append(aName).append("=\"")
                                .append(escapeXml(aValue)).append("\"");
                    }

                    out.append(">\n");
                    tagStack.push(name);
                    break;

                case 0x0103: // END_TAG
                    br.readU32(); // lineNumber
                    br.readU32(); // comment
                    br.readU32(); // ns
                    int endNameIdx = br.readU32();
                    String endName = strings.get(endNameIdx);

                    String openName = tagStack.pop();
                    String endIndent = indent(tagStack.size());
                    out.append(endIndent).append("</").append(endName).append(">\n");
                    break;

                case 0x0101: // START_NAMESPACE
                case 0x0100: // END_NAMESPACE
                    // namespace not necessary for basic manifest readability
                    br.skip(size - 8);
                    break;

                default:
                    // Skip unknown chunk
                    br.skip(size - 8);
                    break;
            }

            // Ensure we move to chunk end
            br.seek(chunkStart + size);
        }

        return out.toString();
    }

    private static int[] parseResourceIds(ByteBufferReader br, int size) {
        int count = (size - 8) / 4;
        int[] ids = new int[count];
        for (int i = 0; i < count; i++) ids[i] = br.readU32();
        return ids;
    }

    private static String decodeTypedValue(int type, int data, StringPool sp) {
        // Most manifest values are strings or ints; handle common ones
        switch (type) {
            case 0x03: // TYPE_STRING
                return sp.get(data);
            case 0x10: // TYPE_INT_DEC
                return Integer.toString(data);
            case 0x11: // TYPE_INT_HEX
                return "0x" + Integer.toHexString(data);
            case 0x12: // TYPE_INT_BOOLEAN
                return data != 0 ? "true" : "false";
            default:
                return "0x" + Integer.toHexString(data);
        }
    }

    private static String indent(int depth) {
        return "  ".repeat(Math.max(0, depth));
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // ==== String Pool ====

    private static class StringPool {
        private final String[] strings;

        private StringPool(String[] strings) {
            this.strings = strings;
        }

        public String get(int idx) {
            if (idx < 0 || idx >= strings.length) return "";
            return strings[idx];
        }

        static StringPool parse(ByteBufferReader br, int chunkStart, int chunkSize) throws IOException {
            int stringCount = br.readU32();
            int styleCount = br.readU32();
            int flags = br.readU32();
            int stringsStart = br.readU32();
            int stylesStart = br.readU32();

            boolean utf8 = (flags & 0x00000100) != 0;

            int[] offsets = new int[stringCount];
            for (int i = 0; i < stringCount; i++) offsets[i] = br.readU32();

            int base = chunkStart + stringsStart;
            String[] decoded = new String[stringCount];

            for (int i = 0; i < stringCount; i++) {
                int off = base + offsets[i];
                decoded[i] = utf8 ? readUtf8String(br, off) : readUtf16String(br, off);
            }
            return new StringPool(decoded);
        }

        private static String readUtf8String(ByteBufferReader br, int offset) throws IOException {
            br.seek(offset);
            readLength8(br); // utf16 len (ignored)
            int byteLen = readLength8(br);
            byte[] buf = br.readBytes(byteLen);
            br.readU8(); // null
            return new String(buf, StandardCharsets.UTF_8);
        }

        private static String readUtf16String(ByteBufferReader br, int offset) throws IOException {
            br.seek(offset);
            int charLen = readLength16(br);
            byte[] buf = br.readBytes(charLen * 2);
            br.readU16(); // null
            return new String(buf, StandardCharsets.UTF_16LE);
        }

        private static int readLength8(ByteBufferReader br) {
            int len = br.readU8();
            if ((len & 0x80) != 0) {
                len = ((len & 0x7F) << 7) | (br.readU8() & 0x7F);
            }
            return len;
        }

        private static int readLength16(ByteBufferReader br) {
            int len = br.readU16();
            if ((len & 0x8000) != 0) {
                len = ((len & 0x7FFF) << 15) | br.readU16();
            }
            return len;
        }
    }

    // ==== Byte buffer reader (Little Endian) ====

    private static class ByteBufferReader {
        private final byte[] data;
        private int pos = 0;

        ByteBufferReader(byte[] data) {
            this.data = data;
        }

        int position() { return pos; }

        void seek(int newPos) {
            pos = newPos;
        }

        void skip(int n) {
            pos += n;
        }

        int readU8() {
            return data[pos++] & 0xFF;
        }

        int readU16() {
            int v = (data[pos] & 0xFF) | ((data[pos + 1] & 0xFF) << 8);
            pos += 2;
            return v;
        }

        int readU32() {
            int v = (data[pos] & 0xFF)
                    | ((data[pos + 1] & 0xFF) << 8)
                    | ((data[pos + 2] & 0xFF) << 16)
                    | ((data[pos + 3] & 0xFF) << 24);
            pos += 4;
            return v;
        }

        byte[] readBytes(int len) {
            byte[] b = Arrays.copyOfRange(data, pos, pos + len);
            pos += len;
            return b;
        }
    }
}

