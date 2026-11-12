import java.io.IOException;
import java.net.URLEncoder;
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
    private static final Pattern PREVIOUS = Pattern.compile("<!-- jokes: (\\d+),(\\d+),(\\d+),(\\d+) -->");

    private static final String[] OPENINGS = {
        "Здесь хранятся мои домашние задания. Некоторые даже выполняют поставленную задачу. Наука пока не объяснила почему.",
        "Это мои домашние задания. Код, который работает, прошу не трогать: мы с ним только-только договорились.",
        "Архив домашних заданий. Если что-то запустилось с первого раза, прошу сообщить. Мне тоже интересно.",
        "Здесь лежат мои домашки. Часть написана осознанно. Остальную часть я пока не готов обсуждать без адвоката.",
        "Мои домашние задания. Задумывалось как учебный архив, а получилось собрание улик.",
        "Здесь хранится код, написанный в учебных целях. Какие именно цели были у кода — отдельный вопрос.",
        "Добро пожаловать. В некоторых файлах есть смысл. Он тщательно замаскирован, но надежда остаётся.",
        "Домашние задания в одном месте. Теперь хотя бы понятно, где искать последствия своих решений.",
        "Здесь мои домашки. Сроки сдачи, к сожалению, никогда не были такими гибкими, как синтаксис.",
        "Репозиторий с домашними заданиями. Я бы объяснил, как всё работает, но боюсь спугнуть.",
        "Собрал домашние задания в репозиторий. Теперь они выглядят организованнее, чем процесс их выполнения.",
        "Мои домашние задания. Некоторые программы работают. Считаю нужным зафиксировать это историческое событие."
    };
    private static final String[] PYTHON_JOKES = {
        "Здесь лежит Python. Синтаксис дружелюбный. Последствия — не всегда.",
        "Питон обещал, что всё будет просто. Не уточнил, кому именно.",
        "Здесь обитает код на Python. Большинство экземпляров безобидны, пока их не запустить.",
        "В этом разделе отступы имеют больше власти, чем здравый смысл.",
        "Написал три строчки. Две работают. Третья считает себя главной.",
        "Python позволяет писать кратко. Ошибаться, как выяснилось, тоже.",
        "Код на Python. Иногда даже понятно, что я хотел этим сказать.",
        "Здесь я изучаю Python. Python, похоже, изучает пределы моего терпения.",
        "Программа запустилась. Я посмотрел на неё. Она посмотрела на меня. Договорились не задавать вопросов.",
        "Змейка красивая. Вот только я просил её посчитать числа.",
        "Отступил на четыре пробела. От собственных принципов — чуть больше.",
        "Тут находятся упражнения по Python. И результаты отдельных переговоров с интерпретатором."
    };
    private static final String[] PROJECT_JOKES = {
        "Проект индивидуальный. По названию — индивидуальный. По ощущениям — нет.",
        "Здесь лежит индивидуальный проект. Масштаб страданий не уточняется.",
        "Раздел для индивидуального проекта. Звучит спокойно. На практике — как получится.",
        "Индивидуальный проект. В названии уже есть обещание. Выполнять его никто не торопится.",
        "Проект индивидуальный. Ответственность тоже. Удобно придумали.",
        "Здесь я делаю индивидуальный проект. Иногда проект делает вид, что согласен.",
        "Индивидуальный проект существует. Осталось выяснить, сколько раз его придётся переделывать.",
        "Это индивидуальный проект. Ведёт себя так, будто у него собственный учебный план.",
        "Папка индивидуального проекта. Сюда складывается всё, что пока рано показывать комиссии.",
        "Проект называется индивидуальным, потому что слово «коллективное бедствие» не поместилось в расписание.",
        "Здесь материалы индивидуального проекта. Некоторые уже стали частью истории. Желательно не уголовной.",
        "Индивидуальный проект. Я хотел сделать аккуратно. Потом открыл требования."
    };
    private static final String[] ENDINGS = {
        "Код работает. Я тоже удивлён.",
        "Программа выдала правильный ответ. ...Подождите, что?",
        "Первый успешный запуск. Всё, больше ничего не трогаем.",
        "Компьютер сделал именно то, что я написал. К сожалению, не то, что я хотел.",
        "Система документирования была автоматизирована раньше, чем появилась необходимость документировать систему.",
        "На вопрос «зачем здесь Java?» у меня есть ответ. Убедительным он не будет.",
        "Ошибок нет. Видимо, они сегодня не пришли.",
        "Всё запустилось... Так. Ничего не нажимаем.",
        "Если программа работает, не надо выяснять почему. Она может передумать.",
        "Я проверил. Работает. Повторную проверку считаю неоправданным риском.",
        "README обновился сам. Прогресс технический, причины психологические.",
        "Программа решила задачу. Я даже на секунду почувствовал себя программистом."
    };

    public static void main(String[] args) throws IOException {
        if (!Files.isDirectory(ROOT.resolve("tools"))) {
            throw new IllegalStateException("Открой корень moiDZ и выполни: java tools/ReadmeGenerator.java");
        }
        int[] previous = readPrevious();
        int[] choice = {
            choose(OPENINGS.length, previous[0]),
            choose(PYTHON_JOKES.length, previous[1]),
            choose(PROJECT_JOKES.length, previous[2]),
            choose(ENDINGS.length, previous[3])
        };

        StringBuilder out = new StringBuilder();
        out.append("<!-- Этот файл создан Java. Для изменения текста редактируй tools/ReadmeGenerator.java -->\n");
        out.append(String.format("<!-- jokes: %d,%d,%d,%d -->\n", choice[0], choice[1], choice[2], choice[3]));
        out.append("# Репозиторий для домашних заданий\n\n");
        out.append("![Псевдотерминал Windows 10: Java документирует домашние задания](assets/terminal-win10.svg)\n\n");
        out.append("> ").append(OPENINGS[choice[0]]).append("\n\n");
        out.append("## 🐍 Введение в Python\n\n");
        out.append(PYTHON_JOKES[choice[1]]).append("\n\n");
        appendDirectory(out, PYTHON, "python");
        out.append("## 🛠️ Индивидуальный проект\n\n");
        out.append(PROJECT_JOKES[choice[2]]).append("\n\n");
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
        out.append("---\n\n## 🎲 Вердикт дня\n\n> ").append(ENDINGS[choice[3]]).append("\n\n");
        out.append("<sub>README создаётся на Java и обновляется через GitHub Actions. Потому что написать Markdown вручную было бы подозрительно разумно.</sub>\n");
        Files.writeString(README, out, StandardCharsets.UTF_8);
        System.out.println("README.md обновлён. Java снова выполнила работу текстового редактора.");
    }

    private static int[] readPrevious() throws IOException {
        int[] result = {-1, -1, -1, -1};
        if (!Files.exists(README)) return result;
        Matcher matcher = PREVIOUS.matcher(Files.readString(README, StandardCharsets.UTF_8));
        if (matcher.find()) for (int i = 0; i < 4; i++) result[i] = Integer.parseInt(matcher.group(i + 1));
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
        out.append("| Материал | Ссылка |\n|---|---|\n");
        for (Path file : files) {
            String relative = folder.relativize(file).toString().replace('\\', '/');
            out.append("| ").append(escape(relative)).append(" | [Открыть](")
                .append(link(file)).append(") |\n");
        }
        out.append('\n');
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
