package dev.rfj.asqs.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Pure Java AndroidManifest.xml decoder (binary AXML -> readable XML) with no dependencies.
 *
 * Features:
 * - Extracts AndroidManifest.xml from APK
 * - Correctly parses AXML chunks
 * - Decodes string pool (UTF-8 + UTF-16)
 * - Proper START_TAG parsing (fixes empty attributes issue)
 * - Adds android: prefix where applicable
 * - Emits xmlns:android only if used
 * - Prints @0x... and ?0x... for references/attributes
 */
public class AndroidManifestDecoder {

    public static final int BYTE_BUFFER_SIZE = 8192;

    // Chunk types
    private static final int CHUNK_AXML_FILE         = 0x0003;
    private static final int CHUNK_STRING_POOL       = 0x0001;
    private static final int CHUNK_RESOURCE_IDS      = 0x0180;
    private static final int CHUNK_XML_START_NS      = 0x0100;
    private static final int CHUNK_XML_END_NS        = 0x0101;
    private static final int CHUNK_XML_START_TAG     = 0x0102;
    private static final int CHUNK_XML_END_TAG       = 0x0103;
    private static final int CHUNK_XML_TEXT          = 0x0104;

    // Value types (Res_value.dataType)
    private static final int TYPE_NULL       = 0x00;
    private static final int TYPE_REFERENCE  = 0x01; // @0x...
    private static final int TYPE_ATTRIBUTE  = 0x02; // ?0x...
    private static final int TYPE_STRING     = 0x03;
    private static final int TYPE_FLOAT      = 0x04;
    private static final int TYPE_INT_DEC    = 0x10;
    private static final int TYPE_INT_HEX    = 0x11;
    private static final int TYPE_INT_BOOLEAN= 0x12;

    // Common Manifest namespace
    private static final String ANDROID_NS_URI = "http://schemas.android.com/apk/res/android";

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

    /**
     * Decode binary AXML bytes into readable XML.
     */
    public static String decodeAxml(byte[] data) throws IOException {
        ByteBufferReader br = new ByteBufferReader(data);

        int type = br.readU16();
        br.readU16(); // headerSize
        int chunkSize = br.readU32();

        if (type != CHUNK_AXML_FILE) {
            throw new IOException("Not an AXML file: type=0x" + Integer.toHexString(type));
        }

        StringPool strings = null;

        StringBuilder out = new StringBuilder();
        Deque<String> tagStack = new ArrayDeque<>();

        // We'll detect if android namespace was used in attributes.
        boolean androidNsUsed = false;

        // We can track namespaces, but for manifest readability we only need android xmlns.
        // If needed later, you can emit additional namespaces here.
        int end = chunkSize;
        while (br.position() < end) {
            int chunkType = br.readU16();
            int headerSize = br.readU16();
            int size = br.readU32();
            int chunkStart = br.position() - 8;

            switch (chunkType) {
                case CHUNK_STRING_POOL:
                    strings = StringPool.parse(br, chunkStart, size);
                    break;

                case CHUNK_RESOURCE_IDS:
                    // We don't need this for decoding attributes by name (string pool already has them)
                    // But we must consume it correctly.
                    br.seek(chunkStart + size);
                    break;

                case CHUNK_XML_START_NS:
                case CHUNK_XML_END_NS:
                    // Namespace chunks not required for basic readability.
                    // Don't manually skip; the loop will seek to chunk end.
                    break;

                case CHUNK_XML_START_TAG: {
                    if (strings == null) throw new IOException("String pool not parsed yet.");

                    br.readU32(); // lineNumber
                    br.readU32(); // comment

                    int nsIdx = br.readU32();
                    int nameIdx = br.readU32();

                    // ✅ Correct ResXMLTree_attrExt structure:
                    int attrStart = br.readU16();
                    int attrSize  = br.readU16();
                    int attrCount = br.readU16();
                    int idIndex   = br.readU16();
                    int classIndex= br.readU16();
                    int styleIndex= br.readU16();

                    String tagName = strings.get(nameIdx);
                    String indent = indent(tagStack.size());

                    // If this is the root <manifest> tag, we may later inject xmlns:android
                    boolean isManifestRoot = tagStack.isEmpty() && "manifest".equals(tagName);

                    out.append(indent).append("<").append(tagName);

                    // Parse attributes
                    List<Attr> attrs = new ArrayList<>(attrCount);

                    for (int i = 0; i < attrCount; i++) {
                        int attrNsIdx = br.readU32();
                        int attrNameIdx = br.readU32();
                        int rawValueIdx = br.readU32();

                        int valueSize = br.readU16();
                        br.readU8(); // res0
                        int valueType = br.readU8();
                        int valueData = br.readU32();

                        String attrName = strings.get(attrNameIdx);
                        String attrNs = (attrNsIdx == -1) ? null : strings.get(attrNsIdx);

                        String value;
                        if (rawValueIdx != -1) {
                            value = strings.get(rawValueIdx);
                        } else {
                            value = decodeTypedValue(valueType, valueData, strings);
                        }

                        boolean isAndroidNs = ANDROID_NS_URI.equals(attrNs);
                        if (isAndroidNs) androidNsUsed = true;

                        String fullAttrName = attrName;
                        if (isAndroidNs) {
                            fullAttrName = "android:" + attrName;
                        }

                        attrs.add(new Attr(fullAttrName, value));
                    }

                    // Inject xmlns:android only once on root <manifest>, only if android ns used.
                    if (isManifestRoot && androidNsUsed) {
                        out.append(" xmlns:android=\"").append(ANDROID_NS_URI).append("\"");
                    }

                    // Write attributes
                    for (Attr a : attrs) {
                        out.append(" ")
                                .append(a.name)
                                .append("=\"")
                                .append(escapeXml(a.value))
                                .append("\"");
                    }

                    out.append(">\n");
                    tagStack.push(tagName);
                    break;
                }

                case CHUNK_XML_END_TAG: {
                    if (strings == null) throw new IOException("String pool not parsed yet.");

                    br.readU32(); // lineNumber
                    br.readU32(); // comment
                    br.readU32(); // nsIdx
                    int nameIdx = br.readU32();

                    String tagName = strings.get(nameIdx);

                    tagStack.pop();
                    out.append(indent(tagStack.size()))
                            .append("</").append(tagName).append(">\n");
                    break;
                }

                case CHUNK_XML_TEXT: {
                    // Optional: manifest rarely uses text nodes.
                    // Layout: lineNumber, comment, textIdx, unknown, unknown
                    br.readU32(); // lineNumber
                    br.readU32(); // comment
                    int textIdx = br.readU32();
                    br.readU32(); // unknown
                    br.readU32(); // unknown

                    if (strings != null) {
                        String text = strings.get(textIdx);
                        if (text != null && !text.trim().isEmpty()) {
                            out.append(indent(tagStack.size()))
                                    .append(escapeXml(text.trim()))
                                    .append("\n");
                        }
                    }
                    break;
                }

                default:
                    // Unknown chunk: skip
                    break;
            }

            // Always move to chunk end
            br.seek(chunkStart + size);
        }

        return out.toString();
    }

    private static String decodeTypedValue(int type, int data, StringPool sp) {
        switch (type) {
            case TYPE_STRING:
                return sp.get(data);

            case TYPE_REFERENCE:
                return "@0x" + Integer.toHexString(data);

            case TYPE_ATTRIBUTE:
                return "?0x" + Integer.toHexString(data);

            case TYPE_INT_DEC:
                return Integer.toString(data);

            case TYPE_INT_HEX:
                return "0x" + Integer.toHexString(data);

            case TYPE_INT_BOOLEAN:
                return data != 0 ? "true" : "false";

            case TYPE_FLOAT:
                return Float.toString(Float.intBitsToFloat(data));

            case TYPE_NULL:
                return "";

            default:
                // Unknown types: print hex
                return "0x" + Integer.toHexString(data);
        }
    }

    private static String indent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.max(0, depth); i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // Simple attr container
    private static class Attr {
        final String name;
        final String value;

        Attr(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    // ========= String Pool =========

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

        private static String readUtf8String(ByteBufferReader br, int offset) {
            br.seek(offset);
            readLength8(br); // utf16 length (ignored)
            int byteLen = readLength8(br);
            byte[] buf = br.readBytes(byteLen);
            br.readU8(); // null terminator
            return new String(buf, StandardCharsets.UTF_8);
        }

        private static String readUtf16String(ByteBufferReader br, int offset) {
            br.seek(offset);
            int charLen = readLength16(br);
            byte[] buf = br.readBytes(charLen * 2);
            br.readU16(); // null terminator
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

    // ========= ByteBufferReader (Little Endian) =========

    private static class ByteBufferReader {
        private final byte[] data;
        private int pos = 0;

        ByteBufferReader(byte[] data) {
            this.data = data;
        }

        int position() {
            return pos;
        }

        void seek(int newPos) {
            pos = newPos;
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
