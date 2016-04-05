package com.model;

import java.beans.PropertyEditorSupport;

public class ExpenseCommentsEditor extends PropertyEditorSupport {
	
	@Override
	public void setAsText(String src) {
		String value = (String)getValue();
		if (value == null) {
			setValue(src);
		}
		else {
			// append to, do not overwrite the comments field 
			setValue(value + "\n" + src);
		}
	}
}
