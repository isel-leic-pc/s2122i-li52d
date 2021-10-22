package pc.leic52d;

import org.junit.Test;
import pc.li52d.hazards.Account;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static pc.l52d.threading.Utils.uninterruptibleJoin;

public class HazardTests {
    private static final int NTRANSFERS = 100000;

    private void transfers(Account[] accounts, int idx) {
        int srcIdx = idx;
        int dstIdx = (1 + srcIdx) % 2;
        Account src = accounts[srcIdx];
        Account dst = accounts[dstIdx];
        for(int i=0; i < NTRANSFERS; ++i) {

            //System.out.printf("start transfer from %d to %d\n", srcIdx, dstIdx);
            src.transferTo1(dst, 10);
            //System.out.printf("end transfer from %d to %d\n", srcIdx, dstIdx);
        }
    }

    @Test
    public  void multiple_account_transfers_test() {
        Account[] accounts = {
            new Account(1000),
            new Account(1000)
        };

        List<Thread> threads =
            IntStream.rangeClosed(0,1)
                .mapToObj(i -> new Thread(() -> transfers(accounts, i)))
                .map(t -> {t.start(); return t; })
                .collect(toList());

        threads.forEach(t -> uninterruptibleJoin(t));

        assertEquals(2000, accounts[0].getBalance() + accounts[1].getBalance());
        System.out.println("Done!");

    }

}
