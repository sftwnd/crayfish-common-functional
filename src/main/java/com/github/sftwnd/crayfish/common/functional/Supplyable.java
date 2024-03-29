package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.github.sftwnd.crayfish.common.functional.With.with;

/**
 * Расширение {@link Supplier} в объединении с {@link Callable}
 * В случае с Supplier::get вызывается метод Callable::call, но если будет выброшено исключение,
 * то оно будет выброшено наружу как unchecked
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
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param processable исполняемый код после вычисления результата
     * @return обогащённый Supplyable
     */
    default @NonNull Supplyable<T> furtherRun(@NonNull Processable processable) {
        return () -> with(this).further(processable);
    }

    /**
     * Выполнение кода после вычисления результата, но до его выдачи
     * @param consumable исполняемый код после вычисления результата, использующий вычисленное значение
     * @return обогащённый Supplyable
     */
    default @NonNull Supplyable<T> furtherAccept(@NonNull Consumable<? super T> consumable) {
        return () -> with(this).consume(consumable);
    }

    /**
     * Выполнение кода после вычисления результата с его трансформацией заданной функцией
     * @param functional исполняемый код после вычисления результата для его преобразования
     * @return обогащённый Supplyable
     * @param <S> тип результата итоговой функции
     */
    default <S> @NonNull Supplyable<S> furtherApply(@NonNull Functional<? super T, ? extends S> functional) {
        Objects.requireNonNull(functional, "Functional::furtherApply - functional is null");
        return () -> functional.apply(this.get());
    }

    /**
     * Выполнение кода перед вычислением результата функции
     * @param processable исполняемый код перед вычислением результата
     * @return обогащённый Functional
     */
    default @NonNull Supplyable<T> previously(@NonNull Processable processable) {
        return () -> with(this).primarily(processable);
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
    
    /**
     * Функция связывается с CompletableFuture и возвращает наружу Processable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull Processable completable(@NonNull CompletableFuture<? super T> completableFuture) {
        Objects.requireNonNull(completableFuture, "Supplyable::completable - completableFuture is null");
        return () -> {
            try {
                if (!completableFuture.isDone()) {
                    completableFuture.complete(this.call());
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

}
