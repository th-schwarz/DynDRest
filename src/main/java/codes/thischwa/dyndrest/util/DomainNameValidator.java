package codes.thischwa.dyndrest.util;

import java.net.IDN;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The DomainNameValidator class is used to validate domain names according to a specific pattern.
 * IDN is respected.
 */
public class DomainNameValidator {

  private static final Pattern pattern =
      Pattern.compile(
          "^(?:(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.){1,}[a-zA-Z]{2,}$");

  public boolean isValidDomainNames(String... domainNames) {
    return Arrays.stream(domainNames).allMatch(this::isValidDomainName);
  }

  /**
   * Determines whether a given domain name is valid.
   * IDN (Internationalized Domain Name) is respected.
   *
   * @param domainName the domain name to be validated
   * @return {@code true} if the domain name is valid, {@code false} otherwise
   */
  public boolean isValidDomainName(String domainName) {
    String asciiDomainName;
    try {
      asciiDomainName = IDN.toASCII(domainName);
    } catch (IllegalArgumentException e) {
      return false;
    }

    Matcher matcher = pattern.matcher(asciiDomainName);
    return matcher.matches();
  }
}
