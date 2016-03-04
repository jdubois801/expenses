package com.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.model.Expense;

public interface ExpenseRepository extends PagingAndSortingRepository<Expense, String> {
}
