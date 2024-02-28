package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.github.sftwnd.crayfish.common.functional.BiFunctional.bifunctional;
import static com.github.sftwnd.crayfish.common.functional.BiFunctional.cast;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class BiFunctionalTest {

    @Test
    void applyTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.apply(left, right), "BiFunctional.apply throws Exception");
        verify(bifunction, times(1)).apply(left, right);
        assertSame(result, bifunctional.apply(left, right), "BiFunctional.apply has to return wrong result");
    }

    @Test
    void applyRethrowTest() {
        var bifunctional = bifunctional((ignoreLeft, ignoreRight) -> { throw new IOException(); });
        assertThrows(IOException.class, () -> bifunctional.apply(left, right), "BiFunctional.apply(parameter) has to throw right exception");
    }

    @Test
    void executeTest() throws Exception {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.execute(left, right), "BiFunctional.execute throws Exception");
        verify(bifunction, times(1)).apply(left, right);
        assertSame(result, bifunctional.execute(left, right), "BiFunctional.execute has to return wrong result");
    }

    @Test
    void consumableTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(bifunctional::consumable, "BiFunctional.consumable throws Exception");
        assertDoesNotThrow(() -> bifunctional.consumable().call(left, right), "BiFunctional.consumable.call(parameter) throws exception");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void supplyableTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.supplyable(left, right), "BiFunctional.supplyable throws Exception");
        assertSame(result, bifunctional.supplyable(left, right).get(), "BiFunctional.supplyable(parameter).get() return wrong result");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void leftTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.left(left), "BiFunctional.left(left) throws Exception");
        var functional = bifunctional.left(left);
        assertSame(result, functional.apply(right), "BiFunctional.left(left).apply(right) has to return right value");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void rightTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.right(right), "BiFunctional.right(right) throws Exception");
        var functional = bifunctional.right(right);
        assertSame(result, functional.apply(left), "BiFunctional.right(right).apply(left) has to return right value");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void processableTest() throws Exception {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.processable(left, right), "BiFunctional.processable(left, right) throws Exception");
        Processable processable = bifunctional.processable(left, right);
        assertNull(processable.call(), "BiFunctional.processable(value).call has to return null");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(bifunction), "BiFunctional.cast unable to create BiFunctional from real Consumer");
    }

    @Test
    void staticBiFunctionalTest() {
        assertDoesNotThrow(() -> bifunctional(bifunction::apply), "BiFunctional.bifunctional unable to create BiFunctional from real BiFunctional method");
        assertNotNull(bifunctional(bifunction::apply), "BiFunctional.bifunctional return null");
        assertDoesNotThrow(() -> bifunctional(bifunction::apply).apply(left, right), "BiFunctional.bifunctional.accept throws Exception");
        verify(bifunction, times(1)).apply(left, right);
        assertSame(result, bifunctional(bifunction::apply).apply(left, right), "BiFunctional.bifunctional.apply(parameter) return wrong value");
    }

    @Test
    void staticCastTest() {
        assertDoesNotThrow(() -> cast(bifunction), "BiFunctional.cast unable to create BiFunctional from real Consumer");
        assertNotNull(cast(bifunction), "BiFunctional.cast return null");
        assertDoesNotThrow(() -> cast(bifunction).execute(left, right), "BiFunctional.cast.execute throws Exception");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void completableTest() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        BiConsumable<Object, Object> consumable = bifunctional(bifunction::apply).completable(completableFuture);
        new Thread(consumable.processable(left, right)).start();
        assertDoesNotThrow(() -> cdl.await(150, TimeUnit.MILLISECONDS), "CompletableFuture is not Done");
        assertEquals(result, completableFuture.get(), "CompletableFuture has wrong result");
        verify(bifunction, times(1)).apply(left, right);
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        BiConsumable<Object, Object> consumable = bifunctional((ignoreLeft, ignoreRight) -> {
            try { throw new IllegalStateException(); } finally { cdl.countDown();}
        }).completable(completableFuture);
        new Thread(consumable.processable(left, right)).start();
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
        bifunctional(this.bifunction::apply).completable(completableFuture).accept(left, right);
        verify(this.bifunction, never()).apply(any(), any());
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.left = mock();
        this.right = mock();
        this.result = mock();
        this.bifunction = mock(BiFunction.class);
        when(bifunction.apply(this.left, this.right)).thenReturn(this.result);
    }

    private Object left;
    private Object right;
    private Object result;
    private BiFunction<Object, Object, Object> bifunction;

}
