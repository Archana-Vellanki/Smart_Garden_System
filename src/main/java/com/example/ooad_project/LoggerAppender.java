package com.example.ooad_project;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "LoggerAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class LoggerAppender extends AbstractAppender {

    private static GardenUIController controller;

    protected LoggerAppender(String name, Layout<?> layout) {
        super(name, null, layout, true, null);
    }

    @PluginFactory
    public static LoggerAppender createAppender(@PluginAttribute("name") String name,
                                                @PluginElement("Layout") Layout<?> layout) {
        if (name == null) {
            LOGGER.error("No name provided for LoggerAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new LoggerAppender(name, layout);
    }

    @Override
    public void append(LogEvent event) {
//        if (controller != null) {
//            String message = new String(getLayout().toByteArray(event));
//            controller.appendLogText(message.trim());
//        }
    }

    public static void setController(GardenUIController controller) {
//        LoggerAppender.controller = controller;
    }
}
