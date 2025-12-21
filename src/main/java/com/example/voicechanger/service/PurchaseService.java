package com.example.voicechanger.service;

import com.example.voicechanger.dto.PurchaseRequest;
import com.example.voicechanger.dto.PurchaseResponse;
import com.example.voicechanger.entity.Package;
import com.example.voicechanger.entity.PackagePurchase;
import com.example.voicechanger.entity.Transaction;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.repository.PackagePurchaseRepository;
import com.example.voicechanger.repository.PackageRepository;
import com.example.voicechanger.repository.TransactionRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PackagePurchaseRepository packagePurchaseRepository;
    private final PackageRepository packageRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    @Transactional
    public PurchaseResponse purchasePackage(Long userId, PurchaseRequest request) {
        log.info("Processing package purchase for user ID: {}, package ID: {}", userId, request.getPackageId());

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Verify package exists and is not expired
        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + request.getPackageId()));

        if (pkg.getExpireDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new RuntimeException("Package has expired");
        }

        // Check if transaction ID already exists
        if (transactionRepository.existsByTnxId(request.getTnxId())) {
            throw new RuntimeException("Transaction ID already exists: " + request.getTnxId());
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTransactionMethod(request.getTransactionMethod());
        transaction.setAmount(pkg.getTotalAmount());
        transaction.setTnxId(request.getTnxId());
        transaction.setDate(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", savedTransaction.getId());

        // Create package purchase record
        PackagePurchase purchase = new PackagePurchase();
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPurchaseAmount(pkg.getTotalAmount());
        purchase.setIdTransaction(savedTransaction.getId());
        purchase.setIdUser(userId);
        purchase.setIdPackage(pkg.getId());

        PackagePurchase savedPurchase = packagePurchaseRepository.save(purchase);
        log.info("Package purchase created with ID: {}", savedPurchase.getId());

        // Add balance to user account
        balanceService.addBalance(userId, pkg.getDuration());
        log.info("Balance added for user ID: {}. Duration: {} seconds", userId, pkg.getDuration());

        return convertToResponse(savedPurchase, pkg, user, savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getMyPurchases(Long userId) {
        log.info("Fetching purchases for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return packagePurchaseRepository.findByIdUser(userId).stream()
                .map(purchase -> {
                    Package pkg = packageRepository.findById(purchase.getIdPackage()).orElse(null);
                    Transaction transaction = transactionRepository.findById(purchase.getIdTransaction()).orElse(null);
                    return convertToResponse(purchase, pkg, user, transaction);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long purchaseId) {
        log.info("Fetching purchase with ID: {}", purchaseId);

        PackagePurchase purchase = packagePurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found with ID: " + purchaseId));

        User user = userRepository.findById(purchase.getIdUser()).orElse(null);
        Package pkg = packageRepository.findById(purchase.getIdPackage()).orElse(null);
        Transaction transaction = transactionRepository.findById(purchase.getIdTransaction()).orElse(null);

        return convertToResponse(purchase, pkg, user, transaction);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAllPurchases() {
        log.info("Fetching all purchases");

        return packagePurchaseRepository.findAll().stream()
                .map(purchase -> {
                    User user = userRepository.findById(purchase.getIdUser()).orElse(null);
                    Package pkg = packageRepository.findById(purchase.getIdPackage()).orElse(null);
                    Transaction transaction = transactionRepository.findById(purchase.getIdTransaction()).orElse(null);
                    return convertToResponse(purchase, pkg, user, transaction);
                })
                .collect(Collectors.toList());
    }

    private PurchaseResponse convertToResponse(PackagePurchase purchase, Package pkg, User user, Transaction transaction) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .purchaseDate(purchase.getPurchaseDate())
                .purchaseAmount(purchase.getPurchaseAmount())
                .packageId(purchase.getIdPackage())
                .packageName(pkg != null ? pkg.getPackageName() : "N/A")
                .duration(pkg != null ? pkg.getDuration() : 0L)
                .transactionId(transaction != null ? transaction.getTnxId() : "N/A")
                .transactionStatus(transaction != null ? transaction.getStatus() : "UNKNOWN")
                .userId(purchase.getIdUser())
                .username(user != null ? user.getUsername() : "N/A")
                .build();
    }
}
