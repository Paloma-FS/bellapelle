package com.bellapelle.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                // Banco já existe: apenas aplica ajustes de estrutura pendentes.
                migrarNumeroDaSala();
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

    /**
     * Migração para bancos criados antes do campo "numero" da sala.
     * Roda só uma vez: se a coluna já existe, não faz nada.
     * Salas antigas recebem o número que aparece no nome ("Sala 3" -> 3);
     * se o número já estiver em uso, recebem o próximo número livre.
     */
    private void migrarNumeroDaSala() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            if (colunaExiste(c, "sala", "numero")) {
                return;
            }

            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE sala ADD COLUMN numero INT");
            }

            Map<Long, String> nomes = new LinkedHashMap<>();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id_sala, nome FROM sala ORDER BY id_sala")) {
                while (rs.next()) {
                    nomes.put(rs.getLong(1), rs.getString(2));
                }
            }

            Map<Long, Integer> atribuidos = new LinkedHashMap<>();
            Set<Integer> usados = new HashSet<>();
            Pattern digitos = Pattern.compile("\\d{1,4}");

            // 1ª passada: usa o número contido no nome, se for válido e ainda livre.
            for (Map.Entry<Long, String> e : nomes.entrySet()) {
                Matcher m = digitos.matcher(e.getValue() == null ? "" : e.getValue());
                if (m.find()) {
                    int n = Integer.parseInt(m.group());
                    if (n > 0 && usados.add(n)) {
                        atribuidos.put(e.getKey(), n);
                    }
                }
            }

            // 2ª passada: quem ficou sem número recebe o próximo livre.
            int proximo = usados.stream().mapToInt(Integer::intValue).max().orElse(0);
            for (Long id : nomes.keySet()) {
                if (!atribuidos.containsKey(id)) {
                    proximo++;
                    atribuidos.put(id, proximo);
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE sala SET numero = ? WHERE id_sala = ?")) {
                for (Map.Entry<Long, Integer> e : atribuidos.entrySet()) {
                    ps.setInt(1, e.getValue());
                    ps.setLong(2, e.getKey());
                    ps.executeUpdate();
                }
            }

            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE sala ALTER COLUMN numero SET NOT NULL");
                st.execute("ALTER TABLE sala ADD CONSTRAINT uk_sala_numero UNIQUE (numero)");
            }
        }
    }

    private boolean colunaExiste(Connection c, String tabela, String coluna) throws SQLException {
        try (ResultSet cols = c.getMetaData().getColumns(null, null, tabela, null)) {
            while (cols.next()) {
                if (coluna.equalsIgnoreCase(cols.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}