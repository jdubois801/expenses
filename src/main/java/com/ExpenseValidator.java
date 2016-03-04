package com;

import com.model.Expense;
import com.msgs.ResponseErr;
import com.msgs.ResponseMsg;

public class ExpenseValidator {

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
