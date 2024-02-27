package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Расширение {@link Supplier} в объединении с {@link Callable}
 * Интерфейс является одновременно и {@link Supplier} и {@link Callable}. В случае с Supplier::get вызывается метод Callable::call,
 * но если будет выброшено исключение, то оно через lombok {@link SneakyThrows} будет обёрнуто в Runtime исключение и выброшено наружу.
 * @param <T> тип результата
 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
@FunctionalInterface
public interface Supplyable<T> extends Supplier<T>, Callable<T> {

    /**
     * Функция вычисляет результат и если не может этого сделать бросает исключение
     * @return результат вычисления
     * @throws Exception исключение, произошедшее в результате вычисления
     */
    @Override
    T call() throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

    /**
     * Функция вычисляет результата
     * @return результат вычисления
     */
    @Override
    @SneakyThrows
    default T get() {
        return this.call();
    }

    /**
     * Создаёт {@link Processable} путём игнорирования результата {@link Supplyable}
     * @return построенный {@link Processable}
     */
    default @NonNull Processable processable() {
        return this::get;
    }

    /**
     * Функция позволяет превратить функцию без параметров к {@link Supplyable} интерфейсу
     * @param supplyable оборачиваемая функция
     * @return {@link Supplyable} обёртка
     * @param <T> тип результата
     */
    static @NonNull <T> Supplyable<T> supplyable(@NonNull Supplyable<T> supplyable) {
        return Objects.requireNonNull(supplyable, "Supplyable::supplyable - supplyable is null");
    }

    /**
     * Функция осуществляет приведение {@link Supplier} к {@link Supplyable}
     * @param supplier приводимый {@link Supplier} объект
     * @return {@link Supplyable} обёртка
     * @param <T> тип результата
     */
    static @NonNull <T> Supplyable<T> cast(@NonNull Supplier<T> supplier) {
        return Objects.requireNonNull(supplier, "Supplyable::functional - supplyable is null")::get;
    }

}
