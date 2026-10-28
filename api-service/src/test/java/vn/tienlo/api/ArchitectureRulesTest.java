package vn.tienlo.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {
    private final JavaClasses classes = new ClassFileImporter().importPackages("vn.tienlo.api");

    @Test
    void API_AC_001_webDoesNotDependOnPersistence() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..web..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..persistence..")
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void API_AC_002_domainDoesNotDependOnSpringOrPersistence() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "..persistence..")
                .allowEmptyShould(true);
        rule.check(classes);
    }
}
