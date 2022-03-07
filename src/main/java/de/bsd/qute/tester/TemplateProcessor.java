package de.bsd.qute.tester;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;
import io.quarkus.qute.TemplateInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class TemplateProcessor {

    @Inject
    Engine engine;

    String renderTemplate(String tmpl, Map<String, Object> payload) {
        // Parse it
        Template t = engine.parse(tmpl);

        // Instantiate it with the payload of the Notification's action
        TemplateInstance ti = t.data(payload);

        // Render the template with the values
        // Quarkus 2 is more strict than Q1, so we need the try/catch block
        String result = null;
        try {
            result = ti.render();
        } catch (Exception e) {
            e.printStackTrace();  // TODO: Customise this generated block
            if (e instanceof TemplateException) {
                result = "ERROR: " + e.getCause().getMessage();
            }
        }
        return result;
    }


}
