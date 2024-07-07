package codes.thischwa.dyndrest.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;

/**
 * A Spring Bean post-processor that performs database restoration based on the provided
 * configuration. This class is responsible for restoring the database if all the necessary
 * conditions are met.
 */
@Profile("!test")
@Slf4j
public abstract class PostProcessor implements BeanPostProcessor {

  private final Collection<Class> wanted;
  private final Collection<Object> initialized = new HashSet<>();
  private boolean processed;

  public PostProcessor() {
    wanted = new HashSet<>();
    Collections.addAll(wanted, getWanted());
  }

  public abstract Class[] getWanted();

  public abstract void process(Collection<Object> wantedBeans) throws Exception;

  @Nullable
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
  }

  /**
   * Performs post-processing after the initialization of a Spring bean. This method checks if the
   * bean matches the desired types and initializes them. Once all the desired beans are
   * initialized, it triggers the processing logic. This method should be implemented by a subclass
   * of PostProcessor.
   *
   * @param bean The Spring bean instance to be post-processed.
   * @param beanName The name of the Spring bean.
   * @return The post-processed bean.
   * @throws BeansException if an error occurs during the post-processing.
   */
  @Nullable
  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Collection<Class> toBeRemoved = new HashSet<>();
    for (Class<?> wantedBean : wanted) {
      if (wantedBean.isInstance(bean) && !initialized.contains(bean)) {
        initialized.add(bean);
        toBeRemoved.add(wantedBean);
      }
    }
    wanted.removeAll(toBeRemoved);
    if (!processed && wanted.isEmpty()) {
      log.info("*** Relevant beans found!");
      try {
        process(initialized);
      } catch (Exception e) {
        throw new BeanInitializationException(
            "Error while processing " + this.getClass().getName());
      }
      processed = true;
    }

    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }
}
