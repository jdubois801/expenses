package expense.api.services;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import expense.api.model.Expense;
import expense.api.repositories.ExpenseRepository;
import expense.api.services.exceptions.NotFoundException;
import expense.api.services.exceptions.OutOfBoundsException;
import expense.api.services.exceptions.ServiceException;
import expense.api.services.exceptions.ValidationException;
import expense.api.services.validation.ExpenseValidator;

@Service
public class ExpenseServiceImpl implements ExpenseService {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private ExpenseRepository repository;
	private MongoTemplate mongoTemplate;
	
	@Autowired
	public ExpenseServiceImpl(ExpenseRepository repository, MongoTemplate mongoTemplate) {
		this.repository = repository;
		this.mongoTemplate = mongoTemplate;
	}
	
	@Override
	public Iterable<Expense> listExpenses() throws OutOfBoundsException {
		return listExpenses(new HashMap<>());
	}
	
	@Override
	public Iterable<Expense> listExpenses(Map<String,String> filters) throws OutOfBoundsException {

		int page = 0;
		int size = 1000;  // arbitrary default size
		
		// look for paging parameters
		if (filters.containsKey("page")) {
			page = Integer.parseInt(filters.get("page"));
			
			if (page < 0) {
				throw new OutOfBoundsException("Paged result set page must be greater than or equal to 0.");
			}
			
			filters.remove("page");
		}
		
		if (filters.containsKey("size")) {
			size = Integer.parseInt(filters.get("size"));
			
			// enforce arbitrary page size limits
			if (size < 0 || size > 10000) {
				throw new OutOfBoundsException("Paged result set size must be between 0 and 10,000.");
			}
			
			filters.remove("size");
		}

		// no filters? - return the paged results
		if (filters.isEmpty()) {
			return repository.findAll(new PageRequest(page, size)).getContent();
		}
		
		// use the submitted filters to build a criteria query
		
		Query query = new Query(CriteriaBuilder.build(filters));
		query.limit(size);
		query.skip(page);
		
		return mongoTemplate.find(query, Expense.class);
	}
	
	@Override
	public void deleteExpense(String id) throws NotFoundException, ValidationException {
		if (id == null || id.length() < 1) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		Expense savedExpense = repository.findOne(id);
		if (savedExpense == null) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		// business rule: can't delete a reimbursed expense
		if ("reimbursed".equals(savedExpense.getStatus())) {
			throw new ValidationException("Reimbursed expense cannot be deleted.");
		}
		
		repository.delete(id);
	}
	
	@Override
	public Expense findExpense(String id) throws NotFoundException {
		
		if (id == null || id.length() < 1) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		Expense result = repository.findOne(id);
		
		if (result == null) { 
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		return result;
	}

	@Override
	public String createExpense(Expense expense) throws ValidationException {
		
		ExpenseValidator.validate(expense);
		
		expense.setId(null);
		// business rule: all expenses start in the 'new' state if not otherwise specified
		if (expense.getStatus() == null) {
			expense.setStatus("new");
		}
		return repository.save(expense).getId();
	}
	
	@Override
	public String updateExpense(String id, Expense expense) throws NotFoundException,ValidationException,ServiceException {
		if (id == null || id.length() < 1) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		Expense savedExpense = repository.findOne(id);
		if (savedExpense == null) {
    		throw new NotFoundException("Expense not found for id [" + id + "]");
		}
		
		if ("reimbursed".equals(savedExpense.getStatus())) {
			throw new ValidationException("Reimbursed expense cannot be updated.");
		}
		
		ExpenseValidator.validate(id, expense);
		
		// business rule: comments can only be added to.
		if (savedExpense.getComments() != null && expense.getComments() != null) {
			expense.setComments(savedExpense.getComments() + "\n" + expense.getComments());
		}
		
		try {
			NullAwareBeanUtils.copy(savedExpense,  expense);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | InvocationTargetException e) {
			throw new ServiceException("Error updating expense.", e);
		}
		
		return repository.save(savedExpense).getId();
	}

	// a small helper class for mapping API filters to db query criteria
	public static class CriteriaBuilder {
		
		public static Criteria build(Map<String,String> filters) throws OutOfBoundsException {
			List<Criteria> criteriaList = new ArrayList<>();
			
			for (String key : filters.keySet()) {
				
				Object value = filters.get(key);
				
				// handle datetime filter
				if ("datetime".equals(key)) {
					try {
						Date dateFilter = SDF.parse((String)value);
						
						// deal with SDF parsing the date in the server local (or JVM local) time zone
						long defaultTimeZoneOffset = TimeZone.getDefault().getOffset(dateFilter.getTime());
						GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
						cal.setTimeInMillis(dateFilter.getTime() + defaultTimeZoneOffset);
						
						value = cal.getTime();
						
					} catch (ParseException e) {
						throw new OutOfBoundsException("Unable to parse requested date filter [" + filters.get(key) + "]");
					}
				}
				
				criteriaList.add(Criteria.where(key).is(value));
			}
			
			return new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
		}
	}
}
