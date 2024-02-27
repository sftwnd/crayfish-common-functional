package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Расширение {@link Consumer}, но метод может бросать исключение.
 * Помимо {@link Consumer} сам {@link Consumable} является {@link Functional}, возвращающим Void
 * @param <T> тип параметра
 */
public interface Consumable<T> extends Consumer<T>, Functional<T, Void> {

    /**
     * Применяет метод к заданному аргументу
     * @param parameter параметр метода
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    void process(T parameter) throws Exception;

    /**
     * Применяет метод к заданному аргументу
     * @param parameter параметр метода
     */
    @SneakyThrows
    @Override
    default void accept(T parameter) {
        process(parameter);
    }

    /**
     * Применяет метод к заданному аргументу
     * @param parameter параметр метода
     * @return null, как значение типа {@link Void}
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    @Override
    default Void execute(T parameter) throws Exception {
        return functionally(parameter);
    }

    /**
     * Применяет метод к заданному аргументу и возвращает null заданного типа
     * @param parameter параметр метода
     * @return null, как значение любого желаемого
     * @throws Exception исключение, произошедшее в результате исполнения
     * @param <X> тип результата (значение всегда null, но заданного типа)
     */
    default <X> X functionally(T parameter) throws Exception {
        process(parameter);
        return null;
    }

    /**
     * Создаёт {@link Processable}, который при вызове подставляет заданное значение в параметры вызова метода process
     * @param parameter фиксируемое значение параметр метода
     * @return построенный Processable
     */
    default @NonNull Processable processable(T parameter) {
        return () -> process(parameter);
    }

    /**
     * Метод приведения {@link Consumable} к функции, возвращающей null как результат заданного типа
     * @return {@link Functional} с результатом null заданного типа
     * @param <TT> тип параметра функции
     * @param <R> тип результата
     */
    default <TT extends T, R> Functional<TT, R> cast() {
        return this::functionally;
    }

    /**
     * Метод приведения {@link Consumable} к функции, возвращающей null как результат заданного типа
     * @param clazz {@link Class} к которому приводится результат
     * @return {@link Functional} с результатом null заданного типа
     * @param <TT> тип параметра функции
     * @param <R> тип результата
     */
    default <TT extends T, R> Functional<TT, R> cast(@NonNull Class<? extends R> clazz) {
        Objects.requireNonNull(clazz, "Consumable::class - clazz is null");
        return this::functionally;
    }

    /**
     * Функция позволяет превратить метод от параметра к {@link Consumable} интерфейсу
     * @param consumable оборачиваемый метод
     * @return Consumable обёртка
     * @param <P> параметр аргумента
     */
    static <P> @NonNull Consumable<P> consumable(@NonNull Consumable<P> consumable) {
        return Objects.requireNonNull(consumable, "Consumable::consumable - consumable is null");
    }

    /**
     * Функция осуществляет приведение {@link Consumer} к {@link Consumable}
     * @param consumer приводимый {@link Consumer} объект
     * @return {@link Consumable} обёртка
     * @param <T> тип параметра
     */
    static @NonNull <T> Consumable<T> cast(@NonNull Consumer<T> consumer) {
        return Objects.requireNonNull(consumer, "Consumable::cast - consumer is null")::accept;
    }

}
