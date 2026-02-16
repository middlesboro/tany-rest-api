import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Pattern: new Wishlist("1", "cust1", "prod1", null)
    # Regex to capture id, customerId, productId. Ignoring date.
    pattern = r'new Wishlist\(\s*"([^"]+)"\s*,\s*([^,]+)\s*,\s*"([^"]+)"\s*,\s*null\s*\)'

    def replacement(match):
        id_val = match.group(1)
        cust_val = match.group(2)
        prod_val = match.group(3)
        return f'new Wishlist({cust_val}, "{prod_val}");\n        wishlist.setId("{id_val}")'

    # This replacement is tricky because it adds a statement.
    # The usage is: Wishlist wishlist = new Wishlist(...);
    # So replacing "new Wishlist(...)" with "new Wishlist(...); wishlist.setId(...)" works only if the variable name is "wishlist".
    # Fortunately in the test it is: Wishlist wishlist = new Wishlist(...);

    # Let's target the full line.
    line_pattern = r'Wishlist\s+wishlist\s*=\s*new\s+Wishlist\(\s*"([^"]+)"\s*,\s*([^,]+)\s*,\s*"([^"]+)"\s*,\s*null\s*\);'

    def line_replacement(match):
        id_val = match.group(1)
        cust_val = match.group(2)
        prod_val = match.group(3)
        return f'Wishlist wishlist = new Wishlist({cust_val}, "{prod_val}");\n        wishlist.setId("{id_val}");'

    content = re.sub(line_pattern, line_replacement, content)

    # Check for WishlistAdminServiceTest usage too
    # It might use different variable names or values.

    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Updated {filepath}")

files = [
    "src/test/java/sk/tany/rest/api/service/client/WishlistClientServiceTest.java",
    "src/test/java/sk/tany/rest/api/service/admin/WishlistAdminServiceTest.java"
]

import os
for f in files:
    if os.path.exists(f):
        process_file(f)
