package com.uniminuto.clinica.support;

import com.uniminuto.clinica.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;

/** Lectura y validación tolerante de cuerpos JSON (Map) enviados por el frontend. */
public final class In {

    private In() {
    }

    public static String str(Map<String, ?> m, String k) {
        if (m == null) {
            return null;
        }
        Object v = m.get(k);
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    public static String req(Map<String, ?> m, String k, String label) {
        String s = str(m, k);
        if (s == null) {
            throw new BadRequestException("El campo '" + label + "' es obligatorio.");
        }
        return s;
    }

    public static Long lng(Map<String, ?> m, String k) {
        String s = str(m, k);
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new BadRequestException("El campo '" + k + "' debe ser un número entero.");
        }
    }

    public static Long reqLng(Map<String, ?> m, String k, String label) {
        Long v = lng(m, k);
        if (v == null) {
            throw new BadRequestException("El campo '" + label + "' es obligatorio.");
        }
        return v;
    }

    public static Integer intg(Map<String, ?> m, String k) {
        Long v = lng(m, k);
        if (v == null) {
            return null;
        }
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
            throw new BadRequestException("El campo '" + k + "' está fuera de rango.");
        }
        return v.intValue();
    }

    public static BigDecimal dec(Map<String, ?> m, String k) {
        String s = str(m, k);
        return s == null ? null : parseDec(s, k);
    }

    public static BigDecimal reqDec(Map<String, ?> m, String k, String label) {
        BigDecimal v = dec(m, k);
        if (v == null) {
            throw new BadRequestException("El campo '" + label + "' es obligatorio.");
        }
        return v;
    }

    /** Acepta 1234.56 o 1234,56 (sin separador de miles). */
    public static BigDecimal parseDec(String s, String label) {
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new BadRequestException("El valor '" + s + "' del campo '" + label + "' no es un número válido.");
        }
    }

    public static LocalDate date(Map<String, ?> m, String k) {
        String s = str(m, k);
        return s == null ? null : parseDate(s, k);
    }

    public static LocalDate reqDate(Map<String, ?> m, String k, String label) {
        LocalDate d = date(m, k);
        if (d == null) {
            throw new BadRequestException("El campo '" + label + "' es obligatorio.");
        }
        return d;
    }

    /** yyyy-MM-dd, dd/MM/yyyy, o un datetime ISO (se toma la fecha). */
    public static LocalDate parseDate(String s, String label) {
        String t = s.trim();
        try {
            if (t.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return LocalDate.parse(t.substring(0, 10));
            }
            if (t.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] p = t.split("/");
                return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            }
        } catch (java.time.DateTimeException e) {
            // cae al error de abajo
        }
        throw new BadRequestException("La fecha '" + s + "' del campo '" + label
                + "' no es válida (use AAAA-MM-DD o DD/MM/AAAA).");
    }

    public static LocalDateTime dateTime(Map<String, ?> m, String k) {
        String s = str(m, k);
        return s == null ? null : parseDateTime(s, k);
    }

    public static LocalDateTime reqDateTime(Map<String, ?> m, String k, String label) {
        LocalDateTime d = dateTime(m, k);
        if (d == null) {
            throw new BadRequestException("El campo '" + label + "' es obligatorio.");
        }
        return d;
    }

    /** ISO local ("2026-11-20T08:30"), con espacio, o con zona/offset (se pasa a hora local del servidor). */
    public static LocalDateTime parseDateTime(String s, String label) {
        String t = s.trim().replace(' ', 'T');
        try {
            if (t.endsWith("Z") || t.matches(".*[+-]\\d{2}:\\d{2}$")) {
                return OffsetDateTime.parse(t).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            }
            if (t.length() == 10) {
                return LocalDate.parse(t).atStartOfDay();
            }
            return LocalDateTime.parse(t);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La fecha/hora '" + s + "' del campo '" + label + "' no es válida.");
        }
    }

    public static boolean bool(Map<String, ?> m, String k, boolean def) {
        String s = str(m, k);
        if (s == null) {
            return def;
        }
        return s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("si") || s.equalsIgnoreCase("sí");
    }

    public static long toLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    public static int toInt(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }

    public static BigDecimal toDec(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal b) {
            return b;
        }
        return new BigDecimal(String.valueOf(o));
    }
}
