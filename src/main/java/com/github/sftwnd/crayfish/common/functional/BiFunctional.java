package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Расширение {@link BiFunctional}, но метод может бросать исключение.
 * @param <T> тип первого параметра
 * @param <U> тип второго параметра
 * @param <R> тип результата
 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
@FunctionalInterface
public interface BiFunctional<T, U, R> extends BiFunction<T, U, R> {

    /**
     * Применяет функцию к заданному аргументу
     * @param left первый параметр функции
     * @param right второй параметр функции
     * @return результат применения функции
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    R execute(T left, U right) throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

    /**
     * Применяет функцию к заданному аргументу
     * @param left первый параметр функции
     * @param right второй параметр функции
     * @return результат применения функции
     */
    @Override
    @SneakyThrows
    default R apply(T left, U right) {
        return execute(left, right);
    }

    /**
     * Создаёт {@link Supplyable}, который при вызове подставляет заданное значение в параметры вызова метода process
     * @param left фиксируемое значение первого параметра функции
     * @param right фиксируемое значение второго параметра функции
     * @return построенный {@link Supplyable}
     */
    default @NonNull Supplyable<R> supplyable(T left, U right) {
        return () -> execute(left, right);
    }

    /**
     * Создаёт {@link Functional}, который при вызове подставляет заданное значение в первый параметр вызова метода
     * @param left фиксируемое значение первого параметра функции от {@link BiFunctional}
     * @return построенный {@link Functional}
     */
    default @NonNull Functional<U, R> left(T left) {
        return right -> execute(left, right);
    }

    /**
     * Создаёт {@link Functional}, который при вызове подставляет заданное значение в первый параметр вызова метода
     * @param right фиксируемое значение первого параметра функции от {@link BiFunctional}
     * @return построенный {@link Functional}
     */
    default @NonNull Functional<T, R> right(U right) {
        return left -> execute(left, right);
    }

    /**
     * Создаёт {@link BiConsumable}, который выполняет вызове и игнорирует результат выполнения функции
     * @return построенный Consumable
     */
    default @NonNull BiConsumable<T, U> consumable() {
        return this::execute;
    }

    /**
     * Создаёт {@link Processable}, который при вызове подставляет заданные значения в параметры вызова метода process
     * @param left фиксируемое значение параметра метода
     * @param right фиксируемое значение параметра метода
     * @return построенный Processable
     */
    default @NonNull Processable processable(T left, U right) {
        return () -> execute(left, right);
    }

    /**
     * Функция позволяет превратить метод от двух параметров к {@link BiFunctional} интерфейсу
     * @param bifunctional оборачиваемый метод
     * @return {@link BiFunctional} обёртка
     * @param <T> параметр первого аргумента функции
     * @param <U> параметр второго аргумента функции
     * @param <R> параметр результата функции
     */
    static <T, U, R> @NonNull BiFunctional<T, U, R> bifunctional(@NonNull BiFunctional<T, U, R> bifunctional) {
        return Objects.requireNonNull(bifunctional, "BiFunctional::bifunctional - bifunctional is null");
    }

    /**
     * Функция осуществляет приведение {@link BiFunction} к {@link BiFunctional}
     * @param bifunction приводимый {@link BiFunction} объект
     * @return {@link BiFunctional} обёртка
     * @param <T> тип первого параметра
     * @param <U> тип второго параметра
     * @param <R> тип результата
     */
    static @NonNull <T, U, R> BiFunctional<T, U, R> cast(@NonNull BiFunction<T, U, R> bifunction) {
        return Objects.requireNonNull(bifunction, "BiFunctional::functional - bifunction is null")::apply;
    }

    /**
     * Функция связывается с CompletableFuture и возвращает наружу BiConsumable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull BiConsumable<T, U> completable(@NonNull CompletableFuture<? super R> completableFuture) {
        Objects.requireNonNull(completableFuture, "BiFunctional::completable - completableFuture is null");
        return (left, right) -> {
            try {
                if (!completableFuture.isDone()) {
                    completableFuture.complete(this.execute(left, right));
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

}
