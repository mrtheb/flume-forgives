import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mrtheb.flume.source.http.ForgivingJSONHandler;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.flume.source.http.*;
import org.apache.flume.Event;
import org.apache.flume.event.JSONEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class TestJSONHandler {

  ForgivingJSONHandler handler;
  private Gson gson;
  
  @Before
  public void setUp() {
    handler = new ForgivingJSONHandler();
    
    gson = new GsonBuilder().disableHtmlEscaping().create();
  }

  @Test
  public void testMultipleEvents() throws Exception {
    String json = "[{\"headers\":{\"a\": \"b\"},\"body\": \"random_body\"},"
            + "{\"headers\":{\"e\": \"f\"},\"body\": \"random_body2\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("random_body", new String(e.getBody(),"UTF-8"));
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("random_body2", new String(e.getBody(),"UTF-8"));

  }

  @Test
  public void testMultipleEventsUTF16() throws Exception {
    String json = "[{\"headers\":{\"a\": \"b\"},\"body\": \"random_body\"},"
            + "{\"headers\":{\"e\": \"f\"},\"body\": \"random_body2\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json, "UTF-16");
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("random_body", new String(e.getBody(), "UTF-16"));
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("random_body2", new String(e.getBody(), "UTF-16"));

  }

  @Test
  public void testMultipleEventsUTF32() throws Exception {
    String json = "[{\"headers\":{\"a\": \"b\"},\"body\": \"random_body\"},"
            + "{\"headers\":{\"e\": \"f\"},\"body\": \"random_body2\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json, "UTF-32");
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("random_body", new String(e.getBody(), "UTF-32"));
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("random_body2", new String(e.getBody(), "UTF-32"));
  }

  @Test
  public void testMultipleEventsUTF8() throws Exception {
    String json = "[{\"headers\":{\"a\": \"b\"},\"body\": \"random_body\"},"
            + "{\"headers\":{\"e\": \"f\"},\"body\": \"random_body2\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json, "UTF-8");
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("random_body", new String(e.getBody(), "UTF-8"));
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("random_body2", new String(e.getBody(), "UTF-8"));

  }

  @Test
  public void testEscapedJSON() throws Exception {
    //JSON allows escaping double quotes to add it in the data.
    String json = "[{\"headers\":{\"a\": \"b\"}},"
            + "{\"headers\":{\"e\": \"f\"},\"body\": \"rand\\\"om_body2\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertTrue(e.getBody().length == 0);
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("rand\"om_body2", new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testNoBody() throws Exception {
    String json = "[{\"headers\" : {\"a\": \"b\"}},"
            + "{\"headers\" : {\"e\": \"f\"},\"body\": \"random_body2\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertTrue(e.getBody().length == 0);
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("random_body2", new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testNullBody() throws Exception {
    String json = "[{\"headers\" : {\"a\": \"b\"}},"
            + "{\"headers\" : {\"e\": \"f\"},\"body\": null}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertTrue(e.getBody().length == 0);
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("", new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testNullBody2() throws Exception {
	List<JSONEvent> events = new ArrayList<JSONEvent>();

	JSONEvent event_0 = new JSONEvent();
    Map<String, String> headers_0 = new HashMap<String, String>();
    headers_0.put("a", "b");
    event_0.setHeaders(headers_0);
    events.add(event_0);
	
    JSONEvent event_1 = new JSONEvent();
    Map<String, String> headers_1 = new HashMap<String, String>();
	  
    headers_1.put("a", "b");
    headers_1.put("e", "f");
    event_1.setHeaders(headers_1);
    event_1.setBody(null);
    events.add(event_1);
    
	String json = gson.toJson(events).toString();
    
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertTrue(e.getBody().length == 0);
    e = deserialized.get(1);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("", new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testNullHeaders() throws Exception {
    String json = "[{\"headers\" : {\"a\": null}},"
            + "{\"headers\" : {\"e\": null, \"f\": 10},\"body\": \"some_body\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    // headers only contains one header
    Assert.assertEquals(1, e.getHeaders().size());
    Assert.assertEquals("NULL", e.getHeaders().get("a"));
    Assert.assertTrue(e.getBody().length == 0);
    e = deserialized.get(1);
    Assert.assertEquals(2, e.getHeaders().size());
    Assert.assertEquals("NULL", e.getHeaders().get("e"));
    Assert.assertEquals("10", e.getHeaders().get("f"));
    Assert.assertEquals("some_body", new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testSingleHTMLEvent() throws Exception {
    String json = "[{\"headers\": {\"a\": \"b\"},"
            + "\"body\": \"<html><body>test</body></html>\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("<html><body>test</body></html>",
            new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testSingleEvent() throws Exception {
    String json = "[{\"headers\" : {\"a\": \"b\"},\"body\": \"random_body\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("random_body", new String(e.getBody(),"UTF-8"));
  }

  @Test(expected = HTTPBadRequestException.class)
  public void testBadEvent() throws Exception {
    String json = "{[\"a\": \"b\"],\"body\": \"random_body\"}";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    handler.getEvents(req);
    Assert.fail();
  }

  @Test(expected = UnsupportedCharsetException.class)
  public void testError() throws Exception {
    String json = "[{\"headers\" : {\"a\": \"b\"},\"body\": \"random_body\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json, "ISO-8859-1");
    handler.getEvents(req);
    Assert.fail();
  }

  @Test
  public void testSingleEventInArray() throws Exception {
    String json = "[{\"headers\": {\"a\": \"b\"},\"body\": \"random_body\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("random_body", new String(e.getBody(),"UTF-8"));
  }

  @Test
  public void testMultipleLargeEvents() throws Exception {
    String json = "[{\"headers\" : {\"a\": \"b\", \"a2\": \"b2\","
            + "\"a3\": \"b3\",\"a4\": \"b4\"},\"body\": \"random_body\"},"
            + "{\"headers\" :{\"e\": \"f\",\"e2\": \"f2\","
            + "\"e3\": \"f3\",\"e4\": \"f4\",\"e5\": \"f5\"},"
            + "\"body\": \"random_body2\"},"
            + "{\"headers\" :{\"q1\": \"b\",\"q2\": \"b2\",\"q3\": \"b3\",\"q4\": \"b4\"},"
            + "\"body\": \"random_bodyq\"}]";
    HttpServletRequest req = new FlumeHttpServletRequestWrapper(json);
    List<Event> deserialized = handler.getEvents(req);
    Event e = deserialized.get(0);
    Assert.assertNotNull(e);
    Assert.assertEquals("b", e.getHeaders().get("a"));
    Assert.assertEquals("b2", e.getHeaders().get("a2"));
    Assert.assertEquals("b3", e.getHeaders().get("a3"));
    Assert.assertEquals("b4", e.getHeaders().get("a4"));
    Assert.assertEquals("random_body", new String(e.getBody(),"UTF-8"));
    e = deserialized.get(1);
    Assert.assertNotNull(e);
    Assert.assertEquals("f", e.getHeaders().get("e"));
    Assert.assertEquals("f2", e.getHeaders().get("e2"));
    Assert.assertEquals("f3", e.getHeaders().get("e3"));
    Assert.assertEquals("f4", e.getHeaders().get("e4"));
    Assert.assertEquals("f5", e.getHeaders().get("e5"));
    Assert.assertEquals("random_body2", new String(e.getBody(),"UTF-8"));
    e = deserialized.get(2);
    Assert.assertNotNull(e);
    Assert.assertEquals("b", e.getHeaders().get("q1"));
    Assert.assertEquals("b2", e.getHeaders().get("q2"));
    Assert.assertEquals("b3", e.getHeaders().get("q3"));
    Assert.assertEquals("b4", e.getHeaders().get("q4"));
    Assert.assertEquals("random_bodyq", new String(e.getBody(),"UTF-8"));

  }

  @Test
  public void testDeserializarion() throws Exception {
    Type listType = new TypeToken<List<JSONEvent>>() {
    }.getType();
    List<JSONEvent> events = Lists.newArrayList();
    Random rand = new Random();
    for (int i = 1; i < 10; i++) {
      Map<String, String> input = Maps.newHashMap();
      for (int j = 1; j < 10; j++) {
        input.put(String.valueOf(i) + String.valueOf(j), String.valueOf(i));
      }
      JSONEvent e = new JSONEvent();
      e.setBody(String.valueOf(rand.nextGaussian()).getBytes("UTF-8"));
      e.setHeaders(input);
      events.add(e);
    }
    Gson gson = new Gson();
    List<Event> deserialized = handler.getEvents(
            new FlumeHttpServletRequestWrapper(gson.toJson(events, listType)));
    int i = 0;
    for (Event e : deserialized) {
      Event current = events.get(i++);
      Assert.assertEquals(new String(current.getBody(),"UTF-8"),
              new String(e.getBody(),"UTF-8"));
      Assert.assertEquals(current.getHeaders(), e.getHeaders());
    }
  }
}
