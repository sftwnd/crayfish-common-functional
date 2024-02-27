module sftwnd_crayfish_common_functional.test {
    requires sftwnd_crayfish_common_functional;
    requires org.junit.jupiter.api;
    requires org.mockito;
    opens com.github.sftwnd.crayfish.common.functional.test to org.junit.platform.commons;
}