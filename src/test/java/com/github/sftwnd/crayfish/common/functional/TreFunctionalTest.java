package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.sftwnd.crayfish.common.functional.TreFunctional.trefunctional;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TreFunctionalTest {

    @Test
    void applyTest() {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.apply(left, middle, right), "TreFunctional.apply throws Exception");
        verify(trefunction, times(1)).apply(left, middle, right);
        assertSame(result, trefunctional.apply(left, middle, right), "TreFunctional.apply has to return wrong result");
    }

    @Test
    void applyRethrowTest() {
        var trefunctional = trefunctional((ignoreLeft, ignoreMiddle, ignoreRight) -> { throw new IOException(); });
        assertThrows(IOException.class, () -> trefunctional.apply(left, middle, right), "TreFunctional.apply(parameter) has to throw right exception");
    }

    @Test
    void executeTest() throws Exception {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.execute(left, middle, right), "TreFunctional.execute throws Exception");
        verify(trefunction, times(1)).apply(left, middle, right);
        assertSame(result, trefunctional.execute(left, middle, right), "TreFunctional.execute has to return wrong result");
    }

    @Test
    void consumableTest() {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(trefunctional::consumable, "TreFunctional.consumable throws Exception");
        assertDoesNotThrow(() -> trefunctional.consumable().call(left, middle, right), "TreFunctional.consumable.call(parameter) throws exception");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void supplyableTest() {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.supplyable(left, middle, right), "TreFunctional.supplyable throws Exception");
        assertSame(result, trefunctional.supplyable(left, middle, right).get(), "TreFunctional.supplyable(parameter).get() return wrong result");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void leftTest() {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.left(left), "TreFunctional.left(left) throws Exception");
        var bifunctional = trefunctional.left(left);
        assertSame(result, bifunctional.apply(middle, right), "TreFunctional.left(left).apply(middle, right) has to return right value");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void middleTest() {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.middle(middle), "TreFunctional.middle(middle) throws Exception");
        var bifunctional = trefunctional.middle(middle);
        assertSame(result, bifunctional.apply(left, right), "TreFunctional.middle(middle).apply(left, right) has to return right value");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void rightTest() {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.right(right), "TreFunctional.right(right) throws Exception");
        var bifunctional = trefunctional.right(right);
        assertSame(result, bifunctional.apply(left, middle), "TreFunctional.right(right).apply(left, middle) has to return right value");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void processableTest() throws Exception {
        var trefunctional = trefunctional(this.trefunction::apply);
        assertDoesNotThrow(() -> trefunctional.processable(left, middle, right), "TreFunctional.processable(left, middle, right) throws Exception");
        Processable processable = trefunctional.processable(left, middle, right);
        assertNull(processable.call(), "TreFunctional.processable(value).call has to return null");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void staticTreFunctionalTest() {
        assertDoesNotThrow(() -> trefunctional(trefunction::apply), "TreFunctional.trefunctional unable to create TreFunctional from real TreFunctional method");
        assertNotNull(trefunctional(trefunction::apply), "TreFunctional.trefunctional return null");
        assertDoesNotThrow(() -> trefunctional(trefunction::apply).apply(left, middle, right), "TreFunctional.trefunctional.accept throws Exception");
        verify(trefunction, times(1)).apply(left, middle, right);
        assertSame(result, trefunctional(trefunction::apply).apply(left, middle, right), "TreFunctional.trefunctional.apply(parameter) return wrong value");
    }

    @Test
    void completableTest() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        TreConsumable<Object, Object, Object> consumable = trefunctional(trefunction::apply).completable(completableFuture);
        new Thread(consumable.processable(left, middle, right)).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertEquals(result, completableFuture.get(), "CompletableFuture has wrong result");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        TreConsumable<Object, Object, Object> consumable = trefunctional((ignoreLeft, ignoreMiddle, ignoreRight) -> { throw new IllegalStateException(); }).completable(completableFuture);
        new Thread(consumable.processable(left, middle, right)).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertThrows(ExecutionException.class, completableFuture::get, "CompletableFuture has to be completed exceptionally");
        try {
            completableFuture.get();
        } catch (ExecutionException eex) {
            assertEquals(IllegalStateException.class, eex.getCause().getClass(), "CompletableFuture has to be completed exceptionally: IllegalStateException");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void andThenTest() {
        var trefunctional = trefunctional(trefunction::apply);
        Function<Object, Object> function = mock(Function.class);
        Object functionResult = mock(Object.class);
        when(function.apply(result)).thenReturn(functionResult);
        assertDoesNotThrow(() -> trefunctional.andThen(function).apply(left, middle, right), "TreFunctional.andThen(function).apply(l, rm, r) throws exception");
        verify(function, times(1)).apply(result);
        assertSame(functionResult, trefunctional.andThen(function).apply(left, middle, right), "TreFunctional::andThen has to return right value");
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.left = mock(Object.class);
        this.right = mock(Object.class);
        this.middle = mock(Object.class);
        this.result = mock(Object.class);
        this.trefunction = mock(TreFunctional.class);
        when(trefunction.apply(this.left, this.middle, this.right)).thenReturn(this.result);
    }

    private Object left;
    private Object middle;
    private Object right;
    private Object result;
    private TreFunctional<Object, Object, Object, Object> trefunction;

}
