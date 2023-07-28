package codes.thischwa.dyndrest.config;

import java.util.ArrayList;
import java.util.List;
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
    List<MvcRequestMatcher> matchers = new ArrayList<>(patterns.length);
    for (String pattern : patterns)  {
      MvcRequestMatcher matcher = new MvcRequestMatcher(this.introspector, pattern);
      if (this.servletPath != null) {
        matcher.setServletPath(this.servletPath);
      }
      matchers.add(matcher);
    }
    return matchers.toArray(new MvcRequestMatcher[0]);
  }

  MvcRequestMatcherBuilder servletPath(String path) {
    return new MvcRequestMatcherBuilder(this.introspector, path);
  }
}
