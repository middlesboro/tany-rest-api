import os
import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Change implements to extends
    if "implements BaseEntity" in content:
        content = content.replace("implements BaseEntity", "extends BaseEntity")
    else:
        return # Skip if not implementing BaseEntity

    # Remove fields that are now in BaseEntity
    # @Id private String id;
    content = re.sub(r'\s*@Id\s*\n\s*private String id;\s*', '\n', content)
    # private String id; (if no @Id, though usually it has it)
    # content = re.sub(r'\s*private String id;\s*', '\n', content)

    # private Instant createDate;
    content = re.sub(r'\s*private Instant createDate;\s*', '\n', content)
    # private Instant updateDate;
    content = re.sub(r'\s*private Instant updateDate;\s*', '\n', content)

    # Remove overrides
    # We match the entire method block roughly.
    # setCreatedDate
    content = re.sub(r'\s*@Override\s*\n\s*public void setCreatedDate\(Instant date\) \{[^}]+\}\s*', '\n', content)
    # getCreatedDate
    content = re.sub(r'\s*@Override\s*\n\s*public Instant getCreatedDate\(\) \{[^}]+\}\s*', '\n', content)
    # setLastModifiedDate
    content = re.sub(r'\s*@Override\s*\n\s*public void setLastModifiedDate\(Instant date\) \{[^}]+\}\s*', '\n', content)
    # getLastModifiedDate
    content = re.sub(r'\s*@Override\s*\n\s*public Instant getLastModifiedDate\(\) \{[^}]+\}\s*', '\n', content)

    # Remove getSortValue if it calls BaseEntity.super.getSortValue
    # This is tricky because custom logic might exist.
    # BaseEntity (abstract class) has getSortValue.
    # If the entity overrides it, it should now call super.getSortValue(field) instead of BaseEntity.super.getSortValue(field).
    content = content.replace("BaseEntity.super.getSortValue", "super.getSortValue")

    # Remove import org.springframework.data.annotation.Id; if no longer used?
    # Actually BaseEntity has @Id, so we removed the field. The import might be unused.
    # We can leave imports for now or try to clean them up.

    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Updated {filepath}")

# List of files from grep
files = [
    "src/main/java/sk/tany/rest/api/domain/supplier/Supplier.java",
    "src/main/java/sk/tany/rest/api/domain/homepage/HomepageGrid.java",
    "src/main/java/sk/tany/rest/api/domain/onedrive/OneDriveToken.java",
    "src/main/java/sk/tany/rest/api/domain/emailnotification/EmailNotification.java",
    "src/main/java/sk/tany/rest/api/domain/cartdiscount/CartDiscount.java",
    "src/main/java/sk/tany/rest/api/domain/common/Sequence.java",
    "src/main/java/sk/tany/rest/api/domain/filter/FilterParameter.java",
    "src/main/java/sk/tany/rest/api/domain/filter/FilterParameterValue.java",
    "src/main/java/sk/tany/rest/api/domain/product/Product.java",
    "src/main/java/sk/tany/rest/api/domain/auth/MagicLinkToken.java",
    "src/main/java/sk/tany/rest/api/domain/auth/AuthorizationCode.java",
    "src/main/java/sk/tany/rest/api/domain/review/Review.java",
    "src/main/java/sk/tany/rest/api/domain/wishlist/Wishlist.java",
    "src/main/java/sk/tany/rest/api/domain/pagecontent/PageContent.java",
    "src/main/java/sk/tany/rest/api/domain/productsales/ProductSales.java",
    "src/main/java/sk/tany/rest/api/domain/productlabel/ProductLabel.java",
    "src/main/java/sk/tany/rest/api/domain/shopsettings/ShopSettings.java",
    "src/main/java/sk/tany/rest/api/domain/category/Category.java",
    "src/main/java/sk/tany/rest/api/domain/carrier/Carrier.java",
    "src/main/java/sk/tany/rest/api/domain/blog/Blog.java",
    "src/main/java/sk/tany/rest/api/domain/customer/Customer.java",
    "src/main/java/sk/tany/rest/api/domain/brand/Brand.java",
    "src/main/java/sk/tany/rest/api/domain/order/Order.java",
    "src/main/java/sk/tany/rest/api/domain/cart/Cart.java",
    "src/main/java/sk/tany/rest/api/domain/payment/Payment.java"
]

for f in files:
    if os.path.exists(f):
        process_file(f)
    else:
        print(f"File not found: {f}")
