package unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import think.ints.IntList;

public final class Ints {

    @Test
    public void intList() {
        final IntList list = new IntList(0);
        Assertions.assertEquals(0, list.size());

        list.add(99);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(99, list.get(0));
        Assertions.assertEquals(0, list.indexOf(99));
        Assertions.assertEquals(IntList.NOT_FOUND, list.indexOf(0));
        final int[] total = { 0 };
        list.forEach(element -> total[0] += element);
        Assertions.assertEquals(99, total[0]);

        list.clear();
        for (int index = 0; index < 10; index += 1) {
            list.add(index);
            list.add(index);
            list.removeAt(0);
            Assertions.assertEquals(index + 1, list.size());
            Assertions.assertEquals(index, list.get(index));
        }
    }
}
