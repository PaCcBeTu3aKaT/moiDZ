import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {
    private final String text;
    private int index;

    private JsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String text) {
        JsonParser parser = new JsonParser(text);
        Object value = parser.value();
        parser.space();
        if (parser.index != text.length()) parser.error("Лишний текст после JSON");
        return value;
    }

    private Object value() {
        space();
        if (index >= text.length()) throw error("Ожидалось значение");
        return switch (text.charAt(index)) {
            case '{' -> object();
            case '[' -> array();
            case '"' -> string();
            case 't' -> keyword("true", Boolean.TRUE);
            case 'f' -> keyword("false", Boolean.FALSE);
            case 'n' -> keyword("null", null);
            default -> {
                if (text.charAt(index) == '-' || digit(text.charAt(index))) yield number();
                throw error("Недопустимое значение");
            }
        };
    }

    private Map<String, Object> object() {
        consume('{');
        Map<String, Object> map = new LinkedHashMap<>();
        space();
        if (take('}')) return map;
        do {
            space();
            if (index >= text.length() || text.charAt(index) != '"') throw error("Ожидался строковый ключ");
            String key = string();
            space();
            consume(':');
            Object value = value();
            if (map.containsKey(key)) throw error("Повторяющийся ключ: " + key);
            map.put(key, value);
            space();
            if (take('}')) return map;
            consume(',');
        } while (true);
    }

    private List<Object> array() {
        consume('[');
        List<Object> list = new ArrayList<>();
        space();
        if (take(']')) return list;
        do {
            list.add(value());
            space();
            if (take(']')) return list;
            consume(',');
        } while (true);
    }

    private String string() {
        consume('"');
        StringBuilder out = new StringBuilder();
        while (index < text.length()) {
            char ch = text.charAt(index++);
            if (ch == '"') return out.toString();
            if (ch < 0x20) throw error("Неэкранированный управляющий символ");
            if (ch != '\\') { out.append(ch); continue; }
            if (index >= text.length()) throw error("Неоконченная escape-последовательность");
            char esc = text.charAt(index++);
            switch (esc) {
                case '"' -> out.append('"');
                case '\\' -> out.append('\\');
                case '/' -> out.append('/');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> out.append(unicode());
                default -> throw error("Неизвестная escape-последовательность");
            }
        }
        throw error("Строка не закрыта");
    }

    private char unicode() {
        if (index + 4 > text.length()) throw error("Неполный Unicode escape");
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int digit = Character.digit(text.charAt(index++), 16);
            if (digit < 0) throw error("Некорректный Unicode escape");
            value = (value << 4) | digit;
        }
        return (char) value;
    }

    private Object number() {
        int start = index;
        take('-');
        if (take('0')) {
            if (index < text.length() && digit(text.charAt(index))) throw error("Ведущий ноль в числе");
        } else {
            if (index >= text.length() || text.charAt(index) < '1' || text.charAt(index) > '9') throw error("Ожидалась цифра");
            while (index < text.length() && digit(text.charAt(index))) index++;
        }
        if (take('.')) {
            if (index >= text.length() || !digit(text.charAt(index))) throw error("Ожидалась дробная часть");
            while (index < text.length() && digit(text.charAt(index))) index++;
        }
        if (take('e') || take('E')) {
            if (!take('+')) take('-');
            if (index >= text.length() || !digit(text.charAt(index))) throw error("Ожидалась степень");
            while (index < text.length() && digit(text.charAt(index))) index++;
        }
        return new BigDecimal(text.substring(start, index));
    }

    private Object keyword(String word, Object value) {
        if (!text.startsWith(word, index)) throw error("Неизвестное ключевое слово");
        index += word.length();
        return value;
    }

    private void space() {
        while (index < text.length()) {
            char ch = text.charAt(index);
            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') index++;
            else break;
        }
    }

    private boolean take(char ch) {
        if (index < text.length() && text.charAt(index) == ch) { index++; return true; }
        return false;
    }

    private void consume(char ch) {
        space();
        if (!take(ch)) throw error("Ожидался символ '" + ch + "'");
    }

    private static boolean digit(char ch) { return ch >= '0' && ch <= '9'; }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " (позиция " + (index + 1) + ")");
    }
}
