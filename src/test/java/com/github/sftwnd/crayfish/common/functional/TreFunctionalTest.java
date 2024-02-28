package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.functional.TreFunctional.trefunctional;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
    void furtherProcessableTest() {
        var runnable = mock(Runnable.class);
        doNothing().when(runnable).run();
        verify(runnable, never()).run();
        assertSame(this.result, trefunction.further(runnable::run).apply(left, middle, right), "Functional::further(processable) has to return right result");
        verify(runnable, times(1)).run();
    }

    @Test
    @SuppressWarnings("unchecked")
    void furtherTest() {
        var consumable = mock(Consumable.class);
        doNothing().when(consumable).accept(any());
        verify(consumable, never()).accept(any());
        assertSame(this.result, trefunction.further(consumable::accept).apply(left, middle, right), "Functional::further(consumable) has to return right result");
        verify(consumable, times(1)).accept(result);
    }

    @Test
    void previouslyTest() {
        var mock = mock(Runnable.class);
        doNothing().when(mock).run();
        Runnable runnable = () -> {
            verify(this.trefunction, never()).apply(any(), any(), any());
            mock.run();
        };
        verify(mock, never()).run();
        assertSame(this.result, trefunction.previously(runnable::run).apply(left, middle, right), "Functional::previously has to return right result");
        verify(mock, times(1)).run();
    }

    @Test
    void withLeftFunctionalTest() {
        @SuppressWarnings("unchecked")
        Function<Integer, Object> function = mock(Function.class);
        when(function.apply(random)).thenReturn(left);
        verify(function, never()).apply(any());
        assertSame(this.result, trefunction.withLeft(function::apply).apply(random, middle, right), "Functional::withLeft(functional) has to return right result");
        verify(function, times(1)).apply(random);
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void withLeftSupplyableTest() {
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(left);
        verify(supplier, never()).get();
        assertSame(this.result, trefunction.withLeft(supplier::get).apply(middle, right), "Functional::withLeft(supplier) has to return right result");
        verify(supplier, times(1)).get();
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void withMiddleFunctionalTest() {
        @SuppressWarnings("unchecked")
        Function<Integer, Object> function = mock(Function.class);
        when(function.apply(random)).thenReturn(middle);
        verify(function, never()).apply(any());
        assertSame(this.result, trefunction.withMiddle(function::apply).apply(left, random, right), "Functional::withMiddle(functional) has to return right result");
        verify(function, times(1)).apply(random);
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void withMiddleSupplyableTest() {
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(middle);
        verify(supplier, never()).get();
        assertSame(this.result, trefunction.withMiddle(supplier::get).apply(left, right), "Functional::withMiddle(supplier) has to return right result");
        verify(supplier, times(1)).get();
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void withRightFunctionalTest() {
        @SuppressWarnings("unchecked")
        Function<Integer, Object> function = mock(Function.class);
        when(function.apply(random)).thenReturn(right);
        verify(function, never()).apply(any());
        assertSame(this.result, trefunction.withRight(function::apply).apply(left, middle, random), "Functional::withRight(functional) has to return right result");
        verify(function, times(1)).apply(random);
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void withRightSupplyableTest() {
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(right);
        verify(supplier, never()).get();
        assertSame(this.result, trefunction.withRight(supplier::get).apply(left, middle), "Functional::withRight(supplier) has to return right result");
        verify(supplier, times(1)).get();
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
        assertDoesNotThrow(() -> cdl.await(150, TimeUnit.MILLISECONDS), "CompletableFuture is not Done");
        assertEquals(result, completableFuture.get(), "CompletableFuture has wrong result");
        verify(trefunction, times(1)).apply(left, middle, right);
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        TreConsumable<Object, Object, Object> consumable = trefunctional((ignoreLeft, ignoreMiddle, ignoreRight) -> {
            try { throw new IllegalStateException(); } finally { cdl.countDown();}
        }).completable(completableFuture);
        new Thread(consumable.processable(left, middle, right)).start();
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
        trefunctional(this.trefunction::apply).completable(completableFuture).accept(left, middle, right);
        verify(this.trefunction, never()).apply(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void andThenTest() {
        Function<Object, Integer> function = mock(Function.class);
        when(function.apply(result)).thenReturn(random);
        verify(function, never()).apply(any());
        assertSame(random, trefunction.andThen(function).apply(left, middle, right), "Functional::andThen(functional) has to return right result");
        verify(function, times(1)).apply(result);
    }

    @BeforeEach
    void startUp() {
        this.left = mock();
        this.right = mock();
        this.middle = mock();
        this.result = mock();
        this.trefunction = spy(new TreFunctionalImpl());
        this.random = new Random().nextInt();
    }

    private Object left;
    private Object middle;
    private Object right;
    private Object result;
    private Integer random;
    private TreFunctional<Object, Object, Object, Object> trefunction;

    class TreFunctionalImpl implements TreFunctional<Object, Object, Object, Object> {
        @Override
        public Object execute(Object left, Object middle, Object right) {
            return result;
        }
    }

}
