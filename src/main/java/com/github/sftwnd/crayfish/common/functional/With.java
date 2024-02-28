package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Objects;

/**
 * Интерфейс для исполнения операция над вычисленным значением
 * FYI: Вычисление значения может быть не вычислено до произведения вызова
 * @param <X> тип используемого значения
 */
@FunctionalInterface
public interface With<X> {

    /**
     * Метод получения значения
     * @return используемое значение
     */
    X value();

    /**
     * Метод вычисляет значение и преобразует его в результат.
     * @param functional функция преобразования значения
     * @return результат преобразования
     * @param <Y> тип итогового значения
     */
    default <Y> Y transform(@NonNull Functional<? super X, ? extends Y> functional) {
        return Objects.requireNonNull(functional, "With::transform - functional is null").apply(value());
    }

    /**
     * После вычисления результата, но перед его выдачей исполняется {@link Consumable}, зависящий от результата
     * @param consumable метод над результатом значения
     * @return вычисленное значение
     */
    default X consume(@NonNull Consumable<? super X> consumable) {
        X value = value();
        Objects.requireNonNull(consumable, "With::consume - consumable is null").accept(value);
        return value;
    }

    /**
     * После вычисления результата, но перед его выдачей исполняется {@link Processable}, не зависящий от результата
     * @param processable метод, вызываемый после получения значения
     * @return вычисленное значение
     */
    default X further(@NonNull Processable processable) {
        X value = value();
        Objects.requireNonNull(processable, "With::process - processable is null").run();
        return value;
    }

    /**
     * Выполнение кода до вычисления значения
     * @param processable метод, вызываемый до получения значения
     * @return вычисленное значение
     */
    default X primarily(@NonNull Processable processable) {
        Objects.requireNonNull(processable, "With::process - processable is null").run();
        return value();
    }

    /**
     * Построение {@link With}, где значение формируется с помощью заданного {@link Supplyable}
     * @param supplyable метод, используемый для вычисления значения
     * @return построенный {@link With}
     * @param <X> type of value
     */
    static <X> @NonNull With<X> with(@NonNull Supplyable<X> supplyable) {
        return Objects.requireNonNull(supplyable, "With::withSupply - supplyable is null")::get;
    }

    /**
     * Построение {@link With}, где значение задано константой
     * @param value заданное заранее значение
     * @return построенный {@link With}
     * @param <X> type of value
     */
    static <X> @NonNull With<X> valued(@Nullable X value) {
        return with(() -> value);
    }

    /**
     * Построение {@link With}, где значение Void, но процесс его формирования идёт после вызова Processable
     * @param processable метод, используемый перед вычислением значения
     * @return построенный {@link With}
     */
    static @NonNull With<Void> voidable(@NonNull Processable processable) {
        return with(processable::call);
    }

    /**
     * Построение {@link With}, где значение всегда null, но заданного типа
     * @return построенный {@link With}
     * @param <X> type of value
     */
    static <X> @NonNull With<X> nulled() {
        return valued(null);
    }

    /**
     * Построение {@link With}, где значение всегда null типа Void
     * @return построенный {@link With}
     */
    static @NonNull With<Void> voided() {
        return valued(null);
    }

}
