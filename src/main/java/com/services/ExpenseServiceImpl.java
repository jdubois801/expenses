package com.services;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import com.model.Expense;
import com.model.ExpenseCommentsEditor;
import com.repositories.ExpenseRepository;
import com.services.exceptions.NotFoundException;
import com.services.exceptions.ServiceException;
import com.services.exceptions.ValidationException;
import com.services.validation.ExpenseValidator;

@Service
public class ExpenseServiceImpl implements ExpenseService {

	private ExpenseRepository repository;
	
	@Autowired
	public ExpenseServiceImpl(ExpenseRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public Iterable<Expense> listExpenses() {

    	// TODO - if you want paging, use findAll(new Pagable());
		return repository.findAll();
	}
	
	@Override
	public void deleteExpense(String id) throws NotFoundException {
		if (id == null || id.length() < 1) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		repository.delete(id);
	}
	
	@Override
	public Expense findExpense(String id) throws NotFoundException {
		
		if (id == null || id.length() < 1) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		return repository.findOne(id);
	}

	@Override
	public String createExpense(Expense expense) throws ValidationException {
		
		ExpenseValidator.validate(expense);
		
		expense.setId(null);
		expense.setStatus("new");
		return repository.save(expense).getId();
	}
	
	@Override
	public String updateExpense(String id, Expense expense, Map<String,Object> values) throws NotFoundException,ValidationException,ServiceException {
		if (id == null || id.length() < 1) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		Expense savedExpense = repository.findOne(id);
		if (savedExpense == null) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		if ("reimbursed".equals(savedExpense.getStatus())) {
			throw new ValidationException("Reimbursed expense cannot be updated.");
		}
		
		MutablePropertyValues mpv = new MutablePropertyValues(values);
		DataBinder db = new DataBinder(savedExpense);
		db.setAllowedFields("merchant","total","datetime","comments","status");
		db.registerCustomEditor(String.class, "comments", new ExpenseCommentsEditor());
		db.setValidator(new com.model.ExpenseValidator());
		
		db.bind(mpv);
		db.validate();
		try {
			db.close();
		} catch (BindException be) {
			throw new ValidationException("Error updating expense", be);
		}
		
		// TODO - would it be better to use the Spring DataBinder to do validation and binding?
		ExpenseValidator.validate(id, expense);
		
		try {
			NullAwareBeanUtils.copy(savedExpense,  expense);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | InvocationTargetException e) {
			throw new ServiceException("Error updating expense.", e);
		}
		
		return repository.save(savedExpense).getId();
	}

}
