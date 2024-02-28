package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static com.github.sftwnd.crayfish.common.functional.BiConsumable.biconsumable;
import static com.github.sftwnd.crayfish.common.functional.BiConsumable.cast;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BiConsumableTest {

    @Test
    void processTest() {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.process(left, right), "BiConsumable.process throws Exception");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void acceptTest() {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.accept(left, right), "BiConsumable.accept throws Exception");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void callTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.call(left, right), "BiConsumable.call throws Exception");
        verify(biconsumer, times(1)).accept(left, right);
        assertNull( consumable.call(left, right), "BiConsumable.call has to return null");
    }

    @Test
    void functionallyTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> { Boolean ignore = consumable.functionally(left, right); },
                "BiConsumable.functionally throws Exception");
        verify(biconsumer, times(1)).accept(left, right);
        assertNull( consumable.functionally(left, right), "BiConsumable.functionally has to return null");
    }

    @Test
    void functionalTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.functional(), "BiConsumable.functional throws Exception");
        assertNull(consumable.functional().execute(left, right), "BiConsumable.functional.execute has to return null");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void functionalClassTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.functional(Void.class), "BiConsumable.functional(Class) throws Exception");
        assertNull(consumable.functional(Void.class).execute(left, right), "BiConsumable.functional(Class).execute has to return null");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void processableTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.processable(left, right), "BiConsumable.processable(left, right) throws Exception");
        Processable processable = consumable.processable(left, right);
        assertNull(processable.call(), "BiConsumable.processable(left, right).call has to return null");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void leftTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.left(left), "BiConsumable.left(left) throws Exception");
        Consumable<Object> functional = consumable.left(left);
        assertNull(functional.call(right), "BiConsumable.left(left).call(right) has to return null");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void rightTest() throws Exception {
        var consumable = biconsumable(this.biconsumer::accept);
        assertDoesNotThrow(() -> consumable.right(right), "BiConsumable.right(right) throws Exception");
        Consumable<Object> functional = consumable.right(right);
        assertNull(functional.call(left), "BiConsumable.right(right).call(left) has to return null");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(biconsumer), "BiConsumable.cast unable to create Consumable from real Consumer");
    }

    @Test
    void staticCastTest() {
        assertDoesNotThrow(() -> cast(biconsumer), "BiConsumable.cast unable to create Consumable from real Consumer");
        assertNotNull(cast(biconsumer), "BiConsumable.cast return null");
        assertDoesNotThrow(() -> cast(biconsumer).accept(left, right), "BiConsumable.cast.accept throws Exception");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void staticConsumableTest() {
        assertDoesNotThrow(() -> biconsumable(biconsumer::accept), "BiBiConsumable.biconsumable unable to create Consumable from real Consumable method");
        assertNotNull(biconsumable(biconsumer::accept), "BiBiConsumable.biconsumable return null");
        assertDoesNotThrow(() -> biconsumable(biconsumer::accept).accept(left, right), "BiBiConsumable.biconsumable.accept throws Exception");
        verify(biconsumer, times(1)).accept(left, right);
    }


    @Test
    void completableTest() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        BiConsumable<Object, Object> biconsumable = biconsumable(biconsumer::accept).completable(completableFuture);
        new Thread(biconsumable.processable(left, right)).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertDoesNotThrow(() -> completableFuture.get(), "CompletableFuture was completed exceptionally");
        assertNull(completableFuture.get(), "CompletableFuture has wrong result");
        verify(biconsumer, times(1)).accept(left, right);
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        BiConsumable<Object, Object> biconsumable = biconsumable((ignoreLeft, ignoreRight) -> { throw new IllegalStateException(); }).completable(completableFuture);
        new Thread(biconsumable.processable(left, right)).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertThrows(ExecutionException.class, completableFuture::get, "CompletableFuture has to be completed exceptionally");
        try {
            completableFuture.get();
        } catch (ExecutionException eex) {
            assertEquals(IllegalStateException.class, eex.getCause().getClass(), "CompletableFuture has to be completed exceptionally: IllegalStateException");
        }
    }
    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.left = mock(Object.class);
        this.right = mock(Object.class);
        this.biconsumer = mock(BiConsumer.class);
    }

    private Object left;
    private Object right;
    private BiConsumer<Object, Object> biconsumer;

}
