/**
 * Functional interfaces provide target types for lambda expressions and method references with throws support
 */
@SuppressWarnings("requires-transitive-automatic")
module sftwnd_crayfish_common_functional {
    requires static lombok;
    requires static transitive com.github.spotbugs.annotations;
    exports com.github.sftwnd.crayfish.common.functional;
}