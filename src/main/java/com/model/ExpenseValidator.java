package com.model;

import java.math.BigDecimal;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ExpenseValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(Expense.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
	
		if (target == null) {
			errors.reject("Expense cannot be null.");
		}
		
		Expense expense = (Expense)target;
		
		if (expense.getMerchant() == null || expense.getMerchant().length() < 1) {
			errors.rejectValue("merchant", "Merchant is required.");
		}

		if (expense.getTotal() == null) { 
			errors.rejectValue("total", "Total is required.");
		}
		else if (expense.getTotal().compareTo(BigDecimal.ZERO) < 1) {
			errors.rejectValue("total", "Total cannot be negative.");
		}

		// TODO - more validations
	}

}
