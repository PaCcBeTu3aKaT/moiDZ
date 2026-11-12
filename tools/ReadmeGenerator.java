import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ReadmeGenerator {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<String> CATEGORIES = List.of("intro", "python", "individual-project", "java", "verdict");
    private static final List<String> PROJECTS = List.of("индивидуальныйПроект", "индивидуальный проект", "введение в специальность", "введение специальность");
    private static final Set<String> IGNORE = Set.of(".gitkeep", ".DS_Store", "пельмени");
    private static final Set<String> SOURCE_EXTENSIONS = Set.of(".java", ".py", ".c", ".cpp", ".h", ".hpp", ".cs", ".rs", ".pas", ".asm", ".js", ".ts");
    private static final Pattern PREVIOUS = Pattern.compile("<!-- jokes: ([0-9,-]+) -->");
    private static final Pattern VARIABLE = Pattern.compile("\\{([a-z_]+)\\}");

    private record Stats(long javaBytes, long allBytes, double javaShare, double pythonShare, boolean available) {}
    private record Scan(int files, int folders, long codeLines, long sizeBytes, int todoCount, int maxDepth) {}
    private record Hazard(String name, Path path, int points, Scan scan) {}

    public static void main(String[] args) throws Exception {
        JokeValidator.validateAll(ROOT);
        List<List<String>> jokes = new ArrayList<>();
        for (String category : CATEGORIES) jokes.add(JokeValidator.load(ROOT, category));
        Path readme = ROOT.resolve("README.md");
        int[] previous = previous(readme);
        int[] choices = new int[CATEGORIES.size()];
        for (int i = 0; i < choices.length; i++) choices[i] = choose(jokes.get(i).size(), previous[i]);
        Stats stats = languages();
        Scan python = scan(ROOT.resolve("python"));
        Scan project = scanProject();
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("repo_name", "moiDZ");
        vars.put("java_percent", stats.available ? fmt(stats.javaShare) : "неизвестно");
        vars.put("python_percent", stats.available ? fmt(stats.pythonShare) : "неизвестно");
        vars.put("java_bytes", String.valueOf(stats.javaBytes));
        vars.put("file_count", String.valueOf(python.files + project.files));
        vars.put("folder_count", String.valueOf(python.folders + project.folders));
        vars.put("jvm_version", System.getProperty("java.version"));

        List<String> diagnostics = diagnostics();
        List<Hazard> hazards = new ArrayList<>();
        hazards.add(hazard("Введение в Python", ROOT.resolve("python"), python));
        List<Path> existingProjects = new ArrayList<>();
        for (String name : PROJECTS) if (Files.isDirectory(ROOT.resolve(name))) existingProjects.add(ROOT.resolve(name));
        for (Path path : existingProjects) hazards.add(hazard("Индивидуальный проект", path, scan(path)));
        if (existingProjects.isEmpty()) hazards.add(new Hazard("Индивидуальный проект", null, 0, new Scan(0,0,0,0,0,0)));

        Path toolFolder = ROOT.resolve("tools");
        long infra = codeLines(toolFolder, ".java");
        long study = sourceLines(ROOT.resolve("python"));
        for (Path folder : existingProjects) study += sourceLines(folder);
        int infraFiles = countFiles(toolFolder, ".java");
        int jokeFiles = countFiles(ROOT.resolve("tools/jokes"), ".json");
        String complexity = study == 0 ? "нет учебного кода для сравнения" : fmt(infra * 1.0 / study) + " строки инфраструктуры на строку учебного кода";
        IncidentResult incident = incidents(python, project, stats);

        StringBuilder out = new StringBuilder();
        out.append("<!-- Файл сгенерирован Java. Изменяй tools/jokes/*.json, не этот файл. -->\n");
        out.append("<!-- jokes: ");
        for (int i=0; i<choices.length; i++) out.append(i == 0 ? "" : ",").append(choices[i]);
        out.append(" -->\n# Репозиторий для домашних заданий\n\n");
        out.append("![Терминал в стиле Windows 10](assets/terminal-win10.svg)\n\n");
        out.append("> ").append(render(jokes.get(0).get(choices[0]), vars)).append("\n\n");
        out.append("## 🐍 Введение в Python\n\n").append(render(jokes.get(1).get(choices[1]), vars)).append("\n\n");
        appendDirectory(out, ROOT.resolve("python"));
        out.append("## 🛠️ Индивидуальный проект\n\n").append(render(jokes.get(2).get(choices[2]), vars)).append("\n\n");
        if (existingProjects.isEmpty()) out.append("Пока материалов нет.\n\n");
        else for (Path folder : existingProjects) appendDirectory(out, folder);
        out.append("## ☕ Java\n\n");
        if (Files.exists(ROOT.resolve("assets/java-coffee.png"))) out.append("<img src=\"assets/java-coffee.png\" alt=\"Классическая чашка Java\" width=\"105\">\n\n");
        out.append(render(jokes.get(3).get(choices[3]), vars)).append("\n\n");
        out.append("Java 21 · GitHub Actions · [Исходник генератора](tools/ReadmeGenerator.java)\n\n");
        if (stats.available) {
            out.append("**Доля Java:** ").append(fmt(stats.javaShare)).append("% распознанного кода GitHub.\n\n");
            writeIfChanged(ROOT.resolve("assets/java-share.svg"), shareSvg(stats.javaShare));
            out.append("![Доля Java в коде](assets/java-share.svg)\n\n");
            out.append("> ").append(shareRemark(stats.javaShare)).append("\n\n");
        } else out.append("Статистика языков GitHub сейчас недоступна. Процент не придумываем.\n\n");
        out.append("### Диагностический журнал\n\n");
        writeIfChanged(ROOT.resolve("assets/diagnostics.svg"), diagnosticSvg(diagnostics));
        out.append("![Ироничный диагностический журнал](assets/diagnostics.svg)\n\n");
        out.append("## ☢️ Оценка опасности домашних заданий\n\n");
        out.append("*Шуточный индекс сложности файловой структуры, а не проверка правильности решений.*\n\n");
        out.append("| Раздел | Баллы условной угрозы | Заключение |\n|---|---:|---|\n");
        for (Hazard h : hazards) out.append("| ").append(h.path == null ? h.name : "[" + md(h.name) + "](" + link(h.path) + ")").append(" | ").append(h.points).append(" | ").append(danger(h.points)).append(" |\n");
        out.append("\nОценка зависит от числа файлов, вложенности, объёма кода и отметок `TODO`/`FIXME`. Угроза носит исключительно бумажный характер.\n\n");
        out.append("## 📜 Летопись технических происшествий\n\n");
        out.append("Все зарегистрированные события: [INCIDENTS.md](INCIDENTS.md).\n\n");
        out.append("> ").append(incident.last).append("\n\n");
        out.append("## 📐 Аудит неоправданной сложности\n\n");
        out.append("| Показатель | Значение |\n|---|---:|\n");
        out.append("| Java-файлов инфраструктуры | ").append(infraFiles).append(" |\n");
        out.append("| JSON-файлов с шутками | ").append(jokeFiles).append(" |\n");
        out.append("| Строк в инструментах Java | ").append(infra).append(" |\n");
        out.append("| Строк учебного исходного кода | ").append(study).append(" |\n\n");
        out.append("**Индекс:** ").append(complexity).append(".\n\n");
        out.append("Считаются все строки исходников, включая пустые; это шуточный показатель объёма, не оценка качества.\n\n");
        out.append("---\n\n## 🎲 Вердикт дня\n\n> ").append(render(jokes.get(4).get(choices[4]), vars)).append("\n");
        writeIfChanged(readme, out.toString());
        System.out.println("README, диагностика, статистика и журнал инцидентов обработаны.");
    }

    private static String fmt(double v) { return String.format(Locale.forLanguageTag("ru-RU"), "%.1f", v); }
    private static int choose(int size, int old) { int n=RANDOM.nextInt(size - (old>=0 && old<size ? 1:0)); return old>=0 && old<size && n>=old ? n+1:n; }
    private static int[] previous(Path readme) throws IOException {
        int[] result = {-1,-1,-1,-1,-1};
        if (!Files.exists(readme)) return result;
        Matcher m=PREVIOUS.matcher(Files.readString(readme));
        if(m.find()) { String[] items=m.group(1).split(","); for(int i=0;i<Math.min(5,items.length);i++) try {result[i]=Integer.parseInt(items[i]);}catch(NumberFormatException ignored){} }
        return result;
    }
    private static String render(String template, Map<String,String> vars) {
        Matcher m=VARIABLE.matcher(template); StringBuilder b=new StringBuilder();
        while(m.find()) {String v=vars.get(m.group(1)); if(v==null) throw new IllegalArgumentException("Неизвестная переменная: "+m.group()); m.appendReplacement(b,Matcher.quoteReplacement(v));}
        m.appendTail(b); return b.toString();
    }
    private static void writeIfChanged(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        if(!Files.exists(path) || !Files.readString(path,StandardCharsets.UTF_8).equals(text)) Files.writeString(path,text,StandardCharsets.UTF_8);
    }
    private static String md(String s) {return s.replace("|","\\|").replace("[","\\[").replace("]","\\]");}
    private static String xml(String s) {return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");}
    private static String link(Path path) {List<String> out=new ArrayList<>();for(Path p:ROOT.relativize(path)) out.add(URLEncoder.encode(p.toString(),StandardCharsets.UTF_8).replace("+","%20"));return String.join("/",out);}
    private static List<Path> files(Path dir) throws IOException {
        if(!Files.isDirectory(dir)) return List.of();
        try(Stream<Path> stream=Files.walk(dir)) {return stream.filter(Files::isRegularFile).filter(p->!IGNORE.contains(p.getFileName().toString())).sorted().toList();}
    }
    private static Scan scan(Path dir) throws IOException {
        List<Path> f=files(dir);int folders=0, depth=0,todo=0;long bytes=0,lines=0;
        if(Files.isDirectory(dir))try(Stream<Path> walk=Files.walk(dir)){folders=(int)walk.filter(Files::isDirectory).count()-1;}
        for(Path p:f) {bytes+=Files.size(p);depth=Math.max(depth,dir.relativize(p).getNameCount());if(isSource(p)) {try {String content=Files.readString(p,StandardCharsets.UTF_8);lines+=content.lines().count();Matcher m=Pattern.compile("(?i)\\b(TODO|FIXME)\\b").matcher(content);while(m.find())todo++;}catch(Exception ignored) {}}}
        return new Scan(f.size(),folders,lines,bytes,todo,depth);
    }
    private static boolean isSource(Path p) {String n=p.getFileName().toString().toLowerCase(Locale.ROOT);return SOURCE_EXTENSIONS.stream().anyMatch(n::endsWith);}
    private static Scan scanProject() throws IOException {int f=0,d=0,t=0,depth=0;long lines=0,bytes=0; for(String name:PROJECTS){Path p=ROOT.resolve(name);if(Files.isDirectory(p)){Scan s=scan(p);f+=s.files;d+=s.folders;lines+=s.codeLines;bytes+=s.sizeBytes;t+=s.todoCount;depth=Math.max(depth,s.maxDepth);}}return new Scan(f,d,lines,bytes,t,depth);}
    private static long sourceLines(Path dir) throws IOException {return scan(dir).codeLines;}
    private static long codeLines(Path dir, String extension) throws IOException {long n=0;for(Path p:files(dir)) if(p.toString().endsWith(extension)) n+=Files.readString(p).lines().count();return n;}
    private static int countFiles(Path dir,String extension) throws IOException {return (int)files(dir).stream().filter(p->p.toString().endsWith(extension)).count();}
    private static Hazard hazard(String name,Path p,Scan s) {int points=Math.min(100,Math.max(0,s.files*3+s.folders*4+s.maxDepth*5+(int)(s.codeLines/60)+s.todoCount*3));return new Hazard(name,p,points,s);}
    private static String danger(int p) {return p<15?"Подозрительно спокойно":p<35?"Умеренная тревожность":p<65?"Нужен план эвакуации":"Документы переданы комиссии";}
    private static void appendDirectory(StringBuilder out,Path folder) throws IOException {
        if(!Files.isDirectory(folder)){out.append("Материалы пока не загружены.\n\n");return;}
        out.append("[Открыть `").append(md(folder.getFileName().toString())).append("/`](").append(link(folder)).append(")\n\n");
        List<Path> child;
        try(Stream<Path> s=Files.list(folder)) {child=s.filter(Files::isDirectory).sorted().toList();}
        if(child.isEmpty()){out.append("Вложенных папок пока нет. Список отдельных файлов не выводится.\n\n");return;}
        out.append("| Папка | Файлов внутри |\n|---|---:|\n");
        for(Path p:child)out.append("| [").append(md(p.getFileName().toString())).append("/](").append(link(p)).append(") | ").append(files(p).size()).append(" |\n");
        out.append("\n");
    }
    private static Stats languages() {
        String repo=System.getenv("GITHUB_REPOSITORY");
        if(repo==null || !repo.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))return new Stats(0,0,0,0,false);
        try {
            HttpRequest.Builder builder=HttpRequest.newBuilder(URI.create("https://api.github.com/repos/"+repo+"/languages")).GET().timeout(Duration.ofSeconds(12)).header("Accept","application/vnd.github+json").header("User-Agent","moiDZ-generator");
            String token=System.getenv("GITHUB_TOKEN");if(token!=null && !token.isBlank()) builder.header("Authorization","Bearer "+token);
            try(HttpClient client=HttpClient.newHttpClient()) {
                HttpResponse<String> resp=client.send(builder.build(),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if(resp.statusCode()!=200)return new Stats(0,0,0,0,false);
                Object value=JsonParser.parse(resp.body());if(!(value instanceof Map<?,?> map))return new Stats(0,0,0,0,false);
                long all=0;for(Object number:map.values())if(number instanceof Number n)all+=n.longValue();
                if(all==0)return new Stats(0,0,0,0,false);
                long java=map.get("Java") instanceof Number n ? n.longValue():0;
                long python=map.get("Python") instanceof Number n ? n.longValue():0;
                return new Stats(java,all,100.0*java/all,100.0*python/all,true);
            }
        } catch(Exception e) {System.err.println("Статистика GitHub недоступна: "+e.getMessage());return new Stats(0,0,0,0,false);}
    }
    private static String shareRemark(double share) {return share<25?"Java пока в меньшинстве. Но уже составляет официальный отчёт о собственном положении.":share<50?"Контрольный пакет пока не получен. Java тем временем ведёт переговоры сама с собой.":share<75?"Java получила большинство. Python уведомили в установленном порядке.":"Java почти поглотила репозиторий. Отчёт об этом, разумеется, подготовила Java.";}
    private static String shareSvg(double value) {
        double width=Math.max(0,Math.min(100,value))*4.8;
        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="560" height="120" viewBox="0 0 560 120">
            <rect width="560" height="120" rx="14" fill="#101827"/>
            <text x="20" y="31" font-family="Arial, sans-serif" font-size="17" fill="#e5e7eb">Доля Java в распознанном коде</text>
            <rect x="20" y="53" width="480" height="19" rx="7" fill="#374151"/>
            """ + "<rect x=\"20\" y=\"53\" width=\"" + String.format(Locale.ROOT,"%.2f",width) + "\" height=\"19\" rx=\"7\" fill=\"#b07219\"/>\n" +
            "<text x=\"20\" y=\"101\" font-family=\"Arial, sans-serif\" font-size=\"16\" fill=\"#e5e7eb\">Java: " + xml(fmt(value)) + "%</text>\n" +
            "<text x=\"500\" y=\"101\" text-anchor=\"end\" font-family=\"Arial, sans-serif\" font-size=\"13\" fill=\"#9ca3af\">Остальные: " + xml(fmt(100-value)) + "%</text>\n</svg>\n";
    }
    private static List<String> diagnostics() throws IOException {
        Object parsed=JsonParser.parse(Files.readString(ROOT.resolve("tools/diagnostics/messages.json"),StandardCharsets.UTF_8));
        Map<?,?> statuses=(Map<?,?>)((Map<?,?>)parsed).get("statuses");
        List<String> result=new ArrayList<>();
        for(String key:List.of("ok","ok","warn","fail","info")) {
            List<?> lines=(List<?>)statuses.get(key);
            result.add(key.toUpperCase(Locale.ROOT)+"|"+lines.get(RANDOM.nextInt(lines.size())));
        }
        return result;
    }
    private static String diagnosticSvg(List<String> lines) {
        StringBuilder s=new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"720\" height=\"208\" viewBox=\"0 0 720 208\"><rect width=\"720\" height=\"208\" rx=\"9\" fill=\"#101010\"/><rect width=\"720\" height=\"30\" rx=\"9\" fill=\"#303030\"/><text x=\"15\" y=\"21\" fill=\"#eeeeee\" font-size=\"14\" font-family=\"Consolas, monospace\">C:\\moiDZ\\diagnostics.exe</text>");
        int y=60;
        for(String line:lines) {String[] p=line.split("\\|",2);String color=switch(p[0]) {case "OK"->"#79d78a";case "WARN"->"#e8c77a";case "FAIL"->"#ee8b8b";default->"#9bb9e2";};s.append("<text x=\"17\" y=\"").append(y).append("\" font-family=\"Consolas,monospace\" font-size=\"13\" fill=\"").append(color).append("\">[").append(p[0]).append("] ").append(xml(p[1])).append("</text>");y+=28;}
        return s.append("</svg>\n").toString();
    }
    private record IncidentResult(String last) {}
    private static IncidentResult incidents(Scan python, Scan project, Stats stats) throws IOException {
        Path state=ROOT.resolve("tools/state/incident-state.json");Path journal=ROOT.resolve("INCIDENTS.md");
        String now=Instant.now().toString();
        String snapshot="python="+python.files+";project="+project.files+";folders="+(python.folders+project.folders)+";digest="+digestStudies();
        String prev="";
        if(Files.exists(state)) {
            Object v=JsonParser.parse(Files.readString(state));
            if(v instanceof Map<?,?> map && map.get("snapshot") instanceof String s)prev=s;
        }
        List<String> entries=new ArrayList<>();
        if(!Files.exists(journal)) entries.add("Первичная регистрация системы. Улики пока не уничтожены.");
        else if(!prev.isEmpty() && !snapshot.equals(prev)) {
            if(python.files!=number(prev,"python")) entries.add("Количество файлов Python изменилось: "+number(prev,"python")+" → "+python.files+".");
            if(project.files!=number(prev,"project")) entries.add("Число материалов индивидуального проекта изменилось: "+number(prev,"project")+" → "+project.files+".");
            if(python.folders+project.folders!=number(prev,"folders")) entries.add("Изменилось количество учебных подпапок. Файловая система просит объяснений.");
            if(entries.isEmpty() && !digest(prev).equals(digest(snapshot))) entries.add("Учебные файлы изменились, хотя их количество осталось прежним. Система всё заметила.");
        }
        if(!Files.exists(journal))writeIfChanged(journal,"# Летопись технических происшествий\n\nЗдесь записываются реальные изменения состава учебных файлов. Шутки — в формулировках, не в фактах.\n\n");
        if(!entries.isEmpty()) {
            StringBuilder updated=new StringBuilder(Files.readString(journal));
            for(String msg:entries)updated.append("- **").append(now).append("** — ").append(msg).append("\n");
            writeIfChanged(journal,updated.toString());
        }
        writeIfChanged(state,"{\"snapshot\":\""+snapshot+"\"}\n");
        if(!entries.isEmpty())return new IncidentResult(entries.get(entries.size()-1));
        List<String> old=Files.readAllLines(journal).stream().filter(x->x.startsWith("- **")).toList();
        return new IncidentResult(old.isEmpty()?"Происшествий пока нет. Это подозрительно.":old.get(old.size()-1).replaceFirst("^- \\*\\*[^*]+\\*\\* — ",""));
    }
    private static String digestStudies() throws IOException {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            List<Path> studyFiles = new ArrayList<>(files(ROOT.resolve("python")));
            for(String name:PROJECTS) studyFiles.addAll(files(ROOT.resolve(name)));
            studyFiles.sort(Comparator.naturalOrder());
            for(Path path:studyFiles) {
                digest.update(ROOT.relativize(path).toString().getBytes(StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(path));
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch(java.security.NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    private static String digest(String snapshot) {
        Matcher matcher=Pattern.compile("(?:^|;)digest=([a-f0-9]+)").matcher(snapshot);
        return matcher.find()?matcher.group(1):"";
    }
    private static int number(String snapshot,String field) {Matcher m=Pattern.compile("(?:^|;)"+field+"=(\\d+)").matcher(snapshot);return m.find()?Integer.parseInt(m.group(1)):0;}
}
