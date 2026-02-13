import os
import re

directory = "src/main/java/sk/tany/rest/api/domain"

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    if "AbstractInMemoryRepository" not in content:
        return

    # Replace package import
    content = content.replace("import sk.tany.rest.api.domain.AbstractInMemoryRepository;", "import org.springframework.data.mongodb.repository.MongoRepository;")

    # Remove Nitrite imports
    content = re.sub(r"import org\.dizitart\.no2\..*?;\n", "", content)

    # Change class to interface and extends
    content = re.sub(r"public class (\w+) extends AbstractInMemoryRepository<(\w+)>", r"public interface \1 extends MongoRepository<\2, String>", content)

    # Remove constructor
    # Pattern: public XRepository(Nitrite nitrite) { ... }
    # This regex handles single line and multi-line constructors reasonably well for standard formatting
    content = re.sub(r"\s*public \w+\(Nitrite nitrite.*?\)\s*\{[\s\S]*?\}\n", "", content)

    # Also remove empty lines resulting from removal
    content = re.sub(r"\n\n\n", "\n\n", content)

    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Processed {filepath}")

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith("Repository.java"):
            process_file(os.path.join(root, file))
