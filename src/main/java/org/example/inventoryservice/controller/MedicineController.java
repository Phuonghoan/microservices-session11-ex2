package org.example.inventoryservice.controller;

import org.example.inventoryservice.entity.Medicine;
import org.example.inventoryservice.service.InventoryService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medicines")
public class MedicineController {

    private final InventoryService inventoryService;

    public MedicineController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET - Lấy thông tin thuốc
    @GetMapping("/{id}")
    public Medicine getMedicineById(@PathVariable Long id) {
        return inventoryService.getMedicineById(id);
    }

    // PUT - Cập nhật giá thuốc
    @PutMapping("/{id}/price")
    public Medicine updatePrice(
            @PathVariable Long id,
            @RequestBody UpdatePriceRequest request
    ) {

        Medicine medicine = new Medicine();

        medicine.setId(id);
        medicine.setPrice(request.price());

        return inventoryService.updateMedicine(medicine);
    }

    public record UpdatePriceRequest(Double price) {
    }
}
