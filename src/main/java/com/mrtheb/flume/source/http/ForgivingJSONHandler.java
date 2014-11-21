/**
 * 
 */
package com.mrtheb.flume.source.http;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.event.JSONEvent;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.apache.flume.source.http.JSONHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * @author mlabbe
 *
 */
public class ForgivingJSONHandler implements HTTPSourceHandler {

	  private static final Logger LOG = LoggerFactory.getLogger(JSONHandler.class);
	  private final Type listType =
	          new TypeToken<List<JSONEvent>>() {
	          }.getType();
	  private final Gson gson;

	  public ForgivingJSONHandler() {
	    gson = new GsonBuilder().disableHtmlEscaping().create();
	  }

	  /**
	   * {@inheritDoc}
	   */
	  @Override
	  public List<Event> getEvents(HttpServletRequest request) throws Exception {
	    BufferedReader reader = request.getReader();
	    String charset = request.getCharacterEncoding();
	    //UTF-8 is default for JSON. If no charset is specified, UTF-8 is to
	    //be assumed.
	    if (charset == null) {
	      LOG.debug("Charset is null, default charset of UTF-8 will be used.");
	      charset = "UTF-8";
	    } else if (!(charset.equalsIgnoreCase("utf-8")
	            || charset.equalsIgnoreCase("utf-16")
	            || charset.equalsIgnoreCase("utf-32"))) {
	      LOG.error("Unsupported character set in request {}. "
	              + "JSON handler supports UTF-8, "
	              + "UTF-16 and UTF-32 only.", charset);
	      throw new UnsupportedCharsetException("JSON handler supports UTF-8, "
	              + "UTF-16 and UTF-32 only.");
	    }

	    /*
	     * Gson throws Exception if the data is not parseable to JSON.
	     * Need not catch it since the source will catch it and return error.
	     */
	    List<Event> eventList = new ArrayList<Event>(0);
	    try {
	      eventList = gson.fromJson(reader, listType);
	    } catch (JsonSyntaxException ex) {
	      throw new HTTPBadRequestException("Request has invalid JSON Syntax.", ex);
	    }

	    for (Event e : eventList) {
	      ((JSONEvent) e).setCharset(charset);
	    }
	    return getSimpleEvents(eventList);
	  }

	  @Override
	  public void configure(Context context) {
	  }

	  private List<Event> getSimpleEvents(List<Event> events) {
	    List<Event> newEvents = new ArrayList<Event>(events.size());
	    for(Event e:events) {
	    	for(Map.Entry<String, String> h : e.getHeaders().entrySet()) {
	    		if (h.getValue() == null) {
	    			h.setValue("NULL");
	    		}
	    	}
	      newEvents.add(EventBuilder.withBody(e.getBody(), e.getHeaders()));
	    }
	    return newEvents;
	  }
	}
