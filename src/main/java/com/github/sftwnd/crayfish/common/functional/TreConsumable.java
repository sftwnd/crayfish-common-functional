package com.github.sftwnd.crayfish.common.functional;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация функционального интерфейса с методом от трёх параметров
 * @param <T> тип первого (левого) параметра
 * @param <U> тип второго (центрального) параметра
 * @param <V> тип третьего (правого) параметра
 * Used sonar warnings:
 *      java:S112   Generic exceptions should never be thrown
 */
public interface TreConsumable<T, U, V> {

    /**
     * Применение метода к заданным аргументам с декларацией пробрасываемого исключения
     * @param left первый параметр метода
     * @param middle второй параметр метода
     * @param right третий параметр метода
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    void process(T left, U middle, V right) throws Exception; //NOSONAR java:S112 Generic exceptions should never be thrown

    /**
     * Применение метода к заданным аргументам
     * @param left первый параметр метода
     * @param middle второй параметр метода
     * @param right третий параметр метода
     */
    @SneakyThrows
    default void accept(T left, U middle, V right) {
        process(left, middle, right);
    }

    /**
     * Применение метода к заданным аргументам с декларацией пробрасываемого исключения
     * В качестве результата выполнения вернётся Void
     * @param left первый параметр метода
     * @param middle второй параметр метода
     * @param right третий параметр метода
     * @return Void как результат выполнения
     * @throws Exception исключение, произошедшее в результате исполнения
     */
    default Void call(T left, U middle, V right) throws Exception {
        return functionally(left, middle, right);
    }

    /**
     * Применение метода к заданным аргументам и возвращает null заданного типа
     * @param left первый параметр метода
     * @param middle второй параметр метода
     * @param right третий параметр метода
     * @return null, как значение любого желаемого
     * @throws Exception исключение, произошедшее в результате исполнения
     * @param <R> тип результата (значение всегда равно null, но заданного типа)
     */
    default <R> @Nullable R functionally(T left, U middle, V right) throws Exception {
        process(left, middle, right);
        return null;
    }

    /**
     * Создаёт {@link BiConsumable}, который передаёт свои атрибуты в правые параметры, а левый считается предустановленным
     * @param left фиксируемое значение первого параметр метода
     * @return построенный {@link BiConsumable}
     */
    default @NonNull BiConsumable<U, V> left(T left) {
        return (middle, right) -> process(left, middle, right);
    }

    /**
     * Создаёт {@link BiConsumable}, который передаёт свои атрибуты в крайние параметры, а центральный считается предустановленным
     * @param middle фиксируемое значение первого параметр метода
     * @return построенный {@link BiConsumable}
     */
    default @NonNull BiConsumable<T, V> middle(U middle) {
        return (left, right) -> process(left, middle, right);
    }

    /**
     * Создаёт {@link BiConsumable}, который передаёт свои атрибуты в левые параметры, а правый считается предустановленным
     * @param right фиксируемое значение первого параметр метода
     * @return построенный {@link BiConsumable}
     */
    default @NonNull BiConsumable<T, U> right(V right) {
        return (left, middle) -> process(left, middle, right);
    }

    /**
     * Создаёт {@link Processable}, который при вызове подставляет заданные значения в параметры вызова метода process
     * @param left фиксируемое значение левого параметр метода
     * @param middle фиксируемое значение центрального параметр метода
     * @param right фиксируемое значение правого параметр метода
     * @return построенный {@link Processable}
     */
    default @NonNull Processable processable(T left, U middle, V right) {
        return () -> process(left, middle, right);
    }

    /**
     * Метод приведения {@link TreConsumable} к функции, возвращающей null как результат заданного типа
     * @return {@link Functional} с результатом null заданного типа
     * @param <X> тип левого параметра
     * @param <Y> тип центрального параметра
     * @param <Z> тип правого параметра
     * @param <R> тип результата
     */
    default <X extends T, Y extends U, Z extends V, R> @NonNull TreFunctional<X, Y, Z, R> functional() {
        return this::functionally;
    }

    /**
     * Метод приведения {@link TreConsumable} к функции, возвращающей null как результат заданного типа
     * @param clazz {@link Class} к которому приводится результат
     * @return {@link Functional} с результатом null заданного типа
     * @param <X> тип левого параметра
     * @param <Y> тип центрального параметра
     * @param <Z> тип правого параметра
     * @param <R> тип результата
     */
    default <X extends T, Y extends U, Z extends V, R> @NonNull TreFunctional<X, Y, Z, R> functional(@NonNull Class<? extends R> clazz) {
        Objects.requireNonNull(clazz, "TreConsumable::class - clazz is null");
        return this::functionally;
    }

    /**
     * Функция позволяет превратить метод от параметра к {@link TreConsumable} интерфейсу
     * @param treconsumable оборачиваемый метод
     * @return {@link Consumable} обёртка
     * @param <T> тип левого параметра
     * @param <U> тип центрального параметра
     * @param <V> тип правого параметра
     */
    static <T, U, V> @NonNull TreConsumable<T, U, V> treconsumable(@NonNull TreConsumable<T, U, V> treconsumable) {
        return Objects.requireNonNull(treconsumable, "TreConsumable::treconsumable - consumable is null");
    }

    /**
     * Функция связывается с CompletableFuture и возвращает наружу TreConsumable. Используется вызов без результата
     * и completableFuture заполняется null в случае успешного выполнения, но вот при возникновении исключения мы
     * complete-им future этим исключением.
     * @param completableFuture связываемая CompletableFuture
     * @return Consumer для вызова функции
     */
    default @NonNull TreConsumable<T, U, V> completable(@NonNull CompletableFuture<?> completableFuture) {
        Objects.requireNonNull(completableFuture, "TreConsumable::completable - completableFuture is null");
        return (left, middle, right) -> {
            try {
                if (!completableFuture.isDone()) {
                    this.process(left, middle, right);
                    completableFuture.complete(null);
                }
            } catch (Exception exception) {
                completableFuture.completeExceptionally(exception);
            }
        };
    }

    /**
     * Returns a composed {@code TreConsumable} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code TreConsumable} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default TreConsumable<T, U, V> andThen(TreConsumable<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after, "TreConsumable::andThen = after is null");

        return (l, m, r) -> {
            accept(l, m, r);
            after.accept(l, m, r);
        };
    }

}
