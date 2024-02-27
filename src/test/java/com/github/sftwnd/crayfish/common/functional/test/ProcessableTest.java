package com.github.sftwnd.crayfish.common.functional.test;

import com.github.sftwnd.crayfish.common.functional.Processable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.sftwnd.crayfish.common.functional.Processable.cast;
import static com.github.sftwnd.crayfish.common.functional.Processable.processable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ProcessableTest {

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
    void castClassTest() {
        assertDoesNotThrow(() -> processable.cast(Number.class), "processable.cast(Class) throws exception");
        assertDoesNotThrow(processable::process, "processable.cast(Class).process() throws exception");
        verify(runnable, times(1)).run();
    }

    @Test
    void castTest() {
        assertDoesNotThrow(() -> processable.cast(), "processable.cast() throws exception");
        assertDoesNotThrow(processable::process, "processable.cast().process() throws exception");
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
        assertDoesNotThrow(() -> cast(runnable), "Processable.cast unable to create Processable from real Runtime");
    }

    @Test
    void staticCastCallProcessableMethodTest() {
        Processable processable = cast(runnable);
        assertDoesNotThrow(processable::process, "Processable.process() has throw an exception");
        verify(runnable, times(1)).run();
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
