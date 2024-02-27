package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;

/**
 * Расширение {@link Runnable}, но метод может бросать исключение.
 * Помимо {@link Runnable} сам {@link Processable} является {@link Supplyable}, возвращающий Void
 */
public interface Processable extends Runnable, Supplyable<Void> {

    /**
     * Выполняет основной метод
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    void process() throws Exception;

    /**
     * Выполняет основной метод
     */
    @Override
    @SneakyThrows
    default void run() {
        process();
    }

    /**
     * Выполняет основной метод и возвращает Void
     * @return Void в качестве результата
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    @Override
    default Void call() throws Exception {
        return supplied();
    }

    /**
     * Выполняет основной метод и возвращает null заданного типа
     * @return null, как значение любого желаемого
     * @throws Exception исключение, произошедшее в результате исполнения
     * @param <X> тип результата (значение всегда null, но заданного типа)
     */
    default <X> X supplied() throws Exception {
        process();
        return null;
    }

    /**
     * Метод приведения {@link Processable} к {@link Supplyable}, возвращающей null как результат заданного типа
     * @return {@link Supplyable} с результатом null заданного типа
     * @param <R> тип результата
     */
    default <R> Supplyable<R> cast() {
        return this::supplied;
    }

    /**
     * Метод приведения {@link Processable} к {@link Supplyable}, возвращающей null как результат заданного типа
     * @param clazz {@link Class} к которому приводится результат
     * @return {@link Supplyable} с результатом null заданного типа
     * @param <R> тип результата
     */
    default <R> Supplyable<R> cast(@NonNull Class<? extends R> clazz) {
        Objects.requireNonNull(clazz, "Consumable::class - clazz is null");
        return this::supplied;
    }

    /**
     * Функция позволяет превратить метод без параметров к {@link Processable} интерфейсу
     * @param processable оборачиваемый метод
     * @return {@link Processable} обёртка
     */
    static @NonNull Processable processable(@NonNull Processable processable) {
        return Objects.requireNonNull(processable, "Processable::processable - processable is null");
    }

    /**
     * Функция осуществляет приведение {@link Runnable} к {@link Processable}
     * @param runnable приводимый {@link Runnable} объект
     * @return {@link Processable} обёртка
     */
    static @NonNull Processable cast(@NonNull Runnable runnable) {
        return Objects.requireNonNull(runnable, "Runnable::cast - runnable is null")::run;
    }

}
