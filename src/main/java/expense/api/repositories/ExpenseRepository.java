package expense.api.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import expense.api.model.Expense;

public interface ExpenseRepository extends PagingAndSortingRepository<Expense, String> {
}
