package expense.api.services.validation;

import java.math.BigDecimal;

import expense.api.model.Expense;
import expense.api.services.exceptions.ValidationException;

public class ExpenseValidator {

	// validate for create
	public static void validate(Expense expense) throws ValidationException {
		if (expense == null) {
    		throw new ValidationException("Submitted expense must not be null");
    	}

		if (expense.getMerchant() == null) {
			throw new ValidationException("Expense merchant is required.");
		}
		
		if (expense.getTotal() == null) {
			throw new ValidationException("Expense total is required.");
		}

		if (expense.getDatetime() == null) {
			throw new ValidationException("Expense datetime is required.");
		}
		
		validate("1", expense);
	}

	// validate for update
	public static void validate(String id, Expense expense) throws ValidationException {

		if (expense == null) {
    		throw new ValidationException("Submitted expense must not be null");
    	}
		
		if (expense.getId() != null) {
			throw new ValidationException("Expense Id cannot be updated.");
		}

		if (expense.getMerchant() != null && expense.getMerchant().length() < 1) {
			throw new ValidationException("Expense merchant cannot be empty.");
		}
		
		if (expense.getTotal() != null && expense.getTotal().compareTo(BigDecimal.ZERO) < 1) {
			throw new ValidationException(
					"Expense total cannot be negative.");
		}
		
		if (expense.getDatetime() != null && expense.getDatetime().getTime() < 0) {
			throw new ValidationException("Expense datetime is invalid (cannot be prior to 1970).");
		}
		
		if (expense.getStatus() != null && !"new".equals(expense.getStatus()) && !"reimbursed".equals(expense.getStatus())) {
			throw new ValidationException("Expense status is invalid (must be either 'new' or 'reimbursed')");
		}
	}

}
