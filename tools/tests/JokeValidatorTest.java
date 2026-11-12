import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class JokeValidatorTest {
    public static void main(String[] args) throws Exception {
        Path root = Path.of("").toAbsolutePath();
        JokeValidator.validateAll(root);
        for (String name : List.of("intro", "python", "individual-project", "java", "verdict")) {
            if (JokeValidator.load(root, name).size() < 2) throw new AssertionError(name);
        }
        if (!"привет".equals(((java.util.Map<?, ?>)JsonParser.parse("{\"x\":\"\\u043f\\u0440\\u0438\\u0432\\u0435\\u0442\"}")).get("x"))) throw new AssertionError("Unicode");
        expectError(() -> JsonParser.parse("{\"broken\": [1,]}"));
        expectError(() -> JsonParser.parse("{\"same\": 1, \"same\": 2}"));
        Path temporary = Files.createTempDirectory("joke-validator-");
        try {
            Path jokes = temporary.resolve("tools/jokes");
            Files.createDirectories(jokes);
            Files.writeString(jokes.resolve("intro.json"), "{\"category\":\"intro\",\"phrases\":[\"один\",\"{unknown_variable}\"]}", StandardCharsets.UTF_8);
            expectError(() -> JokeValidator.load(temporary, "intro"));
            Files.writeString(jokes.resolve("intro.json"), "{\"category\":\"intro\",\"phrases\":[\"один\",\"один\"]}", StandardCharsets.UTF_8);
            expectError(() -> JokeValidator.load(temporary, "intro"));
        } finally {
            Files.deleteIfExists(temporary.resolve("tools/jokes/intro.json"));
            Files.deleteIfExists(temporary.resolve("tools/jokes"));
            Files.deleteIfExists(temporary.resolve("tools"));
            Files.deleteIfExists(temporary);
        }
        System.out.println("Все тесты генератора шуток пройдены. Смех не гарантируется.");
    }
    private static void expectError(Checked task) throws Exception {
        try {task.run();throw new AssertionError("Ожидалась ошибка");}
        catch (IllegalArgumentException expected) { }
    }
    interface Checked { void run() throws Exception; }
}
