public class LZ77 {
  // Goal: Find the next byte would always try of find a match
  // in the last 32KiB of data and encoded them

  static int[] LEN_BASE  = {3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,35,43,51,59,67,83,99,115,131,163,195,227,258};
  static int[] LEN_EXTRA = {0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0};

  static int[] DIST_BASE  = {1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577};
  static int[] DIST_EXTRA = {0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13};

  static class Match {
    int length; // return 0 if no match is found
    int distance;
  }

  // or 1 << 15, 2^15 32KiB
  static final int TABLE_SIZE = 0x8000;

  static int hash(byte[] data, int i) {
    return ((data[i] & 0xFF) << 10 ^ (data[i+1] & 0xFF) << 5 ^ (data[i+2] & 0xFF)) & (TABLE_SIZE - 1);
  }

  static void insert(byte[] data, int[] head, int[] prev, int pos) {
      if (pos + 2 >= data.length) return;  // no valid 3-byte window here
      int h = hash(data, pos);
      prev[pos] = head[h];
      head[h] = pos;
  }

  // Grouping the same bit/char together in the last 32KiB
  // Only >= 3 is grouped otherwise dropped!
  // THIS IS THE COMPARER OF THE BUILT TABLE
  static Match findMatch(byte[] data, int i, int[] head, int[] prev) {
    final int MAX_CHAIN = 258;

    int bestLength = 0;
    int bestDistance = 0;

    if (i+2 < data.length) {
      int candidate = head[hash(data, i)]; // cadidate is the head most seen value
      int tries = 0;

      while (candidate != -1 && tries < MAX_CHAIN) {
        if (i - candidate > 0x8000) {
          break;
        }

        int k = 0;
        while ((i + k < data.length && candidate + k < data.length && k < MAX_CHAIN) && data[candidate + k] == data[i + k]) {
          k += 1;
        }

        if (k > bestLength) {
          bestLength = k;
          bestDistance = i - candidate;
        }

        candidate = prev[candidate]; // go to that last seen
        tries += 1;
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
}

