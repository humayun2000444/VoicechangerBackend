package com.example.voicechanger.repository;

import com.example.voicechanger.entity.PackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, Long> {
    List<PackagePurchase> findByIdUser(Long idUser);
    List<PackagePurchase> findByIdPackage(Long idPackage);
}
