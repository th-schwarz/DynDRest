package codes.thischwa.dyndrest.service;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.UpdateItem;
import codes.thischwa.dyndrest.model.UpdateLogPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * A cache to hold the zone update logs.
 */
@Slf4j
@Service
public class UpdateLogCache implements InitializingBean {

	private final AppConfig conf;

	private List<UpdateItem> updateItems = new CopyOnWriteArrayList<>();

	private DateTimeFormatter dateTimeFormatter;

	public UpdateLogCache(AppConfig conf) {
		this.conf = conf;
	}

	public boolean isEnabled() {
		return conf.isUpdateLogPageEnabled() && conf.getUpdateLogFilePattern() != null;
	}

	public int size() {
		return updateItems.size();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(!isEnabled()) {
			log.info("Log page is disabled or dyndrest.update-log-file-pattern isn't set, prefill is canceled.");
			return;
		}

		dateTimeFormatter = DateTimeFormatter.ofPattern(conf.getUpdateLogDatePattern());

		// build location pattern, if no url type is found 'file:' will be assumed
		String locPattern = (conf.getUpdateLogFilePattern().contains(":")) ?
				conf.getUpdateLogFilePattern() :
				"file:" + conf.getUpdateLogFilePattern();
		log.info("Using the following log file pattern: {}", locPattern);

		List<String> logEntries = new ArrayList<>();
		Resource[] logs = new PathMatchingResourcePatternResolver().getResources(locPattern);
		if(logs.length == 0) {
			log.debug("No log files found.");
			return;
		}
		Arrays.stream(logs).filter(r -> r.getFilename() != null && (r.getFilename().endsWith(".log") || r.getFilename().endsWith(".gz")))
				.forEach(r -> readResource(r, logEntries));

		// ordering and parsing, must be asc because new items will be added at the end
		Pattern pattern = Pattern.compile(conf.getUpdateLogPattern());
		updateItems = logEntries.stream().map(i -> parseLogEntry(i, pattern)).filter(Objects::nonNull)
				.sorted(Comparator.comparing(UpdateItem::getDateTime)).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
		log.info("{} log entries successful read and parsed.", updateItems.size());
	}

	public void addLogEntry(String host, String ipv4, String ipv6) {
		String now = dateTimeFormatter.format(LocalDateTime.now());
		UpdateItem item = new UpdateItem(now, host, ipv4, ipv6);
		updateItems.add(item);
	}

	public List<UpdateItem> getItems() {
		return updateItems;
	}

	public UpdateLogPage getResponseAll() {
		UpdateLogPage logs = new UpdateLogPage();
		logs.setPageSize(conf.getUpdateLogPageSize());
		logs.setTotal(updateItems.size());
		logs.setItems(updateItems.stream().sorted(Comparator.comparing(UpdateItem::getDateTime, Comparator.reverseOrder()))
				.collect(Collectors.toList()));
		return logs;
	}

	public UpdateLogPage getResponsePage(Integer page, String search) {
		log.debug("Entered #getResponsePage with: page={}, search={}", page, search);
		if(page == null || page == 0)
			page = 1;
		UpdateLogPage lp = new UpdateLogPage();
		lp.setPage(page);

		// searching in host and timestamp
		List<UpdateItem> items = (search == null || search.isEmpty()) ?
				new ArrayList<>(updateItems) :
				updateItems.stream().filter(i -> i.getHost().contains(search) || i.getDateTime().contains(search))
						.collect(Collectors.toList());

		// respect ordering of dateTime, must be desc!
		items = items.stream().sorted(Comparator.comparing(UpdateItem::getDateTime, Comparator.reverseOrder()))
				.collect(Collectors.toList());

		int currentIdx = (conf.getUpdateLogPageSize() * page) - conf.getUpdateLogPageSize();
		List<UpdateItem> pageItems = new ArrayList<>();
		int nextIdx = currentIdx + conf.getUpdateLogPageSize();
		for(int i = currentIdx; i < nextIdx; i++) {
			if(i >= items.size())
				break;
			pageItems.add(items.get(i));
		}

		lp.setPageSize(conf.getUpdateLogPageSize());
		lp.setItems(pageItems);
		lp.setTotalPage(((updateItems.size() - 1) / conf.getUpdateLogPageSize()) + 1);
		lp.setTotal(items.size());
		return lp;
	}

	UpdateItem parseLogEntry(String logEntry, Pattern pattern) {
		if(logEntry == null)
			return null;
		Matcher matcher = pattern.matcher(logEntry);
		if(matcher.matches() && matcher.groupCount() == 4) {
			return new UpdateItem(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
		}
		return null;
	}

	private void readResource(Resource res, List<String> logItems) {
		String filename = res.getFilename();
		if(filename == null)
			return;
		log.debug("Process log zone update file: {}", res.getFilename());
		try (InputStream in = filename.endsWith(".gz") ? new GZIPInputStream(res.getInputStream()) : res.getInputStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while(reader.ready()) {
				logItems.add(reader.readLine());
			}
		} catch (IOException e) {
			log.error("Couldn't process log zone update file: {}", filename);
			throw new IllegalArgumentException("Couldn't read: " + filename);
		}
	}

}