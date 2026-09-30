import java.nio.file.*;
import java.util.*;

public class Main {
  static class Entry {
      String name;
      int crc, compSize, size, offset;
  }

  public static void main(String[] args) throws Exception {
//    String name = Path.of(args[0]).getFileName().toString();
//    byte[] nameBytes = name.getBytes(); // filename as bytes

    // Read from a file
//   byte[] data = Files.readAllBytes(java.nio.file.Path.of(args[0]));
//    int crc = CRC32.compute(data);

//   byte[] compressed = Deflate.compress(data);

    ByteUtil idk = new ByteUtil();

    Path root = Path.of(args[0]);
    List<Path> files = Files.walk(root).filter(Files::isRegularFile).toList();

    List<Entry> recall = new ArrayList<Entry>();

    for (var path: files) {
      byte[] data = Files.readAllBytes(path);
      byte[] compressed = Deflate.compress(data);
      int crc = CRC32.compute(data);
      int offset = idk.size();
      String entryName = root.relativize(path).toString().replace('\\', '/');

      byte[] nameBytes = entryName.getBytes();

      // Setting up the Recall for later use
      Entry entry = new Entry();
      entry.name = entryName;
      entry.offset = offset;
      entry.crc = crc;
      entry.compSize = compressed.length;
      entry.size = data.length;

      recall.add(entry);

      // signature
      idk.u32(0x04034b50);
      //version needed
      idk.u16(20);
      // flags
      idk.u16(0);
      // method
      idk.u16(8);
      // mod time
      idk.u16(0);
      // mod date
      idk.u16(0x0021);
      // crc32
      idk.u32(crc);
      // compressed size
      idk.u32(compressed.length);
      // uncompressed size
      idk.u32(data.length);
      // filename length
      idk.u16(nameBytes.length);
      // extra length
      idk.u16(0);
      // filename
      idk.bytes(nameBytes);
      // write the compressed data to zip
      idk.bytes(compressed);
    }


    int cdStart = idk.size();
    for (Entry e: recall) {
      byte[] nameBytes = e.name.getBytes();

      // signature
      idk.u32(0x02014b50);
      // version made by
      idk.u16(20);
      //version needed
      idk.u16(20);
      // flags
      idk.u16(0);
      // method
      idk.u16(8);
      // mod time
      idk.u16(0);
      // mod date
      idk.u16(0x0021);
      // crc32
      idk.u32(e.crc);
      // compressed size
      idk.u32(e.compSize);
      // uncompressed size
      idk.u32(e.size);
      // filename length
      idk.u16(nameBytes.length);
      // extra length
      idk.u16(0);
      // comment length
      idk.u16(0);
      // disk number start
      idk.u16(0);
      // internal attributes
      idk.u16(0);
      // external attributes
      idk.u32(0);
      // local header offset
      idk.u32(e.offset);
      // filename
      idk.bytes(nameBytes);
    }

    int cdEnd = idk.size();
    // signature
    idk.u32(0x06054b50);
    // disk number
    idk.u16(0);
    // disk w/ directory
    idk.u16(0);
    // entries on the disk
    idk.u16(recall.size());
    // total entries
    idk.u16(recall.size());
    // directory size
    idk.u32(cdEnd - cdStart);
    // directory offset
    idk.u32(cdStart);
    // comment length
    idk.u16(0);

    // writer
    java.nio.file.Files.write(Path.of("Portfolio.zip"), idk.toByteArray());

  }
}

