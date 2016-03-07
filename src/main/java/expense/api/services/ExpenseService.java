package expense.api.services;

import java.util.Map;

import expense.api.model.Expense;
import expense.api.services.exceptions.NotFoundException;
import expense.api.services.exceptions.OutOfBoundsException;
import expense.api.services.exceptions.ServiceException;
import expense.api.services.exceptions.ValidationException;

public interface ExpenseService {
	/**
	 * List all the expenses stored in the database.
	 * 
	 * @return The list of expenses.
	 * @throws OutOfBoundsException 
	 */
	Iterable<Expense> listExpenses() throws OutOfBoundsException;
	
	/**
	 * List all the expenses stored in the database with filtering and paging.
	 * 
	 * @param filters The map of filter values.
	 * @return The list of expenses.
	 * @throws OutOfBoundsException if paging values are invalid.
	 */
	Iterable<Expense> listExpenses(Map<String,String> filters) throws OutOfBoundsException;
	
	/**
	 * Delete an expense from the database.  Expenses with a 'reimbursed' status cannot be deleted.
	 * 
	 * @param id The id of the expense.
	 * @throws NotFoundException if the expense id is invalid or the expense does not exist in the database. 
	 * @throws ValidationException if the expense has 'reimbursed' status.
	 */
	void deleteExpense(String id) throws NotFoundException, ValidationException;
	
	/**
	 * Find a specific expense in the database.
	 * 
	 * @param id The id of the expense.
	 * @return The expense.
	 * @throws NotFoundException if the expense id is invalid or the expense does not exist in the database.
	 */
	Expense findExpense(String id) throws NotFoundException;
	
	/**
	 * Save an expense in the database.
	 * 
	 * @param expense The expense to save.
	 * @return The database id of the saved expense.
	 * @throws ValidationException if the expense is invalid.
	 */
	String createExpense(Expense expense) throws ValidationException;
	
	/**
	 * Update an existing expense in the database.  The properties of the expense to save can be sparse, meaning if a property has a null value, the 
	 * no change to that property in the database will occur.  Expenses with a 'reimbursed' status cannot be deleted.
	 * 
	 * @param id The id of the expense.
	 * @param expense The expense to update.
	 * @return The database id of the saved expense.
	 * @throws NotFoundException if the expense id is invalid or the expense does not exist in the database.
	 * @throws ValidationException if the expense is invalid or if the expense has a 'reimbursed' status.
	 * @throws ServiceException if some other unclassified error occurred.
	 */
	String updateExpense(String id, Expense expense) throws NotFoundException,ValidationException,ServiceException;
}
