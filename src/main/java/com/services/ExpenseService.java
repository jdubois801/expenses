package com.services;

import java.util.Map;

import com.model.Expense;
import com.services.exceptions.NotFoundException;
import com.services.exceptions.ServiceException;
import com.services.exceptions.ValidationException;

public interface ExpenseService {
	Iterable<Expense> listExpenses();
	void deleteExpense(String id) throws NotFoundException;
	Expense findExpense(String id) throws NotFoundException;
	String createExpense(Expense expense) throws ValidationException;
	String updateExpense(String id, Expense expense, Map<String,Object> values) throws NotFoundException,ValidationException,ServiceException;
	
}
