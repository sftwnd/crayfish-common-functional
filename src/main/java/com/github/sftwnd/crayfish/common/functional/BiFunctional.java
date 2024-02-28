package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static com.github.sftwnd.crayfish.common.functional.With.with;

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
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param processable исполняемый код после вычисления результата
     * @return обогащённый BiFunctional
     */
    default @NonNull BiFunctional<T, U, R> furtherRun(@NonNull Processable processable) {
        return (left, right) -> with(supplyable(left, right)).further(processable);
    }

    /**
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param consumable исполняемый код после вычисления результата, использующий вычисленное значение
     * @return обогащённый BiFunctional
     */
    default @NonNull BiFunctional<T, U, R> furtherAccept(@NonNull Consumable<? super R> consumable) {
        return (left, right) -> with(supplyable(left, right)).consume(consumable);
    }

    /**
     * Выполнение кода после вычисления результата с его трансформацией заданной функцией
     * @param functional исполняемый код после вычисления результата для его преобразования
     * @return обогащённый BiFunctional
     * @param <S> тип результата итоговой функции
     */
    default <S> @NonNull BiFunctional<T, U, S> furtherApply(@NonNull Functional<? super R, ? extends S> functional) {
        Objects.requireNonNull(functional, "Functional::furtherApply - functional is null");
        return (left, right) -> functional.apply(this.apply(left, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции
     * @param processable исполняемый код перед вычислением результата
     * @return обогащённый BiFunctional
     */
    default @NonNull BiFunctional<T, U, R> previously(@NonNull Processable processable) {
        return (left, right) -> with(BiFunctional.this.supplyable(left, right)).primarily(processable);
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования первого параметра
     * @param functional исполняемый код вычисления первого параметра
     * @return обогащённый BiFunctional
     * @param <L> тип аргумента для вычисления левого параметра функции
     */
    default @NonNull <L> BiFunctional<L, U, R> withLeft(@NonNull Functional<? super L, ? extends T> functional) {
        return (left, right) -> with(functional.supplyable(left)).transform(x -> apply(x, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования первого параметра
     * @param supplyable исполняемый код вычисления первого параметра
     * @return обогащённый BiFunctional
     */
    default @NonNull Functional<U, R> withLeft(@NonNull Supplyable<? extends T> supplyable) {
        return right -> with(supplyable).transform(left -> apply(left, right));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования второго параметра
     * @param functional исполняемый код вычисления второго параметра
     * @return обогащённый BiFunctional
     * @param <H> тип аргумента для вычисления правого параметра функции
     */
    default @NonNull <H> BiFunctional<T, H, R> withRight(@NonNull Functional<? super H, ? extends U> functional) {
        return (left, right) -> with(functional.supplyable(right)).transform(x -> apply(left, x));
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования второго параметра
     * @param supplyable исполняемый код вычисления второго параметра
     * @return обогащённый BiFunctional
     */
    default @NonNull Functional<T, R> withRight(@NonNull Supplyable<? extends U> supplyable) {
        Objects.requireNonNull(supplyable, "BiFunctional::withRight - supplyable is null");
        return left -> with(supplyable).transform(right -> apply(left, right));
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
