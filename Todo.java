import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Todo {

    private static final String DATA_FILE = "tasks.json";

    static class Task {
        int id;
        String title;
        boolean done;
        String due;

        Task(int id, String title, boolean done, String due) {
            this.id = id;
            this.title = title;
            this.done = done;
            this.due = due;
        }
    }

    static List<Task> loadTasks() throws IOException {
        Path path = Path.of(DATA_FILE);
        if (!Files.exists(path)) return new ArrayList<>();
        return parseJson(Files.readString(path));
    }

    static void saveTasks(List<Task> tasks) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String due = t.due != null ? "\"" + t.due + "\"" : "null";
            sb.append("  {\"id\": ").append(t.id)
              .append(", \"title\": \"").append(t.title.replace("\"", "\\\"")).append("\"")
              .append(", \"done\": ").append(t.done)
              .append(", \"due\": ").append(due)
              .append("}");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        Files.writeString(Path.of(DATA_FILE), sb.toString());
    }

    static List<Task> parseJson(String json) {
        List<Task> tasks = new ArrayList<>();
        json = json.trim();
        if (json.equals("[]")) return tasks;
        // strip outer brackets
        json = json.substring(1, json.length() - 1).trim();
        for (String obj : splitObjects(json)) {
            obj = obj.trim();
            if (obj.isEmpty()) continue;
            int id = Integer.parseInt(extractValue(obj, "id"));
            String title = extractValue(obj, "title");
            boolean done = Boolean.parseBoolean(extractValue(obj, "done"));
            String dueRaw = extractValue(obj, "due");
            String due = dueRaw.equals("null") ? null : dueRaw;
            tasks.add(new Task(id, title, done, due));
        }
        return tasks;
    }

    static List<String> splitObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0) objects.add(json.substring(start, i + 1)); }
        }
        return objects;
    }

    static String extractValue(String obj, String key) {
        String search = "\"" + key + "\": ";
        int start = obj.indexOf(search) + search.length();
        char first = obj.charAt(start);
        if (first == '"') {
            int end = obj.indexOf('"', start + 1);
            return obj.substring(start + 1, end);
        }
        int end = obj.indexOf(',', start);
        if (end == -1) end = obj.indexOf('}', start);
        return obj.substring(start, end).trim();
    }

    static String formatTask(Task t) {
        String status = t.done ? "x" : " ";
        String due = t.due != null ? " (due: " + t.due + ")" : "";
        return "[" + status + "] " + t.id + ". " + t.title + due;
    }

    static void addTask(String title, String due) throws IOException {
        List<Task> tasks = loadTasks();
        Task task = new Task(tasks.size() + 1, title, false, due);
        tasks.add(task);
        saveTasks(tasks);
        String dueStr = due != null ? " (due: " + due + ")" : "";
        System.out.println("Added: [" + task.id + "] " + title + dueStr);
    }

    static void listTasks() throws IOException {
        List<Task> tasks = loadTasks().stream().filter(t -> !t.done).toList();
        if (tasks.isEmpty()) { System.out.println("No incomplete tasks."); return; }
        tasks.forEach(t -> System.out.println(formatTask(t)));
    }

    static void listAllTasks() throws IOException {
        List<Task> tasks = loadTasks();
        if (tasks.isEmpty()) { System.out.println("No tasks yet. Add one with: java Todo add \"your task\""); return; }
        tasks.forEach(t -> System.out.println(formatTask(t)));
    }

    static void completeTask(int id) throws IOException {
        List<Task> tasks = loadTasks();
        for (Task t : tasks) {
            if (t.id == id) { t.done = true; saveTasks(tasks); System.out.println("Completed: " + t.title); return; }
        }
        System.out.println("No task with id " + id);
    }

    static void setDue(int id, String due) throws IOException {
        List<Task> tasks = loadTasks();
        for (Task t : tasks) {
            if (t.id == id) { t.due = due; saveTasks(tasks); System.out.println("Set due date for \"" + t.title + "\": " + due); return; }
        }
        System.out.println("No task with id " + id);
    }

    static void deleteTask(int id) throws IOException {
        List<Task> tasks = loadTasks();
        int before = tasks.size();
        tasks.removeIf(t -> t.id == id);
        if (tasks.size() == before) { System.out.println("No task with id " + id); return; }
        saveTasks(tasks);
        System.out.println("Deleted task " + id);
    }

    static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java Todo add \"task title\" [--due YYYY-MM-DD]");
        System.out.println("  java Todo list");
        System.out.println("  java Todo list-all");
        System.out.println("  java Todo due <id> YYYY-MM-DD");
        System.out.println("  java Todo complete <id>");
        System.out.println("  java Todo delete <id>");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) { printUsage(); System.exit(1); }

        String command = args[0];

        if (command.equals("add") && args.length >= 2) {
            List<String> parts = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
            String due = null;
            int dueIdx = parts.indexOf("--due");
            if (dueIdx != -1 && dueIdx + 1 < parts.size()) {
                due = parts.get(dueIdx + 1);
                parts.subList(dueIdx, dueIdx + 2).clear();
            }
            addTask(String.join(" ", parts), due);
        } else if (command.equals("list")) {
            listTasks();
        } else if (command.equals("list-all")) {
            listAllTasks();
        } else if (command.equals("due") && args.length == 3) {
            setDue(Integer.parseInt(args[1]), args[2]);
        } else if (command.equals("complete") && args.length == 2) {
            completeTask(Integer.parseInt(args[1]));
        } else if (command.equals("delete") && args.length == 2) {
            deleteTask(Integer.parseInt(args[1]));
        } else {
            printUsage();
            System.exit(1);
        }
    }
}
