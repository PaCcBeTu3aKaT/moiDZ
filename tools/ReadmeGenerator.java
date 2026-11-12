import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ReadmeGenerator {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path HOMEWORKS = ROOT.resolve("python/homeworks");

    public static void main(String[] args) throws IOException {
        if (!Files.isDirectory(ROOT.resolve("tools"))) {
            throw new IllegalStateException("Запускай программу из корня репозитория: java tools/ReadmeGenerator.java");
        }

        StringBuilder readme = new StringBuilder("""
                # Репозиторий для домашних заданий

                > Здесь хранятся мои домашние задания. Некоторые даже выполняют поставленную задачу. Наука пока не объяснила почему.

                **Текущее состояние:** репозиторий существует, следовательно, вероятность сдачи домашнего задания статистически отлична от нуля.

                ---

                ## 🐍 Введение в Python

                Никаких революций в файловой системе. Папка `python/homeworks` остаётся там, где и была.

                """);

        if (Files.isDirectory(HOMEWORKS)) {
            List<Path> entries = list(HOMEWORKS);
            long files;
            try (Stream<Path> stream = Files.walk(HOMEWORKS)) {
                files = stream.filter(Files::isRegularFile)
                        .filter(path -> !path.getFileName().toString().equals(".DS_Store"))
                        .count();
            }
            readme.append("**Файлов в `python/homeworks`:** ").append(files)
                    .append(". Кто сказал, что количество равно качеству? Никто. Но число красивое.\n\n");
            if (entries.isEmpty()) {
                readme.append("Пока пусто. Величественная тишина перед очередным дедлайном.\n\n");
            } else {
                readme.append("| Что лежит | Куда нажать |\n|---|---|\n");
                for (Path entry : entries) {
                    String label = entry.getFileName().toString();
                    String type = Files.isDirectory(entry) ? "📁 Папка" : "📄 Файл";
                    readme.append("| ").append(type).append(" | [")
                            .append(escapeMarkdown(label)).append("](")
                            .append(link(entry)).append(") |\n");
                }
                readme.append('\n');
            }
        } else {
            readme.append("Папка [`python/homeworks`](python/homeworks) ещё не появилась в локальной копии. Генератор её не создаёт и не перемещает.\n\n");
        }

        readme.append("""
                ## 🧪 Прочие формы академической активности

                Ниже перечислены другие папки в корне репозитория. Ничего не переносится и не переименовывается: у файлов и без этого тяжёлая жизнь.

                """);

        List<Path> folders = new ArrayList<>();
        for (Path entry : list(ROOT)) {
            String name = entry.getFileName().toString();
            if (Files.isDirectory(entry) && !name.startsWith(".")
                    && !name.equals("python") && !name.equals("tools") && !name.equals("assets")) {
                folders.add(entry);
            }
        }
        if (folders.isEmpty()) {
            readme.append("Пока ничего. Не каждую пустоту следует немедленно заполнять.\n\n");
        } else {
            for (Path folder : folders) {
                readme.append("- 📁 [").append(escapeMarkdown(folder.getFileName().toString()))
                        .append("](").append(link(folder)).append(")\n");
            }
            readme.append('\n');
        }

        readme.append("""
                ---

                ## ☕ Почему описание репозитория генерируется на Java?

                Потому что написать `README.md` руками было бы слишком разумно.

                Этот текст собирает [`ReadmeGenerator.java`](tools/ReadmeGenerator.java). GitHub не запускает Java вместо README: Java создаёт обычный Markdown, а GitHub его показывает. Это принципиальная разница между инженерией и попыткой убедить сайт выполнить программу силой мысли.

                **Запуск из корня репозитория (JDK 21 или 25):**

                ```bash
                java tools/ReadmeGenerator.java
                ```

                При наличии GitHub Actions обновление можно выполнять и автоматически. Программа изменяет **только `README.md`**, не трогая домашние задания.

                <sub>Документация репозитория была автоматизирована раньше, чем появилась объективная необходимость в её автоматизации.</sub>
                """);

        Path target = ROOT.resolve("README.md");
        String content = readme.toString();
        if (Files.exists(target) && Files.readString(target, StandardCharsets.UTF_8).equals(content)) {
            System.out.println("README.md уже актуален. Компьютер героически ничего не изменил.");
        } else {
            Files.writeString(target, content, StandardCharsets.UTF_8);
            System.out.println("README.md обновлён. Java снова выполнила работу текстового редактора.");
        }
    }

    private static List<Path> list(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();
        }
    }

    private static String link(Path path) {
        Path relative = ROOT.relativize(path);
        List<String> parts = new ArrayList<>();
        for (Path part : relative) {
            parts.add(URLEncoder.encode(part.toString(), StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return String.join("/", parts);
    }

    private static String escapeMarkdown(String value) {
        return value.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("[", "\\[")
                .replace("]", "\\]");
    }
}
