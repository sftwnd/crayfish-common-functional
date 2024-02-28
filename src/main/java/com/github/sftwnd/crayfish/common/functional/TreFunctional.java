package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Расширение {@link TreFunctional}, но метод может бросать исключение.
 * @param <T> тип первого (левого) параметра
 * @param <U> тип второго (центрального) параметра
 * @param <V> тип третьего (правого) параметра
 * @param <R> тип результата
 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
@FunctionalInterface
public interface TreFunctional<T, U, V, R> {

    /**
     * Применяет функцию к заданному аргументу
     * @param left первый параметр функции
     * @param middle второй параметр функции
     * @param right третий параметр функции
     * @return результат применения функции
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    R execute(T left, U middle, V right) throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

    /**
     * Применяет функцию к заданному аргументу
     * @param left первый параметр функции
     * @param middle второй параметр функции
     * @param right третий параметр функции
     * @return результат применения функции
     */
    @SneakyThrows
    default R apply(T left, U middle, V right) {
        return execute(left, middle, right);
    }

    /**
     * Создаёт {@link Supplyable}, который при вызове подставляет заданное значение в параметры вызова метода process
     * @param left первый параметр функции
     * @param middle второй параметр функции
     * @param right третий параметр функции
     * @return построенный {@link Supplyable}
     */
    default @NonNull Supplyable<R> supplyable(T left, U middle, V right) {
        return () -> execute(left, middle, right);
    }

    /**
     * Создаёт {@link BiFunctional}, который при вызове подставляет заданное значение в первый параметр вызова метода
     * @param left фиксируемое значение первого параметра функции от {@link TreFunctional}
     * @return построенный {@link BiFunctional}
     */
    default @NonNull BiFunctional<U, V, R> left(T left) {
        return (middle, right) -> execute(left, middle, right);
    }

    /**
     * Создаёт {@link BiFunctional}, который при вызове подставляет заданное значение в средний параметр вызова метода
     * @param middle фиксируемое значение среднего параметра функции от {@link TreFunctional}
     * @return построенный {@link BiFunctional}
     */
    default @NonNull BiFunctional<T, V, R> middle(U middle) {
        return (left, right) -> execute(left, middle, right);
    }

    /**
     * Создаёт {@link BiFunctional}, который при вызове подставляет заданное значение в последний параметр вызова метода
     * @param right фиксируемое значение последнего параметра функции от {@link TreFunctional}
     * @return построенный {@link BiFunctional}
     */
    default @NonNull BiFunctional<T, U, R> right(V right) {
        return (left, middle) -> execute(left, middle, right);
    }

    /**
     * Создаёт {@link TreConsumable}, который выполняет вызове и игнорирует результат выполнения функции
     * @return построенный Consumable
     */
    default @NonNull TreConsumable<T, U, V> consumable() {
        return this::execute;
    }

    /**
     * Создаёт {@link Processable}, который при вызове подставляет заданные значения в параметры вызова метода process
     * @param left первый параметр функции
     * @param middle второй параметр функции
     * @param right третий параметр функции
     * @return построенный Processable
     */
    default @NonNull Processable processable(T left, U middle, V right) {
        return () -> execute(left, middle, right);
    }

    /**
     * Функция позволяет превратить метод от двух параметров к {@link TreFunctional} интерфейсу
     * @param trefunctional оборачиваемый метод
     * @return {@link TreFunctional} обёртка
     * @param <T> параметр первого аргумента функции
     * @param <U> параметр второго аргумента функции
     * @param <R> параметр результата функции
     */
    static <T, U, V, R> @NonNull TreFunctional<T, U, V, R> trefunctional(@NonNull TreFunctional<T, U, V, R> trefunctional) {
        return Objects.requireNonNull(trefunctional, "TreFunctional::trefunctional - trefunctional is null");
    }

    /**
     * Функция связывается с CompletableFuture и возвращает наружу BiConsumable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull TreConsumable<T, U, V> completable(@NonNull CompletableFuture<? super R> completableFuture) {
        Objects.requireNonNull(completableFuture, "BiFunctional::completable - completableFuture is null");
        return (left, middle, right) -> {
            try {
                if (!completableFuture.isDone()) {
                    completableFuture.complete(this.execute(left, middle, right));
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <X> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <X> @NonNull TreFunctional<T, U, V, X> andThen(@NonNull Function<? super R, ? extends X> after) {
        Objects.requireNonNull(after, "TreFunctional::andThen - after is null");
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }

}
