package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateRequest;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateResponse;
import sk.tany.rest.api.dto.admin.cart.get.CartAdminGetResponse;
import sk.tany.rest.api.dto.admin.cart.list.CartAdminListResponse;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateResponse;
import sk.tany.rest.api.mapper.CartAdminApiMapper;
import sk.tany.rest.api.service.admin.CartAdminService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/carts")
@RequiredArgsConstructor
public class CartAdminController {

    private final CartAdminService cartService;
    private final CustomerRepository customerRepository;
    private final CartAdminApiMapper cartAdminApiMapper;

    @PostMapping
    public ResponseEntity<CartAdminCreateResponse> createCart(@RequestBody CartAdminCreateRequest cartDto) {
        CartDto savedCart = cartService.save(cartAdminApiMapper.toDto(cartDto));
        return new ResponseEntity<>(cartAdminApiMapper.toCreateResponse(savedCart), HttpStatus.CREATED);
    }

    @GetMapping
    public List<CartAdminListResponse> getAllCarts() {
        List<CartDto> carts = cartService.findAll();
        Set<String> customerIds = carts.stream()
                .map(CartDto::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Customer> customers = customerRepository.findAllById(customerIds);
        Map<String, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, c -> c));

        return carts.stream().map(cart -> {
            CartAdminListResponse response = new CartAdminListResponse();
            response.setCartId(cart.getCartId());
            response.setCreateDate(cart.getCreateDate());
            response.setUpdateDate(cart.getUpdateDate());
            response.setCustomerId(cart.getCustomerId());
            if (cart.getCustomerId() != null && customerMap.containsKey(cart.getCustomerId())) {
                Customer c = customerMap.get(cart.getCustomerId());
                response.setCustomerName(c.getFirstname() + " " + c.getLastname());
            }
            return response;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartAdminGetResponse> getCartById(@PathVariable String id) {
        return cartService.findById(id)
                .map(cartAdminApiMapper::toGetResponse)
                .map(cart -> new ResponseEntity<>(cart, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CartAdminUpdateResponse> updateCart(@PathVariable String id, @RequestBody CartAdminUpdateRequest cartDto) {
        CartDto dto = cartAdminApiMapper.toDto(cartDto);
        dto.setCartId(id);
        CartDto updatedCart = cartService.save(dto);
        return new ResponseEntity<>(cartAdminApiMapper.toUpdateResponse(updatedCart), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable String id) {
        cartService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
