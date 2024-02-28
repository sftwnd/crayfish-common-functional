package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.github.sftwnd.crayfish.common.functional.With.with;

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
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param processable исполняемый код после вычисления результата
     * @return обогащённый TreFunctional
     */
    default @NonNull TreFunctional<T, U, V, R> furtherRun(@NonNull Processable processable) {
        return (left, middle, right) -> with(() -> apply(left, middle, right)).further(processable);
    }

    /**
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param consumable исполняемый код после вычисления результата, использующий вычисленное значение
     * @return обогащённый TreFunctional
     */
    default @NonNull TreFunctional<T, U, V, R> furtherAccept(@NonNull Consumable<? super R> consumable) {
        return (left, middle, right) -> with(() -> apply(left, middle, right)).consume(consumable);
    }

    /**
     * Выполнение кода после вычисления результата с его трансформацией заданной функцией
     * @param functional исполняемый код после вычисления результата для его преобразования
     * @return обогащённый TreFunctional
     * @param <S> тип результата итоговой функции
     */
    default <S> @NonNull TreFunctional<T, U, V, S> furtherApply(@NonNull Functional<? super R, ? extends S> functional) {
        Objects.requireNonNull(functional, "Functional::furtherApply - functional is null");
        return (left, middle, right) -> functional.apply(this.apply(left, middle, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции
     * @param processable исполняемый код перед вычислением результата
     * @return обогащённый TreFunctional
     */
    default @NonNull TreFunctional<T, U, V, R> previously(@NonNull Processable processable) {
        return (left, middle, right) -> with(TreFunctional.this.supplyable(left, middle, right)).primarily(processable);
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования первого параметра
     * @param functional исполняемый код вычисления первого параметра
     * @return обогащённый TreFunctional
     * @param <L> тип аргумента для вычисления левого параметра функции
     */
    default @NonNull <L> TreFunctional<L, U, V, R> withLeft(@NonNull Functional<? super L, ? extends T> functional) {
        return (left, middle, right) -> with(functional.supplyable(left)).transform(x -> apply(x, middle, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования первого параметра
     * @param supplyable исполняемый код вычисления первого параметра
     * @return обогащённый TreFunctional
     */
    default @NonNull BiFunctional<U, V, R> withLeft(@NonNull Supplyable<? extends T> supplyable) {
        return (middle, right) -> with(supplyable).transform(left -> apply(left, middle, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования второго параметра
     * @param functional исполняемый код вычисления второго параметра
     * @return обогащённый TreFunctional
     * @param <M> тип аргумента для вычисления среднего параметра функции
     */
    default @NonNull <M> TreFunctional<T, M, V, R> withMiddle(@NonNull Functional<? super M, ? extends U> functional) {
        return (left, middle, right) -> with(functional.supplyable(middle)).transform(x -> apply(left, x, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования второго параметра
     * @param supplyable исполняемый код вычисления второго параметра
     * @return обогащённый TreFunctional
     */
    default @NonNull BiFunctional<T, V, R> withMiddle(@NonNull Supplyable<? extends U> supplyable) {
        return (left, right) -> with(supplyable).transform(middle -> apply(left, middle, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования третьего параметра
     * @param functional исполняемый код вычисления третьего параметра
     * @return обогащённый TreFunctional
     * @param <H> тип аргумента для вычисления правого параметра функции
     */
    default @NonNull <H> TreFunctional<T, U, H, R> withRight(@NonNull Functional<? super H, ? extends V> functional) {
        return (left, middle, right) -> with(functional.supplyable(right)).transform(x -> apply(left, middle, x));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования третьего параметра
     * @param supplyable исполняемый код вычисления третьего параметра
     * @return обогащённый TreFunctional
     */
    default @NonNull BiFunctional<T, U, R> withRight(@NonNull Supplyable<? extends V> supplyable) {
        Objects.requireNonNull(supplyable, "TreFunctional::withRight - supplyable is null");
        return (left, middle) -> with(supplyable).transform(right -> apply(left, middle, right));
    }

    /**
     * Функция позволяет превратить метод от двух параметров к {@link TreFunctional} интерфейсу
     * @param trefunctional оборачиваемый метод
     * @return {@link TreFunctional} обёртка
     * @param <T> параметр первого аргумента функции
     * @param <U> параметр второго аргумента функции
     * @param <V> параметр третьего аргумента функции
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

}
