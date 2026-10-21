import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Fallback build launcher for machines that ship a JDK runtime (jdk.compiler module)
 * but no `javac` binary. Compiles all sources under src/main/java into out/.
 *
 * Usage:  java tools/CompileAll.java
 */
public final class CompileAll {
    public static void main(String[] args) throws IOException {
        Path sourceRoot = Paths.get("src/main/java");
        Path outputDir = Paths.get("out");

        if (Files.exists(outputDir)) {
            try (Stream<Path> walk = Files.walk(outputDir)) {
                for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(p);
                }
            }
        }
        Files.createDirectories(outputDir);

        List<File> sources = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(sourceRoot)) {
            walk.filter(p -> p.toString().endsWith(".java")).forEach(p -> sources.add(p.toFile()));
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("No system Java compiler available (jdk.compiler module missing).");
            System.exit(2);
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        boolean ok;
        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(sources);
            List<String> options = List.of("-d", outputDir.toString());
            ok = compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
        }

        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            System.out.println(d);
        }
        if (ok) {
            System.out.println("Build OK -> out/ (" + sources.size() + " files)");
            System.exit(0);
        } else {
            System.out.println("Build FAILED");
            System.exit(1);
        }
    }
}
