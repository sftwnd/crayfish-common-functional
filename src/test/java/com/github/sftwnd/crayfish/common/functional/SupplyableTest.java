package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.functional.Supplyable.cast;
import static com.github.sftwnd.crayfish.common.functional.Supplyable.supplyable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    @SuppressWarnings("unchecked")
    void startUp() {
        this.result = mock();
        this.supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(this.result);
    }

    private Object result;
    private Supplier<Object> supplier;

}