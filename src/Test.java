import java.nio.file.*;
import java.util.*;

public class Test {

  public void main(String[] args) throws Exception {
    Path root = Path.of(args[0]);
    List<Path> files = Files.walk(root).filter(Files::isRegularFile).toList();

    for (var path: files) {
      String entryName = root.relativize(path).toString().replace('\\', '/');
      System.out.println(entryName);
    }
  }
}

