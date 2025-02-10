package com.USWCicrcleLink.server.global.validation;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

@ControllerAdvice
public class SanitizationBinder {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                super.setValue(sanitizeContent(text));
            }
        });
    }

    private String sanitizeContent(String content) {
        if (content == null) return null;

        Safelist safelist = Safelist.none()
                .addTags("a", "b", "strong", "i", "em", "u", "ul", "ol", "li", "p", "br")
                .addAttributes("a", "href");

        return Jsoup.clean(content, safelist);
    }
}
