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


def add_task(title):
    tasks = load_tasks()
    task = {"id": len(tasks) + 1, "title": title, "done": False}
    tasks.append(task)
    save_tasks(tasks)
    print(f"Added: [{task['id']}] {title}")


def list_tasks():
    tasks = [t for t in load_tasks() if not t["done"]]
    if not tasks:
        print("No incomplete tasks.")
        return
    for task in tasks:
        print(f"[ ] {task['id']}. {task['title']}")


def list_all_tasks():
    tasks = load_tasks()
    if not tasks:
        print("No tasks yet. Add one with: python todo.py add \"your task\"")
        return
    for task in tasks:
        status = "x" if task["done"] else " "
        print(f"[{status}] {task['id']}. {task['title']}")


def complete_task(task_id):
    tasks = load_tasks()
    for task in tasks:
        if task["id"] == task_id:
            task["done"] = True
            save_tasks(tasks)
            print(f"Completed: {task['title']}")
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
    print("  python todo.py add \"task title\"")
    print("  python todo.py list")
    print("  python todo.py list-all")
    print("  python todo.py complete <id>")
    print("  python todo.py delete <id>")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)

    command = sys.argv[1]

    if command == "add" and len(sys.argv) >= 3:
        add_task(" ".join(sys.argv[2:]))
    elif command == "list":
        list_tasks()
    elif command == "list-all":
        list_all_tasks()
    elif command == "complete" and len(sys.argv) == 3:
        complete_task(int(sys.argv[2]))
    elif command == "delete" and len(sys.argv) == 3:
        delete_task(int(sys.argv[2]))
    else:
        print_usage()
        sys.exit(1)
