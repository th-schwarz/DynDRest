package codes.thischwa.dyndrest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Component
class MvcRequestMatcherBuilder {
  private final HandlerMappingIntrospector introspector;
  @Nullable private final String servletPath;

  @Autowired
  MvcRequestMatcherBuilder(HandlerMappingIntrospector introspector) {
    this(introspector, null);
  }

  private MvcRequestMatcherBuilder(
      HandlerMappingIntrospector introspector, @Nullable String servletPath) {
    this.introspector = introspector;
    this.servletPath = servletPath;
  }

  MvcRequestMatcher[] matchers(String... patterns) {
    MvcRequestMatcher[] matchers = new MvcRequestMatcher[patterns.length];
    for (int index = 0; index < patterns.length; index++) {
      matchers[index] = new MvcRequestMatcher(this.introspector, patterns[index]);
      if (this.servletPath != null) {
        matchers[index].setServletPath(this.servletPath);
      }
    }
    return matchers;
  }

  MvcRequestMatcherBuilder servletPath(String path) {
    return new MvcRequestMatcherBuilder(this.introspector, path);
  }
}
