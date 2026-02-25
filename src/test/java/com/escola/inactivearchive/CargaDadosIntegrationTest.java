package com.escola.inactivearchive;

import com.escola.inactivearchive.repository.AlunoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
public class CargaDadosIntegrationTest {

    @Autowired
    private AlunoRepository alunoRepository;

    @Test
    void cargaDadosDev_shouldImportWithoutErrors() {
        long count = alunoRepository.count();
        assertThat(count).isGreaterThan(0);
    }
}

