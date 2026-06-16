package tn.esprit.cv_generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class PdfTemplateConfig {

    /**
     * A dedicated Thymeleaf engine for PDF rendering.
     * TemplateMode.XML guarantees well-formed XHTML output: self-closing void elements,
     * properly quoted attributes — exactly what OpenHTMLtoPDF expects.
     * The Spring Boot auto-configured HTML-mode engine is left untouched.
     */
    @Bean("pdfTemplateEngine")
    public TemplateEngine pdfTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        // HTML mode: jsoup parses the output — avoids the XHTML-namespace SAX path
        // in OpenHTMLtoPDF that produces empty PDFs when xmlns="..." is present on <html>.
        // The template source remains XHTML-compliant; only the serialization mode changes.
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
