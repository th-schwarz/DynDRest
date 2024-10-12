package codes.thischwa.dyndrest.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@Slf4j
public class LiquibaseService {

  private final DataSource dataSource;

    private final SpringLiquibase springLiquibase;

    public LiquibaseService(DataSource dataSource, SpringLiquibase springLiquibase) {
    this.dataSource = dataSource;
        this.springLiquibase = springLiquibase;
    }

 @Scheduled(cron = "0 * * * * ?")
  public void schedule() {
    createDatabaseSnapshot("backup/snapshot.xml");
  }

  public void createDatabaseSnapshot(String changeLogFile) {
    try {
      Database database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));

        Liquibase liquibase = new Liquibase(springLiquibase.getChangeLog(), new FileSystemResourceAccessor(), database);

        // Generating Diff
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(liquibase.getDatabase(), null, new CompareControl());
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());


        // Writing the output to a file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(changeLogFile))) {
            diffToChangeLog.print(changeLogFile, true);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
