package sk.tany.rest.api.service.admin;

public interface PrestaShopImportService {
    void importAllProducts();
    void importProduct(String id);
    void importAllSuppliers();
    void importAllManufacturers();
}
