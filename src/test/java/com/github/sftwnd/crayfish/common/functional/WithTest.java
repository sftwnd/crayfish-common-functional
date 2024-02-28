package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.sftwnd.crayfish.common.functional.With.nulled;
import static com.github.sftwnd.crayfish.common.functional.With.valued;
import static com.github.sftwnd.crayfish.common.functional.With.voided;
import static com.github.sftwnd.crayfish.common.functional.With.with;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WithTest {

    @Test
    void transformTest() {
        var result = mock();
        Function<Object, Object> function = parameter -> {
            assertSame(value, parameter, "With.transform has to call functional with right value");
            verify(wither, atLeastOnce()).value();
            return result;
        };
        verify(wither, never()).value(); // no value() calls
        assertSame(result, wither.transform(function::apply), "Wither::transform returns wrong value");
        verify(wither, times(1)).value(); // 1 value() call
    }

    @Test
    void transformExceptedTest() {
        assertThrows(IllegalStateException.class, () -> wither.transform(ignore -> { throw new IllegalStateException(); }), "Wither::transform(Excepted) has to throws right Exception");
        verify(wither, times(1)).value(); // no value() calls
    }

    @Test
    void consumeTest() {
        var mock = mock(Runnable.class);
        doNothing().when(mock).run();
        Consumer<Object> consumer = parameter -> {
            assertSame(value, parameter, "With.consume has to call consumable with right value");
            verify(wither, atLeastOnce()).value();
            mock.run();
        };
        verify(wither, never()).value(); // no value() calls
        assertSame(value, wither.consume(consumer::accept), "Wither::consume returns wrong value");
        verify(wither, times(1)).value(); // 1 value() call
        verify(mock, times(1)).run(); // consumer was called once
    }

    @Test
    void consumeExceptedTest() {
        assertThrows(IllegalArgumentException.class, () -> wither.consume(ignore -> { throw new IllegalArgumentException(); }), "Wither::consume(Excepted) has to throws right Exception");
        verify(wither, times(1)).value(); // no value() calls
    }

    @Test
    void furtherTest() {
        var mock = mock(Runnable.class);
        doNothing().when(mock).run();
        Runnable runnable = () -> {
            verify(wither, atLeastOnce()).value();
            mock.run();
        };
        verify(wither, never()).value(); // no value() calls
        assertSame(value, wither.further(runnable::run), "Wither::furtherRun returns wrong value");
        verify(wither, times(1)).value(); // 1 value() call
        verify(mock, times(1)).run(); // processable was called once
    }

    @Test
    void furtherExceptedTest() {
        assertThrows(IOException.class, () -> wither.further(() -> { throw new IOException(); }), "Wither::furtherRun(Excepted) has to throws right Exception");
        verify(wither, times(1)).value(); // no value() calls
    }

    @Test
    void primarilyTest() {
        var mock = mock(Runnable.class);
        doNothing().when(mock).run();
        Runnable runnable = () -> {
            verify(wither, never()).value();
            mock.run();
        };
        verify(wither, never()).value(); // no value() calls
        assertSame(value, wither.primarily(runnable::run), "Wither::primarily returns wrong value");
        verify(wither, times(1)).value(); // 1 value() call
        verify(mock, times(1)).run(); // processable was called once
    }

    @Test
    void primarilyExceptedTest() {
        assertThrows(IOException.class, () -> wither.primarily(() -> { throw new IOException(); }), "Wither::primarily(Excepted) has to throws right Exception");
        verify(wither, never()).value(); // no value() calls
    }

    @Test
    @SuppressWarnings("unchecked")
    void withTest() {
        Supplyable<Object> valuer = mock(Supplyable.class);
        when(valuer.get()).thenReturn(value);
        var wither = with(valuer);
        verify(valuer, never()).get();
        assertDoesNotThrow(wither::value, "With.with throws exception");
        verify(valuer, times(1)).get();
        assertSame(value, wither.value(), "With.with.value - wrong result");
        verify(valuer, times(2)).get();
    }

    @Test
    void voidableTest() {
        Processable processable = mock(Processable.class);
        doNothing().when(processable).run();
        var wither = With.voidable(processable::run);
        assertDoesNotThrow(wither::value, "With.voidable.value throws exception");
        verify(processable, times(1)).run();
        assertNull(wither.value(), "With.voidable.value - wrong result");
        verify(processable, times(2)).run();
    }

    @Test
    void nulledTest() {
        assertNull(nulled().value(), "With.nulled.value - has to return null");
    }

    @Test
    void valuedTest() {
        var wither = valued(value);
        assertSame(value, wither.value(), "With.valued.value - wrong result");
    }

    @Test
    void voidedTest() {
        assertNull(voided().value(), "With.voided.value - has to return null");
    }

    @BeforeEach
    void startU() {
        this.value = mock();
        this.wither = spy(new Wither());
    }

    private Object value;
    private With<Object> wither;

    class Wither implements With<Object> {
        @Override
        public Object value() {
            return value;
        }
    }

}