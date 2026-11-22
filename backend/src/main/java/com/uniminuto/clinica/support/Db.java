package com.uniminuto.clinica.support;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.uniminuto.clinica.exception.BadRequestException;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos con SQL explícito (JdbcTemplate) para los módulos nuevos.
 * Participa en las transacciones de Spring (@Transactional / TransactionTemplate)
 * porque comparte el DataSource con JPA. Normaliza los tipos para que el JSON
 * sea estable: fechas como texto ISO y BIGINT UNSIGNED como Long.
 */
@Component
public class Db {

    private final JdbcTemplate jdbc;

    public Db(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list(String sql, Object... args) {
        List<Map<String, Object>> raw = jdbc.queryForList(sql, args);
        List<Map<String, Object>> out = new ArrayList<>(raw.size());
        for (Map<String, Object> row : raw) {
            out.add(norm(row));
        }
        return out;
    }

    /** Primera fila o null. */
    public Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> l = list(sql, args);
        return l.isEmpty() ? null : l.get(0);
    }

    /** Primera fila o error 404 con el mensaje dado. */
    public Map<String, Object> must(String notFoundMessage, String sql, Object... args) {
        Map<String, Object> r = one(sql, args);
        if (r == null) {
            throw new BadRequestException(notFoundMessage, HttpStatus.NOT_FOUND);
        }
        return r;
    }

    public <T> T scalar(Class<T> type, String sql, Object... args) {
        List<T> l = jdbc.queryForList(sql, type, args);
        return l.isEmpty() ? null : l.get(0);
    }

    public long count(String sql, Object... args) {
        Long v = scalar(Long.class, sql, args);
        return v == null ? 0L : v;
    }

    public int update(String sql, Object... args) {
        return jdbc.update(sql, args);
    }

    /** INSERT que devuelve el id autogenerado. */
    public long insert(String sql, Object... args) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? 0L : key.longValue();
    }

    private static Map<String, Object> norm(Map<String, Object> row) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Timestamp ts) {
                v = ts.toLocalDateTime().toString();
            } else if (v instanceof java.sql.Date d) {
                v = d.toLocalDate().toString();
            } else if (v instanceof java.sql.Time t) {
                v = t.toLocalTime().toString();
            } else if (v instanceof LocalDateTime || v instanceof LocalDate || v instanceof LocalTime) {
                v = v.toString();
            } else if (v instanceof BigInteger bi) {
                v = bi.longValue();
            }
            out.put(camel(e.getKey()), v);
        }
        return out;
    }

    /** stock_actual -> stockActual (el JSON de los módulos nuevos usa camelCase como el resto de la API). */
    private static String camel(String k) {
        if (k.indexOf('_') < 0) {
            return k;
        }
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char ch : k.toCharArray()) {
            if (ch == '_') {
                up = true;
            } else if (up) {
                sb.append(Character.toUpperCase(ch));
                up = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
