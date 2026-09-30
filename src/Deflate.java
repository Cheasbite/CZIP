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

    int i = 0;
    while (i < data.length) {
        LZ77.Match m = LZ77.findMatch(data, i);
        if (m.length > 0) {
            LZ77.writeMatch(out, m.length, m.distance);
            i += m.length;
        } else {
            // your step-5 literal code here, for data[i]
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

            out.writeHuffman(code, length);
            i += 1;
        }
    }

    out.writeHuffman(0, 7);

    return out.toByteArray();
  }

  public static void main(String[] args) {
    byte[] data = "abcabc".getBytes();
    byte[] result = compress(data);

    for (byte b: result) {
      System.out.printf("%02X ", b);
    }
  }
}

