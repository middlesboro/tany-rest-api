package sk.tany.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerContextDto {
    private CustomerDto customerDto;
    private CustomerContextCartDto cartDto;
}
