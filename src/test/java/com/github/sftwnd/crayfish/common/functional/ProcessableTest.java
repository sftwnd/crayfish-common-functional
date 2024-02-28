package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.sftwnd.crayfish.common.functional.Processable.cast;
import static com.github.sftwnd.crayfish.common.functional.Processable.processable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ProcessableTest {

    @Test
    void processTest() {
        assertDoesNotThrow(() -> processable.process(), "processable.process() throws exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void processThrowsTest() {
        assertThrows(IllegalStateException.class, processable(() -> { throw new IllegalStateException(); })::process, "processable.process() has to throws right exception");
        verify(runnable, times(0)).run();
    }

    @Test
    void runTest() {
        assertDoesNotThrow(() -> processable.run(), "processable.run() throws exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void callTest() throws Exception {
        assertDoesNotThrow(() -> processable.call(), "processable.call() throws exception");
        assertNull(processable.call(), "processable.call() has to return null");
        verify(runnable, times(2)).run();
    }

    @Test
    void suppliedTest() throws Exception {
        assertDoesNotThrow(() -> processable.supplied(), "processable.supplied() throws exception");
        assertNull(processable.supplied(), "processable.supplied() has to return null");
        verify(runnable, times(2)).run();
    }

    @Test
    void functionalClassTest() {
        assertDoesNotThrow(() -> processable.functional(Number.class), "processable.functional(Class) throws exception");
        assertDoesNotThrow(processable::process, "processable.functional(Class).process() throws exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void functionalTest() {
        assertDoesNotThrow(() -> processable.functional(), "processable.functional() throws exception");
        assertDoesNotThrow(processable::process, "processable.functional().process() throws exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void staticProcessableDoesNotThrowTest() {
        assertDoesNotThrow(() -> processable(runnable::run), "Processable.processable unable to create Processable from real Processable method");
    }

    @Test
    void staticProcessableThrowTest() {
        assertThrows(IllegalArgumentException.class, processable(() -> { throw new IllegalArgumentException(); })::process, "Processable.process has to throw right exception");
    }

    @Test
    void staticProcessableCallRunnableMethodTest() {
        assertDoesNotThrow(processable::process, "Processable.process throws exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void staticCastDoesNotThrowOnProcessableTest() {
        assertDoesNotThrow(() -> cast(runnable), "Processable.functional unable to create Processable from real Runtime");
    }

    @Test
    void staticCastCallProcessableMethodTest() {
        Processable processable = cast(runnable);
        assertDoesNotThrow(processable::process, "Processable.process() has throw an exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void completableTest() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        Processable processable = processable(runnable::run).completable(completableFuture);
        new Thread(processable).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertDoesNotThrow(() -> completableFuture.get(), "CompletableFuture was completed exceptionally");
        assertNull(completableFuture.get(), "CompletableFuture has wrong result");
        verify(runnable, times(1)).run();
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        Runnable runnable = processable(() -> { throw new IllegalStateException(); }).completable(completableFuture);
        new Thread(runnable).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertThrows(ExecutionException.class, completableFuture::get, "CompletableFuture has to be completed exceptionally");
        try {
            completableFuture.get();
        } catch (ExecutionException eex) {
            assertEquals(IllegalStateException.class, eex.getCause().getClass(), "CompletableFuture has to be completed exceptionally: IllegalStateException");
        }
    }

    @BeforeEach
    void startUp() {
        this.runnable = mock(Runnable.class);
        this.processable = processable(runnable::run);
    }

    @AfterEach
    void tearDown() {
        this.runnable = null;
        this.processable = null;
    }

    private Runnable runnable;
    private Processable processable;

}
