public class CRC32 {
  public static int compute(byte[] data) {
    int crc = 0xFFFFFFFF;
    for (byte b: data) {
      crc = (b & 0xFF) ^ crc;

      // eat 1 bit at a time!
      for (int i=0; i<8; i++) {
        if ((crc & 1) != 0) {
          crc = (crc >>> 1) ^ 0xEDB88320;
        } else {
          crc = (crc >>> 1);
        }
      }
    }
    return ~crc;
  }
}

