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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BiFunctionalTest {

    @Test
    void applyTest() {
        assertDoesNotThrow(() -> bifunction.apply(left, right), "BiFunctional.apply throws Exception");
        verify(this.bifunction, times(1)).apply(left, right);
        assertSame(result, bifunction.apply(left, right), "BiFunctional.apply has to return wrong result");
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
        verify(this.bifunction, times(1)).apply(left, right);
        assertSame(result, bifunctional.execute(left, right), "BiFunctional.execute has to return wrong result");
    }

    @Test
    void consumableTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(bifunctional::consumable, "BiFunctional.consumable throws Exception");
        assertDoesNotThrow(() -> bifunctional.consumable().call(left, right), "BiFunctional.consumable.call(parameter) throws exception");
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void supplyableTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.supplyable(left, right), "BiFunctional.supplyable throws Exception");
        assertSame(result, bifunctional.supplyable(left, right).get(), "BiFunctional.supplyable(parameter).get() return wrong result");
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void leftTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.left(left), "BiFunctional.left(left) throws Exception");
        var functional = bifunctional.left(left);
        assertSame(result, functional.apply(right), "BiFunctional.left(left).apply(right) has to return right value");
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void rightTest() {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.right(right), "BiFunctional.right(right) throws Exception");
        var functional = bifunctional.right(right);
        assertSame(result, functional.apply(left), "BiFunctional.right(right).apply(left) has to return right value");
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void processableTest() throws Exception {
        var bifunctional = bifunctional(this.bifunction::apply);
        assertDoesNotThrow(() -> bifunctional.processable(left, right), "BiFunctional.processable(left, right) throws Exception");
        Processable processable = bifunctional.processable(left, right);
        assertNull(processable.call(), "BiFunctional.processable(value).call has to return null");
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void furtherRunTest() {
        var runnable = mock(Runnable.class);
        doNothing().when(runnable).run();
        verify(runnable, never()).run();
        assertSame(this.result, bifunction.furtherRun(runnable::run).apply(left, right), "BiFunctional::furtherRun(processable) has to return right result");
        verify(runnable, times(1)).run();
    }

    @Test
    @SuppressWarnings("unchecked")
    void furtherAcceptTest() {
        var consumable = mock(Consumable.class);
        doNothing().when(consumable).accept(any());
        verify(consumable, never()).accept(any());
        assertSame(this.result, bifunction.furtherAccept(consumable::accept).apply(left, right), "BiFunctional::furtherAccept(consumable) has to return right result");
        verify(consumable, times(1)).accept(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void furtherApplyTest() {
        Function<Object, Integer> function = mock(Function.class);
        when(function.apply(result)).thenReturn(randomValue);
        verify(function, never()).apply(any());
        assertSame(randomValue, bifunction.furtherApply(function::apply).apply(left, right), "BiFunctional::andThen(functional) has to return right result");
        verify(function, times(1)).apply(result);
    }

    @Test
    void previouslyTest() {
        var mock = mock(Runnable.class);
        doNothing().when(mock).run();
        Runnable runnable = () -> {
            verify(this.bifunction, never()).apply(any(), any());
            mock.run();
        };
        verify(mock, never()).run();
        assertSame(this.result, bifunction.previously(runnable::run).apply(left,  right), "BiFunctional::previously has to return right result");
        verify(mock, times(1)).run();
    }

    @Test
    void withLeftFunctionalTest() {
        @SuppressWarnings("unchecked")
        Function<Integer, Object> function = mock(Function.class);
        when(function.apply(randomValue)).thenReturn(left);
        verify(function, never()).apply(any());
        assertSame(this.result, bifunction.withLeft(function::apply).apply(randomValue, right), "BiFunctional::withLeft(functional) has to return right result");
        verify(function, times(1)).apply(randomValue);
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void withLeftSupplyableTest() {
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(left);
        verify(supplier, never()).get();
        assertSame(this.result, bifunction.withLeft(supplier::get).apply(right), "BiFunctional::withLeft(supplier) has to return right result");
        verify(supplier, times(1)).get();
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void withRightFunctionalTest() {
        @SuppressWarnings("unchecked")
        Function<Integer, Object> function = mock(Function.class);
        when(function.apply(randomValue)).thenReturn(right);
        verify(function, never()).apply(any());
        assertSame(this.result, bifunction.withRight(function::apply).apply(left, randomValue), "BiFunctional::withRight(functional) has to return right result");
        verify(function, times(1)).apply(randomValue);
        verify(this.bifunction, times(1)).apply(left, right);
    }

    @Test
    void withRightSupplyableTest() {
        @SuppressWarnings("unchecked")
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(right);
        verify(supplier, never()).get();
        assertSame(this.result, bifunction.withRight(supplier::get).apply(left), "BiFunctional::withRight(supplier) has to return right result");
        verify(supplier, times(1)).get();
        verify(this.bifunction, times(1)).apply(left, right);
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
        verify(this.bifunction, times(1)).apply(left, right);
        assertSame(result, bifunctional(bifunction::apply).apply(left, right), "BiFunctional.bifunctional.apply(parameter) return wrong value");
    }

    @Test
    void staticCastTest() {
        assertDoesNotThrow(() -> cast(bifunction), "BiFunctional.cast unable to create BiFunctional from real Consumer");
        assertNotNull(cast(bifunction), "BiFunctional.cast return null");
        assertDoesNotThrow(() -> cast(bifunction).execute(left, right), "BiFunctional.cast.execute throws Exception");
        verify(this.bifunction, times(1)).apply(left, right);
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
        verify(this.bifunction, times(1)).apply(left, right);
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
    void startUp() {
        this.left = mock();
        this.right = mock();
        this.result = mock();
        this.bifunction = spy(new BiFunctionalImpl());
        this.randomValue = random.nextInt();
    }

    private static final Random random = new Random();
    private Object left;
    private Object right;
    private Object result;
    private BiFunctional<Object, Object, Object> bifunction;
    private Integer randomValue;

    class BiFunctionalImpl implements BiFunctional<Object, Object, Object> {
        @Override
        public Object execute(Object left, Object right) {
            return result;
        }
    }

}
