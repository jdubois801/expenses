package expense.api.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import expense.api.model.Expense;
import expense.api.msgs.ResponseErr;
import expense.api.msgs.ResponseId;
import expense.api.msgs.ResponseList;
import expense.api.msgs.ResponseMsg;
import expense.api.services.ExpenseService;
import expense.api.services.exceptions.ValidationException;

@RestController
public class ExpenseController {

	private final Logger log = LoggerFactory.getLogger(ExpenseController.class);
	
	public static final SimpleDateFormat SDF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	private ExpenseService service;
	
	@Autowired
	public ExpenseController(ExpenseService service) {
		this.service = service;
	}
	
	/** Create a new expense
	 * 
	 * @param expense The new expense to create.
	 * @return A message with the id of the created expense
	 * @throws IOException
	 */
    @RequestMapping(value="/expense", method=RequestMethod.POST)
    @ResponseBody
    public ResponseMsg createExpense(@RequestBody Expense expense) throws IOException {
    	
    	ResponseMsg validationMsg = ControllerExpenseValidator.isValid(expense);
    	if (validationMsg != null) {
    		return validationMsg;
    	}
    	
    	try {
        	return new ResponseId(service.createExpense(expense));
    	}
    	catch (ValidationException e) {
    		return new ResponseErr(e.getMessage());
    	}
    	catch (Exception e) {
    		log.error("Expense not found to create [" + expense.getId() + "]", e);
    		return new ResponseErr("Expense not found");
    	}
    }
    
    /** Update an expense.  Submitted expense object can be sparse.  Only those properties present will be updated.
     * 
     * @param id The id of the expense to update
     * @param expense The expense object
     * @return a status message
     * @throws IOException
     */
    @RequestMapping(value="/expense/{id}", method=RequestMethod.PUT)
    @ResponseBody
    public ResponseMsg updateExpense(@PathVariable String id, @RequestBody Expense expense) throws IOException {
    	
    	ResponseMsg validationMsg = ControllerExpenseValidator.isValid(id, expense);
    	if (validationMsg != null) {
    		return validationMsg;
    	}
    	
    	try {
    		service.updateExpense(id, expense);
        	return new ResponseId(id);
    	}
    	catch (ValidationException e) {
    		return new ResponseErr(e.getMessage());
    	}
    	catch (Exception e) {
    		log.error("Expense not found to update [" + id + "]", e);
    		return new ResponseErr("Expense not found");
    	}
    }

    /** Delete an existing expense.
     * 
     * @param id The id of the expense to delete
     * @return A success message.
     * @throws IOException
     */
    @RequestMapping(value="/expense/{id}", method=RequestMethod.DELETE)
    @ResponseBody
    public ResponseMsg deleteExpense(@PathVariable String id) throws IOException {

    	if (id == null || id.length() < 1) {
    		return new ResponseErr("Expense id is required.");
		}
    	
    	try {
    		service.deleteExpense(id);
    		log.info("expense [" + id + "] deleted.");
        	return null;
    	}
    	catch (ValidationException e) {
    		return new ResponseErr(e.getMessage());
    	}
    	catch (Exception e) {
    		log.error("Expense not found to delete [" + id + "]", e);
    		return new ResponseErr("Expense not found");
    	}
    }
    
    /** Fetch an expense by id.
     * 
     * @param id The id of the expense
     * @return The expense object.
     * @throws IOException
     */
    @RequestMapping(value="/expense/{id}", method=RequestMethod.GET)
    @ResponseBody
    public Object getExpense(@PathVariable String id) throws IOException {
    	
    	try {
    		return service.findExpense(id);
    	}
    	catch (Exception e) {
    		return new ResponseErr("Expense [" + id + "] not found");
    	}
    }

    /** List expenses with a filter.
     * 
     * @param allRequestParams Filtering and paging parameters
     * @return The list of expenses that match the filtering and paging criteria.
     * @throws IOException
     */
    @RequestMapping(value="/expenses", method=RequestMethod.GET)
    @ResponseBody
    public Object listExpenses(@RequestParam Map<String,String> allRequestParams) throws IOException {

    	try {
    		return new ResponseList(service.listExpenses(allRequestParams));
    	}
    	catch (Exception e) {
    		log.error("Exception listing expenses", e);
    		return new ResponseErr(e.getMessage());
    	}
    }

    // a trivial expense input data validator.  more elaborate validation occurs in the service layer.
    public static class ControllerExpenseValidator {

    	public static ResponseMsg isValid(Expense expense) {
    		if (expense == null) {
        		return new ResponseErr("Submitted expense must not be null");
        	}
    		
    		return null;
    	}
    	
    	public static ResponseMsg isValid(String id, Expense expense) {
    		if (id == null || id.length() < 1) {
        		return new ResponseErr("Expense id is required.");
    		}
    		
    		if (expense == null) {
        		return new ResponseErr("Submitted expense must not be null");
        	}
    		
    		return null;
    	}

    }
}

