public class LZ77 {
  // Goal: Find the next byte would always try of find a match
  // in the last 4GiB of data and encoded them

  static int[] LEN_BASE  = {3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,35,43,51,59,67,83,99,115,131,163,195,227,258};
  static int[] LEN_EXTRA = {0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0};

  static int[] DIST_BASE  = {1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577};
  static int[] DIST_EXTRA = {0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13};

  static class Match {
    int length; // return 0 if no match is found
    int distance;
  }

  // Grouping the same bit/char together in the last 4GiB
  // Only >= 3 is grouped otherwise dropped!
  static Match findMatch(byte[] data, int i) {
    int bestLength = 0;
    int bestDistance = 0;
    for (int candidate = Math.max(0, i - 32768); candidate < i; candidate++) {
      int k = 0;

      while ((i + k < data.length && k < 258) && data[candidate + k] == data[i + k]) {
        k += 1;
      }

      if (k > bestLength) {
        bestLength = k;
        bestDistance = i - candidate;
      }
    }

    if (bestLength < 3) {
      bestLength = 0;
    }

    Match result = new Match();
    result.length = bestLength;
    result.distance = bestDistance;

    return result;
  }

  // Encode the match after findMatch result
  static void writeMatch(BitWriter out, int length, int distance) {
    int lidx = LEN_BASE.length - 1; // take the most value if no best is found!
    for (int i=0; i<LEN_BASE.length; i++) {
      if (LEN_BASE[i] > length) {
        lidx = i - 1;
        break;
      } else {
        continue;
      }
    }

    int symbol = 257 + lidx;

    int value = 0;
    int bits = 0;

    if (symbol <= 279) {
      value = symbol - 256;
      bits = 7;
    } else {
      // for symbol that is in the range of 280 -> 285
      value = 0xC0 + (symbol - 280);
      bits = 8;
    }

    // length symbol
    out.writeHuffman(value, bits);

    // length extra bits
    out.writeBits(length - LEN_BASE[lidx], LEN_EXTRA[lidx]);

    int didx = DIST_BASE.length - 1; // same reason as the last
    for (int i=0; i<DIST_BASE.length; i++) {
      if (DIST_BASE[i] > distance) {
        didx = i - 1;
        break;
      } else {
        continue;
      }
    }

    // distance code
    out.writeHuffman(didx, 5);

    // distance extra bits
    out.writeBits(distance - DIST_BASE[didx], DIST_EXTRA[didx]);
  }


  public static void main(String[] args) {
    BitWriter out = new BitWriter();
    byte[] data1 = "abcabc".getBytes();
    byte[] data2 = "aaaa".getBytes();
    Match m = findMatch(data1, 3);
    Match n = findMatch(data2, 1);

    System.out.println(m.length + " " + m.distance);
    System.out.println(n.length + " " + n.distance);
  }
}

