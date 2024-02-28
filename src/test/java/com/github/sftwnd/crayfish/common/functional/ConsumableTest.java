package com.github.sftwnd.crayfish.common.functional;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.sftwnd.crayfish.common.functional.Consumable.cast;
import static com.github.sftwnd.crayfish.common.functional.Consumable.consumable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConsumableTest {

    @Test
    void processTest() {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.process(parameter), "Consumable.process throws Exception");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void acceptTest() {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.accept(parameter), "Consumable.accept throws Exception");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void callTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.call(parameter), "Consumable.call throws Exception");
        verify(consumer, times(1)).accept(parameter);
        assertNull( consumable.call(parameter), "Consumable.call has to return null");
    }

    @Test
    void functionallyTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> { Boolean ignore = consumable.functionally(parameter); },
                "Consumable.functionally throws Exception");
        verify(consumer, times(1)).accept(parameter);
        assertNull( consumable.functionally(parameter), "Consumable.functionally has to return null");
    }

    @Test
    void functionalTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.functional(), "Consumable.functional throws Exception");
        assertNull(consumable.functional().execute(parameter), "Consumable.functional.execute has to return null");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void functionalClassTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.functional(Void.class), "Consumable.functional(Class) throws Exception");
        assertNull(consumable.functional(Void.class).execute(parameter), "Consumable.functional(Class).execute has to return null");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void processableTest() throws Exception {
        var consumable = consumable(this.consumer::accept);
        assertDoesNotThrow(() -> consumable.processable(parameter), "Consumable.processable(value throws Exception");
        Processable processable = consumable.processable(parameter);
        assertNull(processable.call(), "Consumable.processable(value).call has to return null");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(consumer), "Consumable.cast unable to create Consumable from real Consumer");
    }

    @Test
    void staticCastTest() {
        assertDoesNotThrow(() -> cast(consumer), "Consumable.cast unable to create Consumable from real Consumer");
        assertNotNull(cast(consumer), "Consumable.cast return null");
        assertDoesNotThrow(() -> cast(consumer).accept(parameter), "Consumable.cast.accept throws Exception");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void staticConsumableTest() {
        assertDoesNotThrow(() -> Consumable.consumable(consumer::accept), "Consumable.consumable unable to create Consumable from real Consumable method");
        assertNotNull(Consumable.consumable(consumer::accept), "Consumable.consumable return null");
        assertDoesNotThrow(() -> consumable(consumer::accept).accept(parameter), "Consumable.consumable.accept throws Exception");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void completableTest() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        Consumable<Object> consumable = consumable(consumer::accept).completable(completableFuture);
        new Thread(consumable.processable(parameter)).start();
        assertDoesNotThrow(() -> cdl.await(150, TimeUnit.MILLISECONDS), "CompletableFuture is not Done");
        assertDoesNotThrow(() -> completableFuture.get(), "CompletableFuture was completed exceptionally");
        assertNull(completableFuture.get(), "CompletableFuture has wrong result");
        verify(consumer, times(1)).accept(parameter);
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        Consumable<Object> consumable = consumable(ignore -> {
            try { throw new IllegalStateException(); } finally { cdl.countDown();}
        }).completable(completableFuture);
        new Thread(consumable.processable(parameter)).start();
        boolean completed = cdl.await(1, TimeUnit.SECONDS);
        assertTrue(completed, "CompletableFuture is not Done");
        assertThrows(ExecutionException.class, completableFuture::get, "CompletableFuture has to be completed exceptionally");
        try {
            completableFuture.get();
        } catch (ExecutionException eex) {
            assertEquals(IllegalStateException.class, eex.getCause().getClass(), "CompletableFuture has to be completed exceptionally: IllegalStateException");
        }
    }

    @Test
    void completableOnCompletedFutureTest() {
        var completableFuture = new CompletableFuture<>();
        completableFuture.complete(null);
        consumable(this.consumer::accept).completable(completableFuture).accept(parameter);
        verify(this.consumer, never()).accept(any());
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.parameter = mock();
        this.consumer = mock(Consumer.class);
    }

    private Object parameter;
    private Consumer<Object> consumer;

}
