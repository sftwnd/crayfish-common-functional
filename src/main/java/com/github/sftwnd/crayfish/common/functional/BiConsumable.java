package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * Расширение {@link BiConsumer}, но метод может бросать исключение.
 * @param <T> тип первого параметра
 * @param <U> тип второго параметра
 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
public interface BiConsumable <T, U> extends BiConsumer<T, U> {

    /**
     * Применение метода к заданным аргументам с декларацией пробрасываемого исключения
     * @param left первый параметр метода
     * @param right второй параметр метода
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    void process(T left, U right) throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

    /**
     * Применение метода к заданным аргументам
     * @param left первый параметр метода
     * @param right второй параметр метода
     */
    @Override
    @SneakyThrows
    default void accept(T left, U right) {
        process(left, right);
    }

    /**
     * Применение метода к заданным аргументам с декларацией пробрасываемого исключения
     * В качестве результата выполнения вернётся Void
     * @param left первый параметр метода
     * @param right второй параметр метода
     * @return Void как результат выполнения
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    default Void call(T left, U right) throws Exception {
        return functionally(left, right);
    }

    /**
     * Применение метода к заданным аргументам и возвращает null заданного типа
     * @param left первый параметр метода
     * @param right второй параметр метода
     * @return null, как значение любого желаемого
     * @throws Exception исключение, произошедшее в результате исполнения
     * @param <R> тип результата (значение всегда равно null, но заданного типа)
     */
    default <R> @Nullable R functionally(T left, U right) throws Exception {
        process(left, right);
        return null;
    }

    /**
     * Создаёт {@link Consumable}, который передаёт свой атрибут в правый параметр, а левый считается предустановленным
     * @param left фиксируемое значение первого параметр метода
     * @return построенный {@link Consumable}
     */
    default @NonNull Consumable<U> left(T left) {
        return right -> process(left, right);
    }

    /**
     * Создаёт {@link Consumable}, который передаёт свой атрибут в левый параметр, а правый считается предустановленным
     * @param right фиксируемое значение второго параметр метода
     * @return построенный {@link Consumable}
     */
    default @NonNull Consumable<T> right(U right) {
        return left -> process(left, right);
    }

    /**
     * Создаёт {@link Processable}, который при вызове подставляет заданные значения в параметры вызова метода process
     * @param left фиксируемое значение первого параметр метода
     * @param right фиксируемое значение второго параметр метода
     * @return построенный {@link Processable}
     */
    default @NonNull Processable processable(T left, U right) {
        return () -> process(left, right);
    }

    /**
     * Метод приведения {@link BiConsumable} к функции, возвращающей null как результат заданного типа
     * @return {@link Functional} с результатом null заданного типа
     * @param <X> тип левого параметра
     * @param <Y> тип правого параметра
     * @param <R> тип результата
     */
    default <X extends T, Y extends U, R> @NonNull BiFunctional<X, Y, R> functional() {
        return this::functionally;
    }

    /**
     * Метод приведения {@link BiConsumable} к функции, возвращающей null как результат заданного типа
     * @param clazz {@link Class} к которому приводится результат
     * @return {@link Functional} с результатом null заданного типа
     * @param <X> тип левого параметра
     * @param <Y> тип правого параметра
     * @param <R> тип результата
     */
    default <X extends T, Y extends U, R> @NonNull BiFunctional<X, Y, R> functional(@NonNull Class<? extends R> clazz) {
        Objects.requireNonNull(clazz, "BiConsumable::class - clazz is null");
        return this::functionally;
    }

    /**
     * Функция позволяет превратить метод от параметра к {@link BiConsumable} интерфейсу
     * @param consumable оборачиваемый метод
     * @return {@link Consumable} обёртка
     * @param <L> первый параметр метода
     * @param <R> второй параметр метода
     */
    static <L,R> @NonNull BiConsumable<L,R> biconsumable(@NonNull BiConsumable<L,R> consumable) {
        return Objects.requireNonNull(consumable, "BiConsumable::consumable - consumable is null");
    }

    /**
     * Функция осуществляет приведение {@link BiConsumer} к {@link BiConsumable}
     * @param biconsumer приводимый {@link BiConsumer} объект
     * @return {@link BiConsumable} обёртка
     * @param <T> тип первого параметра
     * @param <U> тип второго параметра
     */
    static @NonNull <T, U> BiConsumable<T, U> cast(@NonNull BiConsumer<T, U> biconsumer) {
        return Objects.requireNonNull(biconsumer, "BiConsumable::functional - biconsumer is null")::accept;
    }

    /**
     * Функция связывается с CompletableFuture и возвращает наружу BiConsumable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull BiConsumable<T, U> completable(@NonNull CompletableFuture<Void> completableFuture) {
        Objects.requireNonNull(completableFuture, "BiConsumable::completable - completableFuture is null");
        return (left, right) -> {
            try {
                if (!completableFuture.isDone()) {
                    this.process(left, right);
                    completableFuture.complete(null);
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

}
