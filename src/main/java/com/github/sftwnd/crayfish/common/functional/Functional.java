package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
            } catch (Throwable throwable) {
                completableFuture.completeExceptionally(throwable);
            }
        };
    }

}
