package com.bellapelle.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class InicializadorBanco {

    private final DataSource dataSource;

    public InicializadorBanco(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void popularSeNecessario() {
        try {
            if (bancoJaTemTabelas()) {
                return;
            }

            var populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/bellapelle.sql"));
            populator.setSqlScriptEncoding("UTF-8");
            populator.execute(dataSource);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao inicializar o banco de dados.", e);
        }
    }

    private boolean bancoJaTemTabelas() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             ResultSet tabelas = conexao.getMetaData()
                     .getTables(null, null, "usuario", null)) {
            return tabelas.next();
        }
    }
}