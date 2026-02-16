import os
import re

directory = "src/main/java/sk/tany/rest/api/domain"

def process_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    new_lines = []
    skip = False

    for line in lines:
        stripped = line.strip()

        # Check if start of a method
        if (stripped.startswith("public ") or stripped.startswith("List<") or stripped.startswith("Optional<") or stripped.startswith("Page<")) and stripped.endswith("{"):
            # It's a method start
            # Check if it's a default method (keep it) or static (keep it)
            if "default " in stripped or "static " in stripped:
                new_lines.append(line)
                continue

            # It's likely a method we want to convert to abstract
            # Replace { with ;
            new_line = line.rstrip().rstrip("{") + ";\n"
            new_lines.append(new_line)
            skip = True
            continue

        if skip:
            if stripped == "}":
                skip = False
            continue

        # Remove empty lines if multiple
        if line.strip() == "" and new_lines and new_lines[-1].strip() == "":
            continue

        new_lines.append(line)

    with open(filepath, 'w') as f:
        f.writelines(new_lines)
    print(f"Fixed {filepath}")

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith("Repository.java"):
            # skip already fixed ones if any (manual)
            if "ProductRepository.java" in file or "OrderRepository.java" in file:
                continue
            process_file(os.path.join(root, file))
