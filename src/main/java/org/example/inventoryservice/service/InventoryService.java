package org.example.inventoryservice.service;

import org.example.inventoryservice.entity.Medicine;
import org.example.inventoryservice.event.OrderEvent;
import org.example.inventoryservice.repository.MedicineRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {

    private final MedicineRepository medicineRepository;

    public InventoryService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    // ====================================
    // REDIS CACHE - LẤY THÔNG TIN THUỐC
    // ====================================

    @Cacheable(value = "medicines", key = "#id")
    public Medicine getMedicineById(Long id) {

        System.out.println(
                ">>> ĐANG TRUY VẤN POSTGRESQL - MEDICINE ID: " + id
        );

        return medicineRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy thuốc có ID: " + id
                        )
                );
    }

    @Transactional
    @CacheEvict(value = "medicines", key = "#medicine.id")
    public Medicine updateMedicine(Medicine medicine) {

        if (medicine.getPrice() == null || medicine.getPrice() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giá thuốc phải lớn hơn hoặc bằng 0"
            );
        }

        Medicine existingMedicine = medicineRepository.findById(medicine.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy thuốc có ID: " + medicine.getId()
                        )
                );

        existingMedicine.setPrice(medicine.getPrice());

        Medicine updatedMedicine = medicineRepository.save(existingMedicine);

        System.out.println(
                ">>> ĐÃ CẬP NHẬT GIÁ THUỐC ID: " + medicine.getId()
        );

        return updatedMedicine;
    }

    // ====================================
    // KAFKA - TRỪ TỒN KHO
    // ====================================

    @Transactional
    @CacheEvict(value = "medicines", key = "#p0.medicineId")
    public void decreaseStock(OrderEvent event) {

        if (event.getQuantity() == null || event.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng thuốc phải lớn hơn 0"
            );
        }

        int updatedRows = medicineRepository.decreaseStock(
                event.getMedicineId(),
                event.getQuantity()
        );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Không tìm thấy thuốc có ID: " + event.getMedicineId()
            );
        }

        System.out.println(
                "Đã trừ " + event.getQuantity()
                        + " thuốc có ID: " + event.getMedicineId()
        );
    }
}
