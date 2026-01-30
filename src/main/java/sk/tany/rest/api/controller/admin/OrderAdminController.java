package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateRequest;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateResponse;
import sk.tany.rest.api.dto.admin.order.get.OrderAdminGetResponse;
import sk.tany.rest.api.dto.admin.order.list.OrderAdminListResponse;
import sk.tany.rest.api.dto.admin.order.patch.OrderPatchRequest;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateResponse;
import sk.tany.rest.api.mapper.OrderAdminApiMapper;
import sk.tany.rest.api.service.admin.InvoiceService;
import sk.tany.rest.api.service.admin.OrderAdminService;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderService;
    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;
    private final OrderAdminApiMapper orderAdminApiMapper;

    @PostMapping
    public ResponseEntity<OrderAdminCreateResponse> createOrder(@RequestBody OrderAdminCreateRequest order) {
        OrderDto savedOrder = orderService.save(orderAdminApiMapper.toDto(order));
        return new ResponseEntity<>(orderAdminApiMapper.toCreateResponse(savedOrder), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<OrderAdminListResponse> getOrders(
            @RequestParam(required = false) Long orderIdentifier,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(required = false) String carrierId,
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) Instant createDateFrom,
            @RequestParam(required = false) Instant createDateTo,
            Pageable pageable) {
        return orderService.findAll(orderIdentifier, status, priceFrom, priceTo, carrierId, paymentId, createDateFrom, createDateTo, pageable)
                .map(orderAdminApiMapper::toListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderAdminGetResponse> getOrder(@PathVariable String id) {
        return orderService.findById(id)
                .map(orderAdminApiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> getOrderInvoice(@PathVariable String id) {
        OrderDto order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        byte[] pdfBytes = invoiceService.generateInvoice(id);

        boolean isCreditNote = order.getStatus() == OrderStatus.CANCELED;
        int year = 2026;
        if (isCreditNote && order.getCancelDate() != null) {
            year = java.time.LocalDateTime.ofInstant(order.getCancelDate(), java.time.ZoneId.systemDefault()).getYear();
        } else if (order.getCreateDate() != null) {
            year = java.time.LocalDateTime.ofInstant(order.getCreateDate(), java.time.ZoneId.systemDefault()).getYear();
        }

        String docNumber = String.format("%d%06d", year, isCreditNote ? order.getCreditNoteIdentifier() : order.getOrderIdentifier());
        String filename = (isCreditNote ? "dobropis-" : "faktura-") + docNumber + ".pdf";

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(pdfBytes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderAdminUpdateResponse> updateOrder(@PathVariable String id, @RequestBody OrderAdminUpdateRequest order) {
        OrderDto orderDto = orderAdminApiMapper.toDto(order);
        OrderDto updatedOrder = orderService.update(id, orderDto);
        return ResponseEntity.ok(orderAdminApiMapper.toUpdateResponse(updatedOrder));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderAdminUpdateResponse> patchOrder(@PathVariable String id, @RequestBody OrderPatchRequest patchDto) {
        OrderDto updatedOrder = orderService.patch(id, patchDto);
        return ResponseEntity.ok(orderAdminApiMapper.toUpdateResponse(updatedOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // todo remove before prod
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteOrder() {
        orderRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
