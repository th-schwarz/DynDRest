package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.UpdateItem;
import codes.thischwa.dyndrest.model.UpdateLogPage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/** A cache to hold the zone update logs. */
@Slf4j
@Service
public class UpdateLogCache implements InitializingBean {

  private final AppConfig conf;

  private List<UpdateItem> updateItems = new CopyOnWriteArrayList<>();

  @SuppressWarnings("NotNullFieldNotInitialized")
  private DateTimeFormatter dateTimeFormatter;

  public UpdateLogCache(AppConfig conf) {
    this.conf = conf;
  }

  public boolean isEnabled() {
    return conf.updateLogPageEnabled();
  }

  public int size() {
    return updateItems.size();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!isEnabled()) {
      log.info(
          "Log page is disabled or dyndrest.update-log-file-pattern isn't set,"
              + " prefill is canceled.");
      return;
    }

    dateTimeFormatter = DateTimeFormatter.ofPattern(conf.updateLogDatePattern());

    // build location pattern, if no url type is found 'file:' will be assumed
    String locPattern =
        (conf.updateLogFilePattern().contains(":"))
            ? conf.updateLogFilePattern()
            : "file:" + conf.updateLogFilePattern();
    log.info("Using the following log file pattern: {}", locPattern);

    List<String> logEntries = new ArrayList<>();
    Resource[] logs = new PathMatchingResourcePatternResolver().getResources(locPattern);
    if (logs.length == 0) {
      log.debug("No log files found.");
      return;
    }
    Arrays.stream(logs)
        .filter(
            r ->
                Objects.requireNonNull(r.getFilename()).endsWith(".log")
                    || r.getFilename().endsWith(".gz"))
        .forEach(r -> readResource(r, logEntries));

    // ordering and parsing, must be asc because new items will be added at the end
    Pattern pattern = Pattern.compile(conf.updateLogPattern());
    updateItems =
        logEntries.stream()
            .map(i -> parseLogEntry(i, pattern))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(UpdateItem::dateTime))
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    log.info("{} log entries successful read and parsed.", updateItems.size());
  }

  /**
   * Generate and add a log item with the desired parameters..
   *
   * @param host the host
   * @param ipv4 the ipv 4
   * @param ipv6 the ipv 6
   */
  public void addLogItem(String host, @Nullable String ipv4, @Nullable String ipv6) {
    if (!isEnabled()) {
      return;
    }
    String now = dateTimeFormatter.format(LocalDateTime.now());
    UpdateItem item = new UpdateItem(now, host, ipv4, ipv6);
    updateItems.add(item);
  }

  /**
   * Gets all log items.
   *
   * @return the all log items
   */
  public List<UpdateItem> getAllItems() {
    return updateItems;
  }

  /**
   * Gets the update log page with all items.
   *
   * @return the update log page
   */
  public UpdateLogPage getResponseAll() {
    UpdateLogPage logs = new UpdateLogPage();
    if (!isEnabled()) {
      return logs;
    }
    logs.setPageSize(conf.updateLogPageSize());
    logs.setTotal(updateItems.size());
    logs.setItems(
        updateItems.stream()
            .sorted(Comparator.comparing(UpdateItem::dateTime, Comparator.reverseOrder()))
            .toList());
    return logs;
  }

  /**
   * Gets the update log page for the desired 'page' and 'search'.
   *
   * @param page the page, can be null
   * @param search the search, can be null
   * @return the update log response page
   */
  public UpdateLogPage getResponsePage(@Nullable Integer page, @Nullable String search) {
    UpdateLogPage lp = new UpdateLogPage();
    if (!isEnabled()) {
      return lp;
    }
    log.debug("Entered #getResponsePage with: page={}, search={}", page, search);
    if (page == null || page == 0) {
      page = 1;
    }
    lp.setPage(page);

    // searching in host and timestamp
    List<UpdateItem> items =
        (search == null || search.isEmpty())
            ? new ArrayList<>(updateItems)
            : updateItems.stream()
                .filter(i -> i.host().contains(search) || i.dateTime().contains(search))
                .toList();

    // respect ordering of dateTime, must be desc!
    items =
        items.stream()
            .sorted(Comparator.comparing(UpdateItem::dateTime, Comparator.reverseOrder()))
            .toList();

    int currentIdx = (conf.updateLogPageSize() * page) - conf.updateLogPageSize();
    List<UpdateItem> pageItems = new ArrayList<>();
    int nextIdx = currentIdx + conf.updateLogPageSize();
    for (int i = currentIdx; i < nextIdx; i++) {
      if (i >= items.size()) {
        break;
      }
      pageItems.add(items.get(i));
    }

    lp.setPageSize(conf.updateLogPageSize());
    lp.setItems(pageItems);
    lp.setTotalPage(((updateItems.size() - 1) / conf.updateLogPageSize()) + 1);
    lp.setTotal(items.size());
    return lp;
  }

  @Nullable
  UpdateItem parseLogEntry(@Nullable String logEntry, Pattern pattern) {
    if (logEntry == null) {
      return null;
    }
    Matcher matcher = pattern.matcher(logEntry);
    if (matcher.matches() && matcher.groupCount() == 4) {
      return new UpdateItem(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
    }
    return null;
  }

  private void readResource(Resource res, List<String> logItems) {
    String filename = res.getFilename();
    if (filename == null) {
      return;
    }
    log.debug("Process log zone update file: {}", res.getFilename());
    try (InputStream in =
        filename.endsWith(".gz")
            ? new GZIPInputStream(res.getInputStream())
            : res.getInputStream()) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      while (reader.ready()) {
        logItems.add(reader.readLine());
      }
    } catch (IOException e) {
      log.error("Couldn't process log zone update file: {}", filename);
      throw new IllegalArgumentException("Couldn't read: " + filename);
    }
  }
}
