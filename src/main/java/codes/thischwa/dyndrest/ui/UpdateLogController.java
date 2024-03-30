package codes.thischwa.dyndrest.ui;

import codes.thischwa.dyndrest.config.AppConfig;
import codes.thischwa.dyndrest.model.FullUpdateLog;
import codes.thischwa.dyndrest.service.UpdateLogService;
import codes.thischwa.dyndrest.util.NetUtil;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** A controller that delivers a page to show the zone update logs. */
@Controller
@ConditionalOnProperty(name = "dyndrest.update-log-page-enabled")
public class UpdateLogController {

  private final AppConfig config;

  private final UpdateLogService updateLogService;

  public UpdateLogController(AppConfig config, UpdateLogService updateLogService) {
    this.config = config;
    this.updateLogService = updateLogService;
  }

  /**
   * Delivers page to show the zone update logs.
   *
   * @return a string that redirects to the 1st zone update logs page
   */
  @GetMapping(value = "/log-ui", produces = MediaType.TEXT_HTML_VALUE)
  public String log() {
    return "redirect:/log-ui/0";
  }

  /**
   * Delivers page to show the zone update logs.
   *
   * @param model optional model for processing
   * @param page number of the requested page, starts with '0'
   * @return the zone update logs page
   */
  @SuppressWarnings("SameReturnValue")
  @GetMapping(value = "/log-ui/{page}", produces = MediaType.TEXT_HTML_VALUE)
  public String log(@PathVariable int page, Model model) {
    String baseUrl = NetUtil.getBaseUrl(config.updateLogRestForceHttps());
    model.addAttribute("server_url", baseUrl + "/log-ui/");

    Page<FullUpdateLog> pageLog = updateLogService.getPage(page);
    model.addAttribute("logDatePattern", config.updateLogDatePattern());
    model.addAttribute("logs", pageLog.getContent());
    model.addAttribute("page", page);
    model.addAttribute("countTotalPages", pageLog.getTotalPages());
    model.addAttribute("countTotalItems", pageLog.getTotalElements());
    model.addAttribute("countPageSize", pageLog.getSize());
    model.addAttribute("countItems", pageLog.getNumberOfElements());
    model.addAttribute("isFirst", pageLog.isFirst());
    model.addAttribute("isLast", pageLog.isLast());

    int totalPages = pageLog.getTotalPages();
    if (totalPages >= 0) {
      List<Integer> pageNumbers =
          IntStream.rangeClosed(0, totalPages - 1).boxed().toList();
      model.addAttribute("pageNumbers", pageNumbers);
    }
    return "log-view";
  }
}
