package expense.api.msgs;

import expense.api.model.Expense;

// a service response message containing a list of expenses.
public class ResponseList extends ResponseMsg {

	public Iterable<Expense> contents;

	public ResponseList(Iterable<Expense> contents) {
		this.contents = contents;
	}
	
	public Iterable<Expense> getContents() {
		return contents;
	}

	public void setContents(Iterable<Expense> contents) {
		this.contents = contents;
	}
}
