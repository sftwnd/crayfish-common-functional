package com.github.sftwnd.crayfish.common.functional;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static com.github.sftwnd.crayfish.common.functional.Consumable.cast;
import static com.github.sftwnd.crayfish.common.functional.Consumable.consumable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConsumableTest {

    @Test
    void processTest() {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.process(value), "Consumable.process throws Exception");
        verify(consumer, times(1)).accept(value);
    }

    @Test
    void acceptTest() {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.accept(value), "Consumable.accept throws Exception");
        verify(consumer, times(1)).accept(value);
    }

    @Test
    void callTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.call(value), "Consumable.call throws Exception");
        verify(consumer, times(1)).accept(value);
        assertNull( consumable.call(value), "Consumable.call has to return null");
    }

    @Test
    void functionallyTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> { Boolean ignore = consumable.functionally(value); },
                "Consumable.functionally throws Exception");
        verify(consumer, times(1)).accept(value);
        assertNull( consumable.functionally(value), "Consumable.functionally has to return null");
    }

    @Test
    void functionalTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.functional(), "Consumable.functional throws Exception");
        assertNull(consumable.functional().execute(value), "Consumable.functional.execute has to return null");
        verify(consumer, times(1)).accept(value);
    }

    @Test
    void functionalClassTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.functional(Void.class), "Consumable.functional(Class) throws Exception");
        assertNull(consumable.functional(Void.class).execute(value), "Consumable.functional(Class).execute has to return null");
        verify(consumer, times(1)).accept(value);
    }

    @Test
    void processableTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.processable(value), "Consumable.processable(value throws Exception");
        Processable processable = consumable.processable(value);
        assertNull(processable.call(), "Consumable.processable(value).call has to return null");
        verify(consumer, times(1)).accept(value);
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(consumer), "Consumable.cast unable to create Consumable from real Consumer");
    }

    @Test
    void staticCastTest() {
        assertDoesNotThrow(() -> cast(consumer), "Consumable.cast unable to create Consumable from real Consumer");
        assertNotNull(cast(consumer), "Consumable.cast return null");
        assertDoesNotThrow(() -> cast(consumer).accept(value), "Consumable.cast.accept throws Exception");
        verify(consumer, times(1)).accept(value);
    }

    @Test
    void staticConsumableTest() {
        assertDoesNotThrow(() -> Consumable.consumable(consumer::accept), "Consumable.consumable unable to create Consumable from real Consumable method");
        assertNotNull(Consumable.consumable(consumer::accept), "Consumable.consumable return null");
        assertDoesNotThrow(() -> consumable(consumer::accept).accept(value), "Consumable.consumable.accept throws Exception");
        verify(consumer, times(1)).accept(value);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.value = mock(Object.class);
        this.consumer = mock(Consumer.class);
    }

    private Object value;
    private Consumer<Object> consumer;

}
