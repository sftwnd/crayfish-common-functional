package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.functional.Supplyable.cast;
import static com.github.sftwnd.crayfish.common.functional.Supplyable.supplyable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupplyableTest {

    @Test
    void getResultTest() {
        Object value = new Object();
        assertSame(value, supplyable(() -> value).get(), "Supplyable.processable.get return wrong result");
    }

    @Test
    void getDoesNotThrowTest() {
        Object value = new Object();
        assertDoesNotThrow(supplyable(() -> value)::get, "Supplyable.get hasn't got to throw exception");
    }

    @Test
    void getThrowTest() {
        Supplyable<?> supplyable = (Supplyable<Object>) () -> { throw new IllegalStateException(); };
        assertThrows(IllegalStateException.class, supplyable::get, "Supplyable.get does not throw right exception");
    }

    @Test
    void callResultTest() throws Exception {
        Object value = new Object();
        assertSame(value, supplyable(() -> value).call(), "Supplyable.processable.call return wrong result");
    }

    @Test
    void callDoesNotThrowTest() {
        Object value = new Object();
        assertDoesNotThrow(supplyable(() -> value)::call, "Supplyable.call hasn't got to throw exception");
    }

    @Test
    void callThrowTest() {
        Supplyable<?> supplyable = (Supplyable<Object>) () -> { throw new IllegalStateException(); };
        assertThrows(IllegalStateException.class, supplyable::call, "Supplyable.call does not throw right exception");
    }

    @Test
    void processableDoesNotThrowTest() {
        assertDoesNotThrow(() -> mock(Supplyable.class).processable(), "Supplyable.processable hasn't got to throw exception");
    }

    @Test
    void processableNotNullTest() {
        assertNotNull(supplyable(Object::new).processable(), "Supplyable.processable hasn't got to return null");
    }

    @Test
    void processableTest() throws Exception {
        Supplyable<Object> supplyable = spy(new TestSupplyable<>(null));
        assertDoesNotThrow(() -> supplyable.processable().run(), "Supplyable.processable.get() hasn't got to throw exception");
        verify(supplyable, times(1)).call();
    }

    @Test
    void staticSupplyableDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> supplyable(Object::new), "Supplyable.supplyable unable to create Supplyable from real Supplier");
    }

    @Test
    void staticSupplyableCallSupplierMethodTest() {
        String somePath = "somePath";
        File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn(somePath);
        Supplyable<String> supplyable = supplyable(file::getAbsolutePath);
        assertSame(somePath, supplyable.get(), "Supplyable.get() has to return same value as supplied method");
        verify(file, times(1)).getAbsolutePath();
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(Object::new), "Supplyable.functional unable to create Supplyable from real Supplier");
    }

    @Test
    void staticCastCallSupplierMethodTest() {
        Object value = new Object();
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(value);
        Supplyable<?> supplyable = cast(supplier);
        assertSame(value, supplyable.get(), "Supplyable.get() has to return same value with Supplier.get()");
        verify(supplier, times(1)).get();
    }

    static class TestSupplyable<T> implements Supplyable<T> {

        private final T value;

        public TestSupplyable(T value) {
            this.value = value;
        }

        @Override
        public T call() {
            return this.value;
        }

    }
}