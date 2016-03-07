package expense.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;

@Configuration
@EnableMongoRepositories
public class ApplicationConfig extends AbstractMongoConfiguration {

	 @Override
	  protected String getDatabaseName() {
	    return "e-store";
	  }

	  @Override
	  public Mongo mongo() throws Exception {
	    return new Mongo();
	  }

	  @Override
	  protected String getMappingBasePackage() {
	    return "com.oreilly.springdata.mongodb";
	  }
}
