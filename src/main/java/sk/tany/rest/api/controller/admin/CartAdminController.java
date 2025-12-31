package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.service.CartService;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/carts")
@RequiredArgsConstructor
public class CartAdminController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDto> createCart(@RequestBody CartDto cartDto) {
        CartDto savedCart = cartService.save(cartDto);
        return new ResponseEntity<>(savedCart, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CartDto> getAllCarts() {
        return cartService.findAll();
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
