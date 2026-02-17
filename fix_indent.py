import os

base_dir = "src/main/java/sk/tany/rest/api/domain"

for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                lines = f.readlines()

            new_lines = []
            changed = False
            for line in lines:
                # If line starts with "private " (no indentation), add 4 spaces
                if line.startswith("private "):
                    new_lines.append("    " + line)
                    changed = True
                # If line starts with "protected " (no indentation), add 4 spaces
                elif line.startswith("protected "):
                    new_lines.append("    " + line)
                    changed = True
                # If line starts with "public " (no indentation) AND is not a class def, add 4 spaces?
                # Usually public methods or constructors.
                # But check if it's "public class" or "public interface" or "public enum".
                elif line.startswith("public ") and not (line.startswith("public class ") or line.startswith("public interface ") or line.startswith("public enum ") or line.startswith("public abstract class ")):
                    # careful not to indent top-level classes if they exist (unlikely in Java file usually one per file)
                    # Java source file structure: package, imports, public class X.
                    # So "public X" is inside class? No, constructor.
                    # But also inner classes?

                    # For safety, let's stick to "private" fields which seem to be the main issue from previous refactoring.
                    # The refactoring removed lines above fields, potentially eating indentation or newline.
                    pass
                    new_lines.append(line)
                else:
                    new_lines.append(line)

            if changed:
                with open(filepath, 'w') as f:
                    f.writelines(new_lines)
                print(f"Fixed indentation in {filepath}")
