package com.services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;

import com.model.Expense;
import com.repositories.ExpenseRepository;
import com.services.exceptions.NotFoundException;
import com.services.exceptions.ValidationException;

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
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		try {
			service.findExpense(null);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {}
	}
	
	// repository exceptions are passed out
	@Test
	public void findExpense_repositoryException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		when(repository.findOne(anyString())).thenThrow(new MockitoException("test"));
		
		try {
			service.findExpense("1");
			fail("Expected MockitoException");
		} catch (MockitoException e) {}
		
		verify(repository, times(1)).findOne(anyString());
	}

	// verify success
	@Test
	public void findExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		try {
			service.deleteExpense(null);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {}
	}
	
	// repository exceptions are passed out
	@Test
	public void deleteExpense_repositoryException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		doThrow(new MockitoException("test")).when(repository).delete(anyString());
		
		try {
			service.deleteExpense("1");
			fail("Expected MockitoException");
		} catch (MockitoException e) {}
		
		verify(repository, times(1)).delete(anyString());
	}
	
	// verify success
	@Test
	public void deleteExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		doNothing().when(repository).delete(anyString());
		
		service.deleteExpense("1");
		
		verify(repository, times(1)).delete(eq("1"));
	}
	
	// repository exceptions are passed out
	@Test
	public void listExpenses_repositoryException() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		when(repository.findAll()).thenThrow(new MockitoException("test"));
		
		try {
			service.listExpenses();
			fail("Expected MockitoException");
		} catch (MockitoException e) {}
		
		verify(repository, times(1)).findAll();
	}
	
	// verify success
	@Test
	public void listExpenses_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		List<Expense> expList = new ArrayList<>();
		Expense expense = new Expense();
		expense.setId("one");
		expList.add(expense);
		
		when(repository.findAll()).thenReturn(expList);
		
		Iterable<Expense> results = service.listExpenses();
		assertNotNull(results);
		assertTrue(results.iterator().hasNext());
		Expense exp = results.iterator().next();
		assertNotNull(exp);
		assertEquals("one", exp.getId());
	
		verify(repository, times(1)).findAll();
	}
	
	// a null object causes a ValidationException
	@Test
	public void createExpense_null() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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
		ExpenseService service = new ExpenseServiceImpl(repository);
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

	// a null id causes a NotFoundException
	@Test
	public void updateExpense_noId() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		Expense expense = new Expense();
		
		try {
			service.updateExpense(null, expense, null);
			fail("Expected NotFoundException");
		} catch (NotFoundException e) {
			assertEquals("Expense not found for id [null]", e.getMessage());
		}
	}
	
	// a null object causes a ValidationException
	@Test
	public void updateExpense_null() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", null, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Submitted expense must not be null", e.getMessage());
		}
	}

	// expense Id cannot be updated
	@Test
	public void updateExpense_updateId() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setId("one");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense Id cannot be updated.", e.getMessage());
		}
	}
	
	// an expense total must not be negative
	@Test
	public void updateExpense_negativeTotal() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setTotal(new BigDecimal("-1.0"));
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense total cannot be negative.", e.getMessage());
		}
	}

	// an expense merchant is required
	@Test
	public void updateExpense_noMerchant() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setMerchant("");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense merchant cannot be empty.", e.getMessage());
		}
	}

	// an expense datetime cannot be prior to 1970
	@Test
	public void updateExpense_invalidDatetime() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setDatetime(SDF.parse("01/01/1900"));
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense datetime is invalid (cannot be prior to 1970).", e.getMessage());
		}
	}
	
	// reimbursed expenses cannot be updated
	@Test
	public void updateExpense_reimbursed() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setMerchant("one");
		
		Expense savedExpense = savedExpense();
		savedExpense.setStatus("reimbursed");
		when(repository.findOne(eq("1"))).thenReturn(savedExpense);
		
		try {
			service.updateExpense("1", expense, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Reimbursed expense cannot be updated.", e.getMessage());
		}
	}

	// an expense status can only be 'new' or 'reimbursed'
	@Test
	public void updateExpense_invalidStatus() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setStatus("invalid");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		try {
			service.updateExpense("1", expense, null);
			fail("Expected ValidationException");
		} catch (ValidationException e) {
			assertEquals("Expense status is invalid (must be either 'new' or 'reimbursed')", e.getMessage());
		}
	}
	
	// validate success
	@Test
	public void updateExpense_success() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setMerchant("Starbucks");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.updateExpense("1", expense, null);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertEquals("1", capturedExpense.getId());  // Id didn't change
		assertEquals("new", capturedExpense.getStatus()); // status didn't change
		assertEquals("Starbucks", capturedExpense.getMerchant()); // merchant was updated
		
		verify(repository, times(1)).findOne(eq("1"));
		verify(repository, times(1)).save(any(Expense.class));
	}
	
	// validate success
	@Test
	public void updateExpense_successMultiFieldUpdate() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setMerchant("Starbucks");
		expense.setStatus("reimbursed");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.updateExpense("1", expense, null);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertEquals("1", capturedExpense.getId());  // Id didn't change
		assertEquals("reimbursed", capturedExpense.getStatus()); // status was changed
		assertEquals("Starbucks", capturedExpense.getMerchant()); // merchant was updated
		
		verify(repository, times(1)).findOne(eq("1"));
		verify(repository, times(1)).save(any(Expense.class));
	}

	// validate success
	@Test
	public void updateExpense_successAppendComments() throws Exception {
		ExpenseService service = new ExpenseServiceImpl(repository);
		Expense expense = new Expense();
		expense.setComments("append");
		
		Map<String,Object> values = new HashMap<>();
		values.put("foo", "bar");
		values.put("comments", "more");
		
		when(repository.findOne(eq("1"))).thenReturn(savedExpense());
		
		Expense resultExpense = savedExpense();
		resultExpense.setId("testing");
		when(repository.save(expenseCaptor.capture())).thenReturn(resultExpense);
		
		String idResult = service.updateExpense("1", expense, values);
		assertEquals("testing", idResult);
		
		Expense capturedExpense = expenseCaptor.getValue();
		assertNotNull(capturedExpense);
		assertEquals("1", capturedExpense.getId());  // Id didn't change
		assertEquals("comments\nappend", capturedExpense.getComments()); // comments are appended
		
		verify(repository, times(1)).findOne(eq("1"));
		verify(repository, times(1)).save(any(Expense.class));
	}

}