package com.finance.manager.config;

import com.finance.manager.entity.Category;
import com.finance.manager.enums.TransactionType;
import com.finance.manager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes the database with default system categories on application startup.
 * Default categories cannot be modified or deleted by users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.findByIsCustomFalse().isEmpty()) {
            List<Category> defaultCategories = List.of(
                // INCOME
                Category.builder().name("Salary").type(TransactionType.INCOME).isCustom(false).build(),
                // EXPENSE
                Category.builder().name("Food").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Rent").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Transportation").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Entertainment").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Healthcare").type(TransactionType.EXPENSE).isCustom(false).build(),
                Category.builder().name("Utilities").type(TransactionType.EXPENSE).isCustom(false).build()
            );
            categoryRepository.saveAll(defaultCategories);
            log.info("Default categories initialized successfully.");
        }
    }
}
