package org.eustrosoft;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eustrosoft.keywords.config.KeywordsReplaceConfig;
import org.eustrosoft.keywords.replacers.ExcelKeywordsReplacer;
import org.eustrosoft.keywords.replacers.ReplacersManager;
import org.eustrosoft.keywords.replacers.WordKeywordsReplacer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.eustrosoft.keywords.Constants.PROP_TEMPLATE_PATH;

public class KeywordsReplacerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            KeywordsReplaceConfig config = new KeywordsReplaceConfig(
                    req.getParameter("filename"),
                    getInitParameter(PROP_TEMPLATE_PATH),
                    null
            );
            ReplacersManager manager = new ReplacersManager(
                    Arrays.asList(
                        new ExcelKeywordsReplacer(), new WordKeywordsReplacer()
                    )
            );

            resp.setContentType("application/octet-stream");
            resp.setHeader(
                    "Content-Disposition",
                    String.format("attachment; filename=\"%s\"", config.getFilename())
            );
            manager.getReplacerByFileExtension(config.getTemplatePath())
                    .replaceKeywordsInFile(config, resp.getOutputStream());
        } catch (Exception ex) {
            PrintWriter writer = resp.getWriter();
            writer.println("Illegal arguments");
            resp.setStatus(400);
            writer.flush();
        }
    }
}
