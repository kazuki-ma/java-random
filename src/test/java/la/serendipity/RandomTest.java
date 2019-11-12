package la.serendipity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

public class RandomTest {
    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;

    @Test
    public void test(){
        // Imagine you are using non secure random.
        final Random nonSecureRandom = new Random();

        // And generate nextInt() to assign secret hash to users or contents etc...
        final int assignedRandom = nonSecureRandom.nextInt();

        // If attacker got weak hash, he can assume internal state.
        List<Integer> nextCandidate = new ArrayList<>();
        for (int i = 0; i < (1 << 16); ++i) { // Bluet force attack. Search space is TOO small.
            long seedCandidate = ((((long)assignedRandom) << 16) + i) & mask;
            nextCandidate.add((int)((seedCandidate * multiplier + addend) >> 16 & mask)); // Random's implementation.
        }

        // Attacker can verify actual internal state if they got next generated value.
        // * It's not required (because saving 1 << 16 is not heavy task).
        // * It's VERY EASY if you using nextLong() and sent it to user. It's just `nextInt() << 32 + nextInt()`.
        final int nextRandom = nonSecureRandom.nextInt();
        System.out.println(nextCandidate.contains(nextRandom));

        // We got internal state of Random.
        long brokenSeed = (Integer.toUnsignedLong(assignedRandom) << 16) + nextCandidate.indexOf(nextRandom);
        // Now we recovered same Random.
        final Random reconstructedRandom = new Random(brokenSeed ^ multiplier); //  ^ multiplier = invert initial scramble.
        reconstructedRandom.nextInt(); // Skip 1st (already guessed.)

        System.out.println("Let's output our reconstructed random.");
        for (int i = 0; i < 10; i++) {
            System.out.println(reconstructedRandom.nextInt());
        }

        System.out.println("Original random's next number.");
        for (int i = 0; i < 10; i++) {
            System.out.println(nonSecureRandom.nextInt());
        }
    }
}
