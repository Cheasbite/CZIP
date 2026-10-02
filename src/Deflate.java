public class Deflate {
  public static byte[] compress(byte[] data) {
    BitWriter out = new BitWriter();

    // header
    // BFINAL=1
    out.writeBits(1, 1); // 1 if last block else 0
    // BTYPE=01
    out.writeBits(1, 2);
    // options:
    // 00: No compression
    // 01: static huffman
    // 10: dynamic huffman
    // 11: Reserved

    int[] head = new int[0x8000]; // the table size thing 2^15
    int[] prev = new int[data.length];
    java.util.Arrays.fill(head, -1);
    java.util.Arrays.fill(prev, -1);

    int i = 0;
    while (i < data.length) {
        LZ77.Match m = LZ77.findMatch(data, i, head, prev);
        if (m.length > 0) {
            LZ77.writeMatch(out, m.length, m.distance);

            for (int k=0; k<m.length; k++) {
              LZ77.insert(data, head, prev, i+k);
            }

            i += m.length;
        } else {
            int symbol = data[i] & 0xFF;

            int code = 0;
            int length = 0;
            if (symbol <= 143) {
              code = 0x30 + symbol;
              length = 8;
            } else {
              code = 0x190 + (symbol - 144);
              length = 9;
            }

            LZ77.insert(data, head, prev, i);

            out.writeHuffman(code, length);
            i += 1;
        }
    }

    out.writeHuffman(0, 7);

    return out.toByteArray();
  }
}

