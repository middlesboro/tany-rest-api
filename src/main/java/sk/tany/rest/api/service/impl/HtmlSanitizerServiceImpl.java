package sk.tany.rest.api.service.impl;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.service.HtmlSanitizerService;

@Service
public class HtmlSanitizerServiceImpl implements HtmlSanitizerService {

    private final Safelist safelist = Safelist.none();

    @Override
    public String sanitize(String content) {
        if (content == null) {
            return null;
        }

        return Jsoup.clean(content, safelist);
    }

}
