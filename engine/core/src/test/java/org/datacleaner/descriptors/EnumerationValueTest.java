package org.datacleaner.descriptors;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class EnumerationValueTest {
    enum TestEnum {
        a, b
    }
    
    @Test
    public void testFromArray() {
        assertArrayEquals(new EnumerationValue[] { new EnumerationValue(TestEnum.a), new EnumerationValue(TestEnum.b) },
                EnumerationValue.fromArray(new TestEnum[] { TestEnum.a, TestEnum.b }));

        assertArrayEquals(new EnumerationValue[] { new EnumerationValue(TestEnum.a), null },
                EnumerationValue.fromArray(new TestEnum[] { TestEnum.a, null }));
    }
}
