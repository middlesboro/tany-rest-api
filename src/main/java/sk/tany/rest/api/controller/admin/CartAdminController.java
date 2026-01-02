package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CartAdminResponse;
import sk.tany.rest.api.dto.CartDto;
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

    @PostMapping
    public ResponseEntity<CartDto> createCart(@RequestBody CartDto cartDto) {
        CartDto savedCart = cartService.save(cartDto);
        return new ResponseEntity<>(savedCart, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CartAdminResponse> getAllCarts() {
        List<CartDto> carts = cartService.findAll();
        Set<String> customerIds = carts.stream()
                .map(CartDto::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Customer> customers = customerRepository.findAllById(customerIds);
        Map<String, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, c -> c));

        return carts.stream().map(cart -> {
            CartAdminResponse response = new CartAdminResponse();
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
    public ResponseEntity<CartDto> getCartById(@PathVariable String id) {
        return cartService.findById(id)
                .map(cart -> new ResponseEntity<>(cart, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CartDto> updateCart(@PathVariable String id, @RequestBody CartDto cartDto) {
        cartDto.setCartId(id);
        CartDto updatedCart = cartService.save(cartDto);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable String id) {
        cartService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
