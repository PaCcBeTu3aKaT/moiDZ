import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JokeValidator {
    private static final Set<String> VARIABLES = Set.of("repo_name", "java_percent", "python_percent", "file_count", "folder_count", "java_bytes", "jvm_version");
    private static final Pattern VARIABLE = Pattern.compile("\\{([^{}]+)\\}");
    private JokeValidator() {}

    public static List<String> load(Path root, String category) throws IOException {
        Path path = root.resolve("tools/jokes/" + category + ".json");
        Object value = JsonParser.parse(Files.readString(path, StandardCharsets.UTF_8));
        if (!(value instanceof Map<?, ?> map) || !category.equals(map.get("category")) || !(map.get("phrases") instanceof List<?> list)) {
            throw new IllegalArgumentException(path + ": неверная структура категории");
        }
        if (list.size() < 2) throw new IllegalArgumentException(path + ": нужно хотя бы две фразы");
        Set<String> unique = new HashSet<>();
        for (Object item : list) {
            if (!(item instanceof String phrase) || phrase.isBlank()) throw new IllegalArgumentException(path + ": пустая или нестроковая фраза");
            if (!unique.add(phrase)) throw new IllegalArgumentException(path + ": повторяющаяся фраза: " + phrase);
            Matcher matcher = VARIABLE.matcher(phrase);
            while (matcher.find()) if (!VARIABLES.contains(matcher.group(1))) throw new IllegalArgumentException(path + ": неизвестная переменная " + matcher.group());
        }
        return list.stream().map(String.class::cast).toList();
    }

    public static void validateAll(Path root) throws IOException {
        for (String category : List.of("intro", "python", "individual-project", "java", "verdict")) load(root, category);
        Path path = root.resolve("tools/diagnostics/messages.json");
        Object parsed = JsonParser.parse(Files.readString(path, StandardCharsets.UTF_8));
        if (!(parsed instanceof Map<?, ?> map) || !"diagnostics".equals(map.get("category")) || !(map.get("statuses") instanceof Map<?, ?> statuses)) {
            throw new IllegalArgumentException(path + ": неверная структура диагностики");
        }
        for (String key : List.of("ok", "warn", "fail", "info")) {
            if (!(statuses.get(key) instanceof List<?> list) || list.isEmpty()) throw new IllegalArgumentException(path + ": пустая категория " + key);
            for (Object text : list) if (!(text instanceof String s) || s.isBlank()) throw new IllegalArgumentException(path + ": пустая диагностическая строка");
        }
    }

    public static void main(String[] args) throws Exception {
        validateAll(Path.of("").toAbsolutePath());
        System.out.println("Проверка JSON завершилась успешно. Юмор синтаксически допустим.");
    }
}
