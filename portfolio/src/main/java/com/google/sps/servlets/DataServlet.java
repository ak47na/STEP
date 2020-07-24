// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*; 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/** Servlet that handles comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  public static final int MAX_COMMENTS = 100;
  private DatastoreService datastore;
  private Query commentsQuery;
  /** Initializes data needed to load comments from datastore when requested */
  @Override
  public void init() {
    datastore = DatastoreServiceFactory.getDatastoreService();
    commentsQuery = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
  }

  /**
   * Sends at most commentsLimit comments as a JSON string
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Integer limit = null;
    try {
      // Get the maximum number of comments to be displayed from the queryString
      limit = Integer.parseInt(request.getParameter("commentsLimit"));
      if (limit <= 0 || limit > MAX_COMMENTS) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The number selected is invalid");
      }
    } catch (Exception e) {
      // Send a HTTP 400 Bad Request response if user provided invalid data.
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    Gson gson = new Gson();
    
    String commentsJson = gson.toJson(getCommentsArray(limit));
    response.setContentType("application/json;");
    response.getWriter().println(commentsJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the comment from the form
    String newComment = null;
    try {
      newComment = getNewComment(request);
    } catch (UnsupportedEncodingException e) {
      // Send a HTTP 400 Bad Request response if user provided invalid data.
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      // Send a HTTP 500 error for other exceptions
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    UserService userService = UserServiceFactory.getUserService();
    User currentUser = userService.getCurrentUser();
    String userEmail = currentUser.getEmail();
    String userId = currentUser.getUserId();
    datastore.put(createCommentEntity(newComment, userEmail, userId));

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
  /*
    Get the maximum number of comments to be displayed from the queryString
  */
  private String getCommentsLimit(HttpServletRequest request) {
    if (request.getQueryString() == null) {
      return "5";
    }
    String[] queryStringArray = request.getQueryString().split("&");

    for (String keyValuePair : queryStringArray) {
      String[] keyValuePairArray = keyValuePair.split("=");
      if (keyValuePairArray[0].equals("commentsLimit")) {
        return keyValuePairArray[1];
      }
    }
    // return default value if none was provided
    return "5";
  }

  private List<Comment> getCommentsArray(int limit) {
    PreparedQuery results = datastore.prepare(commentsQuery);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity: results.asIterable()) {
      if (limit == 0) {
        break;
      }
      -- limit;
      String nickname = (String)entity.getProperty("userNickname");
      
      if (nickname == null) {
        // if the user has no nickname, use the email address instead 
        nickname = (String)entity.getProperty("userEmail");
      }
      Comment comment = new Comment((String)entity.getProperty("message"), nickname);
      comments.add(comment);
    }
    return comments;
  }

  /** Creates Entity with a kind of Comment */
  private Entity createCommentEntity(String newComment, String userEmail, String userId) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", newComment);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    commentEntity.setProperty("timestamp", timestamp.getTime());
    commentEntity.setProperty("userEmail", userEmail);
    commentEntity.setProperty("userNickname", getUserNickname(userId));

    return commentEntity;
  }
  
  private String getNewComment(HttpServletRequest request) throws UnsupportedEncodingException {
    String newComment = request.getParameter("new-comment");
   
    byte[] commentBytes = newComment.getBytes("UTF-8");
    return newComment;
  }
}
