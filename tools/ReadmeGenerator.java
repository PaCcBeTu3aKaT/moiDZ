import java.io.IOException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ReadmeGenerator {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path PYTHON = ROOT.resolve("python");
    private static final Path README = ROOT.resolve("README.md");
    private static final String[] PROJECT_NAMES = {"индивидуальныйПроект", "индивидуальный проект"};
    private static final String[] SPECIALTY_NAMES = {"введение в специальность", "введение специальность"};
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern PREVIOUS = Pattern.compile("<!-- jokes: (\\d+),(\\d+),(\\d+),(\\d+)(?:,(\\d+))? -->");



    private static List<String> loadPhrases(String filename) throws IOException {
        Path source = ROOT.resolve("tools/jokes").resolve(filename + ".json");
        Object json = JsonParser.parse(Files.readString(source, StandardCharsets.UTF_8));
        if (!(json instanceof java.util.Map<?, ?> map)) {
            throw new IllegalArgumentException(source + ": ожидается JSON-объект");
        }
        if (!filename.equals(map.get("category"))) {
            throw new IllegalArgumentException(source + ": неверная категория");
        }
        if (!(map.get("phrases") instanceof List<?> values) || values.size() < 2) {
            throw new IllegalArgumentException(source + ": нужно минимум две фразы");
        }
        List<String> result = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof String phrase) || phrase.isBlank()) {
                throw new IllegalArgumentException(source + ": фразы должны быть непустыми строками");
            }
            result.add(phrase);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        if (!Files.isDirectory(ROOT.resolve("tools"))) {
            throw new IllegalStateException("Открой корень moiDZ и выполни: java tools/ReadmeGenerator.java");
        }
        List<String> openings = loadPhrases("intro");
        List<String> pythonJokes = loadPhrases("python");
        List<String> projectJokes = loadPhrases("individual-project");
        List<String> endings = loadPhrases("verdict");
        List<String> javaJokes = loadPhrases("java");
        String javaPercent = githubJavaPercent();
        int[] previous = readPrevious();
        int[] choice = {
            choose(openings.size(), previous[0]),
            choose(pythonJokes.size(), previous[1]),
            choose(projectJokes.size(), previous[2]),
            choose(endings.size(), previous[3]),
            choose(javaJokes.size(), previous[4])
        };

        StringBuilder out = new StringBuilder();
        out.append("<!-- Этот файл создан Java. Шутки находятся в tools/jokes/*.json -->\n");
        out.append(String.format("<!-- jokes: %d,%d,%d,%d,%d -->\n", choice[0], choice[1], choice[2], choice[3], choice[4]));
        out.append("# Репозиторий для домашних заданий\n\n");
        out.append("![Псевдотерминал Windows 10: Java документирует домашние задания](assets/terminal-win10.svg)\n\n");
        out.append("> ").append(render(openings.get(choice[0]), javaPercent)).append("\n\n");
        out.append("## 🐍 Введение в Python\n\n");
        out.append(render(pythonJokes.get(choice[1]), javaPercent)).append("\n\n");
        appendDirectory(out, PYTHON, "python");
        out.append("## 🛠️ Индивидуальный проект\n\n");
        out.append(render(projectJokes.get(choice[2]), javaPercent)).append("\n\n");
        boolean found = false;
        for (String name : PROJECT_NAMES) {
            Path folder = ROOT.resolve(name);
            if (Files.isDirectory(folder)) {
                if (found) out.append("### ").append(escape(name)).append("\n\n");
                appendDirectory(out, folder, name);
                found = true;
            }
        }
        for (String name : SPECIALTY_NAMES) {
            Path folder = ROOT.resolve(name);
            if (Files.isDirectory(folder)) {
                out.append("### Введение в специальность\n\n");
                appendDirectory(out, folder, name);
                found = true;
            }
        }
        if (!found) out.append("Файлы ещё не появились. README уже встречает их с распростёртыми папками.\n\n");
        out.append("## ☕ Java\n\n");
        out.append(render(javaJokes.get(choice[4]), javaPercent)).append("\n\n");
        if (javaPercent != null) {
            out.append("**Доля Java в распознанном коде репозитория:** ").append(javaPercent).append("%.\n\n");
            double share = Double.parseDouble(javaPercent.replace(',', '.'));
            String[] remarks = share >= 75 ? new String[]{
                "Java заняла почти всё. Python теперь числится в отделе исторического наследия.",
                "Судя по статистике, README постепенно поглощает репозиторий, который должен был описывать."
            } : share >= 50 ? new String[]{
                "Контрольный пакет кода у Java. Переговоры с Python ведутся в одностороннем порядке.",
                "Java перешагнула половину. По документам это всё ещё репозиторий домашних заданий."
            } : share >= 25 ? new String[]{
                "До большинства Java пока не добралась. Зато уже отвечает за публичные заявления всех остальных.",
                "Python ещё держит оборону. Java тем временем самостоятельно подсчитывает собственное влияние."
            } : new String[]{
                "Java пока в меньшинстве. Но именно меньшинство пишет статистику.",
                "Кода на Java немного. Зато он подготовил собственный пресс-релиз."
            };
            int remarkIndex = Math.floorMod(choice[4], remarks.length);
            out.append("> ").append(remarks[remarkIndex]).append("\n\n");
        } else {
            out.append("**Доля Java:** статистика GitHub пока недоступна. Число выдумывать не будем.\n\n");
        }
        out.append("Генератор: [`tools/ReadmeGenerator.java`](tools/ReadmeGenerator.java) · Java 21 · GitHub Actions.\n\n");
        out.append("---\n\n## 🎲 Вердикт дня\n\n> ").append(render(endings.get(choice[3]), javaPercent)).append("\n\n");
        Files.writeString(README, out, StandardCharsets.UTF_8);
        System.out.println("README.md обновлён. Java снова выполнила работу текстового редактора.");
    }

    private static String render(String template, String javaPercent) {
        java.util.Map<String, String> variables = java.util.Map.of(
            "repo_name", "moiDZ",
            "java_percent", javaPercent == null ? "недоступно" : javaPercent
        );
        Matcher matcher = Pattern.compile("\\{([a-z_]+)\\}").matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = variables.get(matcher.group(1));
            if (value == null) throw new IllegalArgumentException("Неизвестная переменная: " + matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static int[] readPrevious() throws IOException {
        int[] result = {-1, -1, -1, -1, -1};
        if (!Files.exists(README)) return result;
        Matcher matcher = PREVIOUS.matcher(Files.readString(README, StandardCharsets.UTF_8));
        if (matcher.find()) for (int i = 0; i < 5; i++) if (matcher.group(i + 1) != null) result[i] = Integer.parseInt(matcher.group(i + 1));
        return result;
    }

    private static int choose(int size, int old) {
        int next = RANDOM.nextInt(size - (old >= 0 && old < size ? 1 : 0));
        return old >= 0 && old < size && next >= old ? next + 1 : next;
    }

    private static void appendDirectory(StringBuilder out, Path folder, String name) throws IOException {
        if (!Files.isDirectory(folder)) {
            out.append("Материалы пока не загружены.\n\n");
            return;
        }
        List<Path> files;
        try (Stream<Path> walk = Files.walk(folder)) {
            files = walk.filter(Files::isRegularFile)
                .filter(p -> !List.of(".DS_Store", ".gitkeep").contains(p.getFileName().toString()))
                .sorted(Comparator.comparing(p -> ROOT.relativize(p).toString())).toList();
        }
        out.append("[Открыть папку `").append(escape(name)).append("`](").append(link(folder)).append(")\n\n");
        out.append("**Файлов:** ").append(files.size()).append("\n\n");
        if (files.isEmpty()) {
            out.append("Пока пусто. Папка морально готова.\n\n");
            return;
        }
        List<Path> directories;
        try (Stream<Path> children = Files.list(folder)) {
            directories = children.filter(Files::isDirectory)
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .toList();
        }
        if (!directories.isEmpty()) {
            out.append("| Папка | Файлов внутри |\n|---|---:|\n");
            for (Path directory : directories) {
                long count;
                try (Stream<Path> nested = Files.walk(directory)) {
                    count = nested.filter(Files::isRegularFile)
                        .filter(p -> !List.of(".DS_Store", ".gitkeep", "пельмени").contains(p.getFileName().toString()))
                        .count();
                }
                out.append("| [").append(escape(directory.getFileName().toString())).append("/](")
                    .append(link(directory)).append(") | ").append(count).append(" |\n");
            }
            out.append('\n');
        } else {
            out.append("Внутри пока нет подпапок. Перечень отдельных файлов здесь не выводится.\n\n");
        }
    }

    private static String githubJavaPercent() {
        String repo = System.getenv("GITHUB_REPOSITORY");
        if (repo == null || !repo.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) return null;
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + repo + "/languages"))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "moiDZ-readme-generator")
                .timeout(java.time.Duration.ofSeconds(12))
                .GET();
            String token = System.getenv("GITHUB_TOKEN");
            if (token != null && !token.isBlank()) request.header("Authorization", "Bearer " + token);
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() != 200) return null;
                Matcher match = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+)").matcher(response.body());
                long all = 0;
                long java = 0;
                while (match.find()) {
                    long bytes = Long.parseLong(match.group(2));
                    all += bytes;
                    if (match.group(1).equals("Java")) java = bytes;
                }
                if (all <= 0) return null;
                return String.format(Locale.forLanguageTag("ru-RU"), "%.1f", 100.0 * java / all);
            }
        } catch (Exception e) {
            System.err.println("Не удалось получить статистику языков: " + e.getMessage());
            return null;
        }
    }

    private static String link(Path path) {
        List<String> parts = new ArrayList<>();
        for (Path part : ROOT.relativize(path)) {
            parts.add(URLEncoder.encode(part.toString(), StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return String.join("/", parts);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|")
            .replace("[", "\\[").replace("]", "\\]");
    }
}
