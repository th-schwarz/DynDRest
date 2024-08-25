package codes.thischwa.dyndrest.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainNameValidatorTest {

  @Test
  void isValid() {
    DomainNameValidator validator = new DomainNameValidator();
    assertTrue(validator.isValidDomainNames("example.com", "üñîçødè.com"));
    assertTrue(validator.isValidDomainNames("a.domain.de", "b.domain.de"));
    assertFalse(validator.isValidDomainNames("b.domain,de"));
    assertFalse(validator.isValidDomainNames("a.domain.de", "b.domain,de"));
    assertFalse(validator.isValidDomainNames("a.domain.de\n", "b.domain.de"));
    assertFalse(validator.isValidDomainNames("a.domain.de", "b.domai n.de"));
  }
}
