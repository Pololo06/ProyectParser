package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.policy.DiagramOptions;
import com.proyectparser.core.infrastructure.plantuml.PlantUmlGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Caso borde CB-NESTED-01 del informe de QA: tipos anidados hasta 4 niveles, interfaces y
 * enums anidados, herencia e implementación. La salida debe coincidir con el PUML de referencia.
 */
class CasoBordeNestedTest {

    private static final String CONNECTABLE = """
            package com.unimag;

            public interface Connectable {
                void connect();
                void disconnect();
            }
            """;

    private static final String NESTED_PROJECT = """
            package com.unimag;

            import java.util.ArrayList;
            import java.util.List;

            public class NestedProject {
                private final Configuration configuration;
                private final List<Module> modules;
                public NestedProject() { this.configuration = new Configuration(); this.modules = new ArrayList<>(); }
                public Configuration getConfiguration() { return configuration; }
                public void addModule(Module module) { modules.add(module); }

                public static class Configuration {
                    private final Database database;
                    private Environment environment;
                    public Configuration() { this.database = new Database(); }
                    public Database getDatabase() { return database; }
                    public Environment getEnvironment() { return environment; }
                    public void setEnvironment(Environment environment) { this.environment = environment; }

                    public static class Database {
                        private final Connection connection;
                        private String databaseName;
                        public Database() { this.connection = new Connection(); }
                        public Connection getConnection() { return connection; }
                        public String getDatabaseName() { return databaseName; }
                        public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }

                        public static class Connection extends AbstractConnection implements Connectable {
                            private String host;
                            private int port;
                            private ConnectionStatus status;
                            public Connection() { }
                            @Override public void connect() { }
                            @Override public void disconnect() { }
                            public String getHost() { return host; }
                            public void setHost(String host) { this.host = host; }
                            public int getPort() { return port; }
                            public void setPort(int port) { this.port = port; }
                            public ConnectionStatus getStatus() { return status; }
                            public enum ConnectionStatus { CONNECTED, DISCONNECTED, ERROR }
                        }

                        public interface DatabaseListener {
                            void onConnect();
                            void onDisconnect();
                        }
                    }

                    public enum Environment { DEVELOPMENT, TEST, PRODUCTION }
                }

                public static class Module implements Identifiable {
                    private final String id;
                    private final Service service;
                    public Module(String id) { this.id = id; this.service = new Service(); }
                    @Override public String getId() { return id; }
                    public Service getService() { return service; }

                    public class Service {
                        private final Repository repository;
                        public Service() { this.repository = new Repository(); }
                        public Repository getRepository() { return repository; }

                        public class Repository {
                            private final Cache cache;
                            public Repository() { this.cache = new Cache(); }
                            public Cache getCache() { return cache; }

                            public class Cache {
                                private int size;
                                public Cache() { this.size = 100; }
                                public int getSize() { return size; }
                                public void setSize(int size) { this.size = size; }
                                public void clear() { this.size = 0; }
                            }
                        }
                    }
                }

                public interface Identifiable {
                    String getId();
                }

                public abstract static class AbstractConnection {
                    protected long timeout = 5000;
                    public long getTimeout() { return timeout; }
                    public void setTimeout(long timeout) { this.timeout = timeout; }
                    public abstract void connect();
                }
            }
            """;

    private static String generate() throws IOException {
        Path root = Files.createTempDirectory("caso-borde-nested");
        Path pkg = Files.createDirectories(root.resolve("com/unimag"));
        Files.writeString(pkg.resolve("Connectable.java"), CONNECTABLE);
        Files.writeString(pkg.resolve("NestedProject.java"), NESTED_PROJECT);
        return new PlantUmlGenerator().render(new ProjectAnalyzer().analyze(root.toString()),
                DiagramOptions.defaults());
    }

    @Test
    void relacionesCoincidenConLaReferencia() throws IOException {
        Set<String> relations = new TreeSet<>();
        for (String line : generate().split("\n")) {
            if (line.startsWith("com.unimag.")) {
                relations.add(line);
            }
        }
        Set<String> expected = new TreeSet<>(List.of(
                "com.unimag.ConnectionStatus +-- com.unimag.Connection",
                "com.unimag.Service +-- com.unimag.Module",
                "com.unimag.Cache +-- com.unimag.Repository",
                "com.unimag.Configuration +-- com.unimag.NestedProject",
                "com.unimag.Connection +-- com.unimag.Database",
                "com.unimag.Environment +-- com.unimag.Configuration",
                "com.unimag.Connectable <|.. com.unimag.Connection",
                "com.unimag.Module +-- com.unimag.NestedProject",
                "com.unimag.Database +-- com.unimag.Configuration",
                "com.unimag.DatabaseListener +-- com.unimag.Database",
                "com.unimag.Identifiable +-- com.unimag.NestedProject",
                "com.unimag.AbstractConnection +-- com.unimag.NestedProject",
                "com.unimag.Repository +-- com.unimag.Service"));
        assertEquals(expected, relations);
    }

    @Test
    void miembrosCoincidenConLaReferencia() throws IOException {
        String puml = generate();
        for (String line : Arrays.asList(
                "package com.unimag {",
                "abstract class AbstractConnection <<Abstract>> {",
                "interface Connectable <<Interface>> {",
                "interface DatabaseListener <<Interface>> {",
                "interface Identifiable <<Interface>> {",
                "-  {readOnly} configuration : Configuration",
                "-  {readOnly} modules : List<Module>",
                "-  {readOnly} cache : Cache",
                "-  {readOnly} connection : Connection",
                "-  {readOnly} repository : Repository",
                "-  {readOnly} id : String",
                "-  {readOnly} service : Service",
                "-  {readOnly} database : Database",
                "#  timeout : long = 5000",
                "+  {abstract} connect() : void",
                "+  {abstract} disconnect() : void",
                "+  {abstract} onConnect() : void",
                "+  {abstract} onDisconnect() : void",
                "+  {abstract} getId() : String",
                "+  {static, readOnly} CONNECTED : ConnectionStatus",
                "+  {static, readOnly} ERROR : ConnectionStatus",
                "+  {static, readOnly} PRODUCTION : Environment",
                "+  NestedProject() : void <<Create>>",
                "+  Module(id : String) : void <<Create>>",
                "+  Cache() : void <<Create>>")) {
            assertTrue(puml.contains(line + "\n"), line + "\n" + puml);
        }
    }
}
