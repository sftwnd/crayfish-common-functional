package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.github.sftwnd.crayfish.common.functional.With.with;

/**
 * Расширение {@link Function}, но метод может бросать исключение.
 * @param <T> тип параметра
 * @param <R> тип результата
 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
@FunctionalInterface
public interface Functional<T, R> extends Function<T, R> {

    /**
     * Применяет функцию к заданному аргументу
     * @param parameter параметр функции
     * @return результат применения функции
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    R execute(T parameter) throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

    /**
     * Применяет функцию к заданному аргументу
     * @param parameter параметр функции
     * @return результат применения функции
     */
    @Override
    @SneakyThrows
    default R apply(T parameter) {
        return execute(parameter);
    }

    /**
     * Создаёт {@link Supplyable}, который при вызове подставляет заданное значение в параметры вызова метода process
     * @param parameter фиксируемое значение параметра метода
     * @return построенный Consumable
     */
    default @NonNull Supplyable<R> supplyable(T parameter) {
        return () -> execute(parameter);
    }

    /**
     * Создаёт {@link Consumable}, который выполняет вызове и игнорирует результат выполнения функции
     * @return построенный Consumable
     */
    default @NonNull Consumable<T> consumable() {
        return this::execute;
    }

    /**
     * Создаёт {@link Processable}, который при вызове подставляет заданное значение в параметры вызова метода process
     * @param parameter фиксируемое значение параметра метода
     * @return построенный Processable
     */
    default @NonNull Processable processable(T parameter) {
        return () -> execute(parameter);
    }

    /**
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param processable исполняемый код после вычисления результата
     * @return обогащённый Functional
     */
    default @NonNull Functional<T, R> furtherRun(@NonNull Processable processable) {
        return parameter -> with(this.supplyable(parameter)).further(processable);
    }

    /**
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param consumable исполняемый код после вычисления результата, использующий вычисленное значение
     * @return обогащённый Functional
     */
    default @NonNull Functional<T, R> furtherAccept(@NonNull Consumable<? super R> consumable) {
        return parameter -> with(this.supplyable(parameter)).consume(consumable);
    }

    /**
     * Выполнение кода после вычисления результата с его трансформацией заданной функцией
     * @param functional исполняемый код после вычисления результата для его преобразования
     * @return обогащённый Functional
     * @param <S> тип результата итоговой функции
     */
    default <S> @NonNull Functional<T, S> furtherApply(@NonNull Functional<? super R, ? extends S> functional) {
        Objects.requireNonNull(functional, "Functional::furtherApply - functional is null");
        return parameter -> functional.apply(this.apply(parameter));
    }

    /**
     * Выполнение кода перед вычислением результата функции
     * @param processable исполняемый код перед вычислением результата
     * @return обогащённый Functional
     */
    default @NonNull Functional<T, R> previously(@NonNull Processable processable) {
        return parameter -> with(this.supplyable(parameter)).primarily(processable);
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования параметра
     * @param functional исполняемый код вычисления первого параметра
     * @return обогащённый Functional
     * @param <L> тип аргумента для вычисления левого параметра функции
     */
    default @NonNull <L> Functional<L,R> withParam(@NonNull Functional<? super L, ? extends T> functional) {
        return parameter -> with(functional.supplyable(parameter)).transform(this::apply);
    }

    /**
     * Выполнение кода перед вычислением результата функции для формирования параметра
     * @param supplyable исполняемый код вычисления первого параметра
     * @return обогащённый Supplyable
     */
    default @NonNull Supplyable<R> withParam(@NonNull Supplyable<? extends T> supplyable) {
        return () -> with(supplyable).transform(this::apply);
    }

    /**
     * Функция позволяет превратить метод от параметра к {@link Functional} интерфейсу
     * @param functional оборачиваемый метод
     * @return {@link Functional} обёртка
     * @param <P> параметр аргумента функции
     * @param <R> параметр результата функции
     */
    static <P, R> @NonNull Functional<P, R> functional(@NonNull Functional<P, R> functional) {
        return Objects.requireNonNull(functional, "Functional::functional - functional is null");
    }

    /**
     * Функция осуществляет приведение {@link Function} к {@link Functional}
     * @param function приводимый {@link Function} объект
     * @return {@link Functional} обёртка
     * @param <T> тип параметра
     * @param <R> тип результата
     */
    static @NonNull <T, R> Functional<T, R> cast(@NonNull Function<T, R> function) {
        return Objects.requireNonNull(function, "Functional::functional - function is null")::apply;
    }

    /**
     * Функция связывается с CompletableFuture и возвращает наружу Consumable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull Consumable<T> completable(@NonNull CompletableFuture<? super R> completableFuture) {
        Objects.requireNonNull(completableFuture, "Functional::completable - completableFuture is null");
        return parameter -> {
            try {
                if (!completableFuture.isDone()) {
                    completableFuture.complete(this.execute(parameter));
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

}
