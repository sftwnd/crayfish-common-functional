package com.github.sftwnd.crayfish.common.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Function;

import static com.github.sftwnd.crayfish.common.functional.Functional.cast;
import static com.github.sftwnd.crayfish.common.functional.Functional.functional;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FunctionalTest {

    @Test
    void applyTest() {
        var functional = functional(this.function::apply);
        assertDoesNotThrow(() -> functional.apply(parameter), "Functional.apply throws Exception");
        verify(function, times(1)).apply(parameter);
        assertSame(result, functional.apply(parameter), "Functional.apply has to return wrong result");
    }

    @Test
    void applyRethrowTest() {
        var functional = functional(ignore -> { throw new IOException(); });
        assertThrows(IOException.class, () -> functional.apply(parameter), "Functional.apply(parameter) has to throw right exception");
    }

    @Test
    void executeTest() throws Exception {
        var functional = functional(this.function::apply);
        assertDoesNotThrow(() -> functional.execute(parameter), "Functional.execute throws Exception");
        verify(function, times(1)).apply(parameter);
        assertSame(result, functional.execute(parameter), "Functional.execute has to return wrong result");
    }

    @Test
    void consumableTest() {
        var functional = functional(this.function::apply);
        assertDoesNotThrow(functional::consumable, "Functional.consumable throws Exception");
        assertDoesNotThrow(() -> functional.consumable().call(parameter), "Functional.consumable.call(parameter) throws exception");
        verify(function, times(1)).apply(parameter);
    }

    @Test
    void supplyableTest() {
        var functional = functional(this.function::apply);
        assertDoesNotThrow(() -> functional.supplyable(parameter), "Functional.supplyable throws Exception");
        assertSame(result, functional.supplyable(parameter).get(), "Functional.supplyable(parameter).get() return wrong result");
        verify(function, times(1)).apply(parameter);
    }

    @Test
    void processableTest() throws Exception {
        var functional = functional(this.function::apply);
        assertDoesNotThrow(() -> functional.processable(parameter), "Functional.processable(value throws Exception");
        Processable processable = functional.processable(parameter);
        assertNull(processable.call(), "Functional.processable(value).call has to return null");
        verify(function, times(1)).apply(parameter);
    }

    @Test
    void staticCastDoesNotThrowOnSupplierTest() {
        assertDoesNotThrow(() -> cast(function), "Functional.cast unable to create Functional from real Consumer");
    }

    @Test
    void staticFunctionalTest() {
        assertDoesNotThrow(() -> functional(function::apply), "Functional.functional unable to create Functional from real Functional method");
        assertNotNull(functional(function::apply), "Functional.functional return null");
        assertDoesNotThrow(() -> functional(function::apply).apply(parameter), "Functional.functional.accept throws Exception");
        verify(function, times(1)).apply(parameter);
        assertSame(result, functional(function::apply).apply(parameter), "Functional.functional.apply(parameter) return wrong value");
    }

    @Test
    void staticCastTest() {
        assertDoesNotThrow(() -> cast(function), "Functional.cast unable to create Functional from real Consumer");
        assertNotNull(cast(function), "Functional.cast return null");
        assertDoesNotThrow(() -> cast(function).execute(parameter), "Functional.cast.execute throws Exception");
        verify(function, times(1)).apply(parameter);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void startUp() {
        this.parameter = mock(Object.class);
        this.result = mock(Object.class);
        this.function = mock(Function.class);
        when(function.apply(this.parameter)).thenReturn(this.result);
    }

    private Object parameter;
    private Object result;
    private Function<Object, Object> function;

}
