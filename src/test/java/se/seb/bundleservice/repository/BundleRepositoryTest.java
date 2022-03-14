package se.seb.bundleservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.seb.bundleservice.model.BundleName;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = BundleRepository.class)
class BundleRepositoryTest {

    @Autowired
    private BundleRepository bundleRepository;

    @Test
    void verifyExistingBundlesByName() {
        assertThat(bundleRepository.getBundleByName(BundleName.JUNIOR_SAVER).getName()).isEqualTo("Junior Saver");
        assertThat(bundleRepository.getBundleByName(BundleName.STUDENT).getName()).isEqualTo("Student");
        assertThat(bundleRepository.getBundleByName(BundleName.CLASSIC).getName()).isEqualTo("Classic");
        assertThat(bundleRepository.getBundleByName(BundleName.CLASSIC_PLUS).getName()).isEqualTo("Classic Plus");
        assertThat(bundleRepository.getBundleByName(BundleName.GOLD).getName()).isEqualTo("Gold");
    }
}