package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.functional.Supplyable.cast;
import static com.github.sftwnd.crayfish.common.functional.Supplyable.supplyable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

class SupplyableTest {

    @Test
    void getResultTest() {
        assertSame(result, supplyable(this.supplier::get).get(), "Supplyable.processable.get return wrong result");
    }

    @Test
    void getDoesNotThrowTest() {
        assertDoesNotThrow(supplyable(this.supplier::get)::get, "Supplyable.get hasn't got to throw exception");
    }

    @Test
    void getThrowTest() {
        Supplyable<?> supplyable = (Supplyable<Object>) () -> { throw new IllegalStateException(); };
        assertThrows(IllegalStateException.class, supplyable::get, "Supplyable.get does not throw right exception");
    }

    @Test
    void callResultTest() throws Exception {
        assertSame(result, supplyable(this.supplier::get).call(), "Supplyable.processable.call return wrong result");
    }

    @Test
    void callDoesNotThrowTest() {
        assertDoesNotThrow(supplyable(this.supplier::get)::call, "Supplyable.call hasn't got to throw exception");
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
        assertNotNull(supplyable(this.supplier::get).processable(), "Supplyable.processable hasn't got to return null");
    }

    @Test
    void processableTest() {
        var supplyable = supplyable(this.supplier::get);
        assertDoesNotThrow(() -> supplyable.processable().run(), "Supplyable.processable.get() hasn't got to throw exception");
        verify(supplier, times(1)).get();
    }

    @Test
    void furtherRunTest() {
        var runnable = mock(Runnable.class);
        doNothing().when(runnable).run();
        verify(runnable, never()).run();
        assertSame(this.result, supplier.furtherRun(runnable::run).get(), "Supplyable::furtherRun(processable) has to return right result");
        verify(runnable, times(1)).run();
    }

    @Test
    @SuppressWarnings("unchecked")
    void furtherAcceptTest() {
        var consumable = mock(Consumable.class);
        doNothing().when(consumable).accept(any());
        verify(consumable, never()).accept(any());
        assertSame(this.result, supplier.furtherAccept(consumable::accept).get(), "Supplyable::furtherAccept(consumable) has to return right result");
        verify(consumable, times(1)).accept(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void furtherApplyTest() {
        Integer randomValue = new Random().nextInt();
        Function<Object, Integer> function = mock(Function.class);
        when(function.apply(result)).thenReturn(randomValue);
        verify(function, never()).apply(any());
        assertSame(randomValue, this.supplier.furtherApply(function::apply).get(), "Supplyable::andThen(functional) has to return right result");
        verify(function, times(1)).apply(result);
    }

    @Test
    void previouslyTest() {
        var mock = mock(Runnable.class);
        doNothing().when(mock).run();
        Runnable runnable = () -> {
            verify(this.supplier, never()).get();
            mock.run();
        };
        verify(mock, never()).run();
        assertSame(this.result, supplier.previously(runnable::run).get(), "Supplyable::previously has to return right result");
        verify(mock, times(1)).run();
    }

    @Test
    void staticSupplyableDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> supplyable(this.supplier::get), "Supplyable.supplyable unable to create Supplyable from real Supplier");
    }

    @Test
    void staticSupplyableCallSupplierMethodTest() {
        assertSame(result, supplyable(this.supplier::get).get(), "Supplyable.get() has to return same value as supplied method");
        verify(this.supplier, times(1)).get();
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(Object::new), "Supplyable.functional unable to create Supplyable from real Supplier");
    }

    @Test
    void staticCastCallSupplierMethodTest() {
        assertDoesNotThrow(() -> cast(this.supplier), "Supplyable.case throws Exception on non null method");
        assertNotNull(cast(this.supplier), "Supplyable.case return null value");
        assertSame(result, cast(this.supplier).get(), "Supplyable.cas.get() has to return same value with Supplier.get()");
        verify(supplier, times(1)).get();
    }

    @Test
    @SuppressWarnings("unchecked")
    void completableTest() throws ExecutionException, InterruptedException {
        var result = mock();
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        Supplier<Object> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(result);
        Processable processable = supplyable(supplier::get).completable(completableFuture);
        new Thread(processable).start();
        assertDoesNotThrow(() -> cdl.await(150, TimeUnit.MILLISECONDS), "CompletableFuture is not Done");
        assertEquals(result, completableFuture.get(), "CompletableFuture has wrong result");
        verify(supplier, times(1)).get();
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        Runnable runnable = supplyable(() -> {
            try { throw new IllegalStateException(); } finally { cdl.countDown();}
        }).completable(completableFuture);
        new Thread(runnable).start();
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
        supplyable(this.supplier::get).completable(completableFuture).run();
        verify(this.supplier, never()).get();
    }

    @BeforeEach
    void startUp() {
        this.result = mock();
        this.supplier = spy(new SupplyableImpl());
    }

    private Object result;
    private Supplyable<Object> supplier;

    class SupplyableImpl implements Supplyable<Object> {
        @Override
        public Object call() {
            return result;
        }
    }

}