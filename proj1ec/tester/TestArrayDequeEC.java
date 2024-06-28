package tester;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;
import static org.junit.Assert.*;

public class TestArrayDequeEC {
    public String buildMessage(ArrayDequeSolution<String> messageDeque) {
        StringBuilder message = new StringBuilder();
        for (String messageItem : messageDeque) {
            message.append(messageItem).append("\n");
        }
        return message.toString();
    }

    @Test
    public void RandomTest() {
        StudentArrayDeque<Integer> deque1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> deque2 = new ArrayDequeSolution<>();
        ArrayDequeSolution<String> messageDeque = new ArrayDequeSolution<>();
        int N = 100;
        for (int i = 0; i < N; i++) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addFirst
                int randomItem = StdRandom.uniform(0, 100);
                deque1.addFirst(randomItem);
                deque2.addFirst(randomItem);
                messageDeque.addLast("addFirst(" + randomItem + ")");
                assertEquals( buildMessage(messageDeque),
                        deque1.get(0), deque2.get(0));
            } else if (operationNumber == 1) {
                // addLast
                int randomItem = StdRandom.uniform(0, 100);
                deque1.addLast(randomItem);
                deque2.addLast(randomItem);
                messageDeque.addLast("addLast(" + randomItem + ")");
                assertEquals( buildMessage(messageDeque),
                        deque1.get(deque1.size() - 1), deque2.get(deque1.size() - 1));
            } else if (operationNumber == 2) {
                // removeFirst
                if (deque1.size() > 0 && deque2.size() > 0) {
                    messageDeque.addLast("removeFirst");
                    assertEquals( buildMessage(messageDeque), deque1.removeFirst(), deque2.removeFirst());
                }
            } else {
                // removeLast
                if (deque1.size() > 0 && deque2.size() > 0) {
                    messageDeque.addLast("removeLast");
                    assertEquals( buildMessage(messageDeque), deque1.removeLast(), deque2.removeLast());
                }
            }

        }


    }
}
