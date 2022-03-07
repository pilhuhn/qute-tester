package de.bsd.qute.tester;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
@Path("/tryout")
public class Tryout {

    public static final String INSTANT_MAIL = "instant_mail";
    @Inject
    TemplateProcessor tp;

    private String sampleParams = "{{\"key1\":\"Value1\",\"key3\":\"value3\"}";

    private String sampleTemplate = "Hello from *Notifications* via _OpenBridge_.\n " +
                    "With {data.events.size()} events: {data.events} {#if data.context.size() > 0} and " +
                    "context:\n{#each data.context}*{it.key}* -> _{it.value}_\n{/each}{/if}";

    @GET
    @Path("/")
    @Produces("text/html")
    @Transactional
    public String showUI(@QueryParam("params") String params,
                         @QueryParam("template") String incomingTemplate,
                         @Context UriInfo uriInfo) {

        String form = getFromResources("form.html");

        if (params == null || params.isBlank()) {
            params = getFromResources("sampleParams.json");
        }

        String template = incomingTemplate;

        template = fillDefaultIfNeeded(template, sampleTemplate);

        String escaped = escape(template);

        Map<String, Object> payload;
        try {
            payload = new ObjectMapper().readValue(params, Map.class);
        } catch (JsonProcessingException e) {
            return "Could not process parameters : " + e.getMessage() +
                    "<p/><hr/><p/>" + form;
        }

        String rendered = tp.renderTemplate(template, payload);
        rendered = htmlifyMrkDown(rendered);

        if (params==null || params.isBlank()) {
            form = form.replaceAll("##PARAMS##", sampleParams);
        } else {
            form = form.replaceAll("##PARAMS##", params.trim());
        }
        form = form.replaceAll("##TEMPLATE##", escaped);

        return rendered +
                "<p/><hr/><p/>" +
                form;


    }

    private String htmlifyMrkDown(String rendered) {
        String tmp = rendered.replaceAll("\n", "<br/>");

        tmp = replaceOne(tmp, "*", "strong");
        tmp = replaceOne(tmp, "_", "i");
        tmp = replaceOne(tmp, "~", "s");
        tmp = replaceOne(tmp, "`", "code");

        return tmp;
    }

    private String replaceOne(String tmp, String mdown, String tag) {
        if (tmp.contains(mdown)) {

            String on = "<" + tag + ">";
            String off = "</" + tag + ">";

            char x = mdown.charAt(0);
            StringBuilder sb = new StringBuilder();
            boolean isStar = false;
            for (int i = 0; i < tmp.length(); i++) {
                if (tmp.charAt(i) == x) {
                    sb.append(isStar ? off : on);
                    isStar = !isStar;
                }
                else {
                    sb.append(tmp.charAt(i));
                }
            }
            tmp = sb.toString();
        }
        return tmp;
    }

    private String getFromResources(String what) {
        String form;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(what)) {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] formBytes = bis.readAllBytes();
            form = new String(formBytes);
        }
        catch (Exception e) {
            form =  "<h1>Reading form failed: " + e.getMessage() + "</h1>";
        }
        return form;
    }

    private String fillDefaultIfNeeded(String what, String defVal) {
        if (what==null || what.isBlank()) {
            return defVal;
        }
        return what;
    }


    private String escape(String in) {
        return in.replaceAll("<", "&lt;");
    }


}