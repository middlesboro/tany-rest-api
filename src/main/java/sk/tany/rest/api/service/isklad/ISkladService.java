package sk.tany.rest.api.service.isklad;

import sk.tany.rest.api.dto.isklad.CreateBrandRequest;
import sk.tany.rest.api.dto.isklad.CreateNewOrderRequest;
import sk.tany.rest.api.dto.isklad.CreateSupplierRequest;
import sk.tany.rest.api.dto.isklad.ISkladResponse;
import sk.tany.rest.api.dto.isklad.InventoryDetailRequest;
import sk.tany.rest.api.dto.isklad.InventoryDetailResult;

public interface ISkladService {
    ISkladResponse<Object> createNewOrder(CreateNewOrderRequest request);
    ISkladResponse<Object> createBrand(CreateBrandRequest request);
    ISkladResponse<Object> createSupplier(CreateSupplierRequest request);
    ISkladResponse<InventoryDetailResult> getInventory(InventoryDetailRequest request);
}
