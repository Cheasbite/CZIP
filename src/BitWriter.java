import java.io.ByteArrayOutputStream;

public class BitWriter {
  private int buffer = 0;
  private int count = 0;
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();

  // starts from LSB
  public void writeBits(int value, int n) {
    for (int i = 0; i<n; i++) {
      int bit = (value >> i) & 1; // gets either 0 or 1
      buffer = (bit << count) | buffer;
      count++;

      if (count == 8) {
        out.write(buffer);
        count = 0; buffer = 0;
      }
    }
  }

  // starts from MSB
  public void writeHuffman(int code, int n) {
    for (int i = n-1; i>=0; i--) {
      int bit = (code >> i) & 1;
      writeBits(bit, 1);
    }
  }

  public byte[] toByteArray() {
    if (count > 0) {
      out.write(buffer);
      count = 0; buffer = 0;
    }
    return out.toByteArray();
  }

  public static void main(String[] args) {
    BitWriter w = new BitWriter();
    w.writeHuffman(0b0011, 4);

    for (byte b: w.toByteArray()) {
      System.out.printf("%02X ", b);
    }
  }
}

