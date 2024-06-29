package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.service.DatabaseRestoreHandler;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * A Spring Bean post-processor that performs database restoration based on the provided
 * configuration. This class is responsible for restoring the database if all the necessary
 * conditions are met.
 */
@Service
@Profile("!test")
@Slf4j
public class ConfigPostProcessor implements BeanPostProcessor {

  @Nullable private DataSource dataSource;
  @Nullable private AppConfig appConfig;

  private boolean processed;

  @Nullable
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
  }

  /**
   * Performs post-processing after bean initialization. If the bean is an instance of {@link
   * DataSource}, it assigns it to the {@link ConfigPostProcessor#dataSource} field. If the bean is
   * an instance of {@link AppConfig}, it assigns it to the {@link ConfigPostProcessor#appConfig}
   * field. If both beans are found, the {@link DatabaseRestoreHandler#restore()} method will be
   * called. If an exception occurs during the restore process, it wraps the exception and throws a
   * {@link BeanInitializationException}.
   *
   * @param bean The initialized bean object.
   * @param beanName The name of the bean.
   * @return The post-processed bean object.
   * @throws BeansException if an error occurs during the bean post-processing.
   */
  @Nullable
  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof DataSource ds) {
      dataSource = ds;
    }
    if (bean instanceof AppConfig ac) {
      appConfig = ac;
    }
    if (!processed && dataSource != null && appConfig != null) {
      log.info("*** Relevant beans found!");
      DatabaseRestoreHandler databaseRestoreHandler =
          new DatabaseRestoreHandler(appConfig, dataSource);
      try {
        databaseRestoreHandler.restore();
        processed = true;
      } catch (Exception e) {
        throw new BeanInitializationException("Error while restoring the Database.", e);
      }
    }

    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }
}
