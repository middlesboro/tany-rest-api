import re

filepath = "src/test/java/sk/tany/rest/api/service/common/CleanupServiceTest.java"

with open(filepath, 'r') as f:
    content = f.read()

# Replace setCreatedDate with setCreateDate
content = content.replace("setCreatedDate", "setCreateDate")

with open(filepath, 'w') as f:
    f.write(content)

print(f"Updated {filepath}")
