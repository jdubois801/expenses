package com.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ExpenseValidator;
import com.model.Expense;
import com.msgs.ResponseErr;
import com.msgs.ResponseId;
import com.msgs.ResponseMsg;
import com.services.ExpenseService;

@RestController
public class ExpenseController {

	public static final SimpleDateFormat SDF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	@Autowired
	private ExpenseService service;
	
    @RequestMapping(value="/expense", method=RequestMethod.POST)
    @ResponseBody
    public ResponseMsg createExpense(@RequestBody Expense expense) throws IOException {
    	
    	ResponseMsg validationMsg = ExpenseValidator.isValid(expense);
    	if (validationMsg != null) {
    		return validationMsg;
    	}
    	
    	// TODO - include the location header

    	try {
        	return new ResponseId(service.createExpense(expense));
    	}
    	catch (Exception e) {
    		return new ResponseErr("Expense not found");
    	}
    }
    
    @RequestMapping(value="/expense/{id}", method=RequestMethod.PUT)
    @ResponseBody
    public ResponseMsg updateExpense(@PathVariable String id, @RequestBody Expense expense) throws IOException {
    	
    	ResponseMsg validationMsg = ExpenseValidator.isValid(id, expense);
    	if (validationMsg != null) {
    		return validationMsg;
    	}
    	
    	try {
    		service.updateExpense(id, expense);
        	return new ResponseId(id);
    	}
    	catch (Exception e) {
    		return new ResponseErr("Expense not found");
    	}
    }

    @RequestMapping(value="/expense/{id}", method=RequestMethod.DELETE)
    @ResponseBody
    public ResponseMsg deleteExpense(@PathVariable String id) throws IOException {

    	if (id == null || id.length() < 1) {
    		return new ResponseErr("Expense id is required.");
		}
    	
    	try {
    		service.deleteExpense(id);
        	return new ResponseMsg();
    	}
    	catch (Exception e) {
    		return new ResponseErr("Expense not found");
    	}
    }
    
    @RequestMapping(value="/expense/{id}", method=RequestMethod.GET)
    @ResponseBody
    public Expense getExpense(@PathVariable String id) throws IOException {
    	
    	try {
    		return service.findExpense(id);
    	}
    	catch (Exception e) {
//    		return new ResponseErr("Expense [" + id + "] not found");
    		return null;
    	}
    }

    @RequestMapping(value="/expenses", method=RequestMethod.GET)
    @ResponseBody
    public Expense[] listExpenses() throws IOException {

    	// TODO - add a search filter
    	
    	try {
    		return service.listExpenses();
    	}
    	catch (Exception e) {
//    		return new ResponseErr("Expense not found");
    		return null;
    	}
    }

}

