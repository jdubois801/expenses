package expense.api.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import expense.api.model.Expense;
import expense.api.repositories.ExpenseRepository;
import expense.api.services.exceptions.NotFoundException;
import expense.api.services.exceptions.ValidationException;

@RunWith(MockitoJUnitRunner.class)
public class ExpenseServiceTest {
	
	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
	
	@Mock
	ExpenseRepository repository;

	ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
	
	// create a saved expense for update tests
	private Expense savedExpense() {
		Expense savedExpense = new Expense();
		savedExpense.setId("1");
		savedExpense.setMerchant("merchant");
		savedExpense.setTotal(new BigDecimal("1.0"));
		savedExpense.setDatetime(new Date());
		savedExpense.setStatus("new");
		savedExpense.setComments("comment");
		
		return savedExpense;
	}
	
	// a null Id causes a NotFoundException
	@Test
	public void findExpense_nullId() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		try {
			service.findExpense(null);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {}
	}
	
	// repository exceptions are passed out
	@Test
	public void findExpense_repositoryException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		when(repository.findOne(anyString())).thenThrow(new MockitoException("test"));
		
		try {
			service.findExpense("1");
			fail("Expected MockitoException");
		} catch (MockitoException e) {}
		
		verify(repository, times(1)).findOne(anyString());
	}

	// not found exceptions are passed out
	@Test
	public void findExpense_notFound() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		when(repository.findOne(anyString())).thenReturn(null);
		
		try {
			service.findExpense("1");
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {}
		
		verify(repository, times(1)).findOne(anyString());
	}

	// verify success
	@Test
	public void findExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setId("one");
		
		when(repository.findOne(anyString())).thenReturn(expense);
		
		Expense exp = service.findExpense("1");
		assertNotNull(exp);
		assertEquals("one", exp.getId());
		
		verify(repository, times(1)).findOne(eq("1"));
	}

	// a null Id causes a NotFoundException
	@Test
	public void deleteExpense_nullId() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		try {
			service.deleteExpense(null);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {}
	}
	
	// repository exceptions are passed out
	@Test
	public void deleteExpense_repositoryException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		Expense savedExpense = savedExpense();
		when(repository.findOne(anyString())).thenReturn(savedExpense);
		
		doThrow(new MockitoException("test")).when(repository).delete(anyString());
		
		try {
			service.deleteExpense("1");
			fail("Expected MockitoException");
		} catch (MockitoException e) {}
		
		verify(repository, times(1)).findOne(anyString());
		verify(repository, times(1)).delete(anyString());
	}
	
	// repository exceptions are passed out
	@Test
	public void deleteExpense_reimbursedException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		Expense savedExpense = savedExpense();
		savedExpense.setStatus("reimbursed");
		when(repository.findOne(anyString())).thenReturn(savedExpense);
		
		doNothing().when(repository).delete(anyString());
		
		try {
			service.deleteExpense("1");
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Reimbursed expense cannot be deleted.", e.getMessage());
		}
		
		verify(repository, times(1)).findOne(anyString());
		verify(repository, times(0)).delete(anyString());
	}

	// verify success
	@Test
	public void deleteExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		Expense savedExpense = savedExpense();
		when(repository.findOne(anyString())).thenReturn(savedExpense);
		
		doNothing().when(repository).delete(anyString());
		
		service.deleteExpense("1");
		
		verify(repository, times(1)).findOne(anyString());
		verify(repository, times(1)).delete(eq("1"));
	}
	
	// repository exceptions are passed out
	@Test
	public void listExpenses_repositoryException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		when(repository.findAll(any(PageRequest.class))).thenThrow(new MockitoException("test"));
		
		try {
			service.listExpenses();
			fail("Expected MockitoException");
		} catch (MockitoException e) {}
		
		verify(repository, times(1)).findAll(any(PageRequest.class));
	}
	
	// verify success
	@Test
	public void listExpenses_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		when(repository.findAll(any(PageRequest.class))).thenReturn(mock(Page.class));
		
		Iterable<Expense> results = service.listExpenses();
		assertNotNull(results);
		assertFalse(results.iterator().hasNext());
	
		verify(repository, times(1)).findAll(any(PageRequest.class));
	}
	
	// a null object causes a ValidationException
	@Test
	public void createExpense_null() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		try {
			service.createExpense(null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Submitted expense must not be null", e.getMessage());
		}
	}
	
	// a new expense total is required
	@Test
	public void createExpense_noTotal() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setDatetime(new Date());
		expense.setMerchant("merchant");
		
		try {
			service.createExpense(expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense total is required.", e.getMessage());
		}
	}

	// a new expense total must not be negative
	@Test
	public void createExpense_negativeTotal() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setDatetime(new Date());
		expense.setMerchant("merchant");
		expense.setTotal(new BigDecimal("-1.0"));
		
		try {
			service.createExpense(expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense total cannot be negative.", e.getMessage());
		}
	}

	// a new expense merchant is required
	@Test
	public void createExpense_noMerchant() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setDatetime(new Date());
		expense.setTotal(new BigDecimal("1.0"));
		
		try {
			service.createExpense(expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense merchant is required.", e.getMessage());
		}
	}

	// a new expense datetime is required
	@Test
	public void createExpense_noDatetime() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("merchant");
		expense.setTotal(new BigDecimal("1.0"));
		
		try {
			service.createExpense(expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense datetime is required.", e.getMessage());
		}
	}

	// a new expense datetime cannot be prior to 1970
	@Test
	public void createExpense_invalidDatetime() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("merchant");
		expense.setTotal(new BigDecimal("1.0"));

		expense.setDatetime(SDF.parse("01/01/1900"));
		
		try {
			service.createExpense(expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense datetime is invalid (cannot be prior to 1970).", e.getMessage());
		}
	}

	// validate success
	@Test
	public void createExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("merchant");
		expense.setTotal(new BigDecimal("1.0"));
		expense.setDatetime(new Date());
		
		Expense resultExpense = new Expense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.createExpense(expense);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertNull(capturedExpense.getId());
		assertEquals("new", capturedExpense.getStatus());  // business rule: status value is defaulted
		
		verify(repository, times(1)).save(any(Expense.class));
	}
	
	// validate success with optional comments
	@Test
	public void createExpense_successOptionalComments() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("merchant");
		expense.setTotal(new BigDecimal("1.0"));
		expense.setDatetime(new Date());
		expense.setComments("comments");
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.createExpense(expense);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertNull(capturedExpense.getId());
		assertEquals("new", capturedExpense.getStatus());  // business rule: status value is defaulted
		assertEquals("comments", capturedExpense.getComments());
		
		verify(repository, times(1)).save(any(Expense.class));
	}

	// validate successful creation of expense in the reimbursed state
	@Test
	public void createExpense_successReimbursed() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("merchant");
		expense.setTotal(new BigDecimal("1.0"));
		expense.setDatetime(new Date());
		expense.setStatus("reimbursed");
		
		Expense resultExpense = new Expense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.createExpense(expense);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertNull(capturedExpense.getId());
		assertEquals("reimbursed", capturedExpense.getStatus());  // business rule: status value is defaulted
		
		verify(repository, times(1)).save(any(Expense.class));
	}
	
	// a null id causes a NotFoundException
	@Test
	public void updateExpense_noId() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		Expense expense = new Expense();
		
		try {
			service.updateExpense(null, expense);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {
			assertEquals("Expense not found for id [null]", e.getMessage());
		}
	}
	
	// a null object causes a ValidationException
	@Test
	public void updateExpense_null() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Submitted expense must not be null", e.getMessage());
		}
	}

	// expense Id cannot be updated
	@Test
	public void updateExpense_updateId() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setId("one");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense Id cannot be updated.", e.getMessage());
		}
	}
	
	// an expense total must not be negative
	@Test
	public void updateExpense_negativeTotal() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setTotal(new BigDecimal("-1.0"));
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense total cannot be negative.", e.getMessage());
		}
	}

	// an expense merchant is required
	@Test
	public void updateExpense_noMerchant() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense merchant cannot be empty.", e.getMessage());
		}
	}

	// an expense datetime cannot be prior to 1970
	@Test
	public void updateExpense_invalidDatetime() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setDatetime(SDF.parse("01/01/1900"));
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense datetime is invalid (cannot be prior to 1970).", e.getMessage());
		}
	}
	
	// reimbursed expenses cannot be updated
	@Test
	public void updateExpense_reimbursed() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("one");
		
		Expense savedExpense = savedExpense();
		savedExpense.setStatus("reimbursed");
		when(repository.findOne(eq("1"))).thenReturn(savedExpense);
		
		try {
			service.updateExpense("1", expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Reimbursed expense cannot be updated.", e.getMessage());
		}
	}

	// an expense status can only be 'new' or 'reimbursed'
	@Test
	public void updateExpense_invalidStatus() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setStatus("invalid");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense status is invalid (must be either 'new' or 'reimbursed')", e.getMessage());
		}
	}
	
	// validate success
	@Test
	public void updateExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("Starbucks");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.updateExpense("1", expense);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertEquals("1", capturedExpense.getId());  // Id didn't change
		assertEquals("new", capturedExpense.getStatus()); // status didn't change
		assertEquals("Starbucks", capturedExpense.getMerchant()); // merchant was updated
		
		verify(repository, times(1)).findOne(eq("1"));
		verify(repository, times(1)).save(any(Expense.class));
	}
	
	// validate success with updating multiple properties
	@Test
	public void updateExpense_successMultiFieldUpdate() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setMerchant("Starbucks");
		expense.setStatus("reimbursed");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.updateExpense("1", expense);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertEquals("1", capturedExpense.getId());  // Id didn't change
		assertEquals("reimbursed", capturedExpense.getStatus()); // status was changed
		assertEquals("Starbucks", capturedExpense.getMerchant()); // merchant was updated
		
		verify(repository, times(1)).findOne(eq("1"));
		verify(repository, times(1)).save(any(Expense.class));
	}

	// validate success that comments are appended
	@Test
	public void updateExpense_successAppendComments() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository, null);
		Expense expense = new Expense();
		expense.setComments("append");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.updateExpense("1", expense);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertEquals("1", capturedExpense.getId());  // Id didn't change
		assertEquals("comment\nappend", capturedExpense.getComments()); // comments are appended
		
		verify(repository, times(1)).findOne(eq("1"));
		verify(repository, times(1)).save(any(Expense.class));
	}

}