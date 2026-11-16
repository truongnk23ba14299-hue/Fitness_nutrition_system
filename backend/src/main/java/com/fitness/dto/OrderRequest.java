package com.fitness.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequest (
    @NotEmpty(message= "Order must contain at least one product")
    @Size(max = 50, message = "Order must not exceed 50")
    List<@Valid OrderItemRequest> items,

    @NotBlank(message = "Receiver name is required")
    @Size(max = 100, message = "Receiver name must not exceed 100 characters")
    String receiverName,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Phone number is invalid")
    String phone,

    @NotBlank(message= "Shipping Address is required")
    @Size(max = 500, message= "Address must not exceed 500 characters")
    String shippingAddress,

    @Size(max = 500, message = "Note must not exceed 500 characters")
    String note
){

}
