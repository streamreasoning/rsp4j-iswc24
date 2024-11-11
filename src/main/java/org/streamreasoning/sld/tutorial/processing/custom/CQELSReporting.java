package org.streamreasoning.sld.tutorial.processing.custom;

import org.streamreasoning.polyflow.api.operators.s2r.execution.instance.Window;
import org.streamreasoning.polyflow.api.secret.content.Content;
import org.streamreasoning.polyflow.api.secret.report.strategies.ReportingStrategy;

public class CQELSReporting implements ReportingStrategy {

    private Object last_content;

    public boolean match(Window w, Content c, long tapp, long tsys) {
        if (this.last_content == null) {
            this.last_content = c.coalesce();
            return true;
        } else {
            boolean b = !this.last_content.equals(c.coalesce());
            if (b) {
                this.last_content = c.coalesce();
            }
            return b;
        }
    }
}
