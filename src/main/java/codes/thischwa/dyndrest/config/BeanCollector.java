package codes.thischwa.dyndrest.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

/**
 * BeanCollector is an abstract class that implements the BeanPostProcessor interface. It provides
 * functionality for collecting and processing desired Spring managed beans.
 *
 * <p>During the bean post-processing, BeanCollector checks if the bean matches the desired types.
 * Once all the desired beans are initialized, it triggers the processing logic by calling the
 * process() method. The processBean() method is called internally to check and initialize the
 * beans.
 */
@Slf4j
public abstract class BeanCollector implements BeanPostProcessor {

  private final Collection<Class<?>> wanted = new HashSet<>();
  private final Collection<Object> initialized = new HashSet<>();
  private boolean processed;

  protected BeanCollector() {
    Collections.addAll(wanted, getWanted());
  }

  /**
   * Retrieves the array of spring-managed bean classes that should be fetched by the
   * post-processor.
   *
   * @return The array of classes.
   */
  public abstract Class<?>[] getWanted();

  /**
   * Starts the process with the desired spring managed beans.
   *
   * @param wantedBeans The collection of Spring bean instances to be processed.
   * @throws BeansException if an error occurs during the processing.
   */
  public abstract void process(Collection<Object> wantedBeans) throws BeansException;

  @Nullable
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
  }

  /**
   * Performs post-processing after the initialization of a Spring bean. This method checks if the
   * bean matches the desired types. Once all the desired beans are initialized, it triggers the
   * processing logic. This method should be implemented by a subclass of PostProcessor.
   *
   * @param bean The Spring bean instance to be post-processed.
   * @param beanName The name of the Spring bean.
   * @return The post-processed bean.
   * @throws BeansException if an error occurs during the post-processing.
   */
  @Nullable
  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    collectBean(bean);
    if (!processed && wanted.isEmpty()) {
      log.info("*** Wanted beans found, start #process!");
      try {
        process(initialized);
      } catch (Exception e) {
        throw new BeanInitializationException(
            "Error while processing " + this.getClass().getName(), e);
      }
      processed = true;
    }

    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }

  private void collectBean(Object bean) {
    Collection<Class<?>> toBeRemoved = new HashSet<>();
    for (Class<?> wantedBean : wanted) {
      if (wantedBean.isInstance(bean) && !initialized.contains(bean)) {
        initialized.add(bean);
        toBeRemoved.add(wantedBean);
      }
    }
    wanted.removeAll(toBeRemoved);
  }
}
