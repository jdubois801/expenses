package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.Expense;
import com.repositories.ExpenseRepository;

@Service
public class ExpenseService {

	@Autowired
	ExpenseRepository repository;
	
	public Expense[] listExpenses() throws Exception {
    	System.err.println("ExpenseService:listExpenses");

    	// TODO - if you want paging, use findAll(new Pagable());
    	for (Expense exp : repository.findAll()) {
        	System.err.println("Expense : " + exp);
    		
    	}
		return new Expense[0];
	}
	
	public void deleteExpense(String id) throws Exception {
		repository.delete(id);
	}
	
	public Expense findExpense(String id) throws Exception {
		
		if (id == null || id.length() < 1) {
    		throw new Exception("not found");  // TODO - throw a better typed exception
		}
		
		return repository.findOne(id);
	}

	public String createExpense(Expense expense) throws Exception {
		expense.setStatus("new");
		return repository.save(expense).getId();
	}
	
	public String updateExpense(String id, Expense expense) throws Exception {
		return repository.save(expense).getId();
	}

}
