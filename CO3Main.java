import java.util.*;
import java.util.concurrent.*;

class WithdrawalException extends Exception {
    WithdrawalException(String message) { super(message); }
}
class ATM {
    private int balance = 5000;
    synchronized void withdraw(int amount) throws WithdrawalException {
        if (amount <= 0) throw new WithdrawalException("Amount must be positive");
        if (amount > balance) throw new WithdrawalException("Insufficient balance");
        balance -= amount;
        System.out.println("Withdrawal successful: Rs." + amount);
        System.out.println("Remaining balance: Rs." + balance);
    }
}
class SeatBooking {
    private final boolean[] seats = new boolean[3];
    private int sold = 0;
    synchronized boolean book(int seat) {
        if (seats[seat]) return false;
        seats[seat] = true; sold++; return true;
    }
    synchronized int getSold() { return sold; }
}
class TokenCounter {
    private final Queue<Integer> queue = new ArrayDeque<>();
    private final int capacity = 3;
    synchronized void put(int value) throws InterruptedException {
        while (queue.size() == capacity) wait();
        queue.add(value); notifyAll();
    }
    synchronized int take() throws InterruptedException {
        while (queue.isEmpty()) wait();
        int value = queue.remove(); notifyAll(); return value;
    }
}
public class CO3Main {
    public static void main(String[] args) throws Exception {
        ATM atm = new ATM();
        try { atm.withdraw(1200); atm.withdraw(6000); }
        catch (WithdrawalException e) { System.out.println("ATM error: " + e.getMessage()); }
        finally { System.out.println("ATM transaction finished."); }

        SeatBooking booking = new SeatBooking();
        Thread[] users = new Thread[10];
        for (int i = 0; i < users.length; i++) {
            final int seat = i % 3;
            users[i] = new Thread(() -> booking.book(seat)); users[i].start();
        }
        for (Thread user : users) user.join();
        System.out.println("Seats sold (3 seats available): " + booking.getSold());

        TokenCounter counter = new TokenCounter();
        int[] sum = {0};
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) counter.put(i); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread consumer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) sum[0] += counter.take(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start(); consumer.start(); producer.join(); consumer.join();
        System.out.println("Producer-consumer total: " + sum[0]);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int score : new int[] {75, 80, 95}) {
                final int value = score; results.add(pool.submit(() -> value + 5));
            }
            int total = 0; for (Future<Integer> result : results) total += result.get();
            System.out.println("Processed result total: " + total);
        } finally { pool.shutdown(); }
    }
}