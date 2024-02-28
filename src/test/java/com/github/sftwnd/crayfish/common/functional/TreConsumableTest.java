package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.sftwnd.crayfish.common.functional.TreConsumable.treconsumable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TreConsumableTest {

    @Test
    void processTest() {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.process(left, middle, right), "TreConsumable.process throws Exception");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void acceptTest() {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.accept(left, middle, right), "TreConsumable.accept throws Exception");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void callTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.call(left, middle, right), "TreConsumable.call throws Exception");
        verify(treconsumer, times(1)).accept(left, middle, right);
        assertNull( treconsumable.call(left, middle, right), "TreConsumable.call has to return null");
    }

    @Test
    void functionallyTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> { Boolean ignore = treconsumable.functionally(left, middle, right); },
                "TreConsumable.functionally throws Exception");
        verify(treconsumer, times(1)).accept(left, middle, right);
        assertNull( treconsumable.functionally(left, middle, right), "TreConsumable.functionally has to return null");
    }

    @Test
    void functionalTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.functional(), "TreConsumable.functional throws Exception");
        assertNull(treconsumable.functional().execute(left, middle, right), "TreConsumable.functional.execute has to return null");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void functionalClassTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.functional(Void.class), "TreConsumable.functional(Class) throws Exception");
        assertNull(treconsumable.functional(Void.class).execute(left, middle, right), "TreConsumable.functional(Class).execute has to return null");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void processableTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.processable(left, middle, right), "TreConsumable.processable(left, middle, right) throws Exception");
        Processable processable = treconsumable.processable(left, middle, right);
        assertNull(processable.call(), "TreConsumable.processable(left, middle, right).call has to return null");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void leftTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.left(left), "TreConsumable.left(left) throws Exception");
        BiConsumable<Object, Object> biconsumable = treconsumable.left(left);
        assertNull(biconsumable.call(middle, right), "TreConsumable.left(left).call(middle, right) has to return null");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void middleTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.right(right), "TreConsumable.right(right) throws Exception");
        BiConsumable<Object, Object> biconsumable = treconsumable.middle(middle);
        assertNull(biconsumable.call(left, right), "TreConsumable.middle(middle).call(left, right) has to return null");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void rightTest() throws Exception {
        var treconsumable = treconsumable(this.treconsumer::accept);
        assertDoesNotThrow(() -> treconsumable.right(right), "TreConsumable.right(right) throws Exception");
        BiConsumable<Object, Object> biconsumable = treconsumable.right(right);
        assertNull(biconsumable.call(left, middle), "TreConsumable.right(right).call(left, middle) has to return null");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void staticConsumableTest() {
        assertDoesNotThrow(() -> treconsumable(treconsumer::accept), "TreTreConsumable.treconsumable unable to create Consumable from real Consumable method");
        assertNotNull(treconsumable(treconsumer::accept), "TreTreConsumable.treconsumable return null");
        assertDoesNotThrow(() -> treconsumable(treconsumer::accept).accept(left, middle, right), "TreTreConsumable.treconsumable.accept throws Exception");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }


    @Test
    void completableTest() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        TreConsumable<Object, Object, Object> treconsumable = treconsumable(treconsumer::accept).completable(completableFuture);
        new Thread(treconsumable.processable(left, middle, right)).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertDoesNotThrow(() -> completableFuture.get(), "CompletableFuture was completed exceptionally");
        assertNull(completableFuture.get(), "CompletableFuture has wrong result");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @Test
    void completableExceptionallyTest() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.thenAccept(ignore -> cdl.countDown());
        TreConsumable<Object, Object, Object> treconsumable = treconsumable((ignoreLeft, ignoreMiddle, ignoreRight) -> { throw new IllegalStateException(); }).completable(completableFuture);
        new Thread(treconsumable.processable(left, middle, right)).start();
        assertDoesNotThrow(() -> cdl.await(1, TimeUnit.SECONDS), "CompletableFuture is not Done");
        assertThrows(ExecutionException.class, completableFuture::get, "CompletableFuture has to be completed exceptionally");
        try {
            completableFuture.get();
        } catch (ExecutionException eex) {
            assertEquals(IllegalStateException.class, eex.getCause().getClass(), "CompletableFuture has to be completed exceptionally: IllegalStateException");
        }
    }

    @Test
    void andThenTest() {
        var thenconsumable = treconsumable(this.treconsumer);
        var treconsumable = treconsumable((ignoreLeft, ignoreMiddle, ignoreRight) -> {});
        assertDoesNotThrow(() -> treconsumable.andThen(thenconsumable).accept(left, middle, right), "TreConsumable::andThen throws exception");
        verify(treconsumer, times(1)).accept(left, middle, right);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.left = mock(Object.class);
        this.middle = mock(Object.class);
        this.right = mock(Object.class);
        this.treconsumer = mock(TreConsumable.class);
    }

    private Object left;
    private Object middle;
    private Object right;
    private TreConsumable<Object, Object, Object> treconsumer;

}
