package com.chaenggim;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class ChaenggimNoteApplicationTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("애플리케이션 컨텍스트가 뜬다")
    void contextLoads() {
    }

    @Test
    @DisplayName("스키마 실행 후 테이블 4개가 만들어진다")
    void schemaCreatesFourTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'",
                String.class);

        assertThat(tables).containsExactlyInAnyOrder("doctor", "visit", "question", "commitment");
    }
}
