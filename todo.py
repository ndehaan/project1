import json
import sys
from pathlib import Path

DATA_FILE = Path("tasks.json")


def load_tasks():
    if not DATA_FILE.exists():
        return []
    return json.loads(DATA_FILE.read_text())


def save_tasks(tasks):
    DATA_FILE.write_text(json.dumps(tasks, indent=2))


def format_task(task):
    status = "x" if task["done"] else " "
    due = f" (due: {task['due']})" if task.get("due") else ""
    return f"[{status}] {task['id']}. {task['title']}{due}"


def add_task(title, due=None):
    tasks = load_tasks()
    task = {"id": len(tasks) + 1, "title": title, "done": False, "due": due}
    tasks.append(task)
    save_tasks(tasks)
    due_str = f" (due: {due})" if due else ""
    print(f"Added: [{task['id']}] {title}{due_str}")


def list_tasks():
    tasks = [t for t in load_tasks() if not t["done"]]
    if not tasks:
        print("No incomplete tasks.")
        return
    for task in tasks:
        print(format_task(task))


def list_all_tasks():
    tasks = load_tasks()
    if not tasks:
        print("No tasks yet. Add one with: python todo.py add \"your task\"")
        return
    for task in tasks:
        print(format_task(task))


def complete_task(task_id):
    tasks = load_tasks()
    for task in tasks:
        if task["id"] == task_id:
            task["done"] = True
            save_tasks(tasks)
            print(f"Completed: {task['title']}")
            return
    print(f"No task with id {task_id}")


def set_due(task_id, due):
    tasks = load_tasks()
    for task in tasks:
        if task["id"] == task_id:
            task["due"] = due
            save_tasks(tasks)
            print(f"Set due date for \"{task['title']}\": {due}")
            return
    print(f"No task with id {task_id}")


def delete_task(task_id):
    tasks = load_tasks()
    remaining = [t for t in tasks if t["id"] != task_id]
    if len(remaining) == len(tasks):
        print(f"No task with id {task_id}")
        return
    save_tasks(remaining)
    print(f"Deleted task {task_id}")


def print_usage():
    print("Usage:")
    print("  python todo.py add \"task title\" [--due YYYY-MM-DD]")
    print("  python todo.py list")
    print("  python todo.py list-all")
    print("  python todo.py due <id> YYYY-MM-DD")
    print("  python todo.py complete <id>")
    print("  python todo.py delete <id>")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)

    command = sys.argv[1]

    if command == "add" and len(sys.argv) >= 3:
        args = sys.argv[2:]
        due = None
        if "--due" in args:
            due_index = args.index("--due")
            if due_index + 1 < len(args):
                due = args[due_index + 1]
                args = args[:due_index] + args[due_index + 2:]
        add_task(" ".join(args), due)
    elif command == "list":
        list_tasks()
    elif command == "list-all":
        list_all_tasks()
    elif command == "due" and len(sys.argv) == 4:
        set_due(int(sys.argv[2]), sys.argv[3])
    elif command == "complete" and len(sys.argv) == 3:
        complete_task(int(sys.argv[2]))
    elif command == "delete" and len(sys.argv) == 3:
        delete_task(int(sys.argv[2]))
    else:
        print_usage()
        sys.exit(1)
