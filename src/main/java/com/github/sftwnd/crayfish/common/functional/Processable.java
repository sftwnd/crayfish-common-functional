package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Расширение {@link Runnable}, но метод может бросать исключение.
 * Помимо {@link Runnable} сам {@link Processable} является {@link Supplyable}, возвращающий Void

 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
public interface Processable extends Runnable {

    /**
     * Выполняет основной метод
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    void process() throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

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
    default <R> Supplyable<R> functional() {
        return this::supplied;
    }

    /**
     * Метод приведения {@link Processable} к {@link Supplyable}, возвращающей null как результат заданного типа
     * @param clazz {@link Class} к которому приводится результат
     * @return {@link Supplyable} с результатом null заданного типа
     * @param <R> тип результата
     */
    default <R> Supplyable<R> functional(@NonNull Class<? extends R> clazz) {
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
    
    /**
     * Функция связывается с CompletableFuture и возвращает наружу Processable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull Processable completable(@NonNull CompletableFuture<Void> completableFuture) {
        Objects.requireNonNull(completableFuture, "Processable::completable - completableFuture is null");
        return () -> {
            try {
                if (!completableFuture.isDone()) {
                    this.process();
                    completableFuture.complete(null);
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

}
