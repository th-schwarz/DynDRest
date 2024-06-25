package codes.thischwa.dyndrest.config;

import codes.thischwa.dyndrest.service.RestoreService;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * A Spring Bean post-processor that performs database restoration based on the provided configuration.
 * This class is responsible for restoring the database if all the necessary conditions are met.
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
    return org.springframework.beans.factory.config.BeanPostProcessor.super
        .postProcessBeforeInitialization(bean, beanName);
  }

  @Nullable
  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof DataSource) {
      dataSource = (DataSource) bean;
    }
    if (bean instanceof AppConfig) {
      appConfig = (AppConfig) bean;
    }
    if (!processed && dataSource != null && appConfig != null) {
      log.info("*** Relevant beans found!");
      RestoreService restoreService = new RestoreService(appConfig, dataSource);
      try {
        restoreService.restore();
        processed = true;
      } catch (Exception e) {
        throw new BeanInitializationException("Error while restoring the Database.", e);
      }
    }

    return org.springframework.beans.factory.config.BeanPostProcessor.super
        .postProcessAfterInitialization(bean, beanName);
  }
}
