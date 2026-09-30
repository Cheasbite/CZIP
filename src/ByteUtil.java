import java.io.ByteArrayOutputStream;

public class ByteUtil {
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();

  public void bytes(byte[] data) {
    out.writeBytes(data);
  }

  public int size() {
    return out.size();
  }

  public void u16(int value) {
    out.write(value);
    out.write(value >>> 8);
  }

  public void u32(int value) {
    out.write(value);
    out.write(value >>> 8);
    out.write(value >>> 16);
    out.write(value >>> 24);
  }

  public byte[] toByteArray() {
    return out.toByteArray();
  }

  public static void main(String[] args) {
     ByteUtil b = new ByteUtil();
     b.u16(0x1234);
     b.u32(0x04034b50);

     for (byte x: b.toByteArray()) {
       System.out.printf("%02X ", x);
     }
     System.out.println();
  }
}

